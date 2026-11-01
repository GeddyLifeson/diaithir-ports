package org.valkyrienskies.mod.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.valkyrienskies.core.impl.config.VSCoreConfig;

/**
 * vs-core defaults physics.physicsThreads to max(cores - 4, 0) and hands each registered world's step to a native
 * Krunch worker pool of that size. On the production server (32 logical cores, about 90 registered levels) that
 * produced 216,000 short-lived native KrunchWorkerThread attaches in 5.5 minutes and physics steps of seconds,
 * while the game thread waited on the physics lock in postTickGame. 0 is vs-core's documented "run everything on
 * the thread that executed krunch" mode; -Dvs.physicsThreads=N opts back in to a pool.
 */
public final class PhysicsThreadPolicy {
    private static final Logger LOGGER = LogManager.getLogger("VS2");
    private static final int WANTED = Math.max(0, Integer.getInteger("vs.physicsThreads", 0));

    private PhysicsThreadPolicy() {
    }

    /** Idempotent and cheap: call at mod init and every tick, so a later config (re)load cannot bring the pool back. */
    public static void apply() {
        try {
            final var physics = VSCoreConfig.SERVER.getPhysics();
            final int current = physics.getPhysicsThreads();
            if (current != WANTED) {
                LOGGER.info("VS2: physicsThreads {} -> {} (vs.physicsThreads, default 0: no native worker pool)", current, WANTED);
                physics.setPhysicsThreads(WANTED);
            }
        } catch (final Throwable t) {
            LOGGER.warn("VS2: could not apply physicsThreads", t);
        }
    }
}
