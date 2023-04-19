package logic;

/**
 * The {@code Logic} class contains utility methods for performing various mathematical calculations.
 * 
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 */
public class Logic {
    
    /**
     * The {@code math} class contains several utility methods for performing various mathematical calculations.
     */
    public static class math {
        
        /**
         * Calculates the logarithm of a {@code double} number to the base 2.
         * @param num The number to calculate the logarithm of.
         * @return The logarithm of the number to the base 2.
         */
        public static double log2(double num) {
            return Math.log(num)/Math.log(2);
        }
    } 

}
