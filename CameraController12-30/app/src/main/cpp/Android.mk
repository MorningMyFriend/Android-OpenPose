LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

ifdef OPENCV_ANDROID_SDK
  ifneq ("","$(wildcard $(OPENCV_ANDROID_SDK)/OpenCV.mk)")
    include ${OPENCV_ANDROID_SDK}/OpenCV.mk
  else
    include ${OPENCV_ANDROID_SDK}/sdk/native/jni/OpenCV.mk
  endif
else
  include ../../sdk/native/jni/OpenCV.mk
endif

include /home/wurui/project/android/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := jniKCF

LOCAL_SRC_FILES := native-test.cpp \
                   test.cpp \
                   ffttools.hpp \
                   fhog.cpp \
                   kcftracker.cpp \
                   labdata.hpp \
                   recttools.hpp \
                   kcfClass.cpp

# LOCAL_SRC_FILES := jni_part.cpp \
                   fhog.hpp \
                   gradientMex.cpp \
                   kcf.cpp \
                   kcf.hpp \
                   selectRoi.h \
                   sse.hpp \
                   wrappers.hpp

LOCAL_LDLIBS +=  -llog -ldl \
                 -lm -llog -ljnigraphics
LOCAL_LDFLAGS += -ljnigraphics ## mat/bitmap convert

include $(BUILD_SHARED_LIBRARY)
