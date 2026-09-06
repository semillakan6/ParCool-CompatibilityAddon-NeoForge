package com.alrexu.parcool.compat.client;

import java.util.ArrayDeque;
import java.util.Deque;

/** Tracks the base player-model portion of LivingEntityRenderer, excluding feature and armor layers. */
public final class PlayerBaseRenderState {
    private static final ThreadLocal<Deque<Scope>> SCOPES = ThreadLocal.withInitial(ArrayDeque::new);

    private PlayerBaseRenderState() {
    }

    public static void beginLivingRender(boolean playerModel) {
        SCOPES.get().push(new Scope(playerModel));
    }

    public static void finishBaseModel() {
        Deque<Scope> scopes = SCOPES.get();
        if (!scopes.isEmpty()) {
            scopes.peek().baseModel = false;
        }
    }

    public static void finishLivingRender() {
        Deque<Scope> scopes = SCOPES.get();
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
        if (scopes.isEmpty()) {
            SCOPES.remove();
        }
    }

    public static boolean isRenderingBaseModel() {
        Deque<Scope> scopes = SCOPES.get();
        return !scopes.isEmpty() && scopes.peek().baseModel;
    }

    private static final class Scope {
        private boolean baseModel;

        private Scope(boolean baseModel) {
            this.baseModel = baseModel;
        }
    }
}
