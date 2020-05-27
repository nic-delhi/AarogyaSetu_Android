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

#include <jni.h>
#include "../../../../distribution/meat_grinder.h"


JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isDetectedTestKeys(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isDetectedTestKeys();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isDetectedDevKeys(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isDetectedDevKeys();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isNotFoundReleaseKeys(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isNotFoundReleaseKeys();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isFoundDangerousProps(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isFoundDangerousProps();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isPermissiveSelinux(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isPermissiveSelinux();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isSuExists(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isSuExists();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isAccessedSuperuserApk(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isAccessedSuperuserApk();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isFoundSuBinary(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isFoundSuBinary();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isFoundBusyboxBinary(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isFoundBusyboxBinary();
}


JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isFoundXposed(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isFoundXposed();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isFoundResetprop(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isFoundResetprop();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isFoundWrongPathPermission(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isFoundWrongPathPermission();
}

JNIEXPORT jboolean JNICALL
Java_nic_goi_aarogyasetu_utility_rootdetection_MeatGrinder_isFoundHooks(
        JNIEnv *env,
        jobject this) {

    return (jboolean) isFoundHooks();
}

JNIEXPORT jboolean JNICALL
       
Java_nic_goi_aarogyasetu_utility_rootdetection_isForTest(JNIEnv *env, jobject thiz) {
    // TODO: implement isForTest()
}