package ai.starlake.transpiler;

public class Utils {
    static public String unquote(String s) {
        if (s == null || s.length() < 2) {
            return s;
        }
        char[] quotes = new char[] { '\'', '"', '`' };
        for (char q : quotes) {
            if (s.charAt(0) == q && s.charAt(s.length() - 1) == q) {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }
}
