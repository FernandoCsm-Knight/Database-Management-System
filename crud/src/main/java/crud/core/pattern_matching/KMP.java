/**
 * KMP class extends Matcher and implements the Knuth-Morris-Pratt (KMP) string search algorithm.
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

package crud.core.pattern_matching;

/**
 * KMP class extends Matcher and implements the Knuth-Morris-Pratt (KMP) string search algorithm.
 */
public class KMP extends Matcher {

    private String pat;
    private int[] lps;
    private int comp = 0;
    private boolean displayed = false;

    /**
     * Constructor for KMP class, initializes the pattern and pre-processes the LPS array.
     * 
     * @param pattern The pattern string for search
     */
    public KMP(String pattern) {
        this.pat = pattern;
        this.lps = new int[pattern.length()];
        computeLPSArray();
    }

    /**
     * Computes the Longest Proper Prefix which is also a Suffix (LPS) array for the pattern.
     */
    private void computeLPSArray() {
        int len = 0;
        int i = 1;
        lps[0] = 0;

        while (i < pat.length()) {
            if(!displayed) comp++;
            if (pat.charAt(i) == pat.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = len;
                    i++;
                }
            }
        }
    }

    /**
     * Searches for the given pattern in the input text using the KMP algorithm.
     * 
     * @param txt The text to search for the pattern
     * @return The index of the first occurrence of the pattern in the text, or -1 if not found
     */
    @Override
    public int search(String txt) {
        int response = -1;

        int M = pat.length();
        int N = txt.length();

        int j = 0; 
        int i = 0; 

        while (i < N) {
            if(!displayed) comp++;
            if (pat.charAt(j) == txt.charAt(i)) {
                j++;
                i++;
            }
            if (j == M) {
                response = i - j;
                if(!displayed) {
                    System.out.println("Comparações: " + comp);
                    displayed = true;
                }
                j = lps[j - 1];
            } else if (i < N && pat.charAt(j) != txt.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i = i + 1;
                }
            }
        }

        return response;
    }
}