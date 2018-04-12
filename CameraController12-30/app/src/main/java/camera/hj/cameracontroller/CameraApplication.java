package camera.hj.cameracontroller;

import android.app.Application;
import android.graphics.Bitmap;

import com.qualcomm.qti.snpe.NeuralNetwork;

import java.util.concurrent.ArrayBlockingQueue;

import camera.hj.cameracontroller.decoder.CountResult;
import camera.hj.cameracontroller.decoder.RunModel;
import camera.hj.cameracontroller.decoder.poseTracker;
import camera.hj.cameracontroller.decoder.actionCounter;


/**
 * Created by NC040 on 2017/11/19.
 */

public class CameraApplication extends Application {
    private static CameraApplication mInstance;
    public static CameraApplication getInstance() {
        return mInstance;
    }

    // global variable
    public static RunModel runModel;
    public static RunModel getRunModel(){return runModel; }
    public static void setRunModel(NeuralNetwork tempNetwork){runModel.mNeuralNetwork = tempNetwork;}

    public static poseTracker kcfTracker;
    public static poseTracker getKcfTracker(){return kcfTracker;}
    public static void resetKcfTracker(Bitmap bmp, int[] keypoint){kcfTracker.reset(bmp, keypoint);}

    public static actionCounter fwcCounter;
    public static actionCounter getFwcCounter(){return fwcCounter;}

    public static ArrayBlockingQueue<CountResult> mCountResultQue;
    public static ArrayBlockingQueue<CountResult> getmCountResultQue(){return mCountResultQue;}
    public static void setmCountResultQue(CountResult c){mCountResultQue.offer(c);}
    public static CountResult takeCountResultQue(){
        try {
            return mCountResultQue.take();
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        //崩溃日志记录，开发测试中需要注释掉
//        CrashHandler crashHandler=CrashHandler.getInstance();
//        crashHandler.initChecker(getApplicationContext());

        // wrz
        runModel = new RunModel(this);
        kcfTracker = new poseTracker();
        kcfTracker.init(16);
        fwcCounter = new actionCounter();
        mCountResultQue = new ArrayBlockingQueue<CountResult>(500);
    }
}
