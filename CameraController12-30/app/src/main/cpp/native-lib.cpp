#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <vector>
#include "test.h"
#include<android/bitmap.h>



using namespace cv;
using namespace std;

extern "C"{

JNIEXPORT jstring

JNICALL
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

JNIEXPORT jint

JNICALL
Java_com_example_wurui_kcf_1ndk_MainActivity_bodyDetect
        (JNIEnv *env, jobject obj, jobject jbmp1, jobject jbmp2, jdouble jmeanRatio, jintArray jtarget_rect){

    AndroidBitmapInfo bmp1info;
    void* bmp1pixels;
    AndroidBitmapInfo bmp2info;
    void* bmp2pixels;
    int height,width,ret,y,x;

//解析bitmap
    if ((ret = AndroidBitmap_getInfo(env, jbmp1, &bmp1info)) < 0) {
        return -1;
    }
    if ((ret = AndroidBitmap_getInfo(env, jbmp2, &bmp2info)) < 0) {
        return -1;
    }

    if (bmp1info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 || bmp1info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
//        LOGE("Bitmap format is not RGBA_8888!");
        return -1;
    }


    if ((ret = AndroidBitmap_lockPixels(env, jbmp1, &bmp1pixels)) < 0) {
//        LOGE("First Bitmap LockPixels Failed return=%d!", ret);
        return -1;
    }


    if ((ret = AndroidBitmap_lockPixels(env, jbmp2, &bmp2pixels)) < 0) {
//        LOGE("Second Bitmap LockPixels Failed return=%d!", ret);
        return -1;
    }
    AndroidBitmap_unlockPixels(env, jbmp1);
    AndroidBitmap_unlockPixels(env, jbmp2);

    height = bmp1info.height;
    width = bmp1info.width;
    cv::Mat src(height,width,CV_8UC4,bmp1pixels);
    cv::Mat body(height,width,CV_8UC4,bmp2pixels);
    if(!(src.data &&body.data)){
//        LOGE("bitmap failed convert to Mat return=%d!", ret);
        return -1;
    }
    int val = src.cols;
//    转换为灰度图像
//    cvtColor(src,src,CV_RGBA2GRAY);
//    cvtColor(body,body,CV_RGBA2GRAY);
    return val;
}

}

