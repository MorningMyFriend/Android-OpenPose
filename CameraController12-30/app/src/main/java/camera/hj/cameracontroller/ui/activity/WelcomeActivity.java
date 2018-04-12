package camera.hj.cameracontroller.ui.activity;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import camera.hj.cameracontroller.CameraApplication;
import camera.hj.cameracontroller.R;
import camera.hj.cameracontroller.decoder.Model;
import camera.hj.cameracontroller.ui.activity.pushUp.PushUpActivity;

/**
 * Created by NC040 on 2017/12/21.
 */

public class WelcomeActivity extends BaseActivity {
    private WelcomeHandler mHandler;
    @Override
    public int getLayoutId() {
        return R.layout.activity_welcome;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

    }

    @Override
    public void initToolBar() {

    }

    @Override
    public void loadData() {
        mHandler=new WelcomeHandler();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // snpe model
    public NeuralNetwork neuralNetwork = null;
    public static final String MODELS_ROOT_DIR = "models";
    private final NeuralNetwork.Runtime mTargetRuntime=NeuralNetwork.Runtime.GPU;

    @Override
    protected void onResume() {
        super.onResume();
        final long start= System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // snpe model
                ModelExtraction();
                File dlcmodelfile = new File("/storage/emulated/0/Android/data/camera.hj.cameracontroller/files/models/alexnet/model.dlc");
                try {
                    final SNPE.NeuralNetworkBuilder builder = new SNPE.NeuralNetworkBuilder((Application)getApplication().getApplicationContext())
                            .setDebugEnabled(false)
                            .setRuntimeOrder(mTargetRuntime)  //mTargetRuntime
                            .setModel(dlcmodelfile);
                    neuralNetwork =  builder.build();
                    CameraApplication.setRunModel(neuralNetwork);

                } catch (IllegalStateException | IOException e) {
                }

                Message startNext = new Message();
                startNext.what = 1;
                mHandler.sendMessage(startNext);
            }
        }).start();

    }

    private class WelcomeHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    Intent i=new Intent(WelcomeActivity.this,RegisterActivity.class);
                    startActivity(i);
            }
        }
    }

    // snpe file
    private void ModelExtraction()
    {
        Intent intent = new Intent(getApplicationContext(), PushUpActivity.class);

        intent.setAction("extract");
        intent.putExtra("model_name", "alexnet");
        intent.putExtra("model_raw_res", R.raw.alexnet160);
        getApplication().startService(intent);

        final int modelRawResId = intent.getIntExtra("model_raw_res", 0);
        final String modelName = intent.getStringExtra("model_name");
        handleModelExtraction(modelName, modelRawResId);
    }
    // extract model and pictures from zip file, create folder on cellphone, copy model and pictures to cellphone
    private void handleModelExtraction(final String modelName, final int modelRawResId) {
        ZipInputStream zipInputStream = null;
        try {
            final File modelsRoot = getOrCreateExternalModelsRootDirectory(); //获取/创建 models目录：/storage/emulated/0/Android/data/com.example.wurui.cz_pose/files/models
            final File modelRoot = createModelDirectory(modelsRoot, modelName); //
            if (modelExists(modelRoot)) {
                return;
            }

            zipInputStream = new ZipInputStream(getResources().openRawResource(modelRawResId));
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
            getContentResolver().notifyChange(
                    Uri.withAppendedPath(Model.MODELS_URI, modelName), null);
        } catch (IOException e) {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
            } catch (IOException ignored) {}
            getContentResolver().notifyChange(Model.MODELS_URI, null);
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
        final File modelsRoot = getExternalFilesDir(MODELS_ROOT_DIR);
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
}
