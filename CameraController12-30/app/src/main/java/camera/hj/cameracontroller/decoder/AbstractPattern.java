package camera.hj.cameracontroller.decoder;

import android.graphics.Bitmap;
import android.net.IpPrefix;

/**
 * Created by NC040 on 2017/12/12.
 */

public abstract class AbstractPattern extends Thread implements IPattern {
    protected WorkLine mWorkLine;
    protected WorkingFlag flag;
    public AbstractPattern(WorkLine workLine,WorkingFlag flag) {
        this.mWorkLine=workLine;
        this.flag=flag;
    }
}
