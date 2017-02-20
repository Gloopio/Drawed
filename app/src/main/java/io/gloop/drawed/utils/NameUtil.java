package io.gloop.drawed.utils;

/**
 * Created by Alex Untertrifaller on 20.02.17.
 */

public class NameUtil {

    public static String randomUserName() {
        return randomAdjective() + randomColor() + randomAnimal();
    }

    public static String randomBoardName() {
        return randomAdjective() + randomColor() + randomObject();
    }

    private static String randomAdjective() {
        return ""; // TODO impl
    }

    private static String randomColor() {
        return ""; // TODO impl
    }

    private static String randomAnimal() {
        return ""; // TODO impl
    }

    private static String randomObject() {
        return ""; // TODO impl
    }
}
