package pl.agh.depressiondetector.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    private ToastUtils() {
    }

    public static void show(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
    }
}
