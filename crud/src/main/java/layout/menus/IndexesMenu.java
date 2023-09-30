package layout.menus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import components.Show;
import crud.CRUD;
import crud.base.StructureValidation;
import crud.core.types.IndexType;
import layout.components.MenuResponse;
import layout.components.MenuSpecification;
import logic.SystemSpecification;
import utils.helpers.WatchTime;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class IndexesMenu implements SystemSpecification, MenuSpecification {

    private final WatchTime watch = new WatchTime("IndexesMenu");
    private final BufferedReader br;
    private CRUD<Show> show = null;
    private int option;

    public IndexesMenu(BufferedReader br) {
        this.br = br;

        File d = new File(configDirectory);
        if(!d.exists()) d.mkdir();

        File f = new File(configFilePath);
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {
                System.out.println("Erro ao criar arquivo de configuração: " + e.getMessage());
            }
        }
    }

    public MenuResponse<CRUD<Show>> build(CRUD<Show> currentCrud) throws Exception {
        long sum = 0;
        show = currentCrud;
        String indexName = "";
        
        do {
            long execution = 0;

            System.out.println("\nMenu Índices - Escolha o Tipo de Índice que Deseja Adicionar:");
            System.out.println("1. Árvore B+ / \"id\"");
            System.out.println("2. Hash / \"id\"");
            System.out.println("3. Lista Invertida / \"title\"");
            System.out.println("4. Listar índices selecionados");
            System.out.println("5. Remover índices");
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
                    if(!show.getIndexTypes().contains(IndexType.Hash)) {                        
                        execution = this.createIndex(IndexType.BPlusTree);
                        indexName = "_BPlusTree";
                    } else System.out.println("O índice B+ não pode ser criado pois já existe outro índice para este atributo.");
                    break;

                case 2:
                    if(!show.getIndexTypes().contains(IndexType.BPlusTree)) {
                        execution = this.createIndex(IndexType.Hash);
                        indexName = "_Hash";
                    } else System.out.println("O índice Hash não pode ser criado pois já existe outro índice para este atributo.");
                    break;
                
                case 3:
                    execution = this.createIndex(IndexType.InvertedIndex);
                    indexName = "_InvertedIndex";
                    break;

                case 4:
                    ArrayList<IndexType> arr = show.getIndexTypes();
                    for(IndexType indexType : IndexType.values()) {
                        if(arr.contains(indexType)) System.out.print("[*] ");
                        else System.out.print("[ ] ");

                        System.out.println(indexType);
                    }
                    break;

                case 5:
                    show = new CRUD<Show>("shows.db", Show.class.getConstructor());
                    StructureValidation.cleanIndexesDirectory();
                    StructureValidation.cleanJsonIndexesDirectory();    

                    BufferedWriter bw = new BufferedWriter(new FileWriter(configFilePath));
                    bw.write("indexes=");
                    bw.close();
                    
                    System.out.println("Índices removidos com sucesso!");
                    break;

                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;

                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }

            if(execution > 0) {
                System.out.println("Índice criado em \"" + INDEXES_FILES_DIRECTORY + "shows" + indexName + ".db\".");
                System.out.println("Tempo de execução: " + execution + "ms.");
                sum += execution;
            }

        } while(option != 0);

        return new MenuResponse<CRUD<Show>>(true, sum, show);
    }

    private long createIndex(IndexType type) throws Exception {
        long result = -1;
        ArrayList<IndexType> arr = show.getIndexTypes();

        IndexType[] params = null;
        if(!arr.contains(type)) {
            arr.add(type);
            params = new IndexType[arr.size()];
            arr.toArray(params);
            System.out.println("Criando índice \"" + type + "\"...");

            watch.reset();
            watch.start();
            show = new CRUD<Show>("shows.db", Show.class.getConstructor(), params);
            show.rebuildIndex(type);
            result = watch.stop();

            BufferedWriter bw = new BufferedWriter(new FileWriter(configFilePath));
            bw.write("indexes=");

            for(int i = 0; i < params.length; i++) {
                bw.write(params[i].name());
                if(i != params.length - 1) bw.write(",");
            }

            bw.close();
        } else System.out.println("Índice já existente!");

        return result;
    }

}
