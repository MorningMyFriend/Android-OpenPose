package camera.hj.cameracontroller.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;
import camera.hj.cameracontroller.R;

/**
 * Created by NC040 on 2017/12/21.
 */

public class RegisterActivity extends BaseActivity {
    @BindView(R.id.register_bt)
    Button register_bt;

    @Override
    public int getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {

    }

    @Override
    public void initToolBar() {

    }

    @Override
    public void loadData() {

    }

    @OnClick({R.id.register_bt})
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.register_bt:
                Intent i1=new Intent(this,MainPageActivity.class);
                startActivity(i1);
                break;
            default:
                break;
        }
    }
}
