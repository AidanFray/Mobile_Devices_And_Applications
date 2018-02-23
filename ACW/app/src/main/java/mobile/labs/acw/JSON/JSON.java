package mobile.labs.acw.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import mobile.labs.acw.ExceptionHandling.Logging;

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

    public static JSONObject ReadFromFile(String filename) {

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader stream = new BufferedReader(new FileReader(filename));

            String line = stream.readLine();
            while (line != null) {
                sb.append(line);
                line = stream.readLine();
            }
            return new JSONObject(sb.toString());

        } catch (Exception e) {
            Logging.Exception(e);
        }
        return null;
    }

    public static JSONArray GetJSONArray(JSONObject jsonObject, String id) {
        try {
            return jsonObject.getJSONArray(id);
        } catch (JSONException e) {
            Logging.Exception(e);
        }
        return null;
    }

    public static Object GetIndex(JSONArray array, int index) {
        try {
            return array.get(index);
        } catch (JSONException e) {
            Logging.Exception(e);
        }
        return null;
    }
}