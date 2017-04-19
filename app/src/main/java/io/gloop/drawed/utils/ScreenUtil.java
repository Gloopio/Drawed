package io.gloop.drawed.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import io.gloop.GloopLogger;
import io.gloop.drawed.model.Point;

/**
 * Created by Alex Untertrifaller on 12.04.17.
 */

public class ScreenUtil {

    public static void getScreenSize(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        GloopLogger.i(metrics.heightPixels);
        GloopLogger.i(metrics.widthPixels);
        GloopLogger.i(metrics.density);
        GloopLogger.i(metrics.densityDpi);
        GloopLogger.i(metrics.scaledDensity);
        GloopLogger.i(metrics.toString());
    }

    public static float getScreenDensity(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }


    public static float getScreenDensity(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        return metrics.density;
    }

    public static Point scalePoints(Point point, Context context) {
        Point scaledPoint = new Point();

        float screenDensity = getScreenDensity(context);

        scaledPoint.setX(point.getX()/screenDensity);
        scaledPoint.setY(point.getY()/screenDensity);

        return scaledPoint;
    }
}
