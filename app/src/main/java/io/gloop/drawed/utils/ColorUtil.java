package io.gloop.drawed.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

/**
 * Util to get random generated colors.
 *
 * Created by Alex Untertrifaller on 17.02.17.
 */
public class ColorUtil {

    private static int previousColor;

    // Generates a random color and takes care that it is not equals to the previous one.
    public static int randomColor(Context context, String typeColor) {
        if (previousColor == 0)
            return getMatColor(context, typeColor);

        int newColor;
        do {
            newColor = getMatColor(context, typeColor);
        } while (newColor == previousColor);
        previousColor = newColor;
        return newColor;
    }

    private static int getMatColor(Context context, String typeColor) {
        int returnColor = Color.BLACK;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + typeColor, "array", context.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLACK);
            colors.recycle();
        }
        return returnColor;
    }
}
