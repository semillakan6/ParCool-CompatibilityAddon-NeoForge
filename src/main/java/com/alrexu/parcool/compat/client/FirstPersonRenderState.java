package com.alrexu.parcool.compat.client;

/** Tracks the private player-hand render pass without depending on EMF internals. */
public final class FirstPersonRenderState {
    private static final ThreadLocal<Integer> HAND_RENDER_DEPTH = ThreadLocal.withInitial(() -> 0);

    private FirstPersonRenderState() {
    }

    public static void enterHandRender() {
        HAND_RENDER_DEPTH.set(HAND_RENDER_DEPTH.get() + 1);
    }

    public static void exitHandRender() {
        int depth = HAND_RENDER_DEPTH.get() - 1;
        if (depth <= 0) {
            HAND_RENDER_DEPTH.remove();
        } else {
            HAND_RENDER_DEPTH.set(depth);
        }
    }

    public static boolean isRenderingHand() {
        return HAND_RENDER_DEPTH.get() > 0;
    }
}
