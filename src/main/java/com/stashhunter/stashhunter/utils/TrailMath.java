package com.stashhunter.stashhunter.utils;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Pure geometry helpers for {@link ElytraController}'s new-chunk trail-following heuristics.
 *
 * <p>Extracted out of {@code ElytraController} specifically so they're safely unit-testable:
 * {@code ElytraController} has an eager {@code static final} field that reaches into Meteor's
 * {@code Modules.get()} singleton, so merely loading that class outside a running Meteor Client
 * instance (e.g. from a plain JUnit test) would fail. These methods take only value types
 * ({@link ChunkPos}, {@link Vec3}, {@link List}) and touch no live game/singleton state.
 */
public final class TrailMath {

    private TrailMath() {}

    /**
     * How close three chunk positions are to forming a straight line, via the angle between the
     * two segments they form.
     *
     * @return {@code 1.0} for perfectly collinear points, {@code 0.0} for a right angle (or
     *         points too close together to form a meaningful direction)
     */
    public static double calculateLinearity(ChunkPos c1, ChunkPos c2, ChunkPos c3) {
        // Using the cross product method to find deviation from straight line
        Vec3 v1 = new Vec3(c2.x() - c1.x(), 0, c2.z() - c1.z());
        Vec3 v2 = new Vec3(c3.x() - c2.x(), 0, c3.z() - c2.z());

        if (v1.lengthSqr() < 0.01 || v2.lengthSqr() < 0.01) {
            return 0; // Points too close together
        }

        // Calculate angle between vectors
        double dot = v1.normalize().dot(v2.normalize());
        dot = Math.max(-1.0, Math.min(1.0, dot)); // Clamp to valid range

        double angle = Math.acos(Math.abs(dot));
        double linearity = 1.0 - (angle / (Math.PI / 2)); // 1.0 = perfectly linear, 0.0 = perpendicular

        return Math.max(0, linearity);
    }

    /**
     * How consistently a sequence of chunks moves in {@code targetDirection}, averaged over
     * each consecutive pair.
     *
     * @return a value in {@code [0.0, 1.0]}; {@code 0.0} if fewer than two consecutive pairs
     *         yield a valid (non-zero-length) direction
     */
    public static double calculateDirectionConsistency(List<ChunkPos> chunks, Vec3 targetDirection) {
        double totalConsistency = 0;
        int comparisons = 0;

        for (int i = 0; i < chunks.size() - 1; i++) {
            ChunkPos from = chunks.get(i);
            ChunkPos to = chunks.get(i + 1);

            Vec3 direction = new Vec3(to.x() - from.x(), 0, to.z() - from.z());
            if (direction.lengthSqr() > 0) {
                double dot = direction.normalize().dot(targetDirection.normalize());
                totalConsistency += Math.max(0, dot); // Only positive correlations
                comparisons++;
            }
        }

        return comparisons > 0 ? totalConsistency / comparisons : 0;
    }
}
