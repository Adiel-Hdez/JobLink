package sv.edu.itca.joblink.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageStorageManager {
    private static final String TAG = "ImageStorageManager";

    public static String saveImageToInternalStorage(Context context, Uri imageUri, String fileNamePrefix) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap == null) return null;

            File directory = new File(context.getFilesDir(), "joblink_photos");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileNamePrefix + "_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving image locally: " + e.getMessage());
            return imageUri.toString();
        }
    }

    public static void loadImageIntoView(ImageView imageView, String imagePathOrUri) {
        if (imagePathOrUri == null || imagePathOrUri.trim().isEmpty()) return;

        try {
            if (imagePathOrUri.startsWith("/")) {
                File imgFile = new File(imagePathOrUri);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                    imageView.setPadding(0, 0, 0, 0);
                }
            } else if (imagePathOrUri.startsWith("content://") || imagePathOrUri.startsWith("file://")) {
                imageView.setImageURI(Uri.parse(imagePathOrUri));
                imageView.setPadding(0, 0, 0, 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image into view: " + e.getMessage());
        }
    }
}