package camera.hj.cameracontroller.decoder;

import android.graphics.Bitmap;

/**
 * Created by wurui on 17-12-11.
 */

public class jniKCF {
    // JNI
    static {
        System.loadLibrary("jniKCF");
    }
    //保存c++类的地址
    long nativeKCF;
    //构造函数
    public jniKCF(){
        this.nativeKCF = createNativeObject();
    }
    /**本地方法：创建c++对象并返回地址*/
    private native long createNativeObject();

    public native void kcfInit(long kcfAddr, Object bmp, int[] rect);

    public native int[] kcfUpdate(long kcfAddr, Object bmp);

    // Functions
    public void init(Bitmap bitmap, int[] rect){
        kcfInit(this.nativeKCF, bitmap, rect);
    }

    public int[] update(Bitmap bitmap){
        int rect[] = new int[4];
        rect = kcfUpdate(this.nativeKCF, bitmap);
        return rect;
    }

}
