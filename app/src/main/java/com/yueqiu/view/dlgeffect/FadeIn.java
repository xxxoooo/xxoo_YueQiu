package com.yueqiu.view.dlgeffect;

import android.view.View;

import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by wangyun on 15/2/2.
 */
public class FadeIn extends BaseEffects{
    @Override
    protected void setupAnimation(View view) {
        getmAnimatorSet().playTogether(ObjectAnimator.ofFloat(view,"alpha",0,1).setDuration(mDuration));
    }
}
