package layout.menus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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
public class MainMenu implements AutoCloseable, SystemSpecification, MenuSpecification {
    private final WatchTime watch = new WatchTime("MainMenu");
    private final BufferedReader br;
    private CRUD<Show> show = null;
    private int option;

    public MainMenu() throws Exception {
        br = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader tmpBr = new BufferedReader(new FileReader(configFilePath));

        String line = tmpBr.readLine();
        while(line != null && !line.startsWith("indexes=")) 
            line = tmpBr.readLine();

        if(line != null && line.substring(8).trim().length() > 0) {
            line = line.substring(8);
            String[] strs = line.split(",");

            IndexType[] params = new IndexType[strs.length];
            for(int i = 0; i < strs.length; i++) 
                params[i] = IndexType.valueOf(strs[i]);

            show = new CRUD<Show>("shows.db", Show.class.getConstructor(), params);
        } else show = new CRUD<Show>("shows.db", Show.class.getConstructor());

        tmpBr.close();
    }

    public void build() throws Exception {
        do {
            System.out.println("\nMenu Principal - Netflix Shows");
            System.out.println("1. Carregar base de dados");
            System.out.println("2. Selecionar tipos de índice");
            System.out.println("3. Operações CRUD");
            System.out.println("4. Ordenação Externa");
            System.out.println("5. Teste de performace");
            System.out.println("6. Remover todos os dados");
            System.out.println("0. Sair");
            System.out.print("Selecione uma opção: ");

            try {
                option = Integer.parseInt(br.readLine().trim());
            } catch(NumberFormatException e) {
                option = -1;
                System.out.println("Opção inválida! Tente novamente.");
                continue;
            }

            if(option >= 1 && option <= 6 && show == null) show = new CRUD<Show>("shows.db", Show.class.getConstructor());
            switch (option) {
                case 1:
                    try {
                        watch.reset();
                        watch.start();
                        show.populateAll("netflix_titles.csv");
                        System.out.println("Base de dados carregada com sucesso em " + watch.stop() + "ms.");
                       
                        show.toJsonFile();
                        System.out.println("Para visualizar os dados acesse: " + JSON_FILES_DIRECTORY + "shows_.json");
                    } catch (IOException e) {
                        System.out.println("Erro ao carregar a base de dados: " + e.getMessage());
                    }
                    break;
                case 2:
                    MenuResponse<CRUD<Show>> response = new IndexesMenu(br).build(show);
                    show = response.body;

                    System.out.println("Tempo de execução para a criação dos índices solicitados: " + response.executionTime + "ms.");
                    break;

                case 3:
                    new CrudMenu(br).build(show);
                    break;

                case 4:
                    new SortMenu(br).build(show);
                    break;

                case 5:
                    if(show == null || show.count() == 0) {
                        System.out.println("Tempo de execução para a leitura de " + 0 + " registros: " + 0 + "ms.");
                    } else {
                        int range = show.count();
                        if(show.count() >= 2000) 
                            range = 2000;
                    
                        watch.reset();
                        watch.start();
                        for(int i = 0; i < range; i++) 
                            show.read("id", i + 1);
    
                        System.out.println("Tempo de execução para a leitura de " + range + " registros: " + watch.stop() + "ms.");
                    }
                    
                    break;

                case 6:
                    show = null;
                    StructureValidation.cleanIndexesDirectory();
                    StructureValidation.cleanJsonIndexesDirectory();
                    StructureValidation.cleanDatabaseFiles();
                    StructureValidation.cleanJsonDatabaseFiles();
                    
                    BufferedWriter bw = new BufferedWriter(new FileWriter(configFilePath));
                    bw.write("indexes=");
                    bw.close();
                    break;

                case 0:
                    System.out.println("Saindo do programa...");
                    break;
                
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        } while (option != 0);
    }

    // Override

    @Override
    public void close() throws Exception {
        br.close();
    }
}