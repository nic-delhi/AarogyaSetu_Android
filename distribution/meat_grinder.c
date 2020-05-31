/*

The MIT License (MIT)

Copyright (c) 2017  Dmitrii Kozhevin <kozhevin.dima@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software
and associated documentation files (the “Software”), to deal in the Software without
restriction, including without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */


#include "meat_grinder.h"


struct mntent *getMntent(FILE *fp, struct mntent *e, char *buf, int buf_len) {

    while (fgets(buf, buf_len, fp) != NULL) {
        // Entries look like "/dev/block/vda /system ext4 ro,seclabel,relatime,data=ordered 0 0".
        // That is: mnt_fsname mnt_dir mnt_type mnt_opts mnt_freq mnt_passno.
        int fsname0, fsname1, dir0, dir1, type0, type1, opts0, opts1;
        if (sscanf(buf, " %n%*s%n %n%*s%n %n%*s%n %n%*s%n %d %d",
                   &fsname0, &fsname1, &dir0, &dir1, &type0, &type1, &opts0, &opts1,
                   &e->mnt_freq, &e->mnt_passno) == 2) {
            e->mnt_fsname = &buf[fsname0];
            buf[fsname1] = '\0';
            e->mnt_dir = &buf[dir0];
            buf[dir1] = '\0';
            e->mnt_type = &buf[type0];
            buf[type1] = '\0';
            e->mnt_opts = &buf[opts0];
            buf[opts1] = '\0';
            return e;
        }
    }
    return NULL;
}


bool isPresentMntOpt(const struct mntent *pMnt, const char *pOpt) {
    char *token = pMnt->mnt_opts;
    const char *end = pMnt->mnt_opts + strlen(pMnt->mnt_opts);
    const size_t optLen = strlen(pOpt);
    while (token != NULL) {
        const char *tokenEnd = token + optLen;
        if (tokenEnd > end) break;
        if (memcmp(token, pOpt, optLen) == 0 &&
            (*tokenEnd == '\0' || *tokenEnd == ',' || *tokenEnd == '=')) {
            return true;
        }
        token = strchr(token, ',');
        if (token != NULL) {
            token++;
        }
    }
    return false;
}

static char *concat2str(const char *pString1, const char *pString2) {
    char *result;
    size_t lengthBuffer = 0;

    lengthBuffer = strlen(pString1) +
                   strlen(pString2) + 1;
    result = malloc(lengthBuffer);
    if (result == NULL) {
        GR_LOGW("malloc failed\n");
        return NULL;
    }
    memset(result, 0, lengthBuffer);
    strcpy(result, pString1);
    strcat(result, pString2);
    return result;
}

static bool
isBadPropertyState(const char *key, const char *badValue, bool isObligatoryProperty, bool isExact) {
    if (badValue == NULL) {
        GR_LOGE("badValue may not be NULL");
        return false;
    }
    if (key == NULL) {
        GR_LOGE("key may not be NULL");
        return false;
    }
    char value[PROP_VALUE_MAX + 1];
    int length = __system_property_get(key, value);
    bool result = false;
    /* A length 0 value indicates that the property is not defined */
    if (length > 0) {
        GR_LOGI("property:[%s]==[%s]", key, value);
        if (isExact) {
            if (strcmp(value, badValue) == 0) {
                GR_LOGW("bad value[%s] equals to [%s] in the property [%s]", value, badValue, key);
                result = true;
            }
        } else {
            if (strlen(value) >= strlen(badValue) && strstr(value, badValue) != NULL) {
                GR_LOGW("bad value[%s] found in [%s] in the property [%s]", value, badValue, key);
                result = true;
            }
        }
    } else {
        GR_LOGI("[%s] property not found", key);
        if (isObligatoryProperty) {
            result = true;
        }
    }
    return result;
}

bool isDetectedTestKeys() {
    const char *TEST_KEYS_VALUE = "test-keys";
    return isBadPropertyState(ANDROID_OS_BUILD_TAGS, TEST_KEYS_VALUE, true, false);
}

bool isDetectedDevKeys() {
    const char *DEV_KEYS_VALUE = "dev-keys";
    return isBadPropertyState(ANDROID_OS_BUILD_TAGS, DEV_KEYS_VALUE, true, false);
}

bool isNotFoundReleaseKeys() {
    const char *RELEASE_KEYS_VALUE = "release-keys";
    return !isBadPropertyState(ANDROID_OS_BUILD_TAGS, RELEASE_KEYS_VALUE, false, true);
}

bool isFoundWrongPathPermission() {

    bool result = false;
    FILE *file = fopen("/proc/mounts", "r");
    char mntent_strings[BUFSIZ];
    if (file == NULL) {
        GR_LOGE("setmntent");
        return result;
    }

    struct mntent ent = {0};
    while (NULL != getMntent(file, &ent, mntent_strings, sizeof(mntent_strings))) {
        for (size_t i = 0; MG_READ_ONLY_PATH[i]; i++) {
            if (strcmp((&ent)->mnt_dir, MG_READ_ONLY_PATH[i]) == 0 &&
                isPresentMntOpt(&ent, "rw")) {
                GR_LOGI("%s %s %s %s\n", (&ent)->mnt_fsname, (&ent)->mnt_dir, (&ent)->mnt_opts,
                        (&ent)->mnt_type);
                result = true;
                break;
            }
        }
        memset(&ent, 0, sizeof(ent));
    }
    fclose(file);
    return result;
}


bool isFoundDangerousProps() {
    const char *BAD_DEBUGGABLE_VALUE = "1";
    const char *BAD_SECURE_VALUE = "0";
    const char *BAD_SYS_INITD_VALUE = "1";
    const char *BAD_SERVICE_ADB_ROOT_VALUE = "1";

    bool result = isBadPropertyState(ANDROID_OS_DEBUGGABLE, BAD_DEBUGGABLE_VALUE, true, true) ||
                  isBadPropertyState(SERVICE_ADB_ROOT, BAD_SERVICE_ADB_ROOT_VALUE, false, true) ||
                  isBadPropertyState(ANDROID_OS_SECURE, BAD_SECURE_VALUE, true, true) ||
                  isBadPropertyState(ANDROID_OS_SYS_INITD, BAD_SYS_INITD_VALUE, false, true);

    return result;
}

bool isPermissiveSelinux() {
    const char *BAD_VALUE = "0";
    return isBadPropertyState(ANDROID_OS_BUILD_SELINUX, BAD_VALUE, false, false);
}

bool isSuExists() {
    char buf[BUFSIZ];
    char *str = NULL;
    char *temp = NULL;
    size_t size = 1;  // start with size of 1 to make room for null terminator
    size_t strlength;

    FILE *pipe = popen("which su", "r");
    if (pipe == NULL) {
        GR_LOGI("pipe is null");
        return false;
    }

    while (fgets(buf, sizeof(buf), pipe) != NULL) {
        strlength = strlen(buf);
        temp = realloc(str, size + strlength);  // allocate room for the buf that gets appended
        if (temp == NULL) {
            // allocation error
            GR_LOGE("Error (re)allocating memory");
            pclose(pipe);
            if (str != NULL) {
                free(str);
            }
            return false;
        } else {
            str = temp;
        }
        strcpy(str + size - 1, buf);
        size += strlength;
    }
    pclose(pipe);
    GR_LOGW("A size of the result from pipe is [%zu], result:\n [%s] ", size, str);
    if (str != NULL) {
        free(str);
    }
    return size > 1 ? true : false;
}

static bool isAccessedFile(const char *path) {
    int result = access(path, F_OK);
    GR_LOGV("[%s] has been accessed with result: [%d]", path, result);
    return result == 0 ? true : false;
}

static bool isFoundBinaryFromArray(const char *const *array, const char *binary) {
    for (size_t i = 0; array[i]; ++i) {
        char *checkedPath = concat2str(array[i], binary);
        if (checkedPath == NULL) { // malloc failed
            return false;
        }
        bool result = isAccessedFile(checkedPath);
        free(checkedPath);
        if (result) {
            return result;
        }
    }
    return false;
}

bool isAccessedSuperuserApk() {
    return isAccessedFile("/system/app/Superuser.apk");
}

bool isFoundResetprop() {
    return isAccessedFile("/data/magisk/resetprop");
}

bool isFoundSuBinary() {
    return isFoundBinaryFromArray(MG_SU_PATH, "su");
}

bool isFoundBusyboxBinary() {
    return isFoundBinaryFromArray(MG_SU_PATH, "busybox");
}

bool isFoundXposed() {
    for (size_t i = 0; MG_EXPOSED_FILES[i]; ++i) {
        bool result = isAccessedFile(MG_EXPOSED_FILES[i]);
        if (result) {
            return result;
        }
    }
    return false;
}

bool isFoundHooks() {
    bool result = false;
    pid_t pid = getpid();
    char maps_file_name[512];
    sprintf(maps_file_name, "/proc/%d/maps", pid);
    GR_LOGI("try to open [%s]", maps_file_name);
    const size_t line_size = BUFSIZ;
    char *line = malloc(line_size);
    if (line == NULL) {
        return result;
    }
    FILE *fp = fopen(maps_file_name, "r");
    if (fp == NULL) {
        free(line);
        return result;
    }
    memset(line, 0, line_size);
    const char *substrate = "com.saurik.substrate";
    const char *xposed = "XposedBridge.jar";
    while (fgets(line, line_size, fp) != NULL) {
        const size_t real_line_size = strlen(line);
        if ((real_line_size >= strlen(substrate) && strstr(line, substrate) != NULL) ||
            (real_line_size >= strlen(xposed) && strstr(line, xposed) != NULL)) {
            GR_LOGI("found in [%s]: [%s]", maps_file_name, line);
            result = true;
            break;
        }
    }
    free(line);
    fclose(fp);
    return result;
}