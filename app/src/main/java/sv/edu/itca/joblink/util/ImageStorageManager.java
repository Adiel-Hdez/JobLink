package sv.edu.itca.joblink.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageStorageManager {
    private static final String TAG = "ImageStorageManager";

    public static String saveImageToInternalStorage(Context context, Uri imageUri, String fileNamePrefix) {
        if (imageUri == null || context == null) return null;

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            
            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) inputStream.close();

            int sampleSize = 1;
            int reqWidth = 800;
            int reqHeight = 800;
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                final int halfHeight = options.outHeight / 2;
                final int halfWidth = options.outWidth / 2;
                while ((halfHeight / sampleSize) >= reqHeight && (halfWidth / sampleSize) >= reqWidth) {
                    sampleSize *= 2;
                }
            }

            // Decode bitmap with inSampleSize
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            
            inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) inputStream.close();

            if (bitmap == null) return null;

            File directory = new File(context.getFilesDir(), "joblink_photos");
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                Log.d(TAG, "Directory created: " + created);
            }

            File file = new File(directory, fileNamePrefix + "_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            Log.d(TAG, "Saved image to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving image locally: " + e.getMessage(), e);
            return null;
        }
    }

    public static void loadImageIntoView(ImageView imageView, String imagePathOrUri) {
        if (imageView == null || imagePathOrUri == null || imagePathOrUri.trim().isEmpty()) return;

        try {
            if (imagePathOrUri.startsWith("/")) {
                File imgFile = new File(imagePathOrUri);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    if (myBitmap != null) {
                        imageView.setImageBitmap(myBitmap);
                        imageView.setPadding(0, 0, 0, 0);
                        return;
                    }
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