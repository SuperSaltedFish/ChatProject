package com.yzx.chat.tool;

import android.content.Context;
import android.content.SharedPreferences;

import com.yzx.chat.configure.AppApplication;

/**
 * Created by YZX on 2017年11月24日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class SharePreferenceHelper {

    private static ConfigurePreferences mConfigurePreferences;


    public static ConfigurePreferences getConfigurePreferences() {
        if (mConfigurePreferences == null) {
            synchronized (SharePreferenceHelper.class) {
                if (mConfigurePreferences == null) {
                    mConfigurePreferences = new ConfigurePreferences(AppApplication.getAppContext());
                }
            }
        }
        return mConfigurePreferences;
    }

    public static class ConfigurePreferences {
        private static final String PREFERENCES_NAME_CONFIGURE = "Configure";
        private static final String CONFIGURE_KEY_KEY_BOARD_HEIGHT = "KeyBoardHeight";
        private static final String CONFIGURE_KEY_IS_FIRST_GUIDE = "IsFirstGuide";

        private SharedPreferences mPreferences;

        ConfigurePreferences(Context context) {
            mPreferences = context.getSharedPreferences(PREFERENCES_NAME_CONFIGURE, Context.MODE_PRIVATE);
        }

        public void putKeyBoardHeight(int value) {
            mPreferences.edit().putInt(CONFIGURE_KEY_KEY_BOARD_HEIGHT, value).apply();
        }

        public int getKeyBoardHeight() {
            return mPreferences.getInt(CONFIGURE_KEY_KEY_BOARD_HEIGHT, 0);
        }

        public void putFirstGuide(boolean value) {
            mPreferences.edit().putBoolean(CONFIGURE_KEY_IS_FIRST_GUIDE, value).apply();
        }

        public boolean isFirstGuide() {
            return mPreferences.getBoolean(CONFIGURE_KEY_IS_FIRST_GUIDE, true);
        }

    }
}
