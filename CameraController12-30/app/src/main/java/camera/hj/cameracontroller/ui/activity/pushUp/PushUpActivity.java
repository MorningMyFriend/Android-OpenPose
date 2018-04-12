package camera.hj.cameracontroller.ui.activity.pushUp;


import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import camera.hj.cameracontroller.CameraApplication;
import camera.hj.cameracontroller.R;
import camera.hj.cameracontroller.constant.Settings;
import camera.hj.cameracontroller.controller.event.PushUpToastEvent;
import camera.hj.cameracontroller.dataSource.CameraManager;
import camera.hj.cameracontroller.decoder.WorkLine;
import camera.hj.cameracontroller.decoder.CountResult;
import camera.hj.cameracontroller.ui.activity.BaseActivity;

public class PushUpActivity extends BaseActivity {//implements CountResult.ResultListener{
    @BindView(R.id.cameraSurface)
    SurfaceView cameraSurface;

    @BindView(R.id.BitmapSurface)
    SurfaceView BitmapSurface;

    @BindView(R.id.square)
    ImageView square;

    @BindView(R.id.result_text)
    TextView result_text;

    @BindView(R.id.action_text)
    TextView action_text;

    @BindView(R.id.time_text)
    TextView time_text;


    private MediaPlayer[] mPlayer = new MediaPlayer[3];
    private boolean isInit = false;
    private int playID = 0;

    static {
        System.loadLibrary("jniKCF");
    }

    public static PushUpActivity pushUpActivity;
    public static PushUpActivity getPushUpActivity(){return pushUpActivity;}

    private CameraManager cameraManager;
    private static boolean CurrentStatus=false;
    public final static String CALCULATE_RESULT="CALCULATE_RESULT";

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

    }

    @Override
    public void initToolBar() {

    }

    @Override
    public void loadData() {
        initSettings();
        WorkLine workLine=WorkLine.getInstance();
        cameraManager=new CameraManager(this,cameraSurface,workLine);
        cameraManager.setBitmapSurface(BitmapSurface);
        Log.d("###count###","queue size is init");
    }

    private void initSettings() {
        DisplayMetrics metrics =new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        Settings.setVideoHeight(metrics.widthPixels);
        Settings.setVideoWidth(metrics.heightPixels);
    }

    @Override
    protected void onDestroy() {
        WorkLine.getInstance().clear();
        for (int i=0;i<mPlayer.length;i++){
            mPlayer[i].stop();
            mPlayer[i].release();
        }
        super.onDestroy();
    }

    // 显示时间
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            long t = System.currentTimeMillis();
            String min = String.valueOf((int) ((t - CameraApplication.getFwcCounter().mCounter.timeBegin)/60000));
            String sec = String.valueOf( ((t - CameraApplication.getFwcCounter().mCounter.timeBegin)/1000)%60 );
            time_text.setText("时间： "+min+"分"+sec+"秒");
        }
    };

    //获取到PlayThread的结果并展示
    public void onEventMainThread(PushUpToastEvent event) {
        String msg = event.getMsg();

        if (msg=="display"){

            // 显示个数统计
            CountResult c = CameraApplication.takeCountResultQue();
            String s = "个数:"+String.valueOf(c.countNum)+" 角度:"+String.valueOf((int)c.angle);
            String s2 = "动作错误：";

            // 显示时间
            // 实时显示 用时
            int t = (int)((System.currentTimeMillis() - CameraApplication.getFwcCounter().mCounter.timeBegin)/1000);
            String min = String.valueOf((int)(t/60));
            String sec = String.valueOf(t%60);
            time_text.setText("时间： "+min+"分"+sec+"秒");

            // 语音提示
            if (!isInit){
                // 语音提示准备开始
                isInit = true;
                playBell(2);
            }

            if (!c.isLegalWrist){
                s2 += "手撑太宽! ";
                playBell(0);
            }

            if (!c.isLegalElbow){
                s2 += " 肘部外翻! ";
                playBell(1);
            }

            action_text.setText(s2);
            result_text.setText(s);

            // 锻炼完毕
            if (c.countNum>5){
                Intent i = new Intent(this,PushUP_ResultActivity.class);
                startActivity(i);
            }
        }
        else {
        }
    }


    public void playBell(int index) {
        switch (index) {
            case 0:
                if (mPlayer[0] == null) {
                    mPlayer[0] = MediaPlayer.create(this, R.raw.alarmhand);
                }
                try {
                    if (mPlayer[0] != null) {
                        if (!mPlayer[0].isPlaying() && !mPlayer[1].isPlaying()) {
                            mPlayer[0].start();
                        }
                    }
                } catch (Exception e) {
                }
                this.playID = 0;
                break;

            case 1:
                if (mPlayer[1] == null) {
                    mPlayer[1] = MediaPlayer.create(this, R.raw.alarmbell);
                }
                try {
                    if (mPlayer[1] != null) {
                        if (!mPlayer[0].isPlaying() && !mPlayer[1].isPlaying()) {
                            mPlayer[1].start();
                        }
                    }
                } catch (Exception e) {
                }
                this.playID = 1;
                break;

            case 2:
                if (mPlayer[2] == null) {
                    mPlayer[2] = MediaPlayer.create(this, R.raw.completebell);
                }
                try {
                    if (mPlayer[2] != null) {
                        if (!mPlayer[2].isPlaying()) {
                            mPlayer[2].start();
                        }
                    }
                } catch (Exception e) {
                }
                this.playID = 2;
                break;
        }
    }

}
