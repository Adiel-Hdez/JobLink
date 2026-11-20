package sv.edu.itca.joblink;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

import sv.edu.itca.joblink.util.LocaleHelper;

public class JobLinkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Force Light Theme (Versión normal, desactiva modo oscuro automáticamente)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        LocaleHelper.onAttach(this);
    }
}
