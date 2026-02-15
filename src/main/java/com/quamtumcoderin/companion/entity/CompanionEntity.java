package com.quamtumcoderin.companion.entity;

import com.quamtumcoderin.companion.entity.ai.LumberGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class CompanionEntity extends PathAwareEntity {

    private static final TrackedData<Integer> HUNGER = DataTracker.registerData(CompanionEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public final SimpleInventory inventory = new SimpleInventory(9);

    public CompanionEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createCompanionAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new LumberGoal(this));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player.isSneaking()) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, player1) ->
                            GenericContainerScreenHandler.createGeneric9x1(syncId, inv),
                    new LiteralText("Mochila de Illojuan")
            ));
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HUNGER, 20);
    }

    public int getHunger() {
        return this.dataTracker.get(HUNGER);
    }

    public void setHunger(int value) {
        this.dataTracker.set(HUNGER, Math.max(0, Math.min(20, value)));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        NbtList listTag = new NbtList();
        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack stack = this.inventory.getStack(i);
            if (!stack.isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) i);
                stack.writeNbt(nbtCompound);
                listTag.add(nbtCompound);
            }
        }
        tag.put("Inventory", listTag);
        tag.putInt("Hunger", getHunger());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        NbtList list = tag.getList("Inventory", 10);
        for (int i = 0; i < list.size(); ++i) {
            NbtCompound nbtCompound = list.getCompound(i);
            int slot = nbtCompound.getByte("Slot") & 255;
            if (slot < this.inventory.size()) {
                this.inventory.setStack(slot, ItemStack.fromNbt(nbtCompound));
            }
        }
        if (tag.contains("Hunger")) {
            setHunger(tag.getInt("Hunger"));
        }
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack stack = this.inventory.getStack(i);
            if (!stack.isEmpty()) {
                this.dropStack(stack);
            }
        }
    }

    public ItemStack getAxeStack() {
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack stack = this.inventory.getStack(i);
            if (stack.getItem() instanceof AxeItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean hasAxe() {
        return !getAxeStack().isEmpty();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClient) {
            ItemStack bestTool = ItemStack.EMPTY;

            if (this.age % 400 == 0) {
                setHunger(getHunger() - 1);
            }

            if (this.age % 20 == 0 && getHunger() >= 18 && this.getHealth() < this.getMaxHealth()) {
                this.heal(1.0F);
                setHunger(getHunger() - 1);
            }

            if (this.age % 80 == 0 && getHunger() == 0) {
                this.damage(DamageSource.STARVE, 1.0F);
            }

            if (getHunger() < 15) {
                eatFromInventory();
            }

            if (hasAxe()) {
                bestTool = getAxeStack();
            }

            this.equipStack(EquipmentSlot.MAINHAND, bestTool);
        }
    }

    private void eatFromInventory() {
        for (int i = 0; i < this.inventory.size(); ++i) {
            ItemStack stack = this.inventory.getStack(i);
            Item item = stack.getItem();

            if (item.isFood()) {
                assert item.getFoodComponent() != null;
                int nutrition = item.getFoodComponent().getHunger();
                setHunger(getHunger() + nutrition);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0F, 1.0F);

                stack.decrement(1);
                return;
            }
        }
    }
}
