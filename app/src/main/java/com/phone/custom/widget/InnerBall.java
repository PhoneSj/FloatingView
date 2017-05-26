package com.phone.custom.widget;

/**
 * Created by Phone on 2017/5/22.
 */

public class InnerBall extends Ball {


    public InnerBall(int centerX, int centerY, int parentRadius, float sizeRate) {
        super(centerX, centerY, parentRadius, sizeRate);
    }

    @Override
    protected void calculateCenter(float fraction) {
        float rate = fraction;
        mCenter.x = (int) (mParentCenter.x + mRadius * rate);
    }

    @Override
    protected void calculateRightRadius(float fraction) {
        //        float rate = 1.0f + fraction * 0.1f;
        //        mRightRadius = (int) (mOriginRadius * rate);
    }

    @Override
    protected void calculateLeftRadius(float fraction) {
        float rate = 1.0f + fraction * 0.05f;
        mLeftRadius = (int) (mOriginRadius * rate);
    }

    @Override
    protected void calculateRadius(float fraction) {
        float rate = 1.0f - fraction * 0.1f;
        mRadius = (int) (mOriginRadius * rate);
    }
}
