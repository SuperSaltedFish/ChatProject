package com.yzx.chat.broadcast;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by YZX on 2018年03月09日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class BackPressedReceive {

    public static final String ACTION = "BackPressedReceiveAction";

    private static List<BackPressedListener> sListenerList;

    public static synchronized void registerBackPressedListener(BackPressedListener listener) {
        if (listener == null) {
            throw new RuntimeException("BackPressedListener can't be null");
        }
        if (sListenerList == null) {
            sListenerList = new LinkedList<>();
        }
        if (!sListenerList.contains(listener)) {
            sListenerList.add(listener);
        }
    }

    public static synchronized void unregisterBackPressedListener(BackPressedListener listener) {
        if (listener == null) {
            throw new RuntimeException("BackPressedListener can't be null");
        }
        if (sListenerList == null) {
            return;
        }
        sListenerList.remove(listener);
        if (sListenerList.size() == 0) {
            sListenerList = null;
        }
    }

    public static synchronized boolean sendBackPressedEvent(String initiator) {
        if (sListenerList == null) {
            return false;
        }

        for (BackPressedListener listener : sListenerList) {
            if (listener.onBackPressed(initiator)) {
                return true;
            }
        }
        return false;
    }


    public interface BackPressedListener {
        boolean onBackPressed(String initiator);
    }
}
