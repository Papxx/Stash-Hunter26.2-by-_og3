package com.stashhunter.stashhunter.modules;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link StashHunterModule}'s pure {@code static} clustering helpers
 * ({@code clusterBlocks}, {@code calculateStashCenter}). Calling a static method on
 * {@code StashHunterModule} triggers class initialization of it and its {@code Module}
 * superclass, but neither declares any static fields with side effects (Meteor's {@code Module}
 * only sets up instance state in its constructor, which a static call never invokes) - so this
 * is safe to run with no running Minecraft/Meteor Client instance.
 */
class StashHunterModuleClusteringTest {

    @Test
    void clusterBlocks_groupsNearbyBlocksTogether() {
        List<BlockPos> blocks = List.of(
            new BlockPos(0, 64, 0),
            new BlockPos(1, 64, 0),
            new BlockPos(2, 64, 0)
        );

        List<List<BlockPos>> clusters = StashHunterModule.clusterBlocks(blocks, 5);

        assertEquals(1, clusters.size());
        assertEquals(3, clusters.get(0).size());
    }

    @Test
    void clusterBlocks_separatesFarApartBlocks() {
        List<BlockPos> blocks = List.of(
            new BlockPos(0, 64, 0),
            new BlockPos(1000, 64, 1000)
        );

        List<List<BlockPos>> clusters = StashHunterModule.clusterBlocks(blocks, 5);

        assertEquals(2, clusters.size());
    }

    @Test
    void clusterBlocks_chainsThroughIntermediateBlocks() {
        // A-B are within range, B-C are within range, but A-C are not directly - should still
        // end up in one cluster via B.
        List<BlockPos> blocks = List.of(
            new BlockPos(0, 64, 0),
            new BlockPos(4, 64, 0),
            new BlockPos(8, 64, 0)
        );

        List<List<BlockPos>> clusters = StashHunterModule.clusterBlocks(blocks, 5);

        assertEquals(1, clusters.size());
        assertEquals(3, clusters.get(0).size());
    }

    @Test
    void clusterBlocks_ofEmptyListReturnsNoClusters() {
        assertEquals(0, StashHunterModule.clusterBlocks(List.of(), 5).size());
    }

    @Test
    void calculateStashCenter_isTheAverageOfBlockPositions() {
        List<BlockPos> blocks = List.of(
            new BlockPos(0, 60, 0),
            new BlockPos(10, 70, 10)
        );

        BlockPos center = StashHunterModule.calculateStashCenter(blocks);

        assertEquals(new BlockPos(5, 65, 5), center);
    }

    @Test
    void calculateStashCenter_ofEmptyListThrows() {
        assertThrows(IllegalArgumentException.class, () -> StashHunterModule.calculateStashCenter(List.of()));
    }
}
