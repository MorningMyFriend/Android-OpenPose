package camera.hj.cameracontroller.decoder;

import android.graphics.Bitmap;
import android.hardware.Camera;
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
import camera.hj.cameracontroller.controller.event.PushUpToastEvent;
import camera.hj.cameracontroller.utils.GCUtils;
import camera.hj.cameracontroller.utils.SizeUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by wurui on 17-12-26.
 */

public class actionPrepare extends AbstractPattern {
    public int[][] CocoPairs = {{2, 3}, {3, 4}, {5, 6}, {6, 7}};
    private int[] BodyPoints = {2 , 3, 4, 5, 6, 7};

    private long timeUse;

    public actionPrepare(WorkLine workLine,WorkingFlag flag) {
        super(workLine,flag);
    }

    @Override
    public int[] init(Bitmap resource, int[] eigen) {
        return new int[36];
    }

    @Override
    public int[] update(Bitmap resource) {
        return null;
    }

    @Override
    public void draw(Bitmap resource, int[] eigen) {
        Canvas canvas = new Canvas(resource);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        for(int i = 0;i<BodyPoints.length;i++) {
            int p1 = BodyPoints[i];
            if (eigen[2 * p1] != 0 && eigen[2 * p1 + 1] != 0) {
                canvas.drawCircle(eigen[2*p1], eigen[2*p1+1],3,paint);
            }
        }
    }

    @Override
    public void run() {
        RunModel runModel = CameraApplication.getRunModel();
        while (true){
            Bitmap temp = mWorkLine.getPrepareData();
            int[] result = runModel.detect(temp);
            boolean isAllPointsDetected = true;
            for(int i=0;i<this.BodyPoints.length;i++){
                int index = this.BodyPoints[i];
                if(result[index*2]==0 || result[index*2+1]==0){
                    isAllPointsDetected = false;
                }
            }
            if (isAllPointsDetected){
                // 所有点都检测到了
                CameraApplication.getFwcCounter().mCounter.initBodyPoints = result;
                draw(temp,result);
                CameraApplication.getFwcCounter().mCounter.update(result);
                // 判断双手距离是否达标
                if (CameraApplication.getFwcCounter().mCounter.isLegal){
                    CameraApplication.getFwcCounter().mCounter.isReady = true;
                    break;
                }
                else {
                    // 没准备好 则直接跳取当前帧
                    mWorkLine.clearPrepareData();
                }
                CameraApplication.getFwcCounter().mCounter.isReady = true;
                break;
            }
            else {
                // 没准备好  则直接跳取当前帧
                mWorkLine.clearPrepareData();
            }
        }
    }


}
