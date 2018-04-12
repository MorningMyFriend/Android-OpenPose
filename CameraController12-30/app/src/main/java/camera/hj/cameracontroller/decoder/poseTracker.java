package camera.hj.cameracontroller.decoder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

import javax.security.auth.PrivateCredentialPermission;

/**
 * Created by wurui on 17-12-11.
 */

public class poseTracker {
    public final static String TAG = "poseTracker";
    //    public jniKCF[] kcfs;// = new jniKCF[9];
    public ArrayList<jniKCF> kcfsArray;// = new ArrayList<jniKCF>();
    private int[] pointToDetect = {2, 3, 4, 5, 6, 7};
    private int[] pointToKCF = {2, 3, 4, 5, 6, 7};
    private int windowSize;

    public poseTracker(){}

    public void init(int windowsize){
        windowSize = windowsize;
        kcfsArray = new ArrayList<jniKCF>();
        for (int i=0;i<pointToDetect.length;i++){
            jniKCF temp = new jniKCF();
            kcfsArray.add(temp);
        }
    }

    public void reset(Bitmap bmp, int[] keypointInit){
        // 如果一开 关键点x=0 y=0 则不去跟踪这个点
        // pointToKCF = {1, 2, 3, 4, 5, 6, 7};
        for (int index=0;index<pointToKCF.length;index++){
            if (index==0){
                pointToKCF[index]=0;
            }
            else {
                pointToKCF[index]=index+1;
            }
        }

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int keypointResult[] = new int[36];
        if (keypointInit.length<36){
            Log.d(TAG," keypoint num < 36 error ");
        }
        else {
            int dx1 = windowSize/2;
            int dx2 = windowSize/2;
            int dy1 = windowSize/2;
            int dy2 = windowSize/2;

            // detect part of body
            for(int i=0;i<pointToDetect.length;i++){
                int pointIndex = pointToDetect[i];
                int cx = keypointInit[2*pointIndex];
                int cy = keypointInit[2*pointIndex+1];

                // 如果一开 关键点x=0 y=0 则不去跟踪这个点
                if (cx==0 && cy==0){
                    pointToKCF[i]=-1;
                    continue;
                }

                // 附加限制条件
                if (cx<=(windowSize/2+1) || cx>=(width-windowSize/2-1) || cy<=(windowSize/2+1) || cy>=(height-windowSize/2-1)){
                    pointToKCF[i]=-1;
                    continue;
                }

                // keypoint -> roi rect
//                dx1 = Math.min(dx1,cx);
//                dx2 = Math.min(dx2,(width - cx));
//                dy1 = Math.min(dy1,cy);
//                dy2 = Math.min(dy2,(height - cy));
                int[] roi = new int[4];
                roi[0] = cx-dx1;
                roi[1] = cy-dy1;
                roi[2] = dx1+dx2+1;
                roi[3] = dy1+dy2+1;
                if(roi[0]<0 || roi[1]<0 || roi[0]+roi[2]>160 || roi[1]+roi[3]>160){
                    pointToKCF[i]=-1;
                    continue;
                }
                // KCF
//                kcfs[i].init(bmp, roi);
                this.drawRect(bmp,roi);
                kcfsArray.get(i).init(bmp, roi);
            }
        }
        return;
    }

    public int[] update(Bitmap bmp){
        int[] keypointResult = new int[36];
        for(int i=0;i<pointToDetect.length;i++){
            // 如果关键点一开始pose没检测到，则不跟踪
            if (pointToKCF[i]<0){
                continue;
            }
            int pointIndex = pointToDetect[i];
            int[] roi = new int[4];
//            roi = kcfs[i].update(bmp);
            roi = kcfsArray.get(i).update(bmp);
            this.drawRect(bmp,roi);
            keypointResult[pointIndex*2] = (int)(roi[0]+roi[2]/2);
            keypointResult[pointIndex*2+1] = (int)(roi[1]+roi[3]/2);
        }
        return keypointResult;
    }

    // debug
    public void drawRect(Bitmap bmp,int[] roi){
//        Canvas canvas = new Canvas(bmp);
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(1);
//
//        canvas.drawRect(roi[0],roi[1],roi[0]+roi[2],roi[1]+roi[3], paint);
    }
}
