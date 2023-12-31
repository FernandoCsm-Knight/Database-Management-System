/**
 * LZWCompressor class that implements Lempel-Ziv-Welch compression algorithm.
 * 
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */

package crud.core.compress;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LZWCompressor class that implements Lempel-Ziv-Welch compression algorithm.
 */
public class LZWCompressor {

    /**
     * Compresses the input file using the LZW algorithm and writes the compressed data to the output file.
     * 
     * @param inputFilePath Path of the input file to be compressed
     * @param outputFilePath Path where the compressed output will be written
     * @throws IOException If an I/O error occurs
     */
     public void compressFile(String inputFilePath, String outputFilePath) throws IOException {
        byte[] inputBytes = Files.readAllBytes(Paths.get(inputFilePath));
        List<Integer> compressed = compress(inputBytes);

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilePath)))) {
            for (int code : compressed) {
                out.writeInt(code);
            }
        }
    }

    /**
     * Compresses the byte array using the LZW algorithm.
     * 
     * @param inputBytes Array of bytes to be compressed
     * @return List of integers representing the compressed data
     */
    private List<Integer> compress(byte[] inputBytes) {
        int dictSize = 256;
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }

        String w = "";
        List<Integer> result = new ArrayList<>();
        for (byte inputByte : inputBytes) {
            String wc = w + (char) (inputByte & 0xFF);
            if (dictionary.containsKey(wc)) {
                w = wc;
            } else {
                result.add(dictionary.get(w));
                dictionary.put(wc, dictSize++);
                w = "" + (char) (inputByte & 0xFF);
            }
        }

        if (!w.isEmpty()) {
            result.add(dictionary.get(w));
        }
        return result;
    }
}
