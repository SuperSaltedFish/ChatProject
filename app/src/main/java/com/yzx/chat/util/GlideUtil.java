package com.yzx.chat.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.yzx.chat.R;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.configure.GlideRequest;
import com.yzx.chat.widget.view.GlideBlurTransform;
import com.yzx.chat.widget.view.GlideHexagonTransform;

import androidx.annotation.IntRange;

/**
 * Created by YZX on 2017年05月23日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */


public class GlideUtil {

    public static void clear(Context context, ImageView view) {
        GlideApp.with(context).clear(view);

    }


    public static void loadFromUrl(Context context, ImageView view, Object url) {
        if (url == null || view == null) {
            return;
        }
        GlideApp.with(context).clear(view);
        GlideApp.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .dontAnimate()
                .format(DecodeFormat.PREFER_RGB_565)
                .into(view);
    }

    public static void loadCircleFromUrl(Context context, ImageView view, Object url) {
        if (url == null || view == null) {
            return;
        }
        GlideApp.with(context).clear(view);
        GlideApp.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .format(DecodeFormat.PREFER_ARGB_8888)
                .transform(new CircleCrop())
                .into(view);
    }

    public static void loadBlurFromUrl(Context context, ImageView view, Object url, @IntRange(from = 1, to = 25) int radius) {
        if (url == null || view == null) {
            return;
        }
        GlideApp.with(context).clear(view);
        GlideApp.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate()
                .transform(new GlideBlurTransform(context, radius))
                .into(view);
    }

    private static final GlideHexagonTransform HEXAGON_TRANSFORM = new GlideHexagonTransform();

    @SuppressLint("CheckResult")
    public static void loadAvatarFromUrl(Context context, ImageView view, Object url) {
        if (view == null) {
            return;
        }
        GlideApp.with(context).clear(view);

        if (url == null) {
            url = R.drawable.ic_avatar_default;
        } else if (url instanceof String) {
            String strUrl = (String) url;
            if (TextUtils.isEmpty(strUrl) || "-".equals(strUrl)) {
                url = R.drawable.ic_avatar_default;
            }
        }

        GlideRequest<Drawable> glideRequest = GlideApp.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .dontAnimate()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .transform(HEXAGON_TRANSFORM)
                .error(R.drawable.ic_avatar_default);

        if (!(url instanceof Integer)) {
            glideRequest.placeholder(R.drawable.ic_avatar_default);
        }
        glideRequest.into(view);
    }


}
