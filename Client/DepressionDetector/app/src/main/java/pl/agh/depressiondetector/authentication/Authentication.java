package pl.agh.depressiondetector.authentication;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pl.agh.depressiondetector.connection.HttpClient;
import pl.agh.depressiondetector.model.User;

import static pl.agh.depressiondetector.connection.API.CONNECTION_ERROR;
import static pl.agh.depressiondetector.connection.API.HOST;
import static pl.agh.depressiondetector.connection.API.MESSAGE;
import static pl.agh.depressiondetector.connection.API.PATH_LOGIN;
import static pl.agh.depressiondetector.connection.API.PATH_REGISTER;
import static pl.agh.depressiondetector.connection.API.TIMEOUT_ERROR;
import static pl.agh.depressiondetector.connection.API.UNKNOWN_ERROR;
import static pl.agh.depressiondetector.connection.HttpClient.JSON_TYPE;

class Authentication {

    private static final String TAG = "Authentication";

    static String register(User user) {
        return authenticate(user, PATH_REGISTER);
    }

    static String login(User user) {
        return authenticate(user, PATH_LOGIN);
    }

    private static String authenticate(User user, String path) {
        String message = UNKNOWN_ERROR;
        try {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host(HOST)
                    .addEncodedPathSegments(path)
                    .build();

            JSONObject json = user.toJSON();

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSON_TYPE, json.toString()))
                    .build();

            Response response = HttpClient.getClient().newCall(request).execute();

            ResponseBody body = response.body();
            if (body != null) {
                message = new JSONObject(body.string()).optString(MESSAGE, UNKNOWN_ERROR);

                if (response.isSuccessful())
                    Log.i(TAG, "Success for " + user.name);
                else
                    Log.i(TAG, "Failed. Server returned: " + response.message() + " with code " + response.code());

                body.close();
            }
        } catch (SocketTimeoutException e) {
            message = TIMEOUT_ERROR;
            e.printStackTrace();
        } catch (IOException e) {
            message = CONNECTION_ERROR;
            e.printStackTrace();
        } catch (JSONException e) {
            message = UNKNOWN_ERROR;
            e.printStackTrace();
        }
        return message;
    }
}
