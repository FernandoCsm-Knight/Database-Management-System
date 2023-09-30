package layout.menus;

import java.io.BufferedReader;
import java.util.ArrayList;

import components.Show;
import crud.CRUD;
import crud.core.types.SortType;
import logic.Logic;
import logic.SystemSpecification;
import utils.helpers.WatchTime;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class SortMenu implements SystemSpecification {
    
    private final WatchTime watch = new WatchTime("SortMenu");
    private final BufferedReader br;
    ArrayList<String> types = null;
    private CRUD<Show> show = null;
    private int option;

    private static class Query {
        public String field;
        public boolean stop;

        public Query(String field, boolean stop) {
            this.field = field;
            this.stop = stop;
        }
    }

    public SortMenu(BufferedReader br) {
        this.br = br;

        String[] strs =  {"id", "type", "title", "directors", "dateAdded", "releaseYear", "duration", "listedIn", "description"};
        ArrayList<String> list = new ArrayList<>();
        for(String type : strs) 
            list.add(type);
        
        this.types = list;
    }

    public void build(CRUD<Show> currentCrud) throws Exception {
        show = currentCrud;

        do {
            System.out.println("\nMenu de Ordenação Externa");
            System.out.println("1. Intercalação balanceada com blocos de tamanho fixo");
            System.out.println("2. Intercalação balanceada com blocos de tamanho variável");
            System.out.println("3. Intercalação balanceada com seleção por substituição");
            System.out.println("0. Voltar ao menu principal");
            System.out.println();
            System.out.println("Para aqueles interessados em acompanhar o processo,");
            System.out.println("os arquivos temporários apareceram em: " + TEMPORARY_FILES_DIRECTORY);
            System.out.println();
            System.out.print("Selecione uma opção: ");
            
            try {
                option = Integer.parseInt(br.readLine().trim());
            } catch(NumberFormatException e) {
                option = -1;
                System.out.println("Opção inválida! Tente novamente.");
                continue;
            }

            if(option == 0) {
                System.out.println("Voltando ao menu principal...");
                break;
            }

            Query query = manageQueryField();
            
            if(!query.stop) {
                String field = query.field;

                int op = 0;
                do {
                    System.out.println("Deseja alterar as configurações de ordenação?");
                    System.out.println("1. Alterar");
                    System.out.println("2. Manter original (fator de bloco do registro / caminhos = 3)");
                    System.out.print("Selecione uma opção: ");

                    try {
                        op = Integer.parseInt(br.readLine().trim());
                    } catch(NumberFormatException e) {
                        op = -1;
                        System.out.println("Opção inválida! Tente novamente.");
                        continue;
                    }

                    if(op != 1 && op != 2) System.out.println("Opção inválida! Tente novamente.");
                } while(op != 1 && op != 2);

                if(op == 1) {
                    boolean b = false;
                    
                    do {
                        try {
                            System.out.println("Digite abaixo as configurações de ordenação:");
                            System.out.print("Número de registros por bloco: ");
                            int blockSize = Integer.parseInt(br.readLine().trim());
        
                            System.out.print("Número de caminhos para a ordenação: ");
                            int paths = Integer.parseInt(br.readLine().trim());

                            
                            if(blockSize <= 1 || paths <= 1) {
                                System.out.println("Erro ao ler os dados (número de registros e caminhos deve ser maior que 1). Tente novamente.");
                                b = true;
                            } else {
                                show.setSortConfig(paths, blockSize);
                                b = false;
                            }
                        } catch(NumberFormatException e) {
                            b = true;
                            System.out.println("Erro ao ler os dados. Tente novamente.");
                        }
                    } while(b);
                } else if(op == 2) show.setSortConfig(3, Logic.database.blockFactor(Show.MAX_REGISTER_SIZE));

                switch (option) {
                    case 1:
                        try {
                            watch.reset();
                            watch.start();
                            if (show.orderBy(field, SortType.FixedBlocks)) {
                                System.out.println("Ordenação concluída com sucesso em " + watch.stop() + "ms.");
                            } else {
                                watch.stop();
                                System.out.println("Erro ao ordenar os dados.");
                            }
                        } catch (Exception e) {
                            System.out.println("Erro ao ordenar os dados: " + e.getMessage());
                        }
                        break;

                    case 2:
                        try {
                            watch.reset();
                            watch.start();
                            if (show.orderBy(field, SortType.VariableBlocks)) {
                                System.out.println("Ordenação concluída com sucesso em " + watch.stop() + "ms.");
                            } else {
                                watch.stop();
                                System.out.println("Erro ao ordenar os dados.");
                            }
                        } catch (Exception e) {
                            System.out.println("Erro ao ordenar os dados: " + e.getMessage());
                        }
                        break;

                    case 3:
                        try {
                            watch.reset();
                            watch.start();
                            if (show.orderBy(field, SortType.Heap)) {
                                System.out.println("Ordenação concluída com sucesso em " + watch.stop() + "ms.");
                            } else {
                                watch.stop();
                                System.out.println("Erro ao ordenar os dados.");
                            }
                        } catch (Exception e) {
                            System.out.println("Erro ao ordenar os dados: " + e.getMessage());
                        }
                        break;
                
                    default:
                        System.out.println("Opção inválida! Tente novamente.");
                }
            }
        } while (option != 0);
    }

    private Query manageQueryField() throws Exception {
        System.out.println("Para realizar a ordenação da base de dados você pode \nescolher entre os seguintes campos:");
        System.out.println("[\n\tid, \n\ttype, \n\ttitle, \n\tdirectors, \n\tdateAdded, \n\treleaseYear, \n\tduration, \n\tlistedIn, \n\tdescription\n]");
        
        System.out.println("Informe qual campo deseja utilizar para a busca: ");
        String field = br.readLine().trim();
        boolean stop = false;

        try {
            int i = Integer.parseInt(field);

            switch(i) {
                case 0: field = "id"; break;
                case 1: field = "type"; break;
                case 2: field = "title"; break;
                case 3: field = "directors"; break;
                case 4: field = "dateAdded"; break;
                case 5: field = "releaseYear"; break;
                case 6: field = "duration"; break;
                case 7: field = "listedIn"; break;
                case 8: field = "description"; break;
                default: 
                    System.out.println("Opção inválida! Tente novamente."); 
                    stop = true;
            }
        } catch(NumberFormatException e) {}

        if(!types.contains(field)) {
            System.out.println("Opção inválida! Tente novamente.");
            stop = true;
        }

        return new Query(field, stop);
    }

}   
