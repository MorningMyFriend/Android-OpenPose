package camera.hj.cameracontroller.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import butterknife.ButterKnife;
import camera.hj.cameracontroller.controller.event.IEvent;
import camera.hj.cameracontroller.utils.AppManager;
import de.greenrobot.event.EventBus;

/**
 * Created by NC040 on 2017/11/19.
 */

public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Camera Activity","######### onCreate:" + this.getClass().getName());
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置布局内容
        setContentView(getLayoutId());
        //初始化黄油刀控件绑定框架
        ButterKnife.bind(this);
        //注册eventBus
        EventBus.getDefault().register(this);
        //初始化控件
        initViews(savedInstanceState);
        //初始化ToolBar
        initToolBar();
        loadData();
        AppManager.getAppManager().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        AppManager.getAppManager().removeActivity(this);
    }

    public abstract int getLayoutId();

    public abstract void initViews(Bundle savedInstanceState);

    public abstract void initToolBar();

    public abstract void loadData();

    public void onEvent(IEvent event) {
        //处理不在ui上的事件时复写此方法
    }
}
