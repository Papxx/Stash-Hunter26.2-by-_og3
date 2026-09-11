package com.stashhunter.stashhunter.baritone;

import com.stashhunter.stashhunter.StashHunter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;

/**
 * Public facade for Stash Hunter's optional Baritone integration.
 *
 * This is the only class outside this package that may be imported for Baritone access - it
 * never mentions a {@code baritone.api.*} type in its own signatures or fields, and every
 * method gates on {@link #isModLoaded()} before touching {@link BaritoneImpl}. That keeps the
 * JVM from ever having to resolve Baritone's classes when the mod isn't installed, so the addon
 * starts and runs normally without it (Baritone is a soft/optional dependency, see fabric.mod.json).
 */
public final class BaritoneBridge {
    private static final String BARITONE_MOD_ID = "baritone-meteor";
    private static final String EXPECTED_VERSION_PREFIX = "26.2";
    private static Boolean modLoadedCache;

    private BaritoneBridge() {}

    /**
     * True only if Baritone is declared as loaded by Fabric Loader AND its API class is
     * actually resolvable. Belt-and-suspenders on top of {@code isModLoaded}: a mod can in
     * principle be registered with Fabric Loader while its jar is missing/corrupted, so this
     * also probes the class directly. Every subsequent Baritone call is still wrapped in its
     * own {@code try/catch(Throwable)} regardless - this only avoids attempting those calls at
     * all when we already know they can't work.
     */
    public static boolean isModLoaded() {
        if (modLoadedCache == null) {
            boolean loaded = FabricLoader.getInstance().isModLoaded(BARITONE_MOD_ID);
            if (loaded) {
                try {
                    Class.forName("baritone.api.BaritoneAPI");
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    StashHunter.LOG.warn("Baritone mod is registered but its API class couldn't be found - " +
                        "falling back to the built-in flight controller.", e);
                    loaded = false;
                }
            }
            if (loaded) {
                checkVersion();
            }
            modLoadedCache = loaded;
        }
        return modLoadedCache;
    }

    /** Logs a one-time, non-blocking warning if the installed Baritone version looks unexpected. */
    private static void checkVersion() {
        FabricLoader.getInstance().getModContainer(BARITONE_MOD_ID).ifPresent(container -> {
            String version = container.getMetadata().getVersion().getFriendlyString();
            if (!version.startsWith(EXPECTED_VERSION_PREFIX)) {
                StashHunter.LOG.warn("Installed Baritone version '{}' doesn't look like a {} build - " +
                    "pathfinding may not work correctly.", version, EXPECTED_VERSION_PREFIX);
            }
        });
    }

    /** True only if Baritone is installed and its elytra process (native nether-pathfinder lib) actually loaded. */
    public static boolean isElytraPathingReady() {
        if (!isModLoaded()) return false;
        try {
            return BaritoneImpl.isElytraLoaded();
        } catch (Throwable t) {
            return false;
        }
    }

    /** Starts (or redirects) Baritone's elytra pathing toward the given target. Returns false if unavailable. */
    public static boolean startElytraPath(BlockPos target) {
        if (!isElytraPathingReady()) return false;
        try {
            BaritoneImpl.pathElytraTo(target);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Starts Baritone's ground pathfinder toward the exact target block. Returns false if unavailable. */
    public static boolean startGroundPath(BlockPos target) {
        if (!isModLoaded()) return false;
        try {
            BaritoneImpl.pathGroundTo(target);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void cancel() {
        if (!isModLoaded()) return;
        try {
            BaritoneImpl.cancel();
        } catch (Throwable t) {
            // Nothing sensible to do if Baritone itself is in a bad state - ignore.
        }
    }

    public static boolean isPathing() {
        if (!isModLoaded()) return false;
        try {
            return BaritoneImpl.isPathing();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasPath() {
        if (!isModLoaded()) return false;
        try {
            return BaritoneImpl.hasPath();
        } catch (Throwable t) {
            return false;
        }
    }
}
