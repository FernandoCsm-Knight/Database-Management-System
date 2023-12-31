/**
 * HuffmanCompressor class that implements Huffman coding for file compression.
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

package crud.core.compress;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * HuffmanCompressor class that implements Huffman coding for file compression.
 */
public class HuffmanCompressor {

    /**
     * Inner class representing a node in the Huffman tree.
     */
    private static class Node {
        byte data;
        int frequency;
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
            this.frequency = leftChild.frequency + rightChild.frequency;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        /**
         * Checks if the node is a leaf in the tree.
         * @return True if the node is a leaf, otherwise false
         */
        boolean isLeaf() {
            return this.leftChild == null && this.rightChild == null;
        }
    }

    /**
     * Builds a frequency map of byte occurrences in the file content.
     * 
     * @param fileContent Content of the file in byte array
     * @return A map of bytes to their frequencies
     */
    private Map<Byte, Integer> buildFrequencyMap(byte[] fileContent) {
        Map<Byte, Integer> frequencyMap = new HashMap<>();
        for (byte b : fileContent) {
            frequencyMap.put(b, frequencyMap.getOrDefault(b, 0) + 1);
        }
        return frequencyMap;
    }

    /**
     * Constructs the Huffman tree based on the frequency map.
     * 
     * @param frequencyMap Map of byte frequencies
     * @return Root node of the constructed Huffman tree
     */
    private Node buildTree(Map<Byte, Integer> frequencyMap) {
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(n -> n.frequency));
        frequencyMap.forEach((data, frequency) -> priorityQueue.add(new Node(data, frequency)));

        while (priorityQueue.size() > 1) {
            Node left = priorityQueue.poll();
            Node right = priorityQueue.poll();
            Node parent = new Node(left, right);
            priorityQueue.add(parent);
        }

        return priorityQueue.poll();
    }

    /**
     * Recursively builds a map of bytes to their Huffman codes.
     * 
     * @param codeMap Map to store byte-to-Huffman-code mappings
     * @param node Current node in the Huffman tree
     * @param code Current code being constructed
     */
    private void buildCodeMap(Map<Byte, String> codeMap, Node node, String code) {
        if (node.isLeaf()) {
            codeMap.put(node.data, code);
        } else {
            buildCodeMap(codeMap, node.leftChild, code + "0");
            buildCodeMap(codeMap, node.rightChild, code + "1");
        }
    }

    /**
     * Compresses the input file using Huffman coding and writes the compressed data to the output file.
     * 
     * @param inputFilePath Path of the input file to be compressed
     * @param outputFilePath Path where the compressed output will be written
     * @throws IOException If an I/O error occurs
     */
    public void compressFile(String inputFilePath, String outputFilePath) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(inputFilePath));
        Map<Byte, Integer> frequencyMap = buildFrequencyMap(fileContent);
        Node root = buildTree(frequencyMap);
        Map<Byte, String> codeMap = new HashMap<>();
        buildCodeMap(codeMap, root, "");

        try (BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilePath)))) {
            writeTree(root, out);
            for (byte b : fileContent) {
                String code = codeMap.get(b);
                for (char c : code.toCharArray()) {
                    out.writeBit(c == '1' ? 1 : 0);
                }
            }
        }
    }


    /**
     * Writes the Huffman tree structure into the BitOutputStream.
     * 
     * @param node Current node in the Huffman tree
     * @param out BitOutputStream to write the tree structure
     * @throws IOException If an I/O error occurs
     */
    private void writeTree(Node node, BitOutputStream out) throws IOException {
        if (node.isLeaf()) {
            out.writeBit(1);
            out.writeByte(node.data);
        } else {
            out.writeBit(0);
            writeTree(node.leftChild, out);
            writeTree(node.rightChild, out);
        }
    }

    /**
     * Inner class representing a BitOutputStream for writing individual bits.
     */
    private static class BitOutputStream implements Closeable {
        private OutputStream out;
        private int currentByte;
        private int numBitsFilled;

        /**
         * Constructor for BitOutputStream.
         * 
         * @param out The OutputStream to write the bits to
         */
        public BitOutputStream(OutputStream out) {
            this.out = out;
        }

        /**
         * Writes a single bit (0 or 1) to the stream.
         * 
         * @param bit The bit value (0 or 1) to be written
         * @throws IOException If an I/O error occurs
         */
        public void writeBit(int bit) throws IOException {
            if (bit != 0 && bit != 1)
                throw new IllegalArgumentException("Argument must be 0 or 1");
            currentByte = (currentByte << 1) | bit;
            numBitsFilled++;
            if (numBitsFilled == 8) {
                out.write(currentByte);
                numBitsFilled = 0;
            }
        }

        /**
         * Writes a byte to the stream.
         * 
         * @param b The byte value to be written
         * @throws IOException If an I/O error occurs
         */
        public void writeByte(byte b) throws IOException {
            if (numBitsFilled == 0) {
                out.write(b);
            } else {
                for (int i = 7; i >= 0; i--) {
                    writeBit((b >>> i) & 1);
                }
            }
        }

        @Override
        public void close() throws IOException {
            while (numBitsFilled != 0) {
                writeBit(0);
            }
            out.close();
        }
    }
}

