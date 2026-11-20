package sv.edu.itca.joblink.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class ImagePickerHelper {

    public interface OnOptionSelectedListener {
        void onCameraSelected();
        void onGallerySelected();
    }

    public static void showImageSourceDialog(Context context, OnOptionSelectedListener listener) {
        if (context == null || listener == null) return;

        CharSequence[] options = new CharSequence[]{"📷 Tomar foto con cámara", "🖼️ Elegir de la galería"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Seleccionar Imagen");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                listener.onCameraSelected();
            } else {
                listener.onGallerySelected();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public static boolean hasCameraPermission(Context context) {
        if (context == null) return false;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasGalleryPermission(Context context) {
        if (context == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static Intent createCameraIntent() {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    }

    public static Intent createGalleryIntent() {
        return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }
}
