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

    /**
     * Converts a picked Uri into a compressed Base64 Data URL ("data:image/jpeg;base64,...")
     * so it can be saved directly in Supabase text columns (foto_perfil_url, imagen_url).
     */
    public static String convertUriToBase64(Context context, Uri imageUri) {
        if (imageUri == null || context == null) return null;

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) inputStream.close();

            int sampleSize = 1;
            int reqWidth = 400;
            int reqHeight = 400;
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                final int halfHeight = options.outHeight / 2;
                final int halfWidth = options.outWidth / 2;
                while ((halfHeight / sampleSize) >= reqHeight && (halfWidth / sampleSize) >= reqWidth) {
                    sampleSize *= 2;
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            
            inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) inputStream.close();

            if (bitmap == null) return null;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            outputStream.close();

            String base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            return "data:image/jpeg;base64," + base64String;

        } catch (Exception e) {
            Log.e(TAG, "Error converting image to base64: " + e.getMessage(), e);
            return null;
        }
    }

    public static String saveImageToInternalStorage(Context context, Uri imageUri, String fileNamePrefix) {
        if (imageUri == null || context == null) return null;

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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving image locally: " + e.getMessage());
            return null;
        }
    }

    /**
     * Renders Base64 strings, file paths, or Uris into an ImageView seamlessly.
     */
    public static void loadImageIntoView(ImageView imageView, String imageStr) {
        if (imageView == null || imageStr == null || imageStr.trim().isEmpty()) return;

        try {
            String str = imageStr.trim();

            if (str.startsWith("data:image") || str.length() > 100) {
                String cleanBase64 = str;
                if (str.contains(",")) {
                    cleanBase64 = str.substring(str.indexOf(",") + 1);
                }
                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.NO_WRAP);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setPadding(0, 0, 0, 0);
                    return;
                }
            }

            if (str.startsWith("/")) {
                File imgFile = new File(str);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    if (myBitmap != null) {
                        imageView.setImageBitmap(myBitmap);
                        imageView.setPadding(0, 0, 0, 0);
                        return;
                    }
                }
            }

            if (str.startsWith("content://") || str.startsWith("http")) {
                imageView.setImageURI(Uri.parse(str));
                imageView.setPadding(0, 0, 0, 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rendering image: " + e.getMessage());
        }
    }
}