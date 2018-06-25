package com.yzx.chat.util;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年06月24日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class StringUtil {
    public static boolean isEquals(ArrayList<String> a1, ArrayList<String> a2, boolean isCheckOrder) {
        if (a1 == a2) {
            return true;
        }
        if (a1 == null && a2 != null && a2.size() == 0) {
            return true;
        }
        if (a2 == null && a1 != null && a1.size() == 0) {
            return true;
        }
        if (a1 != null && a2 != null && a1.size() == 0 && a1.size() == a2.size()) {
            return true;
        }
        if (a1 != null && a2 != null && a1.size() == a2.size() && a1.containsAll(a2)) {
            if (isCheckOrder) {
                for (int i = 0, size = a1.size(); i < size; i++) {
                    if (!a1.get(i).equals(a2.get(i))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
