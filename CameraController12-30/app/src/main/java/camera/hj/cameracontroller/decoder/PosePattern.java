package camera.hj.cameracontroller.decoder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import camera.hj.cameracontroller.CameraApplication;
import camera.hj.cameracontroller.controller.event.PushUpToastEvent;
import camera.hj.cameracontroller.utils.SizeUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by T470S on 2017/12/12.
 */

public class PosePattern extends AbstractPattern {
    public final static String TAG = "PosePattern";
    private static int[] result;
    private PoseListener mListener;
    private boolean isInit = false;

    public PosePattern(WorkLine workLine,WorkingFlag flag) {
        super(workLine,flag);
    }

    public void setOnPoseListener(PoseListener PoseListener) {
        this.mListener = PoseListener;
    }

    public int[][] CocoPairs = {
            {1, 2}, {1, 5}, {2, 3}, {3, 4}, {5, 6}, {6, 7}};
    public int count =0;
    public String timeUpdate;

    @Override
    public int[] init(Bitmap resource, int[] eigen) {
        return new int[36];
    }

    @Override
    public int[] update(Bitmap resource) {
        long time1 = System.currentTimeMillis();
        RunModel mRunModel = CameraApplication.getRunModel();
        int[] keypoint = mRunModel.detect(resource);
        long time2 = System.currentTimeMillis();
        timeUpdate = String.valueOf(time2-time1);
        return keypoint;
    }

    @Override
    public void draw(Bitmap resource, int[] eigen) {
        Canvas canvas = new Canvas(resource);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        for(int i = 0;i<6;i++) {
            int p1 = CocoPairs[i][0];
            int p2 = CocoPairs[i][1];
            if (eigen[2 * p1] != 0 && eigen[2 * p1 + 1] != 0 && eigen[2 * p2] != 0 && eigen[2 * p2 + 1] != 0) {
                canvas.drawCircle(eigen[2 * p1], eigen[2 * p1 + 1], 3, paint);
                canvas.drawCircle(eigen[2 * p2], eigen[2 * p2 + 1], 3, paint);
                canvas.drawLine(eigen[2 * p1], eigen[2 * p1 + 1], eigen[2 * p2], eigen[2 * p2 + 1], paint);
            }
        }

        String text = " time="+timeUpdate;
        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.BLUE);
        paintText.setStyle(Paint.Style.STROKE);
        paintText.setStrokeWidth(2);
        canvas.drawText(text, 10, 40, paintText);
        count++;

//        int w = resource.getWidth();
//        int h = resource.getHeight();
//        int dw = 720;
//        int dh = 720;
//        // resize and rotate
//        float scaleW = ((float)dw) / w;
//        float scaleH = ((float)dh) / h;
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleW,scaleH);
////        matrix.postRotate(90);
//        resource = Bitmap.createBitmap(resource,0 ,0,w,h,matrix,true);
    }

    @Override
    public void run() {
//        while (true){
//            Bitmap temp = null;
//            if (!isInit){
//                // 只初始化一次
//                isInit = true;
//                synchronized(flag0){
//                    temp = mWorkLine.getSource();
//                    result = update(temp);
//                    CameraApplication.getFwcCounter().update(result);
//                    mListener.GetResult(temp,result);
//                    flag0.flagInit = true; // 此后 flag0.flagInit 的值为 true 不变
//                    flag0.notifyAll();
//                }
//            }
//            else {
//                // pose 等kcf取好 再取
//                synchronized (flag1){
//                    if (!flag1.kcfTaken){
//                        try{
//                            flag1.wait();
//                        }
//                        catch (Exception e){
//
//                        }
//                    }
//                    temp = mWorkLine.getSource();
//                    flag1.kcfTaken = false; // flag1 复位
//                    notifyAll();
//                }
//                // 取过图后 开始计算
//                result = update(temp);
//                // 计算后 重置kcf， kcf已经在等待重置，   前提是 kcf 提前于 pose 计算结束
//
//                // 确保kcf 计算完毕
//                synchronized (flag2){
//                    if (!flag2.flagKCFOK){
//                        try{
//                            flag2.wait();
//                        }
//                        catch (Exception e){
//
//                        }
//                    }
//                    flag2.flagKCFOK = false;
//                    flag2.notifyAll();
//                }
//
//                // 重置kcf 并将结果 添加到 mWorkline
//                synchronized (flag3){
//                    if (!flag3.flag3kcfReset){
//                        mListener.GetResult(temp,result);
//                        flag3.flag3kcfReset = true;
//                        flag3.notifyAll();
//                    }
//                }
//            }
//        }

        while(true) {
            Bitmap temp = null;
            if (CameraApplication.getFwcCounter().mCounter.isReady) {

                synchronized (flag) {
                    if (!flag.isPosWorking) {
                        try {
                            flag.wait();
                        } catch (Exception e) {
                            Log.e("Pattern error", "flag wait error.");
                        }
                    }
                    temp = mWorkLine.getSource();
                    long timeend = System.currentTimeMillis();
                    String s = String.valueOf(timeend-CameraApplication.getFwcCounter().mCounter.timeStart);
                    Log.d(TAG, "========================================== pose take TIME: " + s + " ms==================================================");
                    flag.isPosWorking = false;
                    flag.notifyAll();
                }
                if (temp != null) {
                    long time1 = System.currentTimeMillis();
                    String ss = String.valueOf(time1-CameraApplication.getFwcCounter().mCounter.timeStart);
                    Log.d(TAG, "========================================== pose start TIME: " + ss + " ms==================================================");
                    // 计算结果
                    if (!this.isInit){
                        result = CameraApplication.getFwcCounter().mCounter.initBodyPoints;
                        this.isInit = true;
                    }
                    else {
                        result = update(temp);
                    }
//                CameraApplication.getFwcCounter().update(result);
                    CameraApplication.getFwcCounter().draw(temp);
                    CountResult c = new CountResult(CameraApplication.getFwcCounter().mCounter.count, CameraApplication.getFwcCounter().mCounter.angle,
                            CameraApplication.getFwcCounter().mCounter.isLegal, CameraApplication.getFwcCounter().mCounter.isLegalElbow);
                    long time2 = System.currentTimeMillis();
                    String t = String.valueOf(time2 - time1);
                    Log.d(TAG, "===================================== pose detect :" + t + " ms============================");
                    String s = String.valueOf(time2-CameraApplication.getFwcCounter().mCounter.timeStart);
                    Log.d(TAG, "========================================== pose end TIME: " + s + " ms==================================================");

                    mListener.GetResult(temp, result, c);
                }
            }
        }
    }


    interface PoseListener{
        public void GetResult(Bitmap posRes,int[] result,CountResult c);
    }
}
