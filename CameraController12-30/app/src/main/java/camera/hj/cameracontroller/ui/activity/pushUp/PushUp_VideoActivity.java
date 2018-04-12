package camera.hj.cameracontroller.ui.activity.pushUp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import camera.hj.cameracontroller.R;
import camera.hj.cameracontroller.ui.activity.BaseActivity;

/**
 * Created by NC040 on 2017/12/26.
 */

public class PushUp_VideoActivity extends BaseActivity {
    @BindView(R.id.videoView)
    VideoView VideoPlayView;

    @BindView(R.id.startButton)
    Button startButton;


    @BindView(R.id.videoLayout)
    RelativeLayout videoLayout;

    private MediaController mController;


    @Override
    public int getLayoutId() {
        return R.layout.pushup_video;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

    }

    @Override
    public void initToolBar() {

    }

    @Override
    public void loadData() {
        mController = new MediaController(this);
    }


    @OnClick({R.id.startButton, R.id.videoView,R.id.videoLayout})
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.startButton:
                Intent i1=new Intent(this,PushUpActivity.class);
                startActivity(i1);
                break;
            case R.id.videoLayout:
                //todo:播放视频逻辑
                VideoPlayView.setMediaController(mController);
//                VideoPlayView.setVideoURI(Uri.parse("android.resource://com.hj.cameracontroller/"+R.raw.pushup));
                VideoPlayView.setVideoURI(Uri.parse("android.resource://"+this.getPackageName()+"/"+R.raw.pushup));
                VideoPlayView.start();
                VideoPlayView.requestFocus();

//                File videoFile = new File("/sdcard/DCIM/Camera/test.mp4");
//                if (videoFile.exists()) {
//                    VideoPlayView.setVideoPath(videoFile.getAbsolutePath());
//                    VideoPlayView.setMediaController(mController);
//                    VideoPlayView.start();
//                    VideoPlayView.requestFocus();
//                }
                break;
            default:
                break;
        }
    }
}
