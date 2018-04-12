//
// Created by wurui on 17-12-11.
//

#include "kcfClass.h"
#include "kcftracker.hpp"
#include <android/log.h>
#include "eben_hpc_log.h"
#include <math.h>

using namespace cv;
using namespace std;

JNIEXPORT jlong JNICALL
Java_camera_hj_cameracontroller_decoder_jniKCF_createNativeObject(JNIEnv *env, jobject obj){
    jlong result;

    bool HOG = true;
    bool FIXEDWINDOW = false;
    bool MULTISCALE = false;
    bool LAB = false;
    result = (jlong) new KCFTracker(HOG, FIXEDWINDOW, MULTISCALE, LAB);
    return result;
  }

/*
 * Class:     com_example_wurui_kcf_ndk_jniKCF
 * Method:    kcfInit
 * Signature: (Ljava/lang/Object;[I)V
 */
JNIEXPORT void JNICALL Java_camera_hj_cameracontroller_decoder_jniKCF_kcfInit
  (JNIEnv *env, jobject obj, jlong kcfClassAddr,jobject jbmp1, jintArray jrect){
    // 获取图片
    AndroidBitmapInfo bmp1info;
    void* bmp1pixels;
    int height,width,ret,y,x;

    if ((ret = AndroidBitmap_getInfo(env, jbmp1, &bmp1info)) < 0) {
        return ;
    }

    if (bmp1info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 || bmp1info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
        return ;
    }

    if ((ret = AndroidBitmap_lockPixels(env, jbmp1, &bmp1pixels)) < 0) {
        LOGE("First Bitmap LockPixels Failed return=%d!", ret);
        return ;
    }

    AndroidBitmap_unlockPixels(env, jbmp1);
    height = bmp1info.height;
    width = bmp1info.width;
    cv::Mat image1(height,width,CV_8UC4,bmp1pixels);

    if(!(image1.data )){
        LOGE("before bitmap failed convert to Mat return=%d!", ret);
        LOGD("------------------ CV image CV_8UC4 null ----------------------");
        return ;
    }

    // 得到 BGR 图像 ==== image1
    cvtColor(image1,image1,CV_RGBA2BGR);
    if(!(image1.data)){
        LOGE("after bitmap failed convert to Mat return=%d!", ret);
        LOGD("------------------ CV image CV_8UC3 null ----------------------");
        return ;
    }

    // 读取数组 ==== RectBuffer
    int* RectBuffer = new int[4]; // result rect
    int* arrayPointer = (*env).GetIntArrayElements(jrect,NULL);
    for(int i=0;i<4;i++){
        RectBuffer[i] = arrayPointer[i];
    }
    cv::Rect rect = cv::Rect(RectBuffer[0], RectBuffer[1], RectBuffer[2], RectBuffer[3]);

    // 初始化 kcf tracker
    ((KCFTracker*)kcfClassAddr)->init(rect, image1);
    (*env).ReleaseIntArrayElements(jrect,arrayPointer,0);
}

/*
 * Class:     com_example_wurui_kcf_ndk_jniKCF
 * Method:    kcfUpdate
 * Signature: (Ljava/lang/Object;)[I
 */
JNIEXPORT jintArray JNICALL Java_camera_hj_cameracontroller_decoder_jniKCF_kcfUpdate
  (JNIEnv *env, jobject obj, jlong kcfClassAddr,jobject jbmp1){
    // 获取图片
    AndroidBitmapInfo bmp1info;
    void* bmp1pixels;
    int height,width,ret,y,x;

    if ((ret = AndroidBitmap_getInfo(env, jbmp1, &bmp1info)) < 0) {
        return NULL;
    }

    if (bmp1info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 || bmp1info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
        return NULL;
    }

    if ((ret = AndroidBitmap_lockPixels(env, jbmp1, &bmp1pixels)) < 0) {
        LOGE("First Bitmap LockPixels Failed return=%d!", ret);
        return NULL;
    }

    AndroidBitmap_unlockPixels(env, jbmp1);
    height = bmp1info.height;
    width = bmp1info.width;
    cv::Mat image1(height,width,CV_8UC4,bmp1pixels);

    if(!(image1.data )){
        LOGE("before bitmap failed convert to Mat return=%d!", ret);
        LOGD("------------------ CV image CV_8UC4 null ----------------------");
        return NULL;
    }

    // 得到 BGR 图像 ==== image1
    cvtColor(image1,image1,CV_RGBA2BGR);
    if(!(image1.data)){
        LOGE("after bitmap failed convert to Mat return=%d!", ret);
        LOGD("------------------ CV image CV_8UC3 null ----------------------");
        return NULL;
    }

    // Update kcf
    cv::Rect rect;
    rect = ((KCFTracker*)kcfClassAddr)->update(image1);

    // 返回结果 数组
    int resultRect[4];
    resultRect[0] = rect.x;
    resultRect[1] = rect.y;
    resultRect[2] = rect.width;
    resultRect[3] = rect.height;

    jintArray array =(*env).NewIntArray(4);
    (*env).SetIntArrayRegion(array,0,4,resultRect);
    return array;
}

