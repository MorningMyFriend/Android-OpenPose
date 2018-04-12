package camera.hj.cameracontroller.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Stack;

import camera.hj.cameracontroller.CameraApplication;
import camera.hj.cameracontroller.ui.activity.pushUp.PushUpActivity;
/**
 * 统一管理activity的内存，支持在“一键退出”中清除堆栈中的activity
 *
 * 这里堆栈中的activity指的是：未执行finish的。
 *
 * 有无finish的区别：
 *
 * A -> B(finish) -> C:
 * 在C中“返回键”，会返回值A
 *
 * A -> B(未finish) -> C:
 * 在C中“返回键”，会返回值B
 *
 * 参考：http://blog.csdn.net/hust_twj/article/details/75000149
 * Created by C596 on 2016/12/5.
 */

public class AppManager {

    private static Stack<Activity> activityStack;
    private static AppManager INSTANCE = new AppManager();

    private AppManager() {
    }

    public static AppManager getAppManager() {
        return INSTANCE;
    }

    //添加Activity到堆栈
    public void addActivity(Activity activity) {
        Log.d("camera-activity",activity.getLocalClassName()+"added");
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    public void removeActivity(Activity activity) {
        Log.d("camera-activity",activity.getLocalClassName()+"finish");
        if (activity == null) {
            return;
        }
        if (activityStack.contains(activity)) {
            activityStack.remove(activity);
            activity.finish();
        }
    }

    public void finishAllActivity() {
        for (Activity one : activityStack) {
            one.finish();
        }
        activityStack.clear();
    }

    public void quitApp(Context context) {
        finishAllActivity();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void restartApp() {
        finishAllActivity();
        Context context = CameraApplication.getInstance();
        Intent intent = new Intent(context, PushUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void moveTaskToBack(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        activity.startActivity(intent);
    }
}
