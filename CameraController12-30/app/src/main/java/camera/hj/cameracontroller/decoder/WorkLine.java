package camera.hj.cameracontroller.decoder;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;

import camera.hj.cameracontroller.CameraApplication;
import camera.hj.cameracontroller.constant.Settings;

import camera.hj.cameracontroller.utils.GCUtils;

/**
 * Created by NC040 on 2017/12/12.
 */

public class WorkLine {
    ArrayBlockingQueue<Bitmap> ResDataQueue = new ArrayBlockingQueue<Bitmap>(5000);
    ArrayBlockingQueue<Bitmap> ProductDataQueue = new ArrayBlockingQueue<Bitmap>(5000);
    ArrayBlockingQueue<Bitmap> PrepareDataQueue = new ArrayBlockingQueue<Bitmap>(5000);

    ArrayList<IPattern> patterns=new ArrayList<>();

    private static WorkLine ourInstance = null;

    private WorkLine() {
        new BitmapGCThread().start();
    }

    public synchronized static WorkLine getInstance() {
        if (ourInstance == null) {
            ourInstance = new WorkLine();
        }
        return ourInstance;
    }

    public void clear(){
        GCUtils.BitmapGC(ResDataQueue);
        GCUtils.BitmapGC(ProductDataQueue);
    }

    public void addSource(Bitmap data){
        if(data!=null) {
            ResDataQueue.offer(data);
        }
    }

    public void addPrepareData(Bitmap data){
        if(data!=null) {
            PrepareDataQueue.offer(data);
        }
    }

    public Bitmap getPrepareData(){
        try {
            return PrepareDataQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearPrepareData(){
        int len = PrepareDataQueue.size();
        for (int i=0;i<len;i++){
            try {
                Bitmap temp = PrepareDataQueue.take();
            }
            catch (Exception e){
            }
        }
    }


    public void addProduct(Bitmap data){
        if(data!=null) {
            ProductDataQueue.offer(data);
        }
    }

    public void addProducts(Collection<Bitmap> bitmaps){
        Log.d("size","Workline Product Add "+bitmaps.size());
        for(Bitmap b:bitmaps){
            ProductDataQueue.offer(b);
        }
        bitmaps.clear();
    }

    public Bitmap getSource(){
        try {
            return ResDataQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap[] getSources(int size){
        return getAllBitmap(size,ResDataQueue);
    }

    private Bitmap[] getAllBitmap(int size, ArrayBlockingQueue<Bitmap> line) {
        ArrayList<Bitmap> mapList=new ArrayList<>();
        for(int i=0;i<size;i++){
            try {
                long t = System.currentTimeMillis();
                String ss = String.valueOf(t- CameraApplication.getFwcCounter().mCounter.timeStart);
                Log.d("take time", "========================================== kcf take" + i + " start TIME: " + ss + " ms;ResDataQueue num"+ResDataQueue.size()+"===================");
                mapList.add(line.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("Controller error:",e.toString());
            }
        }
        return (Bitmap[])mapList.toArray(new Bitmap[0]);
    }


    public boolean ready(){
        return ProductDataQueue.size()>0;
    }

    public Bitmap play(){
        try {
            if(ProductDataQueue.size()>0){
                    return ProductDataQueue.take();
            }
        } catch (InterruptedException e) {
                e.printStackTrace();
        }
      return null;
    }


    private class BitmapGCThread extends Thread {
        @Override
        public void run() {
            while(true){
                //50m内存最多保存80张bitmap，每2s检查一次，如果超过60张执行一次全局gc,避免内存泄漏
                try {
                    this.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("size","ResData.size="+ResDataQueue.size());
                Log.d("size","ProData.size="+ProductDataQueue.size());
                if(ResDataQueue.size()+ProductDataQueue.size()> Settings.BMP_SAVE_NUM){

                    Log.d("size","all gc start");
                    BitmapGC(getAllBitmap(ResDataQueue.size(),ResDataQueue));
                    BitmapGC(getAllBitmap(ProductDataQueue.size(),ProductDataQueue));
                    System.gc();
                }
            }
        }
    }

    private void BitmapGC(Bitmap[] dataArray) {
        for(int i=0;i<dataArray.length;i++){
            if(!dataArray[i].isRecycled()){
                dataArray[i].recycle();
                dataArray[i]=null;
            }
        }
        dataArray=null;
    }
}
