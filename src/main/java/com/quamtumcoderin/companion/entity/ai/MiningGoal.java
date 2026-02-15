package com.quamtumcoderin.companion.entity.ai;

import com.quamtumcoderin.companion.entity.CompanionEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class MiningGoal extends Goal {
    private final CompanionEntity entity;
    private BlockPos targetBlock;

    public MiningGoal(CompanionEntity entity) {
        this.entity = entity;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (this.entity.getRandom().nextFloat() > 0.02) return false;

        BlockPos entityPos = this.entity.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(entityPos.add(-10, -5, -10), entityPos.add(10, 5, 10))) {
            if (this.entity.world.getBlockState(pos).isOf(Blocks.OAK_LOG)) {
                this.targetBlock = pos.toImmutable();
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.entity.getNavigation().startMovingTo(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1.0F);
    }

    @Override
    public void tick() {
        if (targetBlock != null && this.entity.getBlockPos().isWithinDistance(targetBlock, 2.5)) {
            this.entity.world.breakBlock(targetBlock, true);
            this.targetBlock = null;
        }
    }

    @Override
    public boolean shouldContinue() {
        return targetBlock != null && this.entity.getBlockPos().isWithinDistance(targetBlock, 2.5);
    }
}
