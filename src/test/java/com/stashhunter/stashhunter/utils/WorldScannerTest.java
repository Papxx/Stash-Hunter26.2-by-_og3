package com.stashhunter.stashhunter.utils;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldScannerTest {

    @Test
    void getBoundingBoxVolume_ofASingleBlockIsOne() {
        double volume = WorldScanner.getBoundingBoxVolume(List.of(new BlockPos(10, 64, 10)));

        assertEquals(1.0, volume);
    }

    @Test
    void getBoundingBoxVolume_ofAKnownCuboid() {
        // A 2x3x4 box of corners: (0,0,0) and (1,2,3) -> spans are inclusive, so +1 each axis.
        List<BlockPos> blocks = List.of(
            new BlockPos(0, 0, 0),
            new BlockPos(1, 2, 3)
        );

        double volume = WorldScanner.getBoundingBoxVolume(blocks);

        assertEquals(2.0 * 3.0 * 4.0, volume);
    }

    @Test
    void getBoundingBoxVolume_ofEmptyListIsZero() {
        assertEquals(0.0, WorldScanner.getBoundingBoxVolume(List.of()));
    }

    @Test
    void getBoundingBoxVolume_ofNullListIsZero() {
        assertEquals(0.0, WorldScanner.getBoundingBoxVolume(null));
    }

    @Test
    void calculateCenter_ofSymmetricBlocksIsTheirAverage() {
        List<BlockPos> blocks = List.of(
            new BlockPos(0, 0, 0),
            new BlockPos(10, 20, 30)
        );

        BlockPos center = WorldScanner.calculateCenter(blocks);

        assertEquals(new BlockPos(5, 10, 15), center);
    }

    @Test
    void getMaxDistance_ofTwoBlocksIsTheirDistance() {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(3, 0, 4); // classic 3-4-5 triangle

        double maxDistance = WorldScanner.getMaxDistance(List.of(a, b));

        assertEquals(5.0, maxDistance, 1e-9);
    }

    @Test
    void filterByRadius_excludesBlocksOutsideRadius() {
        BlockPos center = new BlockPos(0, 0, 0);
        BlockPos near = new BlockPos(1, 0, 0);
        BlockPos far = new BlockPos(100, 0, 0);

        List<BlockPos> filtered = WorldScanner.filterByRadius(List.of(near, far), center, 10);

        assertEquals(List.of(near), filtered);
    }
}
