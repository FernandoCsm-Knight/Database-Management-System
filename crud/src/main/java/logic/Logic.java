package logic;

/**
 * The {@code Logic} class contains utility methods for performing various
 * mathematical calculations.
 * 
 * @author Fernando Campos Silva Dal Maria & Bruno Santiago de Oliveira
 * @version 1.0.0
 */
public class Logic implements SystemSpecification {

    /**
     * The {@code math} class contains several utility methods for performing
     * various mathematical calculations.
     */
    public static class math {

        /**
         * Calculates the logarithm of a {@code double} number to the base 2.
         * 
         * @param num The number to calculate the logarithm of.
         * @return The logarithm of the number to the base 2.
         */
        public static double log2(double num) {
            return Math.log(num) / Math.log(2);
        }
    }

    /**
     * The {@code database} class contains several utility methods for performing
     * various database calculations.
     */
    public static class database {

        /**
         * Calculates the block factor of a {@code double} number that represents a
         * register.
         * 
         * @param registerSize The size of the register in bytes.
         * @return The block factor of the register.
         */
        public static int blockFactor(double registerSize) {
            return (int) Math.floor(BLOCK_SIZE / registerSize);
        }

        /**
         * Calculates the height of a B-Tree with the given number of nodes and order.
         * @param numberOfNodes The number of nodes in the Tree.
         * @param order The order of the Tree.
         * @return The height of the Tree.
         */
        public static int bTreeHeight(int numberOfNodes, int order) {
            if(numberOfNodes < 1 || order < 2)
                throw new IllegalArgumentException("The number of nodes must be greater than 0 and the order must be greater than 1.");

            return (int)(Math.log((numberOfNodes + 1) / 2) / Math.log(order));
        }
    }

    /**
     * The {@code compression} class contains several utility methods for performing
     * various compression calculations.
     */
    public static class compression {

        /**
         * Calculates the compression ratio of a {@code double} number that represents
         * 
         * @param originalSize The size of the original file in bytes.
         * @param compressedSize The size of the compressed file in bytes.
         * @return The compression ratio of the file.
         */
        public static double compressionRatio(double originalSize, double compressedSize) {
            return compressedSize / originalSize;
        }

        /**
         * Calculates the compression factor of a {@code double} number that represents
         * 
         * @param originalSize The size of the original file in bytes.
         * @param compressedSize The size of the compressed file inbytes.
         * @return The compression factor of the file.
         */
        public static double compressionFactor(double originalSize, double compressedSize) {
            return originalSize / compressedSize;
        }

        /**
         * Calculates the space savings of a {@code double} number that represents
         * 
         * @param originalSize The size of the original file in bytes.
         * @param compressedSize The size of the compressed file in bytes.
         * @return The space savings of the file.
         */
        public static double spaceSavings(double originalSize, double compressedSize) {
            return 1 - compressionRatio(originalSize, compressedSize);
        }

        /**
         * Calculates the compression gain of a {@code double} number that represents
         * 
         * @param originalSize The size of the original file in bytes.
         * @param compressedSize The size of the compressed file in bytes.
         * @return The compression gain of the file.
         */
        public static double compressionGain(double originalSize, double compressedSize) {
            return Math.log(compressionRatio(originalSize, compressedSize));
        }

    }

}
