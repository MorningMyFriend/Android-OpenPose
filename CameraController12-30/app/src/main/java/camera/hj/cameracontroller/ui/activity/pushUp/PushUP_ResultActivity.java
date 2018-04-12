package camera.hj.cameracontroller.ui.activity.pushUp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import camera.hj.cameracontroller.CameraApplication;
import camera.hj.cameracontroller.R;
import camera.hj.cameracontroller.ui.activity.BaseActivity;
import camera.hj.cameracontroller.utils.AppManager;

/**
 * Created by NC040 on 2017/12/26.
 */

public class PushUP_ResultActivity extends BasePushUP_Activity {

    @BindView(R.id.return_bt)
    Button return_bt;

    @BindView(R.id.result_num)
    TextView resultNum;

    @BindView(R.id.result_time)
    TextView resultTime;

    private MediaPlayer mPlayerCom = new MediaPlayer();

    @Override
    public int getLayoutId() {
        return R.layout.pushup_result;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayResult();
        playBellCom();
    }

    public void displayResult(){
        String num = String.valueOf(CameraApplication.getFwcCounter().mCounter.count);
        resultNum.setText("完成 "+num+"个");

        String min = String.valueOf((int) (CameraApplication.getFwcCounter().mCounter.timeUse/60000));
        String sec = String.valueOf( (CameraApplication.getFwcCounter().mCounter.timeUse/1000)%60 );
        resultTime.setText("用时 "+min+"分"+sec+"秒");
    }

    public void playBellCom(){
        if (mPlayerCom == null) {
            mPlayerCom = MediaPlayer.create(this, R.raw.completebell);
        }
        try {
            if (mPlayerCom != null) {
                if (!mPlayerCom.isPlaying()) {
                    mPlayerCom.start();
                }
            }
        } catch (Exception e) {
        }
    }


    @Override
    public void initToolBar() {

    }

    @Override
    public void loadData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
