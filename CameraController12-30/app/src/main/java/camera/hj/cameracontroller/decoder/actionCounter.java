package camera.hj.cameracontroller.decoder;

import android.graphics.Bitmap;

/**
 * Created by wurui on 17-12-12.
 */

public class actionCounter {
    public actionCounter(){
        mCounter = new fwc_counter();
    }

    public fwc_counter mCounter;

    public void update(int[] keypoint){
        this.mCounter.update(keypoint);
    }

    public void draw(Bitmap bmp){
        mCounter.drawKeypoint(bmp);
    }
}

// counter class
//class counter{
//    protected int flag1;
//    protected int flag2;
//    protected int flag_from;
//    public int count;
//    protected int count2;
//    protected double score;
//    static final String TAG = "COUNTER";
//    public int[][] CocoPairs = {
//            {1, 2}, {1, 5}, {2, 3}, {3, 4}, {5, 6}, {6, 7}, {1, 8}, {1, 11},{1, 0}}; //{1, 0}, {0, 14}, {14, 16}, {0, 15}, {15, 17}, {2, 16}, {5, 17}
//
//
//    counter(){
//        this.flag1 = 0;
//        this.flag2 = 0;
//        this.flag_from = 0;
//        this.count = 0;
//        this.count2 = 0;
//        this.score = 10;  // 每个动作的初始分为10  每次检测角度超过阈值  则扣分
//    }
//
//    void reset_flag(){
//        this.flag1 = 0;
//        this.flag2 = 0;
//        this.flag_from = 0;
//    }
//
//    double[] get_point_by_id(double[] keypoints, int id){
//        double[] keypoint = new double[2];
//        try {
//            keypoint[0] = keypoints[2*id];
//            keypoint[1] = keypoints[2*id+1];
//        }
//        catch (Exception e){
//            return null;
//        }
//        return keypoint;
//    }
//
//
//
//    protected double get_angle(double[] p1,double[] p2,double[] p3){
//        // angle = arcos(p1-p2-p3)
//        double ang = -1;
//        double x2 = p2[0];
//        double y2 = p2[1];
//        double x3 = p3[0];
//        double y3 = p3[1];
//        double x1 = p1[0];
//        double y1 = p1[1];
//        double temp1 = (x3-x2)*(x1-x2)+(y3-y2)*(y1-y2);
//        double temp2 = Math.sqrt(((x3-x2)*(x3-x2)+(y3-y2)*(y3-y2))*((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2)));
//        ang = Math.acos(temp1/temp2);
//        ang = ang/ Math.PI*180;
//        return ang;
//    }
//
//    protected double get_vertical_angle(double[] p2,double[] p3){
//        double ang = -1;
//        double x2 = p2[0];
//        double y2 = p2[1];
//        double x3 = p3[0];
//        double y3 = p3[1];
//        double x1 = x2;
//        double y1 = 0;
//        double[] p1 = new double[2];
//        p1[0] = x1;
//        p1[1] = y1;
//        ang = get_angle(p1, p2, p3);
////        if (Math.abs(ang)>90){
////            ang = 180 - Math.abs(ang);
////        }
//        return ang;
//    }
//
//    protected double get_horizen_angle(double[] p2,double[] p3){
//        double ang = -1;
//        double x2 = p2[0];
//        double y2 = p2[1];
//        double x3 = p3[0];
//        double y3 = p3[1];
//        double x1 = 0;
//        double y1 = y2;
//        double[] p1 = new double[2];
//        p1[0] = x1;
//        p1[1] = y1;
//        ang = get_angle(p1, p2, p3);
////        if (Math.abs(ang)>90){
////            ang = 180 - Math.abs(ang);
////        }
//        return ang;
//    }
//
//    protected double get_distance(double[] p1,double[] p2){
//        double x2 = p2[0];
//        double y2 = p2[1];
//        double x1 = p1[0];
//        double y1 = p1[1];
//        return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
//    }
//}

//class fwc_counter extends counter{
//    private double thresh_h; // <手-肘-肩膀>角度大于这个阈值则计数
//    private double thresh_l; // <手-肘-肩膀>角度小于这个阈值则计数
//    private double thresh_score; // <手-肩膀-另一肩膀>角度超过这个值则扣分
//    public double angle; // 衡量计数的角度<手-肘-肩膀>
//    public double angle_score; // 衡量得分的角度<手-肩膀-另一肩膀>
//
//    // 跟踪算法 肘关节点纠正 3号 6号
//    public double angElbow1;
//    public double angElbow2;
//    public int[] oldElbow;
//    public double angThresh = 10; // 左右差异大于angThresh度 则矫正
//
//    fwc_counter(){
//        super();
//        this.thresh_h = 140;
//        this.thresh_l = 100;
//        this.thresh_score = 120;
//        this.angle = -1;
//        this.angle_score = -1;
//        angElbow1 = -1;
//        angElbow2 = -1;
//        oldElbow = new int[2];
//    }
//
//
//
//    int update(int[] keypoint){
//        // single img : 1person-1pt_dict , default count N0.0-person's action
//        // find Hip-Knee
//        double keypoints[] = new double[keypoint.length];
//        for(int i=0;i<keypoint.length;i++){
//            keypoints[i] = (double)keypoint[i];
//        }
//
//        double[] lshoudlder = new double[2];
//        double[] lelbow = new double[2];
//        double[] lwrist = new double[2];
//        double[] rshoudlder = new double[2];
//        double[] relbow = new double[2];
//        double[] rwrist = new double[2];
//        double langle = 0;
//        double rangle = 0;
//
//        try{
//            // 默认左右胳膊关键点都检测的到
//            int flagL = 0; int flagR = 0;
//            if(get_point_by_id(keypoints, 2)!=null && get_point_by_id(keypoints, 4)!=null && get_point_by_id(keypoints, 3)!=null){
//                lshoudlder = get_point_by_id(keypoints, 2);
//                lwrist = get_point_by_id(keypoints, 4);
//                lelbow = get_point_by_id(keypoints, 3);
//                langle = super.get_angle(lshoudlder, lelbow, lwrist);
//                flagL = 1;
//            }
//            if(get_point_by_id(keypoints, 5)!=null && get_point_by_id(keypoints, 7)!=null && get_point_by_id(keypoints, 6)!=null){
//                rshoudlder = get_point_by_id(keypoints, 5);
//                rwrist = get_point_by_id(keypoints, 7);
//                relbow = get_point_by_id(keypoints, 6);
//                rangle = super.get_angle(rshoudlder, relbow, rwrist);
//                flagR = 1;
//            }
//
//            // 矫正 elbow
//            this.angElbow1 = this.angElbow2;
//            this.angElbow2 = (langle + rangle)/(double)2;
//
//            if (this.angElbow1 <0 ){
//                this.angElbow1 = this.angElbow2;
//                this.angle = angElbow2;
//            }
//            else {
//                // 左右角度差异如果大于阈值就矫正
//                double angDel = Math.abs(langle-rangle);
//                if (angDel>this.angThresh){
//                    int indexElbow = 3;int indexShoulder = 2;
//                    int indexElbowReplace = 6;int indexShoulderReplace = 5;
//                    if ((double)(this.angElbow2-this.angElbow1)<0){
//                        // 正在下降 取小角度的点
//                        this.angle = Math.min(langle,rangle);
//                        if(langle> rangle){
//                            indexElbow = 6;
//                            indexShoulder = 5;
//                            indexElbowReplace = 3;
//                            indexShoulderReplace = 2;
//                        }
//                    }
//                    else if ((double)(this.angElbow2-this.angElbow1)>0){
//                        // 正在上升 取大角度
//                        this.angle = Math.max(langle, rangle);
//                        if(langle<rangle){
//                            indexElbow = 6;
//                            indexShoulder = 5;
//                            indexElbowReplace = 3;
//                            indexShoulderReplace = 2;
//                        }
//                    }
//                    // 储存被替换的点
//                    double[] ptReplace = get_point_by_id(keypoints, indexElbowReplace);
//                    this.oldElbow[0] = (int)ptReplace[0];
//                    this.oldElbow[1] = (int)ptReplace[1];
//                    // 矫正点坐标
////                    keypoint[2*indexElbowReplace] = keypoint[2*indexShoulderReplace] + keypoint[2*indexShoulder] - keypoint[2*indexElbow];
////                    keypoint[2*indexElbowReplace+1] = keypoint[2*indexShoulderReplace+1] - keypoint[2*indexShoulder+1] + keypoint[2*indexElbow+1];
//                }
//                else {
//                    this.angle = this.angElbow2;
//                }
//            }
//
//            if((flagL+flagR)<1) {
//                return 0;// keypoint not found
//            }
//            else {
//                // update count
//                if(this.angle>=0 && this.angle <= this.thresh_l){
//                    this.flag1 = 1;
//                    this.flag_from = 1;
//                }
//                if(this.angle>=this.thresh_h){
//                    this.flag2 = 1;
//                    this.flag_from = 2;
//                }
//                if(this.flag1 == 1 && this.flag2 == 1){
//                    this.count2++;
//                    int old_count = this.count;
//                    this.count = (int) Math.floor(this.count2 / 2.0);
//                    if(this.flag_from == 1){
//                        this.flag2 = 0;
//                    }
//                    if(this.flag_from == 2){
//                        this.flag1 = 0;
//                    }
//                    if (old_count < this.count){
//                        // reset the score for next action
//                        this.score = 10;
//                    }
//                }
//                return 1;
//            }
//        }
//        catch (Exception e){
//            return -1;
//        }
//    }
//
//    private void resizeBitmap(Bitmap bitmap, int dw, int dh){
//        int w = bitmap.getWidth();
//        int h = bitmap.getHeight();
//        float scaleW = ((float)dw) / w;
//        float scaleH = ((float)dh) / h;
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleW,scaleH);
//
//        bitmap = Bitmap.createBitmap(bitmap,0 ,0,w,h, matrix,true);
//        return ;
//    }
//
//    public void drawKeypoint(Bitmap bmp){
//        Canvas canvas = new Canvas(bmp);
//        Paint paint = new Paint();
//        paint.setColor(Color.GREEN);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(2);
//
////        for(int i = 0;i<9;i++){
//////            if(keypoint[2*i]!=0 && keypoint[2*i+1]!=0){
//////                canvas.drawCircle(keypoint[2*i],keypoint[2*i+1],3,paint);
//////            }
////            int p1 = CocoPairs[i][0];
////            int p2 = CocoPairs[i][1];
////            if(keypoint[2*p1]!=0 && keypoint[2*p1+1]!=0 && keypoint[2*p2]!=0 && keypoint[2*p2+1]!=0){
////                canvas.drawCircle(keypoint[2*p1],keypoint[2*p1+1],3,paint);
////                canvas.drawCircle(keypoint[2*p2],keypoint[2*p2+1],3,paint);
////                canvas.drawLine(keypoint[2*p1],keypoint[2*p1+1],keypoint[2*p2],keypoint[2*p2+1],paint);
////            }
////        }
////        resizeBitmap(bmp, 720, 720);
//
//        // new antialised Paint
//        String text = "angle="+ String.valueOf((int)this.angle)+" count="+ String.valueOf(this.count);
//        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paintText.setColor(Color.GREEN);
//        paintText.setStyle(Paint.Style.STROKE);
//        paintText.setStrokeWidth(2);
//        canvas.drawText(text, 10, 50, paintText);
//    }
//}

//class sd_counter extends counter{
//    private double thresh_h; // <臀-膝盖 与 垂直线 夹角>角度大于这个阈值则计数
//    private double thresh_l; // <臀-膝盖 与 垂直线 夹角>角度小于这个阈值则计数
//    private double thresh_score; // <脚-肩膀 与 垂直线 夹角>角度超过这个值则扣分
//    public double angle; // 衡量计数的角度<臀-膝盖 与 垂直线 夹角>
//    public double angle_score; // 衡量得分的角度<脚-肩膀 与 垂直线 夹角>
//
//    sd_counter(){
//        super();
//        this.thresh_h = 40;
//        this.thresh_l = 5;
//        this.thresh_score = 25;
//        this.angle = -1;
//        this.angle_score = -1;
//    }
//
//    int update(ArrayList<HashMap> keypoint){             // 检测  计数用的角度  更新 动作得分和动作计数
//        // single img : 1person-1pt_dict , default count N0.0-person's action
//        // find Hip-Knee
//        HashMap<String, Object> human = keypoint.get(0);
//        double[] Hip = new double[2];
//        double[] Knee = new double[2];
//        double angle = -1;
//
//        int if_scored = action_score(keypoint);
//        if(if_scored == 0){
//            Log.i(TAG, "Class: sd_counter--update(): miss keypoint while scoring ");
//        }
//
//        try{
//            // find Hip Knee
//            int flag = 0;
//            if(get_point_by_id(human, 11)!=null && get_point_by_id(human, 12)!=null){
//                Hip = get_point_by_id(human, 11);
//                Knee = get_point_by_id(human, 12);
//                angle = super.get_vertical_angle(Knee, Hip);
//                flag = 1;
//            }
//            else if(get_point_by_id(human, 8)!=null && get_point_by_id(human, 9)!=null){
//                Hip = get_point_by_id(human, 8);
//                Knee = get_point_by_id(human, 9);
//                angle = super.get_vertical_angle(Knee, Hip);
//                flag = 1;
//            }
//            this.angle =angle;
//
//            if(flag==0) {
//                return 0;// keypoint not found
//            }
//            else {
//                // update count
//                //flag_from = 0;
//                if(angle>=0 && angle <= this.thresh_l){
//                    this.flag1 = 1;
//                    flag_from = 1;
//                }
//                if(angle>=this.thresh_h){
//                    this.flag2 = 1;
//                    flag_from = 2;
//                }
//                if(this.flag1 == 1 && this.flag2 == 1){
//                    this.count2++;
//                    int old_count = this.count;
//                    this.count = (int)Math.floor(this.count2 / 2.0);
//                    if(flag_from == 1){
//                        this.flag2 = 0;
//                    }
//                    if(flag_from == 2){
//                        this.flag1 = 0;
//                    }
//                    if (old_count < this.count){
//                        // reset the score for next action
//                        this.score = 10;
//                    }
//                }
//                return 1;
//            }
//        }
//        catch (Exception e){
//            return -1;
//        }
//    }
//
//    int action_score(ArrayList<HashMap> keypoint){          // 动作打分
//        // single img : 1person-1pt_dict , default count N0.0-person's action
//        // find Hip-Knee
//        HashMap<String, Object> human = keypoint.get(0);
//        double[] Shoulder = new double[2];
//        double[] Ankle = new double[2];
//        double angle = -1;
//        try{
//            // find Hip Knee
//            int flag = 0;
//            if(get_point_by_id(human, 2)!=null && get_point_by_id(human, 10)!=null){
//                Shoulder = get_point_by_id(human, 2);
//                Ankle = get_point_by_id(human, 10);
//                angle = super.get_vertical_angle(Ankle, Shoulder);
//                flag = 1;
//            }
//            else if(get_point_by_id(human, 5)!=null && get_point_by_id(human, 13)!=null){
//                Shoulder = get_point_by_id(human, 5);
//                Ankle = get_point_by_id(human, 13);
//                angle = super.get_vertical_angle(Ankle, Shoulder);
//                flag = 1;
//            }
//            this.angle_score = angle;
//
//            if(flag==0) {
//                return 0;// keypoint not found
//            }
//
//            else {
//                // score the action
//                if(angle > this.thresh_score){
//                    this.score = this.score - 0.5;
//                }
//                return 1;
//            }
//        }
//        catch (Exception e){
//            return -1;
//        }
//    }
//}