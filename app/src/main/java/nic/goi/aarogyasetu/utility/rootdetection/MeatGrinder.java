package nic.goi.aarogyasetu.utility.rootdetection;



import androidx.annotation.NonNull;



import nic.goi.aarogyasetu.BuildConfig;

public class MeatGrinder {
    private final static String LIB_NAME = "native-lib";
    private static boolean isLoaded;
    private static boolean isUnderTest = false;

    private MeatGrinder() {

    }

    public boolean isLibraryLoaded() {
        if (isLoaded) {
            return true;
        }
        try {
            if(isUnderTest) {
                throw new UnsatisfiedLinkError("under test");
            }
            System.loadLibrary(LIB_NAME);
            isLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return isLoaded;
    }

    public native boolean isDetectedDevKeys();

    public native boolean isDetectedTestKeys();

    public native boolean isNotFoundReleaseKeys();

    public native boolean isFoundDangerousProps();

    public native boolean isPermissiveSelinux();

    public native boolean isSuExists();

    public native boolean isAccessedSuperuserApk();

    public native boolean isFoundSuBinary();

    public native boolean isFoundBusyboxBinary();

    public native boolean isFoundXposed();

    public native boolean isFoundResetprop();

    public native boolean isFoundWrongPathPermission();

    public native boolean isFoundHooks();

    @NonNull
    public static MeatGrinder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {
        private static final MeatGrinder INSTANCE = new MeatGrinder();
    }
}
