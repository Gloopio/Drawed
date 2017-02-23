package io.gloop.drawed.utils;

import android.content.Context;

import java.util.Random;

import io.gloop.drawed.R;

/**
 * Created by Alex Untertrifaller on 20.02.17.
 */

public class NameUtil {

    private static final Random random = new Random();

    public static String randomUserName(Context context) {
        return randomAdjective(context) + randomColor(context) + randomAnimal(context);
    }

//    public static String randomBoardName(Context context) {
//        return randomAdjective(context) + randomColor(context) + randomObject(context);
//    }

    public static String randomAdjective(Context context) {
        String[] myString = context.getResources().getStringArray(R.array.adjectives);
        return  myString[random.nextInt(myString.length)];
    }

    public static String randomColor(Context context) {
        String[] myString = context.getResources().getStringArray(R.array.colors);
        return  myString[random.nextInt(myString.length)];
    }

    public static String randomAnimal(Context context) {
        String[] myString = context.getResources().getStringArray(R.array.animals);
        return  myString[random.nextInt(myString.length)];
    }

    public static String randomObject(Context context) {
        String[] myString = context.getResources().getStringArray(R.array.objects);
        return  myString[random.nextInt(myString.length)];
    }
}
