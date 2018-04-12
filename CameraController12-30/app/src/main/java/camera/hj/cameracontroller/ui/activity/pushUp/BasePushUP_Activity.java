package camera.hj.cameracontroller.ui.activity.pushUp;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import camera.hj.cameracontroller.ui.activity.BaseActivity;

/**
 * Created by NC040 on 2017/12/27.
 */

public abstract class BasePushUP_Activity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }
}
