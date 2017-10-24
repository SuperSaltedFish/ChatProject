package com.yzx.chat.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.yzx.chat.configure.GlideApp;
import com.yzx.chat.widget.view.GlideRoundTransform;

/**
 * Created by YZX on 2017年05月23日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class GlideUtil {


    public static void loadFromUrl(Context context, ImageView view, Object url) {
        if (url == null || view == null) {
            return;
        }
        if (view.getTag() != null) {
            GlideApp.with(context).clear(view);
        }
        GlideApp.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(view);
    }

    public static void loadCircleFromUrl(Context context, ImageView view, Object url) {
        if (view.getTag() != null) {
            GlideApp.with(context).clear(view);
        }
        GlideApp.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .format(DecodeFormat.PREFER_RGB_565)
                .transform(new CircleCrop())
                .into(view);
    }

    public static void loadRoundFromUrl(Context context, ImageView view, Object url) {
        if (view.getTag() != null) {
            GlideApp.with(context).clear(view);
        }
        GlideApp.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .format(DecodeFormat.PREFER_RGB_565)
                .transform(new GlideRoundTransform())
                .into(view);
    }

    public static void clear(Context context, ImageView view) {
            GlideApp.with(context).clear(view);

    }


}
