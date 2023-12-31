/**
 * BoyerMoore class extends Matcher and implements the Boyer-Moore string search algorithm.
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

package crud.core.pattern_matching;

import java.util.HashMap;
import java.util.Map;

/**
 * BoyerMoore class extends Matcher and implements the Boyer-Moore string search algorithm.
 */
public class BoyerMoore extends Matcher {

    private final Map<Character, Integer> badCharacterShift;
    private final String pattern;
    private int comp = 0;
    private boolean displayed = false;

    /**
     * Constructor for BoyerMoore class, initializes the pattern and bad character shift table.
     * 
     * @param pattern The pattern string for search
     */
    public BoyerMoore(String pattern) {
        this.pattern = pattern;
        this.badCharacterShift = new HashMap<>();

        for (int i = 0; i < pattern.length(); i++) 
            badCharacterShift.put(pattern.charAt(i), pattern.length() - i - 1);

        comp += pattern.length();
    }

    /**
     * Searches for the given pattern in the input text using the Boyer-Moore algorithm.
     * 
     * @param text The text to search for the pattern
     * @return The index of the first occurrence of the pattern in the text, or -1 if not found
     */
    @Override
    public int search(String text) {
        int m = pattern.length();
        int n = text.length();
        int skip;

        for (int i = 0; i <= n - m; i += skip) {
            skip = 0;
            for (int j = m - 1; j >= 0; j--) {
                if(!displayed) comp++;
                if (pattern.charAt(j) != text.charAt(i+j)) {
                    Integer shift = badCharacterShift.get(text.charAt(i+j));
                    skip = Math.max(1, shift == null ? m : shift);
                    break;
                }
            }

            if (skip == 0) {
                if(!displayed) {
                    System.out.println("Comparações: " + comp);
                    displayed = true;
                }

                return i; 
            }
        }

        return -1; 
    }
}
