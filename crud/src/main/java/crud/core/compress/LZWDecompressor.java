/**
 * LZWDecompressor class that implements Lempel-Ziv-Welch decompression algorithm.
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

package crud.core.compress;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LZWDecompressor class that implements Lempel-Ziv-Welch decompression algorithm.
 */
public class LZWDecompressor {

    /**
     * Decompresses the input file using the LZW algorithm and writes the decompressed data to the output file.
     * 
     * @param inputFilePath Path of the input file to be decompressed
     * @param outputFilePath Path where the decompressed output will be written
     * @throws IOException If an I/O error occurs
     */
    public void decompressFile(String inputFilePath, String outputFilePath) throws IOException {
        List<Integer> compressed = new ArrayList<>();
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFilePath)))) {
            while (in.available() > 0) {
                compressed.add(in.readInt());
            }
        }

        byte[] decompressed = decompress(compressed);
        Files.write(Paths.get(outputFilePath), decompressed);
    }

    /**
     * Decompresses the list of integers using the LZW algorithm.
     * 
     * @param compressed List of integers representing the compressed data
     * @return Array of bytes containing the decompressed data
     */
    private byte[] decompress(List<Integer> compressed) {
        int dictSize = 256;
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, "" + (char) i);
        }

        String w = "" + (char) (int) compressed.remove(0);
        StringBuffer result = new StringBuffer(w);
        for (int k : compressed) {
            String entry;
            if (dictionary.containsKey(k)) {
                entry = dictionary.get(k);
            } else if (k == dictSize) {
                entry = w + w.charAt(0);
            } else {
                throw new IllegalArgumentException("Bad compressed k: " + k);
            }

            result.append(entry);

            dictionary.put(dictSize++, w + entry.charAt(0));
            w = entry;
        }

        byte[] outputBytes = new byte[result.length()];
        for (int i = 0; i < outputBytes.length; i++) {
            outputBytes[i] = (byte) result.charAt(i);
        }
        return outputBytes;
    }
}
