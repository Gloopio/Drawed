package io.gloop.drawed.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;

import java.lang.reflect.Field;

import io.gloop.drawed.R;

/**
 * Util to get random generated colors.
 *
 * Created by Alex Untertrifaller on 17.02.17.
 */
public class ColorUtil {

    private static int previousColor;

    public static int getColorByName(Context context, String name) {
        int colorId = 0;

        try {
            Class res = R.color.class;
            Field field = res.getField(name);
            colorId = field.getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (colorId == 0)
            return randomColor(context);
        return ResourcesCompat.getColor(context.getResources(), colorId, null); //without theme

    }

    // Generates a random color and takes care that it is not equals to the previous one.
    private static int randomColor(Context context) {
        if (previousColor == 0)
            return getMatColor(context);

        int newColor;
        do {
            newColor = getMatColor(context);
        } while (newColor == previousColor);
        previousColor = newColor;
        return newColor;
    }

    private static int getMatColor(Context context) {
        int returnColor = Color.BLACK;
        int arrayId = context.getResources().getIdentifier("mdcolor_500", "array", context.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLACK);
            colors.recycle();
        }
        return returnColor;
    }


    public static int darkenColor(int color) {
        return manipulateColor(color, 0.9f);
    }

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }
}
