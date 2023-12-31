package layout.components;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class MenuCompressionResponse {

    public final String fileName;
    public long timeHuffman = 0;
    public long timeLZW = 0;

    public MenuCompressionResponse(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        String s = "Arquivo: " + fileName + "\n";
        s += "Tempo de execução Huffman: " + timeHuffman + "ms\n";
        s += "Tempo de execução LZW: " + timeLZW + "ms";
        return s;
    }
}
