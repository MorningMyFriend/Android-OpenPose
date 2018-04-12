package camera.hj.cameracontroller.decoder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by wurui on 17-11-8.
 */
class SortByScore implements Comparator {
    public int compare(Object o1, Object o2) {
        HashMap<String, Object> s1 = (HashMap<String, Object>) o1;
        HashMap<String, Object> s2 = (HashMap<String, Object>) o2;
        if (Double.parseDouble(s1.get("score").toString()) > Double.parseDouble(s2.get("score").toString()))
            return -1;
        return 1;
    }
}

public class Common {
    private double NMS_Threshold = 0.05;
    private  int InterMinAbove_Threshold = 9;
    private double Inter_Threashold = 0.05;
    private  int Min_Subset_Cnt = 3;
    private double Min_Subset_Score = 0.4;
    private  int Max_Human = 9;
    private int mWidth;
    private int mHeight;


    private int[][] CocoPairs = {
            {1, 2}, {1, 5}, {2, 3}, {3, 4}, {5, 6}, {6, 7}, {1, 8}, {8, 9}, {9, 10}, {1, 11},
            {11, 12}, {12, 13}, {1, 0}, {0, 14}, {14, 16}, {0, 15}, {15, 17}, {2, 16}, {5, 17}
    };
    private int[][] CocoPairsNetwork = {
            {12, 13}, {20,21}, {14,15}, {16,17}, {22,23}, {24,25}, {0,1}, {2,3}, {4,5},
            {6,7}, {8,9}, {10,11}, {28,29}, {30,31}, {34,35}, {32,33}, {36,37}, {18,19}, {26,27}
    };

    private static final String TAG = "Common";

    public double[][] non_max_suppression(double[][] plain,int windowsize, double _NMS_Threshold){// 滑窗 在5×5窗口中取出最大值
        Log.i(TAG, "begin: non_max_suppression");
        long startTime= System.currentTimeMillis(); //获取结束时间

        double[][] plain_cp = new double[plain.length][plain.length];
        // _NMS_Threshold=0.2;
        for(int i=0;i<plain.length;++i) {
            for(int j=0;j<plain[i].length;++j) {
                if(plain[i][j]<_NMS_Threshold) {
                    plain[i][j]=0;
                }
            }
        }
        for(int i=0;i<plain.length;++i) {
            for (int j = 0; j < plain[i].length; ++j) {
                // in window find max
                int halfwindowsize = windowsize / 2;
                double max = 0;
                for (int idx_i = i - halfwindowsize; idx_i < i + halfwindowsize +1; ++idx_i) {
                    for (int idx_j = j - halfwindowsize; idx_j < j + halfwindowsize +1; ++idx_j) {
                        if (idx_i >= 0 && idx_i < plain.length && idx_j >= 0 && idx_j < plain[i].length) {
                            double tempvalue = plain[idx_i][idx_j];
                            if (tempvalue > max) {
                                max = tempvalue;
                            }
                        }
                    }
                }
// wrz debug
                // max_filter
                plain_cp[i][j] = max;
            }
        }
        for(int i=0;i<plain.length;++i) {
            for (int j = 0; j < plain[i].length; ++j) {
                if(plain_cp[i][j]!=plain[i][j]){
                    plain_cp[i][j]=0;
                }
            }
        }
        // only keep the local maxinum in window
//                for(int idx_i=i-halfwindowsize;idx_i<i+halfwindowsize;++idx_i){
//                    for(int idx_j=j-halfwindowsize;idx_j<j+halfwindowsize;++idx_j){
//                        if(idx_i>=0&&idx_i<plain.length&&idx_j>=0&&idx_j<plain[i].length){
//                            double tempvalue=plain[idx_i][idx_j];
//
//                            if(tempvalue<max){
//                                plain[idx_i][idx_j]=0;
//                            }
//                        }
//                    }
//                }
        long endTime= System.currentTimeMillis(); //获取结束时间
        Log.i(TAG, "end: non_max_suppression. duration: "+(endTime-startTime)+" ms");

        return plain_cp;
    }

    public void get_score(int x1,int y1,int x2,int y2,double[][] pafMatX, double[][] pafMatY, double[] score, int[] count)
    {
        Log.i(TAG, "begin: get_score");

        int __num_inter=10;
        double __num_inter_f=__num_inter;

        int dx=x2-x1;
        int dy=y2-y1;

        double normVec= Math.sqrt(dx*dx+dy*dy);

        if(normVec<0.0001)
            return;

        double vx=(double)dx/normVec;
        double vy=(double)dy/normVec;

        double[] xs=new double[__num_inter];
        double[] ys=new double[__num_inter];
        for(int i=0;i<__num_inter;i++)
        {
            xs[i]=x1+(double)dx/__num_inter*i+0.5;
            ys[i]=y1+(double)dy/__num_inter*i+0.5;
        }

        double[] pafXs=new double[__num_inter];
        double[] pafYs=new double[__num_inter];
        for(int idx=0;idx<__num_inter;idx++)
        {
            int tempys=(int)ys[idx];//new BigDecimal(ys[idx]).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            int tempxs=(int)xs[idx];//new BigDecimal(xs[idx]).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            pafXs[idx]=pafMatX[tempys][tempxs];
            pafYs[idx]=pafMatY[tempys][tempxs];
        }

        double[] local_scores=new double[__num_inter];
        for(int i=0;i<__num_inter;i++)
        {
            local_scores[i]=pafXs[i] * vx + pafYs[i] * vy;
        }

        count[0]=0;
        score[0]=0;
        for(int i=0;i<__num_inter;i++)
        {
            //Inter_Threashold=0.3;//added on 2017 11 11
            if(local_scores[i] >Inter_Threashold)
            {
                count[0]+=1;
                score[0]+=local_scores[i];
            }
        }

        Log.i(TAG, "end: get_score");

    }

    public boolean check_is_in(String elem, ArrayList<String> used_idx1)
    {
        Log.i(TAG, "begin: check_is_in");
        for(int i=0;i<used_idx1.size();i++)
        {
            String itg=used_idx1.get(i);
            if(elem==itg)return true;
        }
        Log.i(TAG, "end: check_is_in");

        return false;
    }

    public ArrayList<HashMap<String, Object>> estimate_pose_pair(ArrayList<Object> coords, int partIdx1, int partIdx2, double[][] pafMatX, double[][] pafMatY){
        Log.i(TAG, "begin: estimate_pose_pair");
        long startTime= System.currentTimeMillis();   //获取开始时间

        ArrayList<HashMap<String, Object>> connection = new ArrayList<HashMap<String, Object>>();
        ArrayList<HashMap<String, Object>> connection_temp = new ArrayList<HashMap<String, Object>>();

        ArrayList<Object> coords_x=(ArrayList<Object>)coords.get(0);
        ArrayList<Object> coords_y=(ArrayList<Object>)coords.get(1);
        ArrayList<Object> part1_coords_x=(ArrayList<Object>)coords_x.get(partIdx1);
        ArrayList<Object> part1_coords_y=(ArrayList<Object>)coords_y.get(partIdx1);
        ArrayList<Object> part2_coords_x=(ArrayList<Object>)coords_x.get(partIdx2);
        ArrayList<Object> part2_coords_y=(ArrayList<Object>)coords_y.get(partIdx2);

        int cnt = 0;

        int part1length=part1_coords_x.size();
        for (int part1ind=0;part1ind<part1length;part1ind++)
        {
            Object x_p1=part1_coords_x.get(part1ind);
            Object y_p1=part1_coords_y.get(part1ind);
            int x1= Integer.parseInt(String.valueOf(x_p1));
            int y1= Integer.parseInt(String.valueOf(y_p1));
            //double x1=new Double(x_p1.toString());
            //double y1=new Double(y_p1.toString());

            int part2length=part2_coords_x.size();
            for (int part2ind=0;part2ind<part2length;part2ind++) {
                Object x_p2 = part2_coords_x.get(part2ind);
                Object y_p2 = part2_coords_y.get(part2ind);
                int x2 = Integer.parseInt(String.valueOf(x_p2));
                int y2 = Integer.parseInt(String.valueOf(y_p2));
//                double x2=new Double(x_p2.toString());
//                double y2=new Double(y_p2.toString());
                double[] score = new double[1];
                int[] count = new int[1];
                get_score(x1, y1, x2, y2, pafMatX, pafMatY, score, count);

                cnt += 1;
                if (partIdx1 == 2 && partIdx2 == 3 || partIdx1 == 3 && partIdx2 == 4 || partIdx1 == 5 && partIdx2 == 6 || partIdx1 == 6 && partIdx2 == 7)
                {
                    if(count[0]<InterMinAbove_Threshold/2 || score[0] <=0.0)
                        continue;
                }
                else if(count[0]<InterMinAbove_Threshold || score[0]<=0.0)
                    continue;

                HashMap<String, Object> connection_map=new HashMap<String,Object>();
                connection_map.put("score",score[0]);
                int[] c1={x1,y1};
                int[] c2={x2,y2};
                int[] idx={part1ind,part2ind};
                int[] partIdx={partIdx1,partIdx2};
                int[] uPartIdx={x1,y1,partIdx1,x2,y2,partIdx2};

                connection_map.put("c1",c1);
                connection_map.put("c2",c2);
                connection_map.put("idx",idx);
                connection_map.put("partIdx",partIdx);
                connection_map.put("uPartIdx",uPartIdx);

                connection_temp.add(connection_map);
            }
        }


        Collections.sort(connection_temp, new SortByScore());

        ArrayList<String> used_idx1=new ArrayList<String>();
        ArrayList<String> used_idx2=new ArrayList<String>();

        for(int ind_ct=0;ind_ct<connection_temp.size();ind_ct++)
        {
            HashMap<String,Object> OneConnection = (HashMap<String,Object>) connection_temp.get(ind_ct);
            int[] idx_oneconnection = (int[]) OneConnection.get("idx");

            String idx1= String.valueOf(idx_oneconnection[0]);
            String idx2= String.valueOf(idx_oneconnection[1]);

            if(check_is_in(idx1,used_idx1)||check_is_in(idx2,used_idx2))continue;
            connection.add(OneConnection);
            used_idx1.add(idx1);
            used_idx2.add(idx2);
        }

        long endTime= System.currentTimeMillis(); //获取结束时间
        Log.i(TAG, "end: estimate_pose_pair. duration:"+(endTime-startTime)+"ms");

        return connection;
    };


    // draw heatmap pafmat
    public void drawmap(float[] input){
        Log.i(TAG, "begin: drawmap");
        float[] heat = new float[19*mWidth*mHeight];
        float[] paf = new float[38*mWidth*mHeight];

        for(int i=0;i<mWidth*mHeight;i++){
            System.arraycopy(input,i*57,heat,i*19,19);
            System.arraycopy(input,i*57+19,paf,i*38,38);
        }

        double[][][] heatMat = new double[19][mWidth][mHeight];
        double[][][] pafMat = new double[38][mWidth][mHeight];

        for (int i=0;i<mWidth;i++){
            for (int j=0;j<mWidth;j++){
                for (int k=0;k<19;k++){
                    heatMat[k][i][j]=(double)heat[k+j*19+i*mHeight*19];
                    //sum += heatMat[k][i][j];
                }
                for (int k=0;k<38;k++){
                    pafMat[k][i][j]=(double)paf[k+j*38+i*mHeight*38];
                }
            }
        }
        Log.i(TAG, " find max min ");
    }


    //  main function //

    public ArrayList<Object> estimate_pose(float[] input, int width, int height){
        mHeight = height;
        mWidth = width;
        Log.i(TAG, "begin: estimate_pose");
        float[] heat = new float[19*width*height];
        float[] paf = new float[38*width*height];

        for(int i=0;i<width*height;i++){
            System.arraycopy(input,i*57,heat,i*19,19);
            System.arraycopy(input,i*57+19,paf,i*38,38);
        }

        double[][][] heatMat = new double[19][width][height];
        double[][][] pafMat = new double[38][width][height];

        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                for (int k=0;k<19;k++){
                    heatMat[k][i][j]=(double)heat[k+j*19+i*height*19];
                    //sum += heatMat[k][i][j];
                }
                for (int k=0;k<38;k++){
                    pafMat[k][i][j]=(double)paf[k+j*38+i*height*38];
                }
            }
        }

        // reliability issue
        ArrayList<Object> min19 = new ArrayList<Object>();
        for (int k=0;k<19;k++){
            double[] temp_19 = new double[width]; // min in maps 1-19
            for (int i=0;i<width;i++) {
                double[] temp = new double[height];
                for (int j=0;j<height;j++){
                    temp[j] = heatMat[k][i][j];
                }
                Arrays.sort(temp);
                temp_19[i] = temp[0];
            }
            Arrays.sort(temp_19);
            min19.add(temp_19[0]);
            for (int i=0;i<width;i++){
                for (int j=0;j<height;j++){
                    heatMat[k][i][j]=heatMat[k][i][j]-temp_19[0];
                }
            }
        }

        for (int k=0;k<19;k++){
            for (int i=0;i<width;i++){
                double[] temp = new double[width];
                System.arraycopy(heatMat[k][i], 0, temp, 0, height);
                Arrays.sort(temp);
                for (int j=0;j<height;j++){
                    heatMat[k][i][j]=heatMat[k][i][j]-temp[0];
                }
            }
        }

        double sum = 0;
        for (int i=0;i<width;i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 19; k++) {
                    sum += heatMat[k][i][j];
                }
            }
        }
        double average = sum / (double) (width*height*19);

        double _NMS_Threshold = Math.max(average*4.0,NMS_Threshold);
        _NMS_Threshold = Math.min(0.3, _NMS_Threshold);
        Log.i(TAG, " _NMS_Threshold ......");

        // debug
        Bitmap bitmap = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
        Bitmap[] maplist = new Bitmap[19];
        Canvas[] canvaslist = new Canvas[19];
        for(int n=0;n<19;n++)
        {
            maplist[n] = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
            canvaslist[n] = new Canvas(maplist[n]);
        }
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(1);  //线的宽度
        // end

        ArrayList<Object> coords = new ArrayList<Object>();
        ArrayList<ArrayList<Object> > coordsx = new ArrayList<ArrayList<Object>>();
        ArrayList<ArrayList<Object> > coordsy = new ArrayList<ArrayList<Object>>();
        for (int k=0;k<18;k++){//not 19 , idx 18 is background
            double[][] plain = new double[width][height];
            for (int i=0;i<width;i++){
                for (int j=0;j<height;j++){
                    plain[i][j]=heatMat[k][i][j];
                }
            }
            double[][] nms = new double[width][height];

            // debug
            double max = 0;
            int dx=0; int dy=0;
            for (int i=0;i<width;i++){
                for (int j=0;j<height;j++){
                    if(plain[i][j]>0.5) {
                        max = plain[i][j];
                        dx=i;dy=j;
//                        canvas.drawCircle(dy, dx,1, paint);
                        canvaslist[k].drawCircle(dy, dx, 1, paint);
                    }
                }
            }
            canvas.drawCircle(dx, dy,1, paint);

            // end

            //NMS
            nms = non_max_suppression(plain, 5, _NMS_Threshold);

            ArrayList<Object> coordsxtemp=new ArrayList<Object>();
            ArrayList<Object> coordsytemp=new ArrayList<Object>();
            for (int i=0;i<width;i++){
                for (int j=0;j<height;j++){
                    if(nms[i][j]>=_NMS_Threshold){
                        coordsxtemp.add(j); // coordsx  -- coordsy
                        coordsytemp.add(i);
                        canvas.drawCircle(j, i,1, paint);
                    }
                }
            }
            coordsx.add(coordsxtemp);
            coordsy.add(coordsytemp);
        }
        coords.add(coordsx);
        coords.add(coordsy);

        Log.i(TAG, "start estimate_pose1 : estimate pairs ......");

        ArrayList<HashMap<String, Object>> connection_all = new ArrayList<HashMap<String, Object>>();
        for (int i=0;i<19;i++){
            int idx1 = CocoPairs[i][0];
            int idx2 = CocoPairs[i][1];
            int paf_x_idx = CocoPairsNetwork[i][0];
            int paf_y_idx = CocoPairsNetwork[i][1];

            double[][] pafMat_paf_x_idx = new double[width][height];
            double[][] pafMat_paf_y_idx = new double[width][height];
            for(int x_idx=0;x_idx<width;x_idx++){
                for(int y_idx=0;y_idx<height;y_idx++){
                    pafMat_paf_x_idx[x_idx][y_idx] = pafMat[paf_x_idx][x_idx][y_idx];
                    pafMat_paf_y_idx[x_idx][y_idx] = pafMat[paf_y_idx][x_idx][y_idx];
                }
            }

            ArrayList<HashMap<String, Object>> connection = new ArrayList<HashMap<String, Object>>();

            //estimate_pose_pair
            connection = estimate_pose_pair(coords, idx1, idx2, pafMat_paf_x_idx, pafMat_paf_y_idx); // get score

            for(int n = 0;n < connection.size(); n ++){
                connection_all.add(connection.get(n));
                // ArrayList<HashMap> connection_all
            }
        }

        Log.i(TAG, "start estimate_pose2 : connection_all ......");

        HashMap<String, ArrayList<HashMap<String, Object>>> connection_by_human = new HashMap<String, ArrayList<HashMap<String, Object>>>();

        for(int idx=0;idx<connection_all.size();idx++){
            String keyname = "human_";
            keyname = keyname + Integer.toString(idx);
            ArrayList<HashMap<String, Object>> connection_all_human = new ArrayList<HashMap<String, Object>>();
            connection_all_human.add(connection_all.get(idx));
            connection_by_human.put(keyname,connection_all_human);
        }

        ///////// connection_by_human
        //  HashMap<String, Hashmap> connection_by_human:
        //  { "human_1": ArrayList:[{Hashmap1: {'score':...  'c1': int[x y] 'c2':... 'idx':... 'partIdx':... 'uPartIdx': int[x,y,idx]} {Hashmap2: {'score':...  'c1': int[x y] 'c2':... 'idx':... 'partIdx':... 'uPartIdx': int[x,y,idx]}..]
        //    "human_2": ArrayList:[{Hashmap: {'score':...  'c1': ... 'c2':... 'idx':... 'partIdx':... 'uPartIdx': x-y-idx}...]}
        //  }
        ///////// connection_by_human

        Set<String> keys_set = connection_by_human.keySet();
        Object[] keys = keys_set.toArray();

        HashMap<String, ArrayList<String>> no_merge_cache = new HashMap<String, ArrayList<String>>();
        for(int i=0;i<keys_set.size();i++){
            String k1 = (String) keys[i];
            no_merge_cache.put(k1,null);
        }

        // no_merge_cache: HashMap<String, ArrayList<Object>>
        // == ["hunman_1":{"human_2","human_7"..}
        //     "hunman_2":{"human_4","human_12"..}    ... ]
        while (Boolean.TRUE){
            Boolean is_merged = Boolean.FALSE;
            try{
                Log.i(TAG, " connection_by_human.size() = " + String.valueOf(connection_by_human.size()));
                keys_set = connection_by_human.keySet();
                keys = keys_set.toArray();
                for (int i = 0;i<keys_set.size();i++){
                    String k1 = (String) keys[i];
                    for (int j = 0;j<keys_set.size();j++){
                        String k2 = (String) keys[j];
                        if (k1==k2){
                            continue;
                        }

                        ArrayList<String> no_merge_cache_k1 = (ArrayList<String>) no_merge_cache.get(k1);
                        if(no_merge_cache_k1 == null){
                            no_merge_cache_k1 = new ArrayList<String>();
                        }
                        if(no_merge_cache_k1!=null){
                            int size=no_merge_cache_k1.size();
                            // ArrayList<Object> --->  String[] k1_array
                            //String[] k1_array = (String[])no_merge_cache_k1.toArray(new String[size]);
                            String[] k1_array = new String[size];
                            for(int t=0;t<size;t++){
                                k1_array[t] = no_merge_cache_k1.get(t).toString();
                            }

                            if(Arrays.asList(k1_array).contains(k2)){
                                continue;
                            }
                        }

                        // for c1, c2 in itertools.product(connection_by_human[k1], connection_by_human[k2]):
                        // HashMap<String, ArrayList<HashMap<String, Object>>> connection_by_human
                        ArrayList<HashMap<String, Object>> connection_by_human_k1 = (ArrayList<HashMap<String, Object>>)connection_by_human.get(k1);
                        ArrayList<HashMap<String, Object>> connection_by_human_k2 = (ArrayList<HashMap<String, Object>>)connection_by_human.get(k2);

                        if(connection_by_human_k1==null){
                            connection_by_human_k1 = new ArrayList<HashMap<String, Object>>();
                        }

                        if(connection_by_human_k2==null){
                            connection_by_human_k2 = new ArrayList<HashMap<String, Object>>();
                        }

                        if(connection_by_human_k1!=null && connection_by_human_k2!=null){
                            Boolean c1_c2_merge = Boolean.FALSE;
                            // force person has 19 points

                            for (int s=0;s<connection_by_human_k1.size();s++){
                                if(c1_c2_merge== Boolean.TRUE){
                                    break;
                                }
                                HashMap<String,Object> c1 = (HashMap<String,Object>) connection_by_human_k1.get(s);
                                for (int t=0;t<connection_by_human_k2.size();t++){
                                    HashMap<String,Object> c2 = (HashMap<String,Object>) connection_by_human_k2.get(t);

                                    int[] uPartIdx1 = (int[])c1.get("uPartIdx");
                                    int[] uPartIdx2 = (int[])c2.get("uPartIdx");
                                    // "uPartIdx1": (int[x1 y1 id1 x2 y2 id2])

                                    int[] uPartIdx1_1 = new int[3];
                                    int[] uPartIdx1_2 = new int[3];
                                    int[] uPartIdx2_1 = new int[3];
                                    int[] uPartIdx2_2 = new int[3];
                                    System.arraycopy(uPartIdx1,0,uPartIdx1_1,0,3);
                                    System.arraycopy(uPartIdx1,3,uPartIdx1_2,0,3);
                                    System.arraycopy(uPartIdx2,0,uPartIdx2_1,0,3);
                                    System.arraycopy(uPartIdx2,3,uPartIdx2_2,0,3);

                                    ArrayList<int[]> uPartIdx1_list = new ArrayList<int []>();
                                    ArrayList<int[]> uPartIdx2_list = new ArrayList<int []>();
                                    uPartIdx1_list.add(uPartIdx1_1);uPartIdx1_list.add(uPartIdx1_2);
                                    uPartIdx2_list.add(uPartIdx2_1);uPartIdx2_list.add(uPartIdx2_2);

                                    // if len(set(c1['uPartIdx']) & set(c2['uPartIdx'])) > 0:
                                    int common_point_count = 0;
                                    for(int m=0;m<2;m++){
                                        int[] p1 = (int[]) uPartIdx1_list.get(m);
                                        for(int n=0;n<2;n++){
                                            int[] p2 = (int[]) uPartIdx2_list.get(n);
                                            int flag = 0;
                                            for(int k=0;k<3;k++){
                                                if(p1[k] == p2[k]){
                                                    flag ++;
                                                }
                                            }
                                            if (flag==3){
                                                common_point_count++;
                                            }
                                        }
                                    }
                                    if(common_point_count>0){  // if len(set(c1['uPartIdx']) & set(c2['uPartIdx'])) > 0:
                                        is_merged = Boolean.TRUE;
                                        for (int l=0;l<connection_by_human_k2.size();l++){
                                            HashMap<String,Object> c2_temp = (HashMap<String,Object>) connection_by_human_k2.get(l);
                                            connection_by_human_k1.add(c2_temp);
                                        }
                                        connection_by_human.put(k1,connection_by_human_k1);
                                        connection_by_human.remove(k2.toString());
                                        c1_c2_merge = Boolean.TRUE;
                                        break;
                                    }
                                }
                            }

                            if (is_merged){
                                if(no_merge_cache.get(k1)!=null){
                                    no_merge_cache.remove(k1);
                                }
                                break;
                            }
                            else {
                                no_merge_cache_k1.add(k2.toString());
                                no_merge_cache.put(k1,no_merge_cache_k1);
                            }
                        }
                    }
                }
                if (! is_merged) {
                    break;
                }
            }
            catch (IndexOutOfBoundsException e){
                Log.i(TAG, "while :: "+connection_by_human.size());
            }

        }

        Log.i(TAG, "start estimate_pose3 : connection_by_human ......");

        ArrayList<String> key_to_remove = new ArrayList<>();
        Set<String> keys_set_c = connection_by_human.keySet();
        Object[] keys_c = keys_set_c.toArray();
        for(int i=0;i<keys_set_c.size();i++){
            ArrayList<HashMap<String, Object>> v =(ArrayList<HashMap<String, Object>>) connection_by_human.get(keys_c[i]);
            // reject by subset count
            if(v.size()<Min_Subset_Cnt){
                key_to_remove.add(keys_c[i].toString());
                continue;
            }
            // reject by subset max score
            double score_sum = 0;
            for(int j=0;j<v.size();j++){
                HashMap<String,Object> ii = (HashMap<String,Object>)v.get(j);
                double score = Double.parseDouble(ii.get("score").toString());
                score_sum += score;
            }
            if(score_sum<Min_Subset_Score){
                key_to_remove.add(keys_c[i].toString());
                continue;
            }
        }
        for(int i=0;i<key_to_remove.size();i++){
            connection_by_human.remove(key_to_remove.get(i));
        }

        Log.i(TAG, "start estimate_pose4....");

        //return [connections_to_human(conn, heatMat) for conn in connection_by_human.values()]
        ArrayList<Object> human = new ArrayList<Object>();

        Set<String> keys_set_4 = connection_by_human.keySet();
        Object[] keys_4 = keys_set_4.toArray();
        for(int i=0;i<keys_set_4.size();i++){
            ArrayList<HashMap<String, Object>> conn =(ArrayList<HashMap<String, Object>>) connection_by_human.get(keys_4[i]);
            // conn contains 19 dict
            HashMap<String,Object> point_dict = new HashMap<String,Object>();
            for (int j=0;j<conn.size();j++){
                HashMap<String,Object> con = (HashMap<String,Object>)conn.get(j);

                ArrayList<Object> point1 = new ArrayList<Object>();
                ArrayList<Object> point2 = new ArrayList<Object>();

                int[] ptid = (int[]) con.get("partIdx");
                int[] c1 = (int[]) con.get("c1");
                int[] c2 = (int[]) con.get("c2");
                double x_w;
                double y_h;

                x_w = (double)c1[0] / (double)width;
                y_h = (double)c1[1] / (double)height;
                point1.add(String.valueOf(ptid[0]));
                point1.add(x_w);
                point1.add(y_h);
                point1.add(heatMat[(int)ptid[0]][(int)c1[1]][(int)c1[0]]);

                x_w = (double)c2[0] / (double)width;
                y_h = (double)c2[1] / (double)height;
                point2.add(String.valueOf(ptid[1]));
                point2.add(x_w);
                point2.add(y_h);
                point2.add(heatMat[(int)ptid[1]][(int)c2[1]][(int)c2[0]]);

                point_dict.put(String.valueOf(ptid[0]),point1);
                point_dict.put(String.valueOf(ptid[1]),point2);
            }
            // sort
            human.add(point_dict);
        }
        return human;
    }
}