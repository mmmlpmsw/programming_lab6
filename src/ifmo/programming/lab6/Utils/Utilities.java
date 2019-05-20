package ifmo.programming.lab6.Utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    /**
     * Раскрашивает текст, заменяя подсроки-маркеры специальными ASCII-кодами
     * Подстрокой-маркером считается текст, заключенный в двойные прямоугольные скобки.
     * Например: [[RED]], [[BG_BLUE]]
     *
     * Информация о цветах получается из {@link Constants.ANSI_COLORS}
     * @param source исходная строка с кодами
     * @return раскрашенный текст
     */
    public static String colorize(String source) {
        try {
            StringBuilder result = new StringBuilder(source);
            HashMap<String, String> colorsMap = new HashMap<>();
            Field[] colorFields = Constants.ANSI_COLORS.class.getDeclaredFields();
            for (Field field : colorFields) {
                if (field.getType() == String.class)
                    colorsMap.put(field.getName(), (String)field.get(Constants.ANSI_COLORS.class));
            }
            String regex = "\\[\\[\\w+]]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(result);
            while (matcher.find()) {
                String colorName = result.substring(matcher.start()+2, matcher.end()-2).toUpperCase();
                String color = colorsMap.get(colorName);
                if (color != null) {
                    result.replace(matcher.start(), matcher.end(), color);
                    matcher.region(matcher.start(), result.length());
                }
            }
            return result.toString();
        } catch (IllegalAccessException e) {
            return source;
        }
    }
}
