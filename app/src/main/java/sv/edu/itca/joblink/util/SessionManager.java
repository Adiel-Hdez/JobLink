package sv.edu.itca.joblink.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import sv.edu.itca.joblink.model.User;

public class SessionManager {
    private static final String PREF_NAME = "JobLinkSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_JSON = "userJson";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        gson = new Gson();
    }

    public void saveUserSession(User user) {
        if (user == null) return;
        String json = gson.toJson(user);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_JSON, json);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public User getUserSession() {
        String json = pref.getString(KEY_USER_JSON, null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}