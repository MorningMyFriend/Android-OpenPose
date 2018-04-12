package camera.hj.cameracontroller.decoder;

import android.graphics.Bitmap;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.security.PublicKey;
import java.util.Collections;
import java.util.SimpleTimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import camera.hj.cameracontroller.CameraApplication;
import camera.hj.cameracontroller.constant.Settings;
import camera.hj.cameracontroller.utils.GCUtils;
import camera.hj.cameracontroller.utils.SizeUtils;

import static camera.hj.cameracontroller.constant.Settings.KCF_DATA_GROUP;

/**
 * Created by T470S on 2017/12/12.
 */

public class KCFPattern extends AbstractPattern implements PosePattern.PoseListener{

    private class kcfFlag{
        boolean isKCFinitReady=false;
    }
    public static ArrayBlockingQueue<Bitmap> tempResblock=new ArrayBlockingQueue<Bitmap>(KCF_DATA_GROUP*2);
    public static ArrayBlockingQueue<Bitmap> tempProblock=new ArrayBlockingQueue<Bitmap>(KCF_DATA_GROUP*2);

    public static ArrayBlockingQueue<int[]> keypoints=new ArrayBlockingQueue<int[]>(KCF_DATA_GROUP*2);
    public static ArrayBlockingQueue<CountResult> countResults=new ArrayBlockingQueue<CountResult>(KCF_DATA_GROUP*2);


    private kcfFlag mkcfFlag;
    public KCFPattern(WorkLine workLine,WorkingFlag flag) {
        super(workLine,flag);
        mkcfFlag=new kcfFlag();
    }

    // wrz
    public final static String TAG = "KCFPattern";
    public int[][] CocoPairs = {{2, 3}, {3, 4}, {5, 6}, {6, 7},{2, 5}};
    public int count =0;
    public String timeUpdate;
    private boolean isInit = false;

    @Override
    public int[] init(Bitmap resource, int[] eigen) {
        CameraApplication.resetKcfTracker(resource, eigen);
        return new int[36];
    }

    @Override
    public int[] update(Bitmap resource) {
        long time1 = System.currentTimeMillis();
        poseTracker kcfTracker = CameraApplication.getKcfTracker();
        int[] keypoint = kcfTracker.update(resource);
        long time2 = System.currentTimeMillis();
        timeUpdate = String.valueOf(time2-time1);
        return keypoint;
    }

    @Override
    public void draw(Bitmap resource, int[] eigen) {
        Canvas canvas = new Canvas(resource);
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        // 左右肩膀中心给一个点=脖子，脖子和鼻子要连线
//        int[] neck = new int[2];
//        if (eigen[2*2]>0 && eigen[2*2+1]>0 && eigen[2*5]>0 && eigen[2*5+1]>0){
//            neck[0] = (int)((eigen[2*2]+eigen[2*5])/2);
//            neck[1] = (int)((eigen[2*2+1]+eigen[2*5+1])/2);
//            canvas.drawCircle(neck[0], neck[1], 3, paint);
//            canvas.drawLine(eigen[2*2],eigen[2*2+1],neck[0],neck[1],paint);
//            canvas.drawLine(eigen[2*5],eigen[2*5+1],neck[0],neck[1],paint);
//        }

//        if (neck[0]>0 && neck[1]>0 && eigen[0]>0 && eigen[1]>0){
//            canvas.drawCircle(eigen[2*0], eigen[2 * 0 + 1], 3, paint);
//            canvas.drawLine(eigen[2*0],eigen[2*0+1],neck[0],neck[1],paint);
//        }

        for(int i = 0;i<CocoPairs.length;i++) {
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
        paintText.setColor(Color.RED);
        paintText.setStyle(Paint.Style.STROKE);
        paintText.setStrokeWidth(2);
        canvas.drawText(text, 10, 10, paintText);
        count++;
        Log.d(TAG,"========================================== KCF DETECT ==================================================");

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
//
//        resource = Bitmap.createBitmap(resource,0 ,0,w,h,matrix,true);
    }

    @Override
    public void run() {
//        while (true){
//            if (!isInit){
//                // 第一次初始化
//                synchronized (flag0){
//                    if (!flag0.flagInit){
//                        try{
//                            flag0.wait();
//                        }
//                        catch (Exception e){
//
//                        }
//                    }
//                }
//                isInit = true;
//            }
//            else {
//                // kcf 取图像 2 3 4 pose 取 组末 5
//                synchronized (flag1){
//                    if (!flag1.kcfTaken){
//                        // kcf 取图
//                        Collections.addAll(tempResblock,mWorkLine.getSources(Settings.KCF_DATA_GROUP));
//                        flag1.kcfTaken = true;
//                        flag1.notifyAll();
//                    }
//                }
//
//                // 取过图后开始计算  计算结果图在 temProblock 里
//                try{
//                    Log.d("size","kcf update work start,res size="+tempResblock.size()+";pro size="+tempProblock.size());
//                    int res_num=tempResblock.size();
//                    for(int i=0;i<res_num;i++){
//                        Bitmap b=tempResblock.take();
//                        long time1 = System.currentTimeMillis();
//                        int[] result = update(b); // 计算结果 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//                        CameraApplication.getFwcCounter().update(result);
//    //                    CameraApplication.getFwcCounter().draw(b);
//                        draw(b,result);
//                        long time2 = System.currentTimeMillis();
//                        String t = String.valueOf(time2-time1);
//                        Log.d(TAG,"========================================== KCF DETECT: "+t+" ms==================================================");
//                        b = SizeUtils.preprocessBitmap(b, 0, 600,600);
//                        tempProblock.offer(b);
//                    }
//                    Log.d("size","kcf update work over size="+tempResblock.size()+";pro size="+tempProblock.size());
//                }
//                catch (Exception e){
//    //                      TODO:异常处理待优化，可能回收已处理数据
//                    Log.e("Pattern error","kcf update interrupt.");
//                    GCUtils.BitmapGC(tempResblock);
//                    GCUtils.BitmapGC(tempProblock);
//                }
//
//                synchronized (flag2){
//                    flag2.flagKCFOK = true;
//                    flag2.notifyAll();
//                }
//
//                synchronized (flag3){
//                    if (!flag3.flag3kcfReset){
//                        try{
//                            flag3.wait();
//                        }
//                        catch (Exception e){
//
//                        }
//                    }
//                    // 这里说明 Pose 已经重置 kcf
//                    flag3.flag3kcfReset = false;
//                    flag3.notifyAll();
//
//                }
//
//            }
//        }

        while(true) {
            if (CameraApplication.getFwcCounter().mCounter.isReady) {
                synchronized (flag) {
                    if (flag.isPosWorking) {
                        try {
                            flag.wait();
                        } catch (Exception e) {
                            Log.e("Pattern error", "flag wait error.");
                        }
                    }
                    long timeend = System.currentTimeMillis();
                    String ss = String.valueOf(timeend-CameraApplication.getFwcCounter().mCounter.timeStart);
                    Log.d(TAG, "========================================== kcf take start TIME: " + ss + " ms==================================================");
                    Collections.addAll(tempResblock, mWorkLine.getSources(Settings.KCF_DATA_GROUP));
                    long time = System.currentTimeMillis();
                    String ts = String.valueOf(time-CameraApplication.getFwcCounter().mCounter.timeStart);
                    Log.d(TAG, "========================================== kcf take end TIME: " + ts + " ms==================================================");
                    flag.isPosWorking = true;
                    flag.notifyAll();
                }

                synchronized (mkcfFlag) {
                    if (!mkcfFlag.isKCFinitReady) {
                        try {
                            mkcfFlag.wait();
                        } catch (Exception e) {
                            Log.e("Pattern error", "flag wait error.");
                        }
                    }
                    mkcfFlag.isKCFinitReady = false;
                    mkcfFlag.notifyAll();
                }
                long timeend = System.currentTimeMillis();
                String s = String.valueOf(timeend-CameraApplication.getFwcCounter().mCounter.timeStart);
                Log.d(TAG, "==========================================kcf reset ok TIME: " + s + " ms==================================================");

                try {
                    Log.d("size", "kcf update work start,res size=" + tempResblock.size() + ";pro size=" + tempProblock.size());
                    int res_num = tempResblock.size();
                    long time1 = System.currentTimeMillis();
                    String s1 = String.valueOf(time1-CameraApplication.getFwcCounter().mCounter.timeStart);
                    Log.d(TAG, "========================================== KCF START TIME: " + s1 + " ms==================================================");
                    for (int i = 0; i < res_num; i++) {
                        Bitmap b = tempResblock.take();
//                    long time1 = System.currentTimeMillis();
                        int[] result = update(b); // 计算结果 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        this.keypoints.offer(result);
//                    CameraApplication.getFwcCounter().update(result);
//                    CameraApplication.getFwcCounter().draw(b);
                        draw(b, result);
//                    CountResult c = new CountResult(CameraApplication.getFwcCounter().mCounter.count,CameraApplication.getFwcCounter().mCounter.angle);
//                    countResults.offer(c);
//                    long time2 = System.currentTimeMillis();
//                    String t = String.valueOf(time2-time1);
//                    Log.d(TAG,"========================================== KCF DETECT ==================================================");
                        b = SizeUtils.preprocessBitmap(b, 0, 600, 600);
                        tempProblock.offer(b);
                    }
                    long time2 = System.currentTimeMillis();
                    String t = String.valueOf(time2 - time1);
                    Log.d(TAG, "========================================== KCF DETECT: " + t + " ms==================================================");
                    String ss = String.valueOf(time2-CameraApplication.getFwcCounter().mCounter.timeStart);
                    Log.d(TAG, "========================================== KCF END TIME: " + ss + " ms==================================================");
                    Log.d("size", "kcf update work over size=" + tempResblock.size() + ";pro size=" + tempProblock.size());
                } catch (Exception e) {
//                      TODO:异常处理待优化，可能回收已处理数据
                    Log.e("Pattern error", "kcf update interrupt.");
                    GCUtils.BitmapGC(tempResblock);
                    GCUtils.BitmapGC(tempProblock);
                }
            }
        }
    }

    @Override
    public void GetResult(Bitmap posRes,int[] result,CountResult c) {
//
//        if (!isInit){
//            init(posRes,result);
//            // 添加 第 1 张
//            draw(posRes, result);
//            posRes = SizeUtils.preprocessBitmap(posRes,0,600,600);
//            mWorkLine.addProduct(posRes);
//        }
//        else {
//            // kcf，pose 计算，pose 重置kcf 后，添加计算结果
//
//            // kcf 第 2 3 4 张
//            if (tempProblock.size() > 0) {
//                Log.d("size","kcf Add to Workline"+tempProblock.size());
//                mWorkLine.addProducts(tempProblock);
//            }
//            // pose 第 5 张
//            draw(posRes, result);
//            posRes = SizeUtils.preprocessBitmap(posRes,0,600,600);
//            mWorkLine.addProduct(posRes);
//
//        }

        synchronized (mkcfFlag) {
            if (posRes != null) {
                init(posRes, result);
            }
            mkcfFlag.isKCFinitReady=true;
            mkcfFlag.notifyAll();
            Log.d("size","mkc notify");
        }
        draw(posRes, result);
        posRes = SizeUtils.preprocessBitmap(posRes,0,600,600);
        if (tempProblock.size() > 0) {
            Log.d("size","kcf Add to Workline"+tempProblock.size());
            mWorkLine.addProducts(tempProblock);
        }
        mWorkLine.addProduct(posRes);

        long time = System.currentTimeMillis();
        String s = String.valueOf(time-CameraApplication.getFwcCounter().mCounter.timeStart);
        Log.d(TAG, "========================================== getResult TIME: " + s + " ms==================================================");
        int len = keypoints.size();
        for (int i=0;i<len;i++){
            try{
                int[] pts = keypoints.take();
                CameraApplication.getFwcCounter().update(pts);
                CountResult tempc = new CountResult(CameraApplication.getFwcCounter().mCounter.count,CameraApplication.getFwcCounter().mCounter.angle,
                        CameraApplication.getFwcCounter().mCounter.isLegal, CameraApplication.getFwcCounter().mCounter.isLegalElbow);
                CameraApplication.setmCountResultQue(tempc);
            }
            catch (Exception e){

            }
        }
        CameraApplication.getFwcCounter().update(result);
        CameraApplication.setmCountResultQue(c);
        long timeend = System.currentTimeMillis();
        String ss = String.valueOf(timeend-CameraApplication.getFwcCounter().mCounter.timeStart);
        Log.d(TAG, "========================================== getResult end TIME: " + ss + " ms==================================================");
    }
}
