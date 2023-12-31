/**
 * Abstract class for string pattern matching algorithms.
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

package crud.core.pattern_matching;

/**
 * Abstract class for string pattern matching algorithms.
 */
public abstract class Matcher {

    /**
     * Abstract method to search for a pattern in a given text.
     * 
     * @param text The text to search for the pattern
     * @return The index of the first occurrence of the pattern in the text, or -1 if not found
     */
    abstract public int search(String text);
}
