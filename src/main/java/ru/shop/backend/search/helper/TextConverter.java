package ru.shop.backend.search.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TextConverter {

    private final static Map<Character, Character> cyrillicToLatin = new HashMap<>();
    private final static Map<Character, Character> latinToCyrillic = new HashMap<>();

    static {
        fillMappings();
    }
    private static final Pattern pattern = Pattern.compile("\\d+");

    public static String convert(String message) {
        boolean hasCyrillic = message.matches(".*\\p{InCyrillic}.*");
        StringBuilder builder = new StringBuilder();
        Map<Character, Character> mapping = hasCyrillic? cyrillicToLatin : latinToCyrillic;

        for (int i = 0; i < message.length(); i++) {
            char currentChar = message.charAt(i);
            if (mapping.containsKey(currentChar)) {
                builder.append(mapping.get(currentChar));
            }
        }
        return builder.toString();
    }

    public static String convert(String message, boolean needConvert) {
        if (!needConvert) {
            return message;
        }
        return convert(message);
    }

    private static void fillMappings() {
        char[] ru = {'й','ц','у','к','е','н','г','ш','щ','з','х','ъ','ф','ы','в','а','п','р','о','л','д','ж','э', 'я','ч', 'с','м','и','т','ь','б', 'ю','.',
                ' ','0','1','2','3','4','5','6','7','8','9','-'};
        char[] en = {'q','w','e','r','t','y','u','i','o','p','[',']','a','s','d','f','g','h','j','k','l',';','"','z','x','c','v','b','n','m',',','.','/',
                ' ','0','1','2','3','4','5','6','7','8','9','-'};
        for (int i = 0; i < ru.length; i++) {
            cyrillicToLatin.put(ru[i], en[i]);
            latinToCyrillic.put(en[i], ru[i]);
        }
    }

    public static boolean isContainErrorChar(String text) {
        return Stream.of("[", "]", "\"", "/", ";").anyMatch(text::contains);
    }

    public static boolean isNumeric(String strNum) {
        return strNum != null && pattern.matcher(strNum).matches();
    }


}
