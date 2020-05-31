package nic.goi.aarogyasetu.annotation;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_STATE_CHECKED_ERROR;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_STATE_CHECKED_ROOT_DETECTED;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_STATE_CHECKED_ROOT_NOT_DETECTED;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_STATE_STILL_GOING;
import static nic.goi.aarogyasetu.constant.GeneralConst.CH_STATE_UNCHECKED;


//Why? Just look here https://noobcoderblog.wordpress.com/2015/04/12/java-enum-and-android-intdefstringdef-annotation/
@Retention(SOURCE)
@IntDef({CH_STATE_UNCHECKED,
        CH_STATE_STILL_GOING,
        CH_STATE_CHECKED_ROOT_DETECTED,
        CH_STATE_CHECKED_ROOT_NOT_DETECTED,
        CH_STATE_CHECKED_ERROR,
})
public @interface CheckingState {
}

