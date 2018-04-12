package camera.hj.cameracontroller.utils;

import android.graphics.Bitmap;

import java.util.Collection;;

/**
 * Created by T470S on 2017/12/12.
 */

public class GCUtils {
    public static void BitmapGC(Bitmap[] dataArray) {
        for(int i=0;i<dataArray.length;i++){
            if(!dataArray[i].isRecycled()){
                dataArray[i].recycle();
                dataArray[i]=null;
            }
        }
        dataArray=null;
    }
    public static void BitmapGC(Collection<Bitmap> dataCollection) {
        for(Bitmap b:dataCollection){
            if(!b.isRecycled()){
                b.recycle();
            }
            dataCollection.clear();
        }
    }
    public static void BitmapGC(Bitmap bitmap) {
       if(!bitmap.isRecycled()){
           bitmap.recycle();
           bitmap=null;
       }
    }
}
