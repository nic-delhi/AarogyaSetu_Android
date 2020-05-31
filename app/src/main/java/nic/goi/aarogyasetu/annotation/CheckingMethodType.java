package nic.goi.aarogyasetu.annotation;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;


import static java.lang.annotation.RetentionPolicy.SOURCE;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_BUSYBOX_BINARY;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_DANGEROUS_PROPS;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_DEV_KEYS;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_HOOKS;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_NON_RELEASE_KEYS;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_PERMISSIVE_SELINUX;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_RESETPROP;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_SUPER_USER_APK;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_SU_BINARY;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_SU_EXISTS;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_TEST_KEYS;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_UNKNOWN;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_WRONG_PATH_PERMITION;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_TYPE_XPOSED;


//Why? Just look here https://noobcoderblog.wordpress.com/2015/04/12/java-enum-and-android-intdefstringdef-annotation/
@Retention(SOURCE)
@IntDef({CH_TYPE_UNKNOWN,
        CH_TYPE_TEST_KEYS,
        CH_TYPE_DEV_KEYS,
        CH_TYPE_NON_RELEASE_KEYS,
        CH_TYPE_DANGEROUS_PROPS,
        CH_TYPE_PERMISSIVE_SELINUX,
        CH_TYPE_SU_EXISTS,
        CH_TYPE_SUPER_USER_APK,
        CH_TYPE_SU_BINARY,
        CH_TYPE_BUSYBOX_BINARY,
        CH_TYPE_XPOSED,
        CH_TYPE_RESETPROP,
        CH_TYPE_WRONG_PATH_PERMITION,
        CH_TYPE_HOOKS,
})
public @interface CheckingMethodType {
}

