package layout.menus;

import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import components.Show;
import crud.CRUD;
import layout.components.MenuCompressionResponse;
import logic.Logic;
import logic.SystemSpecification;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class BackupMenu implements SystemSpecification {
    private final BufferedReader br;
    private CRUD<Show> show = null;
    private int option;

    public BackupMenu(BufferedReader br) {
        this.br = br;
    }

    public void build(CRUD<Show> currCrud) throws Exception {
        show = currCrud;
        
        do {
            System.out.println("\nMenu Backup - Netflix Shows");
            System.out.println("1. Realizar backup");
            System.out.println("2. Restaurar backup");
            System.out.println("3. Deletar backup");
            System.out.println("0. Voltar ao menu principal");
            System.out.print("Selecione uma opção: ");

            try {
                option = Integer.parseInt(br.readLine().trim());
            } catch(NumberFormatException e) {
                option = -1;
                System.out.println("Opção inválida! Tente novamente.");
                continue;
            }

            switch (option) {
                case 1:
                    System.out.println("Realizando backup...");
                    MenuCompressionResponse response = show.compress();
                    System.out.println("Backup realizado com sucesso!\n");
                    System.out.println(response + "\n");

                    RandomAccessFile compressedf = new RandomAccessFile(HUFFMAN_FILES_DIRECTORY + response.fileName, "r");
                    long comp = compressedf.length();

                    System.out.print("Taxa de compressão Huffman: ");
                    System.out.println(Logic.compression.compressionRatio(show.length(), compressedf.length()));
                    System.out.print("Fator de compressão Huffman: ");
                    System.out.println(Logic.compression.compressionFactor(show.length(), compressedf.length()));
                    System.out.print("Percentual de redução Huffman: ");
                    System.out.println(100 * Logic.compression.spaceSavings(show.length(), compressedf.length()));
                    System.out.print("Ganho de compressão Huffman: ");
                    System.out.println(100 * Logic.compression.compressionGain(show.length(), compressedf.length()));
                    System.out.println("Tamanho do arquivo comprimido Huffman: " + compressedf.length() + " bytes\n");
                    compressedf.close();

                    compressedf = new RandomAccessFile(LZW_FILES_DIRECTORY + response.fileName, "r");

                    System.out.print("Taxa de compressão LZW: ");
                    System.out.println(Logic.compression.compressionRatio(show.length(), compressedf.length()));
                    System.out.print("Fator de compressão LZW: ");
                    System.out.println(Logic.compression.compressionFactor(show.length(), compressedf.length()));
                    System.out.print("Percentual de redução LZW: ");
                    System.out.println(100 * Logic.compression.spaceSavings(show.length(), compressedf.length()));
                    System.out.print("Ganho de compressão LZW: ");
                    System.out.println(100 * Logic.compression.compressionGain(show.length(), compressedf.length()));
                    System.out.println("Tamanho do arquivo comprimido LZW: " + compressedf.length() + " bytes\n");

                    System.out.println("O algoritmo " + (comp < compressedf.length() ? "Huffman" : "LZW") + " foi mais eficiente na compressão do arquivo.\n");
                    break;
                case 2:
                    String tmp = query();

                    if(tmp != null) {
                        System.out.println("Restaurando backup...");
                        MenuCompressionResponse res = show.decompress(tmp);
                        System.out.println("Backup restaurado com sucesso!\n");
                        System.out.println(res);
                    }
                    
                    break;

                case 3:
                    tmp = query();

                    if(tmp != null) {
                        System.out.println("Deletando backup...");
                        show.deleteBackup(tmp);
                        System.out.println("Backup deletado com sucesso!");
                    }
                    
                    break;
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while(option != 0);
    }

    private String query() throws Exception {
        ArrayList<String> paths = show.getBackupFilesNames();
        
        int op = -1;
        String res = null;

        if(paths.size() > 0) {
            do {
                System.out.println("\nSelecione um arquivo de backup: ");
                for(int i = 0; i < paths.size(); i++) System.out.println((i + 1) + ". " + paths.get(i));
                System.out.println("0. Voltar ao menu principal");
                System.out.print("Selecione uma opção: ");
    
                op = Integer.parseInt(br.readLine());
                if(op != 0 && op >= 1 && op <= paths.size()) {
                    res = paths.get(op - 1);
                    op = 0;
                } else if(op == 0) {
                    System.out.println("Voltando ao menu principal...");
                } else {
                    System.out.println("Opção inválida!");
                }  
            } while(op != 0);
        } else {
            System.out.println("Não há arquivos de backup!");
        }
     
        return res;
    }

}
