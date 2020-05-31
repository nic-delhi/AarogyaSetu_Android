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

#ifndef MEAT_GRINDER_MEAT_GRINDER_H
#define MEAT_GRINDER_MEAT_GRINDER_H

#include <sys/system_properties.h>
#include <sys/types.h>
#include <android/log.h>
#include <jni.h>

#include <string.h>
#include <stdlib.h>
#include <stdbool.h>
#include <mntent.h>
#include <unistd.h>
#include "const_general.h"

#include "const_properties.h"



#define GR_LOG_TAG "MeatGrinder"

#ifndef NDEBUG

#define GR_LOGI(...)  __android_log_print(ANDROID_LOG_INFO,GR_LOG_TAG,__VA_ARGS__)
#define GR_LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,GR_LOG_TAG,__VA_ARGS__)
#define GR_LOGW(...)  __android_log_print(ANDROID_LOG_WARN,GR_LOG_TAG,__VA_ARGS__)
#define GR_LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,GR_LOG_TAG,__VA_ARGS__)
#define GR_LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,GR_LOG_TAG,__VA_ARGS__)

#else //NDEBUG

#define GR_LOGI(...)
#define GR_LOGE(...)
#define GR_LOGW(...)
#define GR_LOGD(...)
#define GR_LOGV(...)

#endif //NDEBUG

bool isDetectedTestKeys();

bool isDetectedDevKeys();

bool isNotFoundReleaseKeys();

bool isFoundDangerousProps();

bool isPermissiveSelinux();

bool isSuExists();

bool isAccessedSuperuserApk();

bool isFoundSuBinary();

bool isFoundBusyboxBinary();

bool isFoundXposed();

bool isFoundResetprop();

bool isFoundWrongPathPermission();

//http://d3adend.org/blog/?p=589 Stab 4: Use /proc/[pid]/maps to detect suspicious shared objects or JARs loaded into memory.
bool isFoundHooks();

#endif //MEAT_GRINDER_MEAT_GRINDER_H