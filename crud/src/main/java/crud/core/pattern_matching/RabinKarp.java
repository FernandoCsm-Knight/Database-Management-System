/**
 * RabinKarp class extends Matcher and implements the Rabin-Karp string search algorithm.
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

package crud.core.pattern_matching;

/**
 * RabinKarp class extends Matcher and implements the Rabin-Karp string search algorithm.
 */
public class RabinKarp extends Matcher {
    private int prime = 101; // Prime number for hash calculation
    private int patternHash;
    private String pattern;
    private int comp = 0;
    private boolean displayed = false;

    /**
     * Constructor for RabinKarp class, initializes the pattern and its hash value.
     * 
     * @param pattern The pattern string for search
     */
    public RabinKarp(String pattern) {
        this.pattern = pattern;
        patternHash = hash(pattern);
    }

    /**
     * Searches for the given pattern in the input text using the Rabin-Karp algorithm.
     * 
     * @param text The text to search for the pattern
     * @return The index of the first occurrence of the pattern in the text, or -1 if not found
     */
    @Override
    public int search(String text) {
        if (pattern.length() > text.length()) return -1;

        int textHash = hash(text.substring(0, pattern.length()));
    
        for (int i = 0; i <= text.length() - pattern.length(); i++) {
            if(!displayed) comp += 2;
            if (textHash == patternHash) {
                comp += pattern.length();
                if(text.substring(i, i + pattern.length()).equals(pattern)) {
                    if(!displayed) {
                        System.out.println("Comparações: " + comp);
                        displayed = true;
                    }
                    return i;
                }
            }
    
            if (i < text.length() - pattern.length()) 
                textHash = rehash(text, textHash, i);
        }
        return -1;
    }

    /**
     * Generates a hash value for a string using Rabin-Karp hash function.
     * 
     * @param str The input string to calculate the hash
     * @return The hash value of the string
     */
    private int hash(String str) {
        int hash = 0;
        for (int i = 0; i < pattern.length(); i++) {
            hash += str.charAt(i) * Math.pow(prime, i);
        }
        return hash;
    }

    /**
     * Updates the hash value based on the next substring in the text.
     * 
     * @param text The input text
     * @param oldHash The previous hash value
     * @param index The index of the substring to rehash
     * @return The updated hash value after rehashing
     */
    private int rehash(String text, int oldHash, int index) {
        int newHash = oldHash - text.charAt(index);
        newHash = newHash / prime;
        newHash += text.charAt(index + pattern.length()) * Math.pow(prime, pattern.length() - 1);
        return newHash;
    }
}
