package mobile.labs.acw.Utility;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;


/**
 * Static class that is used to grab dimensions of the screen
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
