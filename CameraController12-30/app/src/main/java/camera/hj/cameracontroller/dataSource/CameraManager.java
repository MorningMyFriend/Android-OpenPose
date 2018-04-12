package camera.hj.cameracontroller.dataSource;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import camera.hj.cameracontroller.CameraApplication;
import camera.hj.cameracontroller.constant.Settings;
import camera.hj.cameracontroller.controller.event.PushUpToastEvent;
import camera.hj.cameracontroller.decoder.KCFPattern;
import camera.hj.cameracontroller.decoder.PosePattern;
import camera.hj.cameracontroller.decoder.actionPrepare;
import camera.hj.cameracontroller.decoder.WorkLine;
import camera.hj.cameracontroller.decoder.WorkingFlag;
import camera.hj.cameracontroller.ui.activity.MainPageActivity;
import camera.hj.cameracontroller.ui.activity.pushUp.PushUpActivity;
import camera.hj.cameracontroller.utils.IOUtils;
import camera.hj.cameracontroller.utils.SizeUtils;
import camera.hj.cameracontroller.utils.YUVToRGBHelper;
import de.greenrobot.event.EventBus;

import static camera.hj.cameracontroller.utils.GCUtils.BitmapGC;

/**
 * Created by NC040 on 2017/11/27.
 */

public class CameraManager implements SurfaceHolder.Callback, Camera.PreviewCallback  {
   private Context ctx;
    private SurfaceView cameraSurface;
    private SurfaceView bitmapSurface;
    private Camera camera;
    private YUVToRGBHelper mConvertHelper;
    private Size optionSize;
    private boolean playFlag=true;
    private static long timerBefore=System.currentTimeMillis();

    private WorkLine WorkLine;

    // wrz debug
    private int index = 30;
    private int FRAME_COUNT = 0;

    public CameraManager(Context ctx,SurfaceView cameraSurface,WorkLine WorkLine) {
        this.ctx=ctx;
        this.cameraSurface=cameraSurface;
        this.WorkLine = WorkLine;
        Camera mCamera = Camera.open();
        Camera.Parameters p = mCamera.getParameters();
        p.setPreviewFormat(ImageFormat.NV21);
        mConvertHelper=new YUVToRGBHelper(ctx);
        WorkingFlag wf=new WorkingFlag();
        PosePattern posPattern=new PosePattern(WorkLine,wf);
        KCFPattern kcfPattern=new KCFPattern(WorkLine,wf);
        actionPrepare actionPrepare = new actionPrepare(WorkLine,wf);
        posPattern.setOnPoseListener(kcfPattern);
        actionPrepare.start();
        posPattern.start();
        kcfPattern.start();
        new PlayThread().start();
        cameraSurface.getHolder().addCallback(this);
    }

    public void setBitmapSurface(SurfaceView surface){
        bitmapSurface=surface;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            // 前置摄像头
            int cameraCount = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();
            for (int i=0;i<cameraCount;i++){
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                    camera = Camera.open(i);//camera = Camera.open(i);
                }
            }
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            if (null != camera) {
                camera.release();
                camera = null;
            }
            e.printStackTrace();
            Toast.makeText(ctx, "启动摄像头失败,请开启摄像头权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Camera.Parameters parameters = camera.getParameters();//获取camera的parameter实例
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();//获取所有支持的camera尺寸
        Size[] sizes = new Size[sizeList.size()];
        // 获取支持的 fps  [15000,15000] 15fps  [30000,30000] 30 fps
        List<int[]> FPS =  parameters.getSupportedPreviewFpsRange();
        int[] fpsSupport = FPS.get(0);
        parameters.setPreviewFpsRange(fpsSupport[0],fpsSupport[1]);

        int ii = 0;
        for (Camera.Size size : sizeList) {
            sizes[ii++] = new Size(size.width, size.height);
        }
        optionSize = SizeUtils.chooseOptimalSize(sizes, cameraSurface.getWidth(), cameraSurface.getHeight());//获取一个最为适配的camera.size
        mConvertHelper.setOptionSize(optionSize);
        parameters.setPreviewSize(optionSize.getWidth(),optionSize.getHeight());//把camera.size赋值到parameters
        camera.setParameters(parameters);
        camera.setPreviewCallback(this);
        camera.startPreview();//开始预览
        camera.setDisplayOrientation(90);//将预览旋转90度

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (null != camera) {
            stop();
            Log.d("size","camera stop");
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        //todo:GC全部未处理数据
    }

    // 保存bitmap到本地
    private boolean saveBitmapTofile(Bitmap bmp, String filename) {
        if (bmp == null || filename == null)
            return false;
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream("/storage/emulated/0/Android/frames/" + filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bmp.compress(format, quality, stream);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        // 跳帧
        if (this.FRAME_COUNT%Settings.FRAME_STEP == 0){
            Bitmap bmpout= mConvertHelper.getBitmap(bytes);

            // debug 读取sd卡图片
            Bitmap testbitmap = null;
            try {
                int imageIndex = index%40;
                File file1 = new File("/storage/emulated/0/Android/video/"+String.valueOf(imageIndex)+".jpg");
                index ++;
                InputStream in = new FileInputStream(file1);
                testbitmap = BitmapFactory.decodeStream(in).copy(Bitmap.Config.ARGB_8888, true); // 不允许对drawable的文件修改，canvas会报错，要拷贝;
            }catch (Exception e){
            }

            if (bmpout!=null) {
                // 预处理  旋转缩放
                bmpout = SizeUtils.preprocessBitmap(bmpout, -90, 160, 160);
                if (!CameraApplication.getFwcCounter().mCounter.isReady){
                    WorkLine.addPrepareData(bmpout);
                }
                else {
                    long timeend = System.currentTimeMillis();
                    String ss = String.valueOf(timeend-CameraApplication.getFwcCounter().mCounter.timeStart);
                    Log.d(" ", "========================================== add frame TIME: " + ss + " ms===================");
                    WorkLine.addSource(bmpout);
                }
            }
        }
        this.FRAME_COUNT ++;
    }

    public void stop(){
        //主线程睡一帧时间，避免play线程未能终止就回收surfaceView
        playFlag=false;
        try {
            Thread.sleep(1000/ Settings.FrameRate);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class PlayThread extends Thread{
        @Override
        public void run() {
            while (playFlag) {
                long time1 = 0;
                long time2 = 0;
                    if (WorkLine.ready()) {
                        time2 = time1;
                        time1 = System.currentTimeMillis();
                        String s = String.valueOf(time1 - time2);
//                    Log.d("display","================================ display time "+s+" ms===========================================");
                        Bitmap temp = WorkLine.play();
//                    temp = SizeUtils.preprocessBitmap(temp, 0, 160, 160);
                        if (!temp.isRecycled()) {
                            Canvas canvas = bitmapSurface.getHolder().lockCanvas();
//                        IOUtils.TimeBlance(timerBefore);// 调整播放帧率
                            canvas.drawBitmap(temp, 0.0f, 0.0f, new Paint());

                            Message msg = new Message();
                            Bundle b = new Bundle();
                            b.putString("color", "mine");
                            msg.setData(b);
//                        PushUpActivity.getPushUpActivity().handler.sendMessage(msg);
//                        ((Activity)ctx).runOnUiThread(new Runnable(){
//                            @Override
//                            public void run(){
////                                Message msg = new Message();
////                                Bundle b = new Bundle();
////                                b.putString("color","mine");
////                                msg.setData(b);
////                                PushUpActivity.getPushUpActivity().handler.sendMessage(msg);
//
//                            }
//                        });
                        timerBefore=System.currentTimeMillis();
                        bitmapSurface.getHolder().unlockCanvasAndPost(canvas);
                        //发送方法通知ui线程更新toast
                        EventBus.getDefault().post(new PushUpToastEvent("display"));
                        BitmapGC(temp);
//                        try{
//                            Thread.sleep(50);
//                        }
//                        catch (Exception e){
//                        }
                        }
                    }
                }
                while (!this.isInterrupted()) {
                    this.interrupt();
            }
        }
    }
}
