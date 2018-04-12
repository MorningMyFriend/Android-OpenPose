package camera.hj.cameracontroller.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by NC040 on 2017/11/27.
 */

public class SizeUtils {

    private static final int MINIMUM_PREVIEW_SIZE = 320;

    public static Bitmap preprocessBitmap(Bitmap bmp, int angle, int dw, int dh){
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        // cut
        int scaled_size =  Math.min(w, h);
        bmp = Bitmap.createScaledBitmap(bmp, scaled_size,scaled_size,true);

        // resize and rotate
        float scaleW = ((float)dw) / scaled_size;
        float scaleH = ((float)dh) / scaled_size;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleW,scaleH);
        matrix.postRotate(angle);
//        Bitmap res = Bitmap.createBitmap(bmp,0 ,0,scaled_size, scaled_size,matrix,true);
        bmp = Bitmap.createBitmap(bmp,0, 0, scaled_size, scaled_size, matrix, true);
        return bmp;
    }


    public static Size chooseOptimalSize(final Size[] choices, final int width, final int height) {
        final int minSize = Math.max(Math.min(width, height), MINIMUM_PREVIEW_SIZE);
        final Size desiredSize = new Size(width, height);

        // Collect the supported resolutions that are at least as big as the preview Surface
        boolean exactSizeFound = false;
        final List<Size> bigEnough = new ArrayList<Size>();
        final List<Size> tooSmall = new ArrayList<Size>();
        for (final Size option : choices) {
            if (option.equals(desiredSize)) {
                // Set the size but don't return yet so that remaining sizes will still be logged.
                exactSizeFound = true;
            }

            if (option.getHeight() >= minSize && option.getWidth() >= minSize) {
                bigEnough.add(option);
            } else {
                tooSmall.add(option);
            }
        }

        Log.i("CameraContoller","Desired size: " + desiredSize + ", min size: " + minSize + "x" + minSize);
        Log.i("CameraContoller","Valid preview sizes: [" + TextUtils.join(", ", bigEnough) + "]");
        Log.i("CameraContoller","Rejected preview sizes: [" + TextUtils.join(", ", tooSmall) + "]");

        if (exactSizeFound) {
            Log.i("CameraContoller","Exact size match found.");
            return desiredSize;
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            final Size chosenSize = Collections.min(bigEnough, new CompareSizesByArea());
            Log.i("CameraContoller","Chosen size: " + chosenSize.getWidth() + "x" + chosenSize.getHeight());
            return chosenSize;
        } else {
            Log.e("CameraContoller","Couldn't find any suitable preview size");
            return choices[0];
        }
    }


    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(final Size lhs, final Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }


    public static int getYUVByteSize(final int width, final int height) {
        // The luminance plane requires 1 byte per pixel.
        final int ySize = width * height;

        // The UV plane works on 2x2 blocks, so dimensions with odd size must be rounded up.
        // Each 2x2 block takes 2 bytes to encode, one each for U and V.
        final int uvSize = ((width + 1) / 2) * ((height + 1) / 2) * 2;

        return ySize + uvSize;
    }

    public void saveBitmap(Bitmap bm, String picName) {
        File f = new File("/storage/emulated/0/Android/frames", picName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
