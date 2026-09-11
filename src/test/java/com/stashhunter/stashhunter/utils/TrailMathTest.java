package com.stashhunter.stashhunter.utils;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrailMathTest {

    @Test
    void calculateLinearity_isHighForCollinearChunks() {
        // (0,0) -> (1,0) -> (2,0): a straight line along X
        double linearity = TrailMath.calculateLinearity(
            new ChunkPos(0, 0), new ChunkPos(1, 0), new ChunkPos(2, 0));

        assertEquals(1.0, linearity, 1e-9);
    }

    @Test
    void calculateLinearity_isLowForARightAngleTurn() {
        // (0,0) -> (1,0) -> (1,1): a 90-degree turn
        double linearity = TrailMath.calculateLinearity(
            new ChunkPos(0, 0), new ChunkPos(1, 0), new ChunkPos(1, 1));

        assertEquals(0.0, linearity, 1e-9);
    }

    @Test
    void calculateLinearity_isZeroWhenPointsCoincide() {
        double linearity = TrailMath.calculateLinearity(
            new ChunkPos(5, 5), new ChunkPos(5, 5), new ChunkPos(9, 9));

        assertEquals(0.0, linearity, 1e-9);
    }

    @Test
    void calculateDirectionConsistency_isOneForChunksMovingInTargetDirection() {
        List<ChunkPos> chunks = List.of(
            new ChunkPos(0, 0), new ChunkPos(1, 0), new ChunkPos(2, 0), new ChunkPos(3, 0));

        double consistency = TrailMath.calculateDirectionConsistency(chunks, new Vec3(1, 0, 0));

        assertEquals(1.0, consistency, 1e-9);
    }

    @Test
    void calculateDirectionConsistency_isZeroForChunksMovingOppositeTargetDirection() {
        List<ChunkPos> chunks = List.of(
            new ChunkPos(3, 0), new ChunkPos(2, 0), new ChunkPos(1, 0), new ChunkPos(0, 0));

        double consistency = TrailMath.calculateDirectionConsistency(chunks, new Vec3(1, 0, 0));

        assertEquals(0.0, consistency, 1e-9);
    }

    @Test
    void calculateDirectionConsistency_isZeroWithFewerThanTwoValidSegments() {
        // A single chunk has no consecutive pair at all.
        double consistency = TrailMath.calculateDirectionConsistency(
            List.of(new ChunkPos(0, 0)), new Vec3(1, 0, 0));

        assertEquals(0.0, consistency);
    }

    @Test
    void calculateDirectionConsistency_isPartialForAZigZagPath() {
        // Alternates +X/-X around a net +X drift - consistency should land strictly between 0 and 1.
        List<ChunkPos> chunks = List.of(
            new ChunkPos(0, 0), new ChunkPos(2, 0), new ChunkPos(1, 0), new ChunkPos(3, 0));

        double consistency = TrailMath.calculateDirectionConsistency(chunks, new Vec3(1, 0, 0));

        assertTrue(consistency > 0.0 && consistency < 1.0);
    }
}
