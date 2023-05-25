package com.example.learnningproject.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FitUtils {
    /** 手机品牌枚举 */
    public enum Brand {
        // 未知品牌
        UNKNOWN,
        // 华为
        HUAWEI,
        // 小米
        XIAO_MI,
        // vivo
        VIVO,
        // oppo
        OPPO,
        // 魅族
        MEI_ZU,
        // 三星
        SAMSUNG,
        // 诺基亚
        NOKIA,
        // google
        NEXUS,
    }
    /** 返回手机系统的品牌 */
    public static Brand getBrand() {
        String brandString = Build.BRAND;
        if (TextUtils.isEmpty(brandString)) {
            return Brand.UNKNOWN;
        }
        switch (brandString.toLowerCase()) {
            case "huawei":
                return Brand.HUAWEI;

            case "xiaomi":
                return Brand.XIAO_MI;

            case "vivo":
                return Brand.VIVO;

            case "oppo":
                return Brand.OPPO;

            case "meizu":
                return Brand.MEI_ZU;

            case "samsung":
                return Brand.SAMSUNG;

            case "google":
                return Brand.NEXUS;

            case "nokia":
                return Brand.NOKIA;

            default:
                return Brand.UNKNOWN;
        }
    }

    //小米手机是否有刘海
    private static boolean isWindowHasFringeXiaoMi(){
        try {
            Class<?> properties = Class.forName("android.os.SystemProperties");
            Method method = properties.getMethod("getInt", String.class,int.class);
            Object value =  method.invoke(null,"ro.miui.notch",0);
            return (int)value == 1;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //vivo手机是否有刘海
    private static boolean isWindowHasFringeViVo() {
        try {
            Class<?> clz = Class.forName("android.util.FtFeature");
            Method method = clz.getMethod("isFeatureSupport",int.class);
            return (Boolean) method.invoke(null,0x00000020);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //OPPO手机是否有刘海
    private static boolean isWindowHasFringeOppo(Context context) {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    //华为
    private static boolean isWindowHasFringeHuawei(Context context) {
        try {
            ClassLoader loader = context.getClassLoader();
            Class clz = loader.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method method = clz.getMethod("HasNotchInScreen");
            return (boolean) method.invoke(clz);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
