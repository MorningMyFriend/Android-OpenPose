package camera.hj.cameracontroller.constant;

/**
 * Created by NC040 on 2017/11/19.
 */

public class Settings {


    //码率
    public final static int FrameRate = 15;

    public static void setVideoWidth(int videoWidth) {
        VIDEO_WIDTH = videoWidth;
    }

    public static void setVideoHeight(int videoHeight) {
        VIDEO_HEIGHT = videoHeight;
    }

    //视频分辨率
    public static int VIDEO_WIDTH = 800;
    public static int VIDEO_HEIGHT = 600;

    //Pattern使用的Bitmap高宽
    public static int PATTERRN_WIDTH = 800;
    public static int PATTERRN_HEIGHT = 600;

    //pose结束后kcf每次获取的数据数量
    public final static int KCF_DATA_GROUP = 7;

    //workline缓存图片最大数量 160*160  1920*1080=80
    public final static int BMP_SAVE_NUM = 15*100;

    // 跳帧数量
    public final static int FRAME_STEP = 2;
}
