package layout.menus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import components.Show;
import crud.CRUD;
import crud.base.StructureValidation;
import crud.core.types.PatternMatchingType;
import logic.SystemSpecification;
import utils.helpers.WatchTime;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class MatchingMenu implements SystemSpecification {
    private final BufferedReader br;
    private CRUD<Show> show = null;
    private int option;
    
    public MatchingMenu(BufferedReader br) {
        this.br = br;
        StructureValidation.createJSONPatternMatchingDirectory();
    }
    
    
    public void build(CRUD<Show> currCrud) throws Exception {
        show = currCrud;
        String pattern = null;
        ArrayList<Show> shows = null;
        WatchTime watch = new WatchTime();
        long executionTime = 0;

        do {
            System.out.println("\nMenu Busca - Netflix Shows");
            System.out.println("Selecione o algoritmo de casamento de padrões que deseja realizar: ");
            System.out.println("1. KMP");
            System.out.println("2. Boyer Moore");
            System.out.println("3. Rabin Karp");
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
                    pattern = query();

                    if(pattern != null && pattern.length() > 0) {
                        System.out.println("\nRealizando busca com KMP...");
                        watch.reset();
                        watch.start();
                        shows = show.match(PatternMatchingType.KMP, pattern.trim(), "netflix_titles.csv");
                        executionTime = watch.stop();
                        System.out.println("Busca realizada com sucesso!");
                        System.out.println("Tempo de execução: " + executionTime + "ms");
                        
                        if(shows != null && shows.size() > 0) {
                            System.out.println("Número de instâncias encontradas: " + shows.size());
                            BufferedWriter bw = new BufferedWriter(new FileWriter(JSON_PATTERN_MATCHING_DIRECTORY + "response_kmp.json"));

                            bw.write("[\n");
                            
                            while(shows.size() > 0) 
                                bw.write(shows.remove(0).toString() + (shows.size() > 0 ? ",\n" : "\n"));
                            
                            bw.write("]");
                            bw.close();
                        } else System.out.println("Nenhum resultado encontrado!");

                        System.out.println("Para visualizar os dados acesse: " + JSON_PATTERN_MATCHING_DIRECTORY + "response_kmp.json");
                    } else System.out.println("Nenhum resultado encontrado!");

                    break;

                case 2:
                    pattern = query();

                    if(pattern != null && pattern.length() > 0) {
                        System.out.println("\nRealizando busca com Boyer Moore...");
                        watch.reset();
                        watch.start();
                        shows = show.match(PatternMatchingType.BoyerMoore, pattern.trim(), "netflix_titles.csv");
                        executionTime = watch.stop();
                        System.out.println("Busca realizada com sucesso!");
                        System.out.println("Tempo de execução: " + executionTime + "ms");
                        
                        if(shows != null && shows.size() > 0) {
                            System.out.println("Número de instâncias encontradas: " + shows.size());
                            BufferedWriter bw = new BufferedWriter(new FileWriter(JSON_PATTERN_MATCHING_DIRECTORY + "response_boyermoore.json"));

                            bw.write("[\n");
                            
                            while(shows.size() > 0) 
                                bw.write(shows.remove(0).toString() + (shows.size() > 0 ? ",\n" : "\n"));
                            
                            bw.write("]");
                            bw.close();
                        } else System.out.println("Nenhum resultado encontrado!");

                        System.out.println("Para visualizar os dados acesse: " + JSON_PATTERN_MATCHING_DIRECTORY + "response_boyermoore.json");
                    } else System.out.println("Nenhum resultado encontrado!");
        
                    break;

                case 3:
                    pattern = query();

                    if(pattern != null && pattern.length() > 0) {
                        System.out.println("\nRealizando busca com Rabin Karp...");
                        watch.reset();
                        watch.start();
                        shows = show.match(PatternMatchingType.RabinKarp, pattern.trim(), "netflix_titles.csv");
                        executionTime = watch.stop();
                        System.out.println("Busca realizada com sucesso!");
                        System.out.println("Tempo de execução: " + executionTime + "ms");
                        
                        if(shows != null && shows.size() > 0) {
                            System.out.println("Número de instâncias encontradas: " + shows.size());
                            BufferedWriter bw = new BufferedWriter(new FileWriter(JSON_PATTERN_MATCHING_DIRECTORY + "response_rabinkarp.json"));

                            bw.write("[\n");
                            
                            while(shows.size() > 0) 
                                bw.write(shows.remove(0).toString() + (shows.size() > 0 ? ",\n" : "\n"));
                            
                            bw.write("]");
                            bw.close();
                        } else System.out.println("Nenhum resultado encontrado!");

                        System.out.println("Para visualizar os dados acesse: " + JSON_PATTERN_MATCHING_DIRECTORY + "response_rabinkarp.json");
                    } else System.out.println("Nenhum resultado encontrado!");
        
                    break;

                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }

        } while (option != 0);
    }

    private String query() {
        String pattern = null;
    
        System.out.println("Digite o padrão que deseja buscar: ");
        try {
            pattern = br.readLine();
        } catch (Exception e) {
            pattern = null;
            System.out.println("Erro ao ler padrão: " + e.getMessage());
        }

        if(pattern.toLowerCase().equals("exit"))
            pattern = null;

        return pattern;
    }
}
