package com.ghostops.tv.ui;

import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class FocusScaleHelper {
    private static final float FOCUS_SCALE = 1.08f;
    private static final float NORMAL_SCALE = 1.0f;
    private static final long DURATION_MS = 200L;
    private static final DecelerateInterpolator INTERPOLATOR = new DecelerateInterpolator();

    public static void applyFocusAnimation(View view, boolean hasFocus) {
        float targetScale = hasFocus ? FOCUS_SCALE : NORMAL_SCALE;
        float targetElevation = hasFocus ? 12f : 2f;

        view.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .translationZ(targetElevation)
                .setDuration(DURATION_MS)
                .setInterpolator(INTERPOLATOR)
                .start();

        if (hasFocus) {
            view.playSoundEffect(SoundEffectConstants.CLICK);
        }
    }
}
