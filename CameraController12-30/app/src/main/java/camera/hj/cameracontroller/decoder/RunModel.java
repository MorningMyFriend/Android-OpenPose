/*
 * Copyright (c) 2016 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package camera.hj.cameracontroller.decoder;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import com.qualcomm.qti.snpe.FloatTensor;
import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

//public class RunModel extends AsyncTask<Bitmap, Void, String[]> {

public class RunModel{
    private String modelstyle="openpose_caffe_coco";//openpose_caffe_coco

    private static final String LOG_TAG = "RunModel";

    //public static final String OUTPUT_LAYER;// = "softmax:0";//"prob";
    public String OUTPUT_LAYER;// = "softmax:0";//"prob";

    private static final int FLOAT_SIZE = 4;

    public NeuralNetwork mNeuralNetwork = null;
    public static final String MODELS_ROOT_DIR = "models";
    private final NeuralNetwork.Runtime mTargetRuntime=NeuralNetwork.Runtime.GPU;

    private static Context context;

    private Bitmap mImage;
    public String timeRunModel;
    public String timeRunPose;

    private static final int input_witdh = 160;
    private static final int input_height = 160;

    // extract model and pictures from zip file, create folder on cellphone, copy model and pictures to cellphone
    private void handleModelExtraction(final String modelName, final int modelRawResId) {
        ZipInputStream zipInputStream = null;
        try {
            final File modelsRoot = getOrCreateExternalModelsRootDirectory(); //获取/创建 models目录：/storage/emulated/0/Android/data/com.example.wurui.cz_pose/files/models
            final File modelRoot = createModelDirectory(modelsRoot, modelName); //
            if (modelExists(modelRoot)) {
                return;
            }

            zipInputStream = new ZipInputStream(context.getResources().openRawResource(modelRawResId));
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                final File entry = new File(modelRoot, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    doCreateDirectory(entry);
                } else {
                    doCreateFile(entry, zipInputStream);
                }
                zipInputStream.closeEntry();
            }
            context.getContentResolver().notifyChange(
                    Uri.withAppendedPath(Model.MODELS_URI, modelName), null);
        } catch (IOException e) {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
            } catch (IOException ignored) {}
            context.getContentResolver().notifyChange(Model.MODELS_URI, null);
        }
    }

    private File createModelDirectory(File modelsRoot, String modelName) throws IOException {
        final File modelRoot = new File(modelsRoot, modelName);
        if (!modelRoot.isDirectory() && !modelRoot.mkdir()) {
            throw new IOException("Unable to create model root directory: " +
                    modelRoot.getAbsolutePath());
        }
        return modelRoot;
    }

    private File getOrCreateExternalModelsRootDirectory() throws IOException {
        final File modelsRoot = context.getExternalFilesDir(MODELS_ROOT_DIR);
        if (modelsRoot == null) {
            throw new IOException("Unable to access application external storage.");
        }

        if (!modelsRoot.isDirectory() && !modelsRoot.mkdir()) {
            throw new IOException("Unable to create model root directory: " +
                    modelsRoot.getAbsolutePath());
        }
        return modelsRoot;
    }

    private boolean modelExists(File modelRoot) {
        return modelRoot.listFiles().length > 0;
    }

    private void doCreateDirectory(File directory) throws IOException {
        if (!directory.mkdirs()) {
            throw new IOException("Can not create directory: " + directory.getAbsolutePath());
        }
    }

    // write to file, read from inputStream
    private void doCreateFile(File file, ZipInputStream inputStream) throws IOException {
        final FileOutputStream outputStream = new FileOutputStream(file);
        final byte[] chunk = new byte[1024];
        int read;
        while ((read = inputStream.read(chunk)) != -1) {
            outputStream.write(chunk, 0, read);
        }
        outputStream.close();
    }

//    private void ModelExtraction()
//    {
//        final String modelName = "alexnet";
//        handleModelExtraction(modelName, R.raw.alexnet160);
//    }

    //public RunModel(NeuralNetwork network) {
    //    mNeuralNetwork = network;
    public RunModel(Context ctx) {
        this.context=ctx;
        OUTPUT_LAYER="softmax:0";
        if (modelstyle=="inception_v3") {
            OUTPUT_LAYER = "softmax:0";
        }
        else if(modelstyle=="alexnet"){
            OUTPUT_LAYER="prob";
        }else if(modelstyle=="tf_mobilenet"){
            OUTPUT_LAYER="Openpose/concat_stage7:0";
        }
        else if(modelstyle=="sphereface"){
            OUTPUT_LAYER="fc5";
        }
        else if(modelstyle=="openpose_caffe_coco"){
            OUTPUT_LAYER="net_output";
        }
        else if(modelstyle=="openpose_caffe_mpi"){
            OUTPUT_LAYER="net_output";
        }
//        ModelExtraction();
//        File dlcmodelfile=new File("/storage/emulated/0/Android/data/com.example.wurui.cz_pose/files/models/alexnet/model.dlc");
//        File dlcmodelfile = new File("/storage/emulated/0/Android/model160.dlc");
//        try {
//            final SNPE.NeuralNetworkBuilder builder = new SNPE.NeuralNetworkBuilder((Application)context.getApplicationContext())
//                    .setDebugEnabled(false)
//                    .setRuntimeOrder(mTargetRuntime)  //mTargetRuntime
//                    .setModel(dlcmodelfile);
//            mNeuralNetwork = builder.build();
//        } catch (IllegalStateException | IOException e) {
//
//        }
    }

    protected Bitmap forward(Bitmap image) {
        mImage = resizeBitmap(image);

        String datalayername="Mul:0";
        if(modelstyle=="inception_v3"){
            datalayername="Mul:0";
        }
        else if(modelstyle=="alexnet"){
            datalayername="data";
        }
        else if(modelstyle=="tf_mobilenet"){
            datalayername="image:0";
        }
        else if(modelstyle=="sphereface"){
            datalayername="data";
        }
        else if(modelstyle=="openpose_caffe_coco"){
            datalayername="image";
        }
        else if(modelstyle=="openpose_caffe_mpi"){
            datalayername="image";
        }

        //data input
        final FloatTensor tensor = mNeuralNetwork.createFloatTensor(
                mNeuralNetwork.getInputTensorsShapes().get(datalayername));

        final int[] dimensions = tensor.getShape();

        final boolean isGrayScale = (dimensions[dimensions.length -1] == 1);
        if (!isGrayScale) {
            writeRgbBitmapAsFloat(mImage, tensor);
        } else {
            Log.d(LOG_TAG,"error while loading input data  :isGrayScale = True ! ");
        }

        final Map<String, FloatTensor> inputs = new HashMap<>();

        inputs.put(datalayername, tensor);

        //forward
        long startTime= System.currentTimeMillis(); //获取结束时间
        final Map<String, FloatTensor> outputs = mNeuralNetwork.execute(inputs);
        long endTime= System.currentTimeMillis(); //获取结束时间

        timeRunModel = String.valueOf(endTime-startTime)+" ms";

        Log.i("execute", " duration: "+(endTime-startTime)+" ms");


        for (Map.Entry<String, FloatTensor> output : outputs.entrySet()) {
            if (output.getKey().equals(OUTPUT_LAYER)) {
                FloatTensor concatelayer = output.getValue();
                float[] concatelist = new float[input_witdh*input_witdh*57/64];  // 368/8*368/8*57=120612   // 160/8*160/8*57=22800  //656*368*3--82*46*57=215004
                int length = concatelayer.read(concatelist,0,input_witdh*input_witdh*57/64,0);

                // wrz debug
                ArrayList<Object> keypoints = new ArrayList<Object>();
                Common common = new Common();
                long time1= System.currentTimeMillis();
                keypoints = common.estimate_pose(concatelist,input_witdh/8,input_witdh/8);
                long time2= System.currentTimeMillis();
                timeRunPose = String.valueOf(time2-time1)+" ms";

                //    // draw on bitmap
//                Bitmap tempBitmap = Bitmap.createBitmap( 368, 368, Bitmap.Config.ARGB_8888 );
                Canvas canvas = new Canvas(mImage);
                //Canvas canvas = new Canvas(bitmap);

                Paint paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);//不填充
                paint.setStrokeWidth(4);  //线的宽度
                //canvas.drawRect(10, 20, 100, 100, paint);

                for(int i=0; i<keypoints.size();i++){
                    HashMap pt_dict = (HashMap)keypoints.get(i);
                    Set<String> keys_set = pt_dict.keySet();
                    Object[] keys = keys_set.toArray();
                    for (int j=0; j<keys_set.size();j++){
                        String k = (String) keys[j];
                        try {
                            ArrayList<Object> pt = (ArrayList<Object>) pt_dict.get(k);
                            Object cx = (Object)pt.get(1);
                            Object cy = (Object)pt.get(2);
                            double x = (double) cx * input_witdh;
                            double y = (double) cy * input_height;
                            int px = (new Double(x)).intValue();
                            int py = (new Double(y)).intValue();
                            canvas.drawCircle(px, py,3, paint);
                        }
                        catch (Exception e){
                            continue; // NO.j keypoint is not detected
                        }

                    }
                }
                Log.i("draw keypoint", " duration: "+(endTime-startTime)+" ms");
                // wrz end
            }
        }
        return mImage;
    }

    protected int[] detect(Bitmap image) {
        mImage = resizeBitmap(image);

        int[] pointlist = new int[36];//[x1 y1 x2 y2 ... x18 y18]

        String datalayername="Mul:0";
        if(modelstyle=="inception_v3"){
            datalayername="Mul:0";
        }
        else if(modelstyle=="alexnet"){
            datalayername="data";
        }
        else if(modelstyle=="tf_mobilenet"){
            datalayername="image:0";
        }
        else if(modelstyle=="sphereface"){
            datalayername="data";
        }
        else if(modelstyle=="openpose_caffe_coco"){
            datalayername="image";
        }
        else if(modelstyle=="openpose_caffe_mpi"){
            datalayername="image";
        }

        //data input
        final FloatTensor tensor = mNeuralNetwork.createFloatTensor(
                mNeuralNetwork.getInputTensorsShapes().get(datalayername));

        final int[] dimensions = tensor.getShape();

        final boolean isGrayScale = (dimensions[dimensions.length -1] == 1);
        if (!isGrayScale) {
            writeRgbBitmapAsFloat(mImage, tensor);
        } else {
            Log.d(LOG_TAG,"error while loading input data  :isGrayScale = True ! ");
        }

        final Map<String, FloatTensor> inputs = new HashMap<>();

        inputs.put(datalayername, tensor);

        //forward
        long startTime= System.currentTimeMillis(); //获取结束时间
        final Map<String, FloatTensor> outputs = mNeuralNetwork.execute(inputs);
        long endTime= System.currentTimeMillis(); //获取结束时间

        timeRunModel = String.valueOf(endTime-startTime)+" ms";

        for (Map.Entry<String, FloatTensor> output : outputs.entrySet()) {
            if (output.getKey().equals(OUTPUT_LAYER)) {
                FloatTensor concatelayer = output.getValue();
                float[] concatelist = new float[input_witdh*input_witdh*57/64];  // 368/8*368/8*57=120612   // 160/8*160/8*57=22800  //656*368*3--82*46*57=215004
                int length = concatelayer.read(concatelist,0,input_witdh*input_witdh*57/64,0);

                // wrz debug
                ArrayList<Object> keypoints = new ArrayList<Object>();
                Common common = new Common();
                long time1= System.currentTimeMillis();
                keypoints = common.estimate_pose(concatelist,input_witdh/8,input_witdh/8);
                long time2= System.currentTimeMillis();
                timeRunPose = String.valueOf(time2-time1)+" ms";

////              // draw on bitmap
//              Bitmap tempBitmap = Bitmap.createBitmap( 368, 368, Bitmap.Config.ARGB_8888 );
//                Canvas canvas = new Canvas(image);
//
//                Paint paint = new Paint();
//                paint.setColor(Color.GREEN);
//                paint.setStyle(Paint.Style.STROKE);//不填充
//                paint.setStrokeWidth(4);  //线的宽度
                //canvas.drawRect(10, 20, 100, 100, paint);

                int maxIndex = 0;
                int temp = 0;
                for(int i=0; i<keypoints.size();i++) {
                    HashMap pt_dict = null;
                    try {
                        pt_dict = (HashMap) keypoints.get(i);
                    }
                    catch (Exception e){
                        break;
                    }
                    Set<String> keys_set = pt_dict.keySet();
                    Object[] keys = keys_set.toArray();
                    if (keys_set.size()>temp){
                        maxIndex = i;
                    }
                }

                HashMap pt_dict = null;
                try {
                    pt_dict = (HashMap)keypoints.get(maxIndex);
                }catch (Exception e){
                    return new int[36];
                }
                for(int i=0; i<18;i++) {
                    String k = String.valueOf(i);
                    ArrayList<Object> pt = (ArrayList<Object>) pt_dict.get(k);
                    if (pt == null){
                        pointlist[i * 2] = 0;
                        pointlist[i * 2 + 1] = 0;
                    }
                    else {
                        Object cx = (Object) pt.get(1);
                        Object cy = (Object) pt.get(2);
                        double x = (double) cx * input_witdh;
                        double y = (double) cy * input_height;
                        int px = (new Double(x)).intValue();
                        int py = (new Double(y)).intValue();
//                        canvas.drawCircle(px,py,3,paint);
                        pointlist[i * 2] = px;
                        pointlist[i * 2 + 1] = py;
                    }
                }

                Log.i("draw keypoint", " duration: "+(endTime-startTime)+" ms");
                // wrz end
            }
        }
        return pointlist; // int[36]
    }

    private Bitmap resizeBitmap(Bitmap bitmap){
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float scaleW = ((float)input_witdh) / w;
        float scaleH = ((float)input_height) / h;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleW,scaleH);

        Bitmap res = Bitmap.createBitmap(bitmap,0 ,0,w,h,matrix,true);
        return res;
    }

    private void writeRgbBitmapAsFloat(Bitmap image, FloatTensor tensor) {
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0,
            image.getWidth(), image.getHeight());

        float[][][] image_data = new float[input_witdh][input_height][3];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                final int rgb = pixels[y * image.getWidth() + x];
                float b,g,r;
                if(modelstyle=="openpose_caffe_coco"){
                    b = ((rgb)       & 0xFF)  *1 / (float)255 ;//- (float)1;
                    g = ((rgb >>  8) & 0xFF)  *1 / (float)255 ;//- (float)1;
                    r = ((rgb >> 16) & 0xFF)  *1 / (float)255 ;//- (float)1;

                    image_data[y][x][0] = r;
                    image_data[y][x][1] = g;
                    image_data[y][x][2] = b;
                }
                else{
                    b = (float)(((rgb)       & 0xFF) - 128);
                    g = (float)(((rgb >>  8) & 0xFF) - 128);
                    r = (float)(((rgb >> 16) & 0xFF) - 128);
                }
                float[] pixelFloats = {r, g, b};
                tensor.write(pixelFloats, 0, pixelFloats.length, y, x);
            }
        }
    }

    private Pair<Integer, Float>[] topK(int k, FloatTensor tensor) {
        final float[] array = new float[tensor.getSize()];
        tensor.read(array, 0, array.length);

        final boolean[] selected = new boolean[tensor.getSize()];
        final Pair<Integer, Float> topK[] = new Pair[k];
        int count = 0;
        while (count < k) {
            final int index = top(array, selected);
            selected[index] = true;
            topK[count] = new Pair<>(index, array[index]);
            count++;
        }
        return topK;
    }

    private int top(float[] array, boolean[] selected) {
        int index = 0;
        float max = -1.f;
        for (int i = 0; i < array.length; i++) {
            if (selected[i]) {
                continue;
            }
            if (array[i] > max) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }
}
