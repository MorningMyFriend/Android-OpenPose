 # This is a nearly-realtime person pose detect app on Android , developed in November 2017.  

It's based on OpenPose(CMU) and tracking algorithm. To speed up, i tried tensorflow-mobile, but the accuracy and speed is not acceptable. And HuaWei's NPU is not friendly to deverlopers. Fortunately,  snpe-1.6.0 surport caffe CNN models, so i just convert CMU's original model to a smaller snpe model(160*160).

speed: about 500-600ms on cellphone with snap dragon835, my classmate tried the model on ios, 0.48s on A11.

there are some source files may be useful:

- snpe api example: it shows how to load and run a snpe model. (load: src/main/java/camera/hj/cameracontroller/utils/CameraApplication.java  run:decoder/RunModel.java)
- a tracking algorithm(KCF) jni lib: a java class using KCF C++ source with jni and opencv-android-sdk(decoder/jniKCF.java)
- a scripts to connect the keypoint from CNN output heatmaps to a human body (decoder/common.java)

Notice:
- the project need to setup environment yourself. dependent packages: opencv-android-sdk, snpe libs
- i change the input size of cmu openpose model to 160*160
- now the snpe model can be download here: 链接: https://pan.baidu.com/s/1hQ0AxcK3cge0ZfdslRMg8Q 提取码: qgkd 复制这段内容后打开百度网盘手机App，操作更方便哦


# have fun

though the algotithm can detect 18 keypoints of human body, consider the speed, i just track and draw 6 of them to realize a push-up counter. 

![image](https://github.com/RuiZeWu/Android-OpenPose/blob/master/demo1-1.gif)

![image](https://github.com/RuiZeWu/Android-OpenPose/blob/master/demo1-1.gif)

![image](https://github.com/RuiZeWu/Android-OpenPose/blob/master/demo2-1.gif)


