package camera.hj.cameracontroller.controller.event;

/**
 * Created by NC040 on 2017/12/27.
 */

public abstract class CameraControllerEvent implements IEvent {
    private String mMsg;
    public CameraControllerEvent(String msg) {
        mMsg = msg;
    }
    public String getMsg(){
        return mMsg;
    }
}
