//
// Created by wurui on 17-12-9.
//
#include "native-test.h"
#include "kcftracker.hpp"
#include <android/log.h>
#include "eben_hpc_log.h"
#include <math.h>

using namespace cv;
using namespace std;

JNIEXPORT jstring JNICALL
Java_com_example_wurui_kcf_1ndk_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    cv::Mat image;
    cv::Point pt = cv::Point(1,1);
    test t = test();
    hello = t.a;
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jintArray JNICALL
Java_com_example_wurui_kcf_1ndk_MainActivity_bodyDetect
        (JNIEnv *env, jobject obj, jobject jbmp1, jobject jbmp2, jintArray jtarget_rect){

    AndroidBitmapInfo bmp1info;
    void* bmp1pixels;
    AndroidBitmapInfo bmp2info;
    void* bmp2pixels;
    int height,width,ret,y,x;

//解析bitmap
    if ((ret = AndroidBitmap_getInfo(env, jbmp1, &bmp1info)) < 0) {
        return NULL;
    }
    if ((ret = AndroidBitmap_getInfo(env, jbmp2, &bmp2info)) < 0) {
        return NULL;
    }

    if (bmp1info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 || bmp1info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
//        LOGE("Bitmap format is not RGBA_8888!");
        return NULL;
    }


    if ((ret = AndroidBitmap_lockPixels(env, jbmp1, &bmp1pixels)) < 0) {
//        LOGE("First Bitmap LockPixels Failed return=%d!", ret);
        return NULL;
    }


    if ((ret = AndroidBitmap_lockPixels(env, jbmp2, &bmp2pixels)) < 0) {
//        LOGE("Second Bitmap LockPixels Failed return=%d!", ret);
        return NULL;
    }

    AndroidBitmap_unlockPixels(env, jbmp1);
    AndroidBitmap_unlockPixels(env, jbmp2);

    height = bmp1info.height;
    width = bmp1info.width;
    cv::Mat image1(height,width,CV_8UC4,bmp1pixels);
    cv::Mat image2(height,width,CV_8UC4,bmp2pixels);
//
    if(!(image1.data &&image2.data)){
        LOGE("before bitmap failed convert to Mat return=%d!", ret);
        LOGD("------------------ CV image CV_8UC4 null ----------------------");
        return NULL;
    }
//
    cvtColor(image1,image1,CV_RGBA2BGR);
    cvtColor(image2,image2,CV_RGBA2BGR);
    if(!(image1.data &&image2.data)){
        LOGE("after bitmap failed convert to Mat return=%d!", ret);
        LOGD("------------------ CV image CV_8UC3 null ----------------------");
        return NULL;
    }

    // jintArray --> int* pointer -->jintArray
    int* RectBuffer = new int[4]; // result rect
    int* arrayPointer = (*env).GetIntArrayElements(jtarget_rect,NULL);
    for(int i=0;i<4;i++){
        RectBuffer[i] = arrayPointer[i];
    }


    // KCF
    bool HOG = true;
    bool FIXEDWINDOW = false;
    bool MULTISCALE = false;
    bool LAB = false;

    KCFTracker kcf_tracker(HOG, FIXEDWINDOW, MULTISCALE, LAB);

    cv::Rect rectSelect = cv::Rect(RectBuffer[0], RectBuffer[1], RectBuffer[2], RectBuffer[3]);
    cv::Rect resultRect;

    kcf_tracker.init(rectSelect, image1);
    resultRect = kcf_tracker.update(image2);
    RectBuffer[0] = resultRect.x;
    RectBuffer[1] = resultRect.y;
    RectBuffer[2] = resultRect.width;
    RectBuffer[3] = resultRect.height;

    (*env).ReleaseIntArrayElements(jtarget_rect,arrayPointer,0);
    // return jniArray
    jintArray array =(*env).NewIntArray(4);
    (*env).SetIntArrayRegion(array,0,4,RectBuffer);
    return array;
}

JNIEXPORT jintArray JNICALL
Java_com_example_wurui_kcf_1ndk_MainActivity_kcfDetect
        (JNIEnv *env, jobject obj, jobject jbmp1, jobject jbmp2, jintArray jkeypoint){
    AndroidBitmapInfo bmp1info;
    void* bmp1pixels;
    AndroidBitmapInfo bmp2info;
    void* bmp2pixels;
    int height,width,ret,y,x;

    // kcf variable
    bool HOG = true;
    bool FIXEDWINDOW = false;
    bool MULTISCALE = false;
    bool LAB = false;
    KCFTracker kcf_tracker(HOG, FIXEDWINDOW, MULTISCALE, LAB);

//解析bitmap
    if ((ret = AndroidBitmap_getInfo(env, jbmp1, &bmp1info)) < 0) {
        return NULL;
    }
    if ((ret = AndroidBitmap_getInfo(env, jbmp2, &bmp2info)) < 0) {
        return NULL;
    }

    if (bmp1info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 || bmp1info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
//        LOGE("Bitmap format is not RGBA_8888!");
        return NULL;
    }


    if ((ret = AndroidBitmap_lockPixels(env, jbmp1, &bmp1pixels)) < 0) {
//        LOGE("First Bitmap LockPixels Failed return=%d!", ret);
        return NULL;
    }


    if ((ret = AndroidBitmap_lockPixels(env, jbmp2, &bmp2pixels)) < 0) {
//        LOGE("Second Bitmap LockPixels Failed return=%d!", ret);
        return NULL;
    }

    AndroidBitmap_unlockPixels(env, jbmp1);
    AndroidBitmap_unlockPixels(env, jbmp2);

    height = bmp1info.height;
    width = bmp1info.width;
    cv::Mat image1(height,width,CV_8UC4,bmp1pixels);
    cv::Mat image2(height,width,CV_8UC4,bmp2pixels);
//
    if(!(image1.data &&image2.data)){
        LOGE("before bitmap failed convert to Mat return=%d!", ret);
        LOGD("------------------ CV image CV_8UC4 null ----------------------");
        return NULL;
    }
//
    cvtColor(image1,image1,CV_RGBA2BGR);
    cvtColor(image2,image2,CV_RGBA2BGR);
    if(!(image1.data &&image2.data)){
        LOGE("after bitmap failed convert to Mat return=%d!", ret);
        LOGD("------------------ CV image CV_8UC3 null ----------------------");
        return NULL;
    }

    // jintArray --> int* pointer -->jintArray
    int* RectBuffer = new int[36]; // result rect
    int* arrayPointer = (*env).GetIntArrayElements(jkeypoint,NULL);
    for(int i=0;i<36;i++){
        RectBuffer[i] = arrayPointer[i];
    }

    // Roi
    int keypoint[36];
    int windowSize = 20;
    int dx1 = windowSize/2;
    int dx2 = windowSize/2;
    int dy1 = windowSize/2;
    int dy2 = windowSize/2;

    // 14 part of body
    for(int i=0;i<18;i++){
        int cx = RectBuffer[2*i];
        int cy = RectBuffer[2*i+1];
        if(cx==0 && cy==0){
            // 这个点没有检测到
            keypoint[2*i]=0;
            keypoint[2*i+1]=0;
            continue;
        }
        if ( i>14 ){
            // 头部只检测一个点  节约时间
            keypoint[2*i]=0;
            keypoint[2*i+1]=0;
            continue;
        }
        else {
            // keypoint -> roi rect
            dx1 = min(dx1,cx);
            dx2 = min(dx2,(width - cx));
            dy1 = min(dy1,cy);
            dy2 = min(dy2,(height - cy));
            int roi[4];
            roi[0] = cx-dx1;
            roi[1] = cy-dy1;
            roi[2] = dx1+dx2;
            roi[3] = dy1+dy2;

            // KCF
            cv::Rect rectSelect = cv::Rect(roi[0], roi[1], roi[2], roi[3]);
            cv::Rect resultRect;
            kcf_tracker.init(rectSelect, image1);
            resultRect = kcf_tracker.update(image2);

            keypoint[2*i] = resultRect.x + dx1;
            keypoint[2*i+1] = resultRect.y + dy1;
        }
    }

    (*env).ReleaseIntArrayElements(jkeypoint,arrayPointer,0);
    // return jniArray
    jintArray array =(*env).NewIntArray(4);
    (*env).SetIntArrayRegion(array,0,4,keypoint);
    return array;
}
