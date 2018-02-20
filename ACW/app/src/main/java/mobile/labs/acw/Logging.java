package mobile.labs.acw;
import android.util.Log;

public class Logging {

    public static void Exception(Exception e) {
        String callingMethod = getCallingMethod();
        Log.i("ACW_Application", "Error [" + callingMethod + "]: " + e.getMessage());
    }

    //Method that uses the stack trace to find the method that called the Exception logging
    private static String getCallingMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        //TODO: test that the 5th element is always the calling method
        return stackTraceElements[4].getMethodName();

    }
}
