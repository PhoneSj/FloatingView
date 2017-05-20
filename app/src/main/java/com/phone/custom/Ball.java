package com.phone.custom;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Created by Phone on 2017/5/20.
 */

public class Ball {

    private int mRadius=100;
    private float mAngle=60;
    private Point mCenter=new Point(50,50);

    private Paint mPaint;

    private int mInnerColor= 0x00ffffff;
    private int mOutterColor=0xaaffffff;

    public Ball(Point center,int radius,float angle) {
        this.mCenter=center;
        this.mRadius=radius;
        this.mAngle=angle;
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        RadialGradient shader=new RadialGradient(0,0,mRadius,new int[]{mInnerColor,mOutterColor},
                new float[]{0.5f,1.0f}, Shader.TileMode.CLAMP);
        mPaint.setShader(shader);
    }

    public void draw(Canvas canvas){

        canvas.save();
        canvas.translate(mCenter.x,mCenter.y);
        canvas.rotate(mAngle);
        canvas.drawArc(-mRadius,-mRadius,mRadius,mRadius,90, 180,false,mPaint);

        canvas.save();
        canvas.scale(1.5f,1.0f);
        canvas.drawArc(-mRadius,-mRadius,+mRadius,mRadius,-90, 180,false,mPaint);
        canvas.restore();

        canvas.restore();
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    public void setAngle(float mAngle) {
        this.mAngle = mAngle;
    }

    public void setCenter(Point mCenter) {
        this.mCenter = mCenter;
    }
}
