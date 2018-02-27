package mobile.labs.acw;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;


/**
 * TODO
 */
public class Screen {

    private static Point getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static float getHeight(Activity pActivity) {
        return getScreenSize(pActivity).y;
    }

    public static float getWidth(Activity pActivity) {
        return getScreenSize(pActivity).x;
    }
}
