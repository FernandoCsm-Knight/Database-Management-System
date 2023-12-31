/**
 * HuffmanDecompressor class that implements a huffman decompressor.
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

package crud.core.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * HuffmanDecompressor class that implements a huffman decompressor.
 */
public class HuffmanDecompressor {

    /**
     * Inner class representing a node in the Huffman tree for decompression.
     */
    private static class Node {
        final byte data;
        final int frequency;
        Node leftChild, rightChild;

        /**
         * Constructor for a leaf node.
         *
         * @param data Byte data
         * @param frequency Frequency of the byte data
         */
        Node(byte data, int frequency) {
            this.data = data;
            this.frequency = frequency;
        }

        /**
         * Constructor for an internal node.
         * 
         * @param leftChild Left child node
         * @param rightChild Right child node
         */
        Node(Node leftChild, Node rightChild) {
            this.data = 0;
            this.frequency = leftChild.frequency + rightChild.frequency;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        /**
         * Checks if the node is a leaf in the tree.
         * 
         * @return True if the node is a leaf, otherwise false
         */
        boolean isLeaf() {
            return this.leftChild == null && this.rightChild == null;
        }
    }

    /**
     * Inner class representing a BitInputStream for reading individual bits.
     */
    private static class BitInputStream implements Closeable {
        private InputStream in;
        private int nextBits;
        private int bitsRemaining;

        /**
         * Constructor for BitInputStream.
         * 
         * @param in The InputStream to read the bits from
         */
        BitInputStream(InputStream in) {
            this.in = in;
        }

        /**
         * Reads a single bit (0 or 1) from the stream.
         * 
         * @return The bit value (0 or 1) read, or -1 if end of stream is reached
         * @throws IOException If an I/O error occurs
         */
        public int readBit() throws IOException {
            if (bitsRemaining == 0) {
                nextBits = in.read();
                if (nextBits == -1) {
                    return -1;
                }
                bitsRemaining = 8;
            }
            bitsRemaining--;
            return (nextBits >>> bitsRemaining) & 1;
        }

        /**
         * Reads a byte from the stream by assembling bits.
         * 
         * @return The byte value read, or -1 if end of stream is reached
         * @throws IOException If an I/O error occurs
         */
        public int readByte() throws IOException {
            int byteVal = 0;
            int bit;
            int bitsRead = 0;
            while (bitsRead < 8 && (bit = readBit()) != -1) {
                byteVal = (byteVal << 1) | bit;
                bitsRead++;
            }
            return bitsRead > 0 ? byteVal : -1;
        }

        /**
         * Closes the stream, releasing any system resources associated with it.
         * 
         * @throws IOException If an I/O error occurs
         */
        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    /**
     * Decompresses the input file using Huffman decoding and writes the decompressed data to the output file.
     * 
     * @param inputFilePath Path of the input file to be decompressed
     * @param outputFilePath Path where the decompressed output will be written
     * @throws IOException If an I/O error occurs
     */
    public void decompressFile(String inputFilePath, String outputFilePath) throws IOException {
        try (BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFilePath)));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFilePath))) {
            Node root = readTree(in);
            Node current = root;
            while (true) {
                int bit = in.readBit();
                if (bit == -1) break;
                current = (bit == 0) ? current.leftChild : current.rightChild;
                if (current.isLeaf()) {
                    out.write(current.data);
                    current = root;
                }
            }
        }
    }

    /**
     * Recursively reads the Huffman tree structure from the BitInputStream.
     * 
     * @param in The BitInputStream to read the tree from
     * @return Root node of the reconstructed Huffman tree
     * @throws IOException If an I/O error occurs
     */
    private Node readTree(BitInputStream in) throws IOException {
        int bit = in.readBit();
        if (bit == 1) {
            return new Node((byte) in.readByte(), -1);
        } else {
            Node leftChild = readTree(in);
            Node rightChild = readTree(in);
            return new Node(leftChild, rightChild);
        }
    }

    /**
     * Compares two files to check if their content is identical.
     * 
     * @param filePath1 Path of the first file for comparison
     * @param filePath2 Path of the second file for comparison
     * @return True if the files have identical content, otherwise false
     * @throws IOException If an I/O error occurs
     */
    public boolean compareFiles(String filePath1, String filePath2) throws IOException {
        try (InputStream in1 = new BufferedInputStream(new FileInputStream(filePath1));
             InputStream in2 = new BufferedInputStream(new FileInputStream(filePath2))) {
            int byte1 = in1.read();
            int byte2 = in2.read();
            while (byte1 != -1 || byte2 != -1) {
                if (byte1 != byte2) {
                    return false;
                }
                byte1 = in1.read();
                byte2 = in2.read();
            }
            return true;
        }
    }
}