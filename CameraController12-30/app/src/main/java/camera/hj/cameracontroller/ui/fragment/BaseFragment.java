package camera.hj.cameracontroller.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import camera.hj.cameracontroller.CameraApplication;

/**
 * Created by NC040 on 2017/12/22.
 */

public abstract class BaseFragment extends Fragment {

    private View rootView;
    private Unbinder unbinder;
    protected CameraApplication app;
    protected Activity activityCtx;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 设置布局
        rootView = inflater.inflate(getLayoutId(), container, false);
        // 绑定fragment
        unbinder = ButterKnife.bind(this, rootView);
        app = (CameraApplication) getActivity().getApplication();
        activityCtx = this.getActivity();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    abstract public int getLayoutId();

    abstract public void initViews();

    abstract public void loadData();
}

