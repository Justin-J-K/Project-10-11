import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Symbol {
    private static final Set<Character> SYMBOL_MAP = new HashSet<>(Arrays.asList(
            '{', '}', '(', ')', '[', ']', '.', ',', ';', '+',
            '-', '*', '/', '&', '|', '<', '>', '=', '~'));

    public static boolean isSymbol(char c) {
        return SYMBOL_MAP.contains(c);
    }
}
