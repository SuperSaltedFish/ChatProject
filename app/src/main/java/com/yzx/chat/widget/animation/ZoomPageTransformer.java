package com.yzx.chat.widget.animation;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;


/**
 * Created by YZX on 2017年12月05日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class ZoomPageTransformer implements ViewPager.PageTransformer {

    private static final float MAX_SCALE = 0.95f;
    private static final float MIN_SCALE = 0.70f;
    private static final float MIN_AlPHA = 0.50f;
    private static final float MAX_ALPHA = 1.00f;
    private static final float MIN_SATURATION = 0.00f;
    private static final float MAX_SATURATION = 1.00f;

    private ColorMatrix mGrayColorMatrix = new ColorMatrix();

    @Override
    public void transformPage(View page, float position) {

        if (position <= -1) {
            page.setScaleX(MIN_SCALE);
            page.setScaleY(MIN_SCALE);
            page.setAlpha(MIN_AlPHA);
        } else if (position <= 1) {
            float scaleFactor = MIN_SCALE + (1 - Math.abs(position)) * (MAX_SCALE - MIN_SCALE);
            float alphaFactor = MIN_AlPHA + (1 - Math.abs(position)) * (MAX_ALPHA - MIN_AlPHA);
            float saturationFactor = MIN_SATURATION + (1 - Math.abs(position)) * (MAX_SATURATION - MIN_SATURATION);
            page.setScaleX(scaleFactor);
            if (position > 0) {
                page.setTranslationX(-scaleFactor * 2);
            } else if (position < 0) {
                page.setTranslationX(scaleFactor * 2);
            }
            page.setScaleY(scaleFactor);
            page.setAlpha(alphaFactor);
            mGrayColorMatrix.setSaturation(saturationFactor);
            ((ImageView) page).setColorFilter(new ColorMatrixColorFilter(mGrayColorMatrix));
        } else {
            page.setScaleX(MIN_SCALE);
            page.setScaleY(MIN_SCALE);
            page.setAlpha(MIN_AlPHA);
        }
    }
}
