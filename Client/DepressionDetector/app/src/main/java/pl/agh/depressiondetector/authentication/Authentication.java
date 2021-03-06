package pl.agh.depressiondetector.authentication;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pl.agh.depressiondetector.connection.HttpClient;
import pl.agh.depressiondetector.model.User;

import static pl.agh.depressiondetector.connection.API.CONNECTION_ERROR;
import static pl.agh.depressiondetector.connection.API.EMAIL;
import static pl.agh.depressiondetector.connection.API.HOST;
import static pl.agh.depressiondetector.connection.API.MESSAGE_AUTHENTICATE;
import static pl.agh.depressiondetector.connection.API.MESSAGE_DELETE;
import static pl.agh.depressiondetector.connection.API.MESSAGE_USER_DATA;
import static pl.agh.depressiondetector.connection.API.PASSWORD_EMAIL_SENT;
import static pl.agh.depressiondetector.connection.API.PATH_RESET_PASSWORD;
import static pl.agh.depressiondetector.connection.API.PATH_USER;
import static pl.agh.depressiondetector.connection.API.PATH_LOGIN;
import static pl.agh.depressiondetector.connection.API.PATH_REGISTER;
import static pl.agh.depressiondetector.connection.API.SENT_EMAIL;
import static pl.agh.depressiondetector.connection.API.TIMEOUT_ERROR;
import static pl.agh.depressiondetector.connection.API.UNKNOWN_ERROR;
import static pl.agh.depressiondetector.connection.HttpClient.JSON_TYPE;


public class Authentication {

    private static final String TAG = "Authentication";

    static RequestResult register(User user) {
        return authenticate(user, PATH_REGISTER);
    }

    static RequestResult login(User user) {
        return authenticate(user, PATH_LOGIN);
    }

    private static RequestResult authenticate(User user, String path) {
        RequestResult requestResult = new RequestResult();
        requestResult.message = UNKNOWN_ERROR;
        try {
            HttpUrl url = buildHttpsUrl(path);

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSON_TYPE, user.toJSON().toString()))
                    .build();

            Response response = HttpClient.getClient().newCall(request).execute();

            ResponseBody body = response.body();
            if (body != null) {
                JSONObject json = new JSONObject(body.string());
                requestResult.message = json.optString(MESSAGE_AUTHENTICATE, UNKNOWN_ERROR);
                requestResult.json = json.optJSONObject(MESSAGE_USER_DATA);

                if (response.isSuccessful())
                    Log.i(TAG, "Success for " + user.email);
                else
                    Log.i(TAG, "Failed. Server returned: " + response.message() + " with code " + response.code());

                body.close();
            }
        } catch (SocketTimeoutException e) {
            requestResult.message = TIMEOUT_ERROR;
            e.printStackTrace();
        } catch (IOException e) {
            requestResult.message = CONNECTION_ERROR;
            e.printStackTrace();
        } catch (JSONException e) {
            requestResult.message = UNKNOWN_ERROR;
            e.printStackTrace();
        }

        return requestResult;
    }


    public static String delete(User user) {
        String message = UNKNOWN_ERROR;
        try {
            HttpUrl url = buildHttpsUrl(PATH_USER);

            JSONObject json = user.toJSON();

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", Credentials.basic(user.name, user.password))
                    .delete(RequestBody.create(JSON_TYPE, json.toString()))
                    .build();

            Response response = HttpClient.getClient().newCall(request).execute();

            ResponseBody body = response.body();
            if (body != null) {
                boolean emailSent = new JSONObject(body.string()).getBoolean(MESSAGE_DELETE);
                if (emailSent)
                    message = SENT_EMAIL;

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

    static RequestResult changePassword(String email) {
        RequestResult requestResult = new RequestResult();
        requestResult.message = UNKNOWN_ERROR;
        try {
            HttpUrl url = buildHttpsUrl(PATH_RESET_PASSWORD);
            JSONObject json = new JSONObject().put(EMAIL, email);

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSON_TYPE, json.toString()))
                    .build();

            Response response = HttpClient.getClient().newCall(request).execute();

            ResponseBody body = response.body();
            if (body != null) {
                if (response.isSuccessful())
                    requestResult.message = PASSWORD_EMAIL_SENT;
                else
                    requestResult.message = UNKNOWN_ERROR;

                body.close();
            }
        } catch (SocketTimeoutException e) {
            requestResult.message = TIMEOUT_ERROR;
            e.printStackTrace();
        } catch (IOException e) {
            requestResult.message = CONNECTION_ERROR;
            e.printStackTrace();
        } catch (JSONException e) {
            requestResult.message = UNKNOWN_ERROR;
            e.printStackTrace();
        }

        return requestResult;
    }

    private static HttpUrl buildHttpsUrl(String path) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addEncodedPathSegments(path)
                .build();
    }
}
