package camera.hj.cameracontroller.controller.event;

import camera.hj.cameracontroller.CameraApplication;

/**
 * Created by NC040 on 2017/12/27.
 */

public class PushUpToastEvent extends CameraControllerEvent {
    public PushUpToastEvent(String msg) {
        super(msg);
    }
}
