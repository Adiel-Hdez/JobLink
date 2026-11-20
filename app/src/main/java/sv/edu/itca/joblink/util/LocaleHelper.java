package sv.edu.itca.joblink.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_NAME = "joblink_locale_pref";
    private static final String KEY_LANGUAGE = "selected_language";

    public static Context setLocale(Context context, String languageCode) {
        saveLanguage(context, languageCode);
        return updateResources(context, languageCode);
    }

    public static String getLanguage(Context context) {
        if (context == null) return "es";
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "es");
    }

    private static void saveLanguage(Context context, String languageCode) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    public static Context onAttach(Context context) {
        String lang = getLanguage(context);
        return setLocale(context, lang);
    }

    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
            return context;
        }
    }

    public static String getLanguageDisplayName(String code) {
        if (code == null) return "Español";
        switch (code.toLowerCase()) {
            case "en": return "English";
            case "pt": return "Português";
            case "fr": return "Français";
            case "ca": return "Català";
            case "es":
            default:
                return "Español";
        }
    }
}
