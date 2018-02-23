package mobile.labs.acw.Images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import mobile.labs.acw.ExceptionHandling.Logging;

public class Image {

    public static Bitmap DownloadFromURL(String url) {

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(1000);

            return BitmapFactory.decodeStream(connection.getInputStream());

        } catch (IOException e){
            Logging.Exception(e);
            return null;
        }
    }
}
