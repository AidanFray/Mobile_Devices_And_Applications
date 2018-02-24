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

    /**
     * Returns if successful a JSONObject from a url
     * @param pURL - The url that contains a JSON object
     * @return - The parsed JSON in the form of a JSONObject
     */
    public static JSONObject ReadFromURL(String pURL) {

        //Downloads JSON code from an url
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(pURL);
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

    /**
     * Reads a JSON object from a file
     * @param pFilename - The filename of the target JSON file
     * @return - The parsed JSON in the form of a JSON object
     */
    public static JSONObject ReadFromFile(String pFilename) {

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader stream = new BufferedReader(new FileReader(pFilename));

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

    /**
     * Returns a JSONArray from a provided JSONObject. This is a separate method so the error
     * handling can be performed in one location - Try/Catch statements are ugly
     * @param pJSONObject - JSON object containing the JSON array
     * @param pJSONArrayID - The key value of the JSON array
     * @return - The JSON array
     */
    public static JSONArray GetJSONArray(JSONObject pJSONObject, String pJSONArrayID) {
        try {
            return pJSONObject.getJSONArray(pJSONArrayID);
        } catch (JSONException e) {
            Logging.Exception(e);
        }
        return null;
    }

    /**
     * Grabs a single index from a JSON array. This is a separate method so the error handling
     * can be done in one location
     * @param pArray - The JSONArray object
     * @param pIndex - The required index
     * @return - Returns an object obtained from the array. It needs to be cast at the request.
     */
    public static Object GetIndex(JSONArray pArray, int pIndex) {
        try {
            return pArray.get(pIndex);
        } catch (JSONException e) {
            Logging.Exception(e);
        }
        return null;
    }
}