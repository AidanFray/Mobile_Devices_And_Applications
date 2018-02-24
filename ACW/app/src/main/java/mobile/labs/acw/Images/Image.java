package mobile.labs.acw.Images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import mobile.labs.acw.ExceptionHandling.Logging;

public class Image {

    /**
     * As the name suggest this method uses a url to download an images as an Bitmap objecy
     * @param pURL - Url that contains an image
     * @return - Bitmap object of the image
     */
    public static Bitmap DownloadFromURL(String pURL) {

        try {
            URLConnection connection = new URL(pURL).openConnection();
            connection.setConnectTimeout(1000);

            return BitmapFactory.decodeStream(connection.getInputStream());

        } catch (IOException e){
            Logging.Exception(e);
            return null;
        }
    }
}
