package io.gloop.drawed.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

import io.gloop.drawed.model.Line;
import io.gloop.drawed.model.Point;

/**
 * Created by Alex Untertrifaller on 12.04.17.
 */

public class ScreenUtil {

    private static Activity activity;
    private static float scaleFactor;

    public static void setActivity(Activity a) {
        activity = a;
        scaleFactor = getScaleFactor();
    }

    private static float getScreenHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    private static float getScreenWidth(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    private static float getScaleFactor() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float factor1 = metrics.heightPixels / 1920f;
        float factor2 = metrics.widthPixels / 1080f;

        if (factor1 >= factor2)
            return factor1;
        else
            return factor2;
    }

    public static Line normalize(Line line) {
        if (line != null) {
            for (Point point : line.getPoints()) {
                point.setX(point.getX() / scaleFactor);
                point.setY(point.getY() / scaleFactor);
            }
        }
        return line;
    }

    public static Line scale(Line line) {
        if (line != null) {
            Line l = new Line();
            l.setColor(line.getColor());
            l.setBrushSize(line.getBrushSize());

            for (Point point : line.getPoints()) {
                Point p = new Point();
                p.setX(point.getX() * scaleFactor);
                p.setY(point.getY() * scaleFactor);
                l.addPoint(p);
            }

            return l;
        }
        return null;
    }

    public static float scale(float lineThickness) {
        return lineThickness * scaleFactor;
    }

    public static float normalize(float lineThickness) {
        return lineThickness / scaleFactor;
    }
}
