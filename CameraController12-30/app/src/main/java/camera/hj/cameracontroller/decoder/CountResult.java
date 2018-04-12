package camera.hj.cameracontroller.decoder;

import camera.hj.cameracontroller.CameraApplication;

/**
 * Created by wurui on 17-12-18.
 */

public class CountResult {
    public int countNum;
    public double angle;
    public boolean isLegalWrist = true;
    public boolean isLegalElbow = true;
    public String timeUse;

    public CountResult(int num, double ang, boolean isLegalWrist, boolean isLegalElbow){
        this.angle = ang;
        this.countNum = num;
        this.isLegalElbow = isLegalElbow;
        this.isLegalWrist = isLegalWrist;

    }

    public void getTime(){
        this.timeUse = String.valueOf(CameraApplication.getFwcCounter().mCounter.timeUse);
    }
}
