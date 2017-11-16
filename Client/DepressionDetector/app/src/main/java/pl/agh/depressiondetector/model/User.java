package pl.agh.depressiondetector.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import pl.agh.depressiondetector.utils.DateUtils;

import static pl.agh.depressiondetector.connection.API.DATE_OF_BIRTH;
import static pl.agh.depressiondetector.connection.API.EMAIL;
import static pl.agh.depressiondetector.connection.API.PASSWORD;
import static pl.agh.depressiondetector.connection.API.SEX;
import static pl.agh.depressiondetector.connection.API.USERNAME;
import static pl.agh.depressiondetector.utils.DateUtils.convertToServerDateFormat;

public class User {
    public String name;
    public String password;
    public String email;
    public Boolean sex;
    public Date dateOfBirth;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        if (name != null)
            json.put(USERNAME, name);
        if (password != null)
            json.put(PASSWORD, password);
        if (email != null)
            json.put(EMAIL, email);
        if (sex != null)
            json.put(SEX, sex ? "M" : "F");
        if (dateOfBirth != null)
            json.put(DATE_OF_BIRTH, DateUtils.convertToServerDateTimeFormat(dateOfBirth));

        return json;
    }
}
