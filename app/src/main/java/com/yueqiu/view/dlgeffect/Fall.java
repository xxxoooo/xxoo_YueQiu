package com.yueqiu.view.dlgeffect;

import android.view.View;

import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by wangyun on 15/2/2.
 */
public class Fall extends BaseEffects{
    @Override
    protected void setupAnimation(View view) {
        getmAnimatorSet().playTogether(
                ObjectAnimator.ofFloat(view,"scaleX",2,1.5f,1).setDuration(mDuration),
                ObjectAnimator.ofFloat(view,"scaleY",2,1.5f,1).setDuration(mDuration),
                ObjectAnimator.ofFloat(view,"alpha",0,1).setDuration(mDuration * 2 / 3)
        );
    }
}
