package camera.hj.cameracontroller.decoder;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by NC040 on 2017/12/12.
 */

public interface IPattern {
    public int[] init(Bitmap resource,int[] eigen);
    public int[] update(Bitmap resource);
    public void draw(Bitmap resource,int[] eigen);
}
