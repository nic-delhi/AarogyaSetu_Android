package nic.goi.aarogyasetu.constant;


public class GeneralConst {

    private GeneralConst(){

    }

    final static public int CH_TYPE_UNKNOWN = 0;
    final static public int CH_TYPE_TEST_KEYS = 10;
    final static public int CH_TYPE_DEV_KEYS = 20;
    final static public int CH_TYPE_NON_RELEASE_KEYS = 30;
    final static public int CH_TYPE_DANGEROUS_PROPS = 40;
    final static public int CH_TYPE_PERMISSIVE_SELINUX = 50;
    final static public int CH_TYPE_SU_EXISTS = 60;
    final static public int CH_TYPE_SUPER_USER_APK = 70;
    final static public int CH_TYPE_SU_BINARY = 80;
    final static public int CH_TYPE_BUSYBOX_BINARY = 90;
    final static public int CH_TYPE_XPOSED = 100;
    final static public int CH_TYPE_RESETPROP = 110;
    final static public int CH_TYPE_WRONG_PATH_PERMITION = 120;
    final static public int CH_TYPE_HOOKS = 130;

    final static public int CH_STATE_UNCHECKED = 0;
    final static public int CH_STATE_STILL_GOING = 10;
    final static public int CH_STATE_CHECKED_ROOT_DETECTED = 20;
    final static public int CH_STATE_CHECKED_ROOT_NOT_DETECTED = 30;
    final static public int CH_STATE_CHECKED_ERROR = 40;

    final static public String GITHUB = "https://github.com/DimaKoz/meat-grinder";
}
