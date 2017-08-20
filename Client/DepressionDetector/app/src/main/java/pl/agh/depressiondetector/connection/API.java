package pl.agh.depressiondetector.connection;

public interface API {
    String HOST = "depressionserver.herokuapp.com";

    String PATH_REGISTER = "auth/register";
    String PATH_LOGIN = "auth/login";
    String PATH_SOUND_FILES = "sound_files";
    String PATH_RESULTS = "results";


    String USERNAME = "username";
    String PASSWORD = "password";
    String EMAIL = "email";
    String SEX = "sex";
    String DATE_OF_BIRTH = "date_of_birth";


    String MESSAGE = "message";

    String SIGNUP_USER_REGISTERED = "SIGNUP_USER_REGISTERED";
    String SIGNUP_LOGIN_ALREADY_USED = "SIGNUP_LOGIN_ALREADY_USED";
    String SIGNUP_EMAIL_ALREADY_USED = "SIGNUP_EMAIL_ALREADY_USED";

    String LOGIN_USER_LOGGED_IN = "LOGIN_USER_LOGGED_IN";
    String LOGIN_LOGIN_DOES_NOT_EXIST = "LOGIN_LOGIN_DOES_NOT_EXIST";
    String LOGIN_PASSWORD_INVALID = "LOGIN_PASSWORD_INVALID";

    String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    String TIMEOUT_ERROR = "TIMEOUT_ERROR";
    String CONNECTION_ERROR = "CONNECTION_ERROR";
}