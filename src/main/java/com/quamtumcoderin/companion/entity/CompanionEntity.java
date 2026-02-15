package com.quamtumcoderin.companion.entity;

import com.quamtumcoderin.companion.entity.ai.LumberGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class CompanionEntity extends PathAwareEntity {

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
}
