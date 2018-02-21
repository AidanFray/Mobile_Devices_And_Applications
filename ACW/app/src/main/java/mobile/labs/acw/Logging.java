package mobile.labs.acw;
import android.util.Log;

public class Logging<T> {


    /**
     * This method deals with general exception handling
     * @param e - Exception object
     */
    public static void Exception(Exception e) {
        String callingMethod = getCallingMethod();
        Log.i("ACW_Application", "Error [" + callingMethod + "]: " + e.getMessage());
    }

    /**
     * Generic eception method that can handle any exception type
     * @param e - Exception object
     * @param msg - Exception .getMessage() string
     */
    public void Exception(T e, String msg) {
        String callingMethod = getCallingMethod();
        Log.i("ACW_Application", "Error [" + callingMethod + "]: " + e.toString() + ":" + msg);
    }

    /**
     * Method that uses the stack trace to find the method that called the Exception logging
     * @return - String containing the calling method
     */
    private static String getCallingMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        //TODO: test that the 5th element is always the calling method
        return stackTraceElements[4].getClassName() + "." + stackTraceElements[4].getMethodName();

    }
}
