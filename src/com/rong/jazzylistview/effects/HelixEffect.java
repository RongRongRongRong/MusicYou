package com.rong.jazzylistview.effects;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.rong.jazzylistview.JazzyEffect;

import android.view.View;

public class HelixEffect implements JazzyEffect {

    private static final int INITIAL_ROTATION_ANGLE = 180;

    @Override
    public void initView(View item, int position, int scrollDirection) {
        ViewHelper.setRotationY(item, INITIAL_ROTATION_ANGLE);
    }

    @Override
    public void setupAnimation(View item, int position, int scrollDirection, ViewPropertyAnimator animator) {
        animator.rotationYBy(INITIAL_ROTATION_ANGLE * scrollDirection);
    }

}
