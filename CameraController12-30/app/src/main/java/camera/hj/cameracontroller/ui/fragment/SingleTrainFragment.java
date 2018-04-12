package camera.hj.cameracontroller.ui.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.OnClick;
import camera.hj.cameracontroller.R;
import camera.hj.cameracontroller.ui.activity.pushUp.PushUpActivity;
import camera.hj.cameracontroller.ui.activity.pushUp.PushUp_VideoActivity;

/**
 * Created by NC040 on 2017/12/22.
 */

public class SingleTrainFragment extends BaseFragment {

    @BindView(R.id.train_type_1)
    ImageView train_type_1;

    @Override
    public int getLayoutId() {
        return R.layout.frag_single_train;
    }

    @Override
    public void initViews() {

    }

    @Override
    public void loadData() {

    }

    @OnClick({R.id.train_type_1})
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.train_type_1:
                Intent i1=new Intent(activityCtx,PushUp_VideoActivity.class);
                startActivity(i1);
                break;
            default:
                break;
        }
    }
}
