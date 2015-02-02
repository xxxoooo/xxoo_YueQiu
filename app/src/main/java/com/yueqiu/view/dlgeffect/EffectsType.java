package com.yueqiu.view.dlgeffect;

/**
 * Created by wangyun on 15/2/2.
 */
public enum EffectsType {
    FadeIn(FadeIn.class),
    SlideLeft(SlideLeft.class),
    SlideTop(SlideTop.class),
    SlideBottom(SlideBottom.class),
    SlideRight(SlideRight.class),
    Fall(Fall.class),
    NewsPaper(NewsPaper.class),
    FlipH(FlipH.class),
    FlipV(FlipV.class),
    RotateBottom(RotateBottom.class),
    RotateLeft(RotateLeft.class),
    Shake(Shake.class),
    SlideFall(SlideFall.class);

    private Class<? extends BaseEffects> mEffectClass;

    EffectsType(Class<? extends BaseEffects> baseClass) {
        this.mEffectClass = baseClass;
    }
    public BaseEffects getAnimator(){
        BaseEffects effect;
        try{
            effect = mEffectClass.newInstance();
        } catch (ClassCastException e) {
            throw new Error("Can not init animatorClazz instance");
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            throw new Error("Can not init animatorClazz instance");
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            throw new Error("Can not init animatorClazz instance");
        }
        return effect;
    }
}
