package mobile.labs.acw.ExceptionHandling;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Logging<T> {

    private static final String LOG_ID = "ACW_APPLICATION";

    /**
     * This method deals with general exception handling
     * @param e - Exception object
     */
    public static void Exception(Exception e) {
        String callingMethod = getCallingMethod();
        e.printStackTrace();
        Log.i(LOG_ID, "Error [" + callingMethod + "]: " + e.getMessage());
        SaveToLogFile(e.getMessage());
    }

    /**
     * Generic eception method that can handle any exception type
     * @param e - Exception object
     * @param msg - Exception .getMessage() string
     */
    public void Exception(T e, String msg) {
        String callingMethod = getCallingMethod();
        Log.i(LOG_ID, "Error [" + callingMethod + "]: " + e.toString() + ":" + msg);
        SaveToLogFile(msg);
    }

    /**
     * Method that uses the stack trace to find the method that called the Exception logging
     * @return - String containing the calling method
     */
    private static String getCallingMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return stackTraceElements[4].getClassName() + "." + stackTraceElements[4].getMethodName();

    }

    private static void SaveToLogFile(String pMsg) {
        File logFile = new File("data/data/ACW_log.txt");

        try {
            Writer stream = new FileWriter(logFile);
            stream.write(pMsg);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
