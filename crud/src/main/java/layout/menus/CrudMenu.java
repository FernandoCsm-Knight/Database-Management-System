package layout.menus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import components.Show;
import crud.CRUD;
import logic.Logic;
import logic.SystemSpecification;
import utils.helpers.WatchTime;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class CrudMenu implements SystemSpecification {

    private final WatchTime watch = new WatchTime("CrudMenu");
    private final BufferedReader br;
    ArrayList<String> types = null;
    private CRUD<Show> show = null;
    private int option;

    private static class Query {
        public String field;
        public String query;
        public boolean stop;

        public Query(String field, String query, boolean stop) {
            this.field = field;
            this.query = query;
            this.stop = stop;
        }
    }

    public CrudMenu(BufferedReader br) {
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
            System.out.println("\nMenu CRUD");
            System.out.println("1. Criar um registro");
            System.out.println("2. Ler um registro");
            System.out.println("3. Atualizar um registro");
            System.out.println("4. Deletar um registro");
            System.out.println("5. Visualizar registros");
            System.out.println("6. Informações sobre a base");
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
                    System.out.println("Informe os dados para criar o registro: ");

                    try {
                        Show s = new Show();
                        System.out.print("Tipo do show: ");
                        s.setType(br.readLine().trim());

                        System.out.print("Título do show: ");
                        s.setTitle(br.readLine().trim());

                        System.out.print("Diretores do show: ");
                        String directorsCreate = br.readLine().trim();
                        directorsCreate.replace("[", "");
                        directorsCreate.replace("]", "");
                        directorsCreate.replaceAll("\"", "");
                        s.setDirectors(directorsCreate);

                        s.setDateAdded(new Date());

                        System.out.print("Ano de lançamento: ");
                        s.setReleaseYear(Short.parseShort(br.readLine().trim()));

                        System.out.print("Duração do show: ");
                        s.setDuration(br.readLine().trim());

                        System.out.print("Lista de gêneros: ");
                        s.setListedIn(br.readLine().trim());

                        System.out.print("Descrição do show: ");
                        s.setDescription(br.readLine().trim());

                        watch.reset();
                        watch.start();
                        if(show.create(s)) {
                            System.out.println("Registro criado com sucesso em " + watch.stop() + "ms.");
                        } else {
                            watch.stop();
                            System.out.println("Erro ao criar registro.");
                        }
                    } catch (Exception e) {
                        System.out.println("Erro ao criar o registro: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 2:
                    Query qRead = manageQueryField();
                    if(qRead.stop) break;

                    String fieldRead = qRead.field;
                    String queryRead = qRead.query;

                    try {
                        Show record = null;

                        watch.reset();
                        watch.start();
                        if(fieldRead.equals("id")) record = show.read(fieldRead, (Integer)Integer.parseInt(queryRead));
                        else if(fieldRead.equals("releaseYear")) record = show.read(fieldRead, (Short)Short.parseShort(queryRead));
                        else record = show.read(fieldRead, queryRead);

                        System.out.println("Registro lido: " + record);
                        System.out.println("O registro foi lido em " + watch.stop() + "ms.");
                    } catch (Exception e) {
                        watch.stop();
                        System.out.println("Erro ao ler registro: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 3:
                    Query qUpdate = manageQueryField();
                    if(qUpdate.stop) break;

                    String fieldUpdate = qUpdate.field;
                    String queryUpdate = qUpdate.query;

                    try {
                        Show response = null;

                        if(fieldUpdate.equals("id")) response = show.read(fieldUpdate, (Integer)Integer.parseInt(queryUpdate));
                        else if(fieldUpdate.equals("releaseYear")) response = show.read(fieldUpdate, (Short)Short.parseShort(queryUpdate));
                        else response = show.read(fieldUpdate, queryUpdate);

                        if(response != null) {
                            System.out.println("Registro encontrado. Informe as atualizações:");

                            Show updatedShow = new Show();

                            System.out.print("Novo tipo do show: ");
                            updatedShow.setType(br.readLine().trim());

                            System.out.print("Novo título do show: ");
                            updatedShow.setTitle(br.readLine().trim());

                            System.out.print("Novos diretores do show: ");
                            String directorsUpdate = br.readLine().trim();
                            directorsUpdate.replace("[", "");
                            directorsUpdate.replace("]", "");
                            directorsUpdate.replaceAll("\"", "");
                            updatedShow.setDirectors(directorsUpdate);

                            System.out.print("Nova data de adição (dd/MM/yyyy): ");
                            String dateStr = br.readLine().trim();
                            Date newDate = updatedShow.dateParser(dateStr);
                            updatedShow.setDateAdded(newDate);

                            System.out.print("Novo ano de lançamento: ");
                            updatedShow.setReleaseYear(Short.parseShort(br.readLine().trim()));

                            System.out.print("Nova duração do show: ");
                            updatedShow.setDuration(br.readLine().trim());

                            System.out.print("Nova lista de gêneros: ");
                            updatedShow.setListedIn(br.readLine().trim());

                            System.out.print("Nova descrição do show: ");
                            updatedShow.setDescription(br.readLine().trim());

                            watch.reset();
                            watch.start();
                            if(show.update(response.getId(), updatedShow)) {
                                System.out.println("Registro atualizado com sucesso em " + watch.stop() + "ms.");
                            } else {
                                watch.stop();
                                System.out.println("Erro ao atualizar registro.");
                            }
                        } else {
                            System.out.println("Registro com o \"" + fieldUpdate + "\" informado não encontrado.");
                        }
                    } catch (Exception e) {
                        System.out.println("Erro ao atualizar registro: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 4:
                    Query qDelete = manageQueryField();
                    if(qDelete.stop) break;

                    String fieldDelete = qDelete.field;
                    String queryDelete = qDelete.query;
                    
                    try {
                        Show response = null;

                        if(fieldDelete.equals("id")) response = show.read(fieldDelete, (Integer)Integer.parseInt(queryDelete));
                        else if(fieldDelete.equals("releaseYear")) response = show.read(fieldDelete, (Short)Short.parseShort(queryDelete));
                        else response = show.read(fieldDelete, queryDelete);

                        watch.reset();
                        watch.start();
                        if(show.delete(response.getId())) {
                            System.out.println("Registro deletado com sucesso em " + watch.stop() + "ms.");
                        } else {
                            watch.stop();
                            System.out.println("Erro ao deletar registro.");
                        }
                    } catch (IOException e) {
                        System.out.println("Erro ao deletar registro: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("Aguarde enquanto os registros são carregados...");
                    show.toJsonFile();
                    System.out.println("Acesse a pasta \"" + JSON_FILES_DIRECTORY + "\" para visualizar os registros.");
                    System.out.println("Para visualizar os índices acesse a pasta \"" + JSON_INDEXES_DIRECTORY + "\".");
                    break;
                
                case 6:
                    System.out.println("Informações sobre a base de dados:");
                    System.out.println("Número de registros: " + show.count());
                    System.out.println("Número de registros deletados: " + show.countTrash());
                    System.out.println("Tamanho máximo para um registro: " + Show.MAX_REGISTER_SIZE + " bytes");
                    System.out.println("Fator de bloco: " + Logic.database.blockFactor(Show.MAX_REGISTER_SIZE));
                    System.out.println("Índices utilizados: " + show.getIndexTypes());
                    break;

                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;
                
                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        } while (option != 0);
    }

    private Query manageQueryField() throws Exception {
        System.out.println("Para realizar a busca na base de dados você pode \nescolher entre os seguintes campos:");
        System.out.println("[\n\tid, \n\ttype, \n\ttitle, \n\tdirectors, \n\tdateAdded, \n\treleaseYear, \n\tduration, \n\tlistedIn, \n\tdescription\n]");
        
        System.out.println("Informe qual campo deseja utilizar para a busca: ");
        String field = br.readLine().trim();
        
        boolean stop = false;
        String query = null;
        
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

        if(!stop) {
            System.out.print("Informe o \"" + field + "\" do registro a ser atualizado: ");
            query = br.readLine().trim();
        }

        return new Query(field.trim(), query.trim(), stop);
    }

}
