import layout.menus.MainMenu;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public class Main {

    public static void main(String[] args) throws Exception {
        MainMenu menu = new MainMenu();
        menu.build();
        menu.close();
    }

}