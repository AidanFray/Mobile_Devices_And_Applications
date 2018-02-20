package mobile.labs.acw.JSON;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import mobile.labs.acw.Logging;

public class JSON {

    public static JSONObject ReadFromURL(String url_sting) {

        //Downloads JSON code from an url
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(url_sting);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(1000);
            BufferedReader bufferedInputStream
                    = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            //Reads each line
            String line = null;
            while ((line = bufferedInputStream.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedInputStream.close();

            String result = stringBuilder.toString();
            return new JSONObject(result);

        } catch (Exception e) {
            Logging.Exception(e);
        }
        return null;
    }
}