package net.mctitan.rpg.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.HashMap;
import java.util.Map;

public class Text {
    private static final int BOOK_WIDTH = 228;
    private static final Map<Character,Integer> fontwidth = new HashMap<>();

    public static String enumtoprint(String name) {
        String ret = "";

        boolean upper = true;
        for(int i = 0; i < name.length(); ++i) {
            Character c = name.charAt(i);
            if(c == '_' || c == ' ') {
                upper = true;
                ret += ' ';
            } else if(upper) {
                upper = false;
                ret += c.toString().toUpperCase();
            } else {
                ret += c.toString().toLowerCase();
            }
        }

        return ret;
    }

    public static String enumtocraftable(String name) {
        return name.toLowerCase();
    }

    private static final int[] romannum = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4 , 1};
    private static final String[] romansym = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    public static String romanint(int number) { return romanint(number, Integer.MAX_VALUE); }
    public static String romanint(int number, int max) {
        if(max == 1) { return ""; }

        String ret = "";
        for(int index = 0; index < romannum.length; ++index) {
            if(number < romannum[index]) { continue; }

            int divisor = number / romannum[index];
            for(int i = 0; i < divisor; ++i) {
                ret += romansym[index];
            }

            number = number % romannum[index];
        }

        return ret;
    }

    public static Component bookcenter(String text, TextDecoration decoration) {
        int spacewidth = fontwidth.get(' ') + 2;
        int textwidth = width(text, decoration);
        int widthneeded = (bookwidth() - textwidth) / 2;
        int spaces = widthneeded / spacewidth;
        if(widthneeded % spacewidth > spacewidth / 2) { spaces++; }

        String spacesstr = "";
        for(int i = 0; i < spaces; ++i) { spacesstr = String.format("%s ", spacesstr); }

        Component textcomponent = Component.text(text);
        if(decoration != null) {
            textcomponent = textcomponent.decorate(decoration);
        }

        return Component.text(spacesstr).append(textcomponent);
    }

    public static int bookwidth() { return BOOK_WIDTH; }
    public static int width(String text, TextDecoration decoration) {
        // get width of text
        int width = 0;
        for(int index = 0; index < text.length(); ++index) {
            char c = text.charAt(index);
            width += fontwidth.getOrDefault(c,10) + 2;
            if(decoration == TextDecoration.BOLD) { width += 2; }
        }

        if(decoration == TextDecoration.ITALIC) {
            width += 3;
        }

        // remove the extra space at the ned
        width -= 2;

        // get the width
        return width;
    }

    static {
        // lower case
        fontwidth.put('f', 8);
        fontwidth.put('i', 2);
        fontwidth.put('k', 8);
        fontwidth.put('l', 2);
        fontwidth.put('t', 2);

        // upper case
        fontwidth.put('I', 6);

        // special
        fontwidth.put('[', 6);
        fontwidth.put(']', 6);
        fontwidth.put('{', 6);
        fontwidth.put('}', 6);
        fontwidth.put('(', 6);
        fontwidth.put(')', 6);
        fontwidth.put('<', 8);
        fontwidth.put('>', 8);
        fontwidth.put('\'', 2);
        fontwidth.put('|', 2);
        fontwidth.put('"', 6);
        fontwidth.put(';', 2);
        fontwidth.put(':', 2);
        fontwidth.put(',', 2);
        fontwidth.put('.', 2);
        fontwidth.put(' ', 6);
    }
}
