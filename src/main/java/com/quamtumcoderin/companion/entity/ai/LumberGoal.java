package com.quamtumcoderin.companion.entity.ai;

import com.quamtumcoderin.companion.entity.CompanionEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class LumberGoal extends Goal {
    private final CompanionEntity entity;
    private final List<BlockPos> treeLogs = new ArrayList<>();
    private BlockPos targetLog;
    private int tickCounter;

    public LumberGoal(CompanionEntity entity) {
        this.entity = entity;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (this.entity.world.getTime() % 20 == 0) return false;

        if (!treeLogs.isEmpty()) return true;

        return findTree();
    }

    private boolean findTree() {
        BlockPos entityPos = this.entity.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(entityPos.add(-10, 0, -10), entityPos.add(10, 5, 10))) {
            if (isLog(pos)) {
                scanTree(pos);
                return !treeLogs.isEmpty();
            }
        }
        return false;
    }

    private void scanTree(BlockPos startNode) {
        treeLogs.clear();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startNode);
        visited.add(startNode);

        while (!queue.isEmpty() && treeLogs.size() < 64) {
            BlockPos current = queue.poll();
            treeLogs.add(current);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        BlockPos neighbor = current.add(x, y, z);
                        if (!visited.contains(neighbor) && isLog(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        treeLogs.sort(Comparator.comparingInt(BlockPos::getY));
    }

    private boolean isLog(BlockPos pos) {
        return this.entity.world.getBlockState(pos).isIn(BlockTags.LOGS);
    }

    @Override
    public void start() {
        if (!treeLogs.isEmpty()) {
            this.targetLog = treeLogs.get(0);
        }
    }

    @Override
    public void tick() {
        if (targetLog == null && !treeLogs.isEmpty()) {
            targetLog = treeLogs.get(0);
        }

        if (targetLog != null) {
            double distance = this.entity.squaredDistanceTo(targetLog.getX(), targetLog.getY(), targetLog.getZ());

            this.entity.getLookControl().lookAt(targetLog.getX(), targetLog.getY(), targetLog.getZ());

            if (distance > 4.0) {
                this.entity.getNavigation().startMovingTo(targetLog.getX(), targetLog.getY(), targetLog.getZ(), 0.1D);
            } else {
                tickCounter++;
                if (tickCounter >= 10) {
                    BlockState state = this.entity.world.getBlockState(targetLog);
                    Block block = state.getBlock();

                    this.entity.world.breakBlock(targetLog, false);

                    ItemStack woodStack = new ItemStack(block);

                    ItemStack remainder = this.entity.inventory.addStack(woodStack);

                    if (!remainder.isEmpty()) {
                        Block.dropStack(this.entity.world, targetLog, remainder);
                        this.stop();
                    }

                    treeLogs.remove(targetLog);
                    targetLog = null;
                    tickCounter = 0;
                }
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        return !treeLogs.isEmpty();
    }
}
