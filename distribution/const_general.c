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

#include "const_general.h"

//TODO Magisk
const char * const MG_SU_PATH[] = {
        "/data/local/",
        "/data/local/bin/",
        "/data/local/xbin/",
        "/sbin/",
        "/system/bin/",
        "/system/bin/.ext/",
        "/system/bin/failsafe/",
        "/system/sd/xbin/",
        "/su/xbin/",
        "/su/bin/",
        "/magisk/.core/bin/",
        "/system/usr/we-need-root/",
        "/system/xbin/",
        0
};

const char * const MG_EXPOSED_FILES[] = {
        "/system/lib/libxposed_art.so",
        "/system/lib64/libxposed_art.so",
        "/system/xposed.prop",
        "/cache/recovery/xposed.zip",
        "/system/framework/XposedBridge.jar",
        "/system/bin/app_process64_xposed",
        "/system/bin/app_process32_xposed",
        "/magisk/xposed/system/lib/libsigchain.so",
        "/magisk/xposed/system/lib/libart.so",
        "/magisk/xposed/system/lib/libart-disassembler.so",
        "/magisk/xposed/system/lib/libart-compiler.so",
        "/system/bin/app_process32_orig",
        "/system/bin/app_process64_orig",
        0
};

const char * const MG_READ_ONLY_PATH[] = {
        "/system",
        "/system/bin",
        "/system/sbin",
        "/system/xbin",
        "/vendor/bin",
        "/sbin",
        "/etc",
        0
};

