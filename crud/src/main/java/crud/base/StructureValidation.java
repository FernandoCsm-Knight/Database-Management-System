package crud.base;

import java.io.File;
import java.util.Scanner;

import logic.SystemSpecification;

/**
 * The {@code StructureValidation} class represents a structure validation for the database.
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public final class StructureValidation implements SystemSpecification {
    
    /**
     * Verifies and creates the necessary directory structure.
     */
    public static void verifyDirectoryStructure() {
        createTemporaryDirectory();
        createJSONDirectory();
    }

    /**
     * Creates the temporary directory if it does not exist.
     */
    public static void createTemporaryDirectory() {
        File temporaryDirectory = new File(TEMPORARY_FILES_DIRECTORY);
        if (!temporaryDirectory.exists()) {
            temporaryDirectory.mkdir();
        }
    }

    /**
     * Creates the JSON directory if it does not exist.
     */
    public static void createJSONDirectory() {
        File jsonDirectory = new File(JSON_FILES_DIRECTORY);
        if (!jsonDirectory.exists()) {
            jsonDirectory.mkdir();
        }
    }

    /**
     * Creates the JSON index directory if it does not exist.
     */
    public static void createJSONIndexDirectory() {
        createJSONDirectory();
        File indexDirectory = new File(JSON_INDEXES_DIRECTORY);
        if (!indexDirectory.exists()) {
            indexDirectory.mkdir();
        }
    }

    /**
     * Creates the indexes directory if it does not exist.
     */
    public static void createIndexesDirectory() {
        File indexDirectory = new File(INDEXES_FILES_DIRECTORY);
        if (!indexDirectory.exists()) {
            indexDirectory.mkdir();
        }
    }

    /**
     * Cleans the indexes directory by deleting its contents.
     */
    public static void cleanIndexesDirectory() {
        File indexDirectory = new File(INDEXES_FILES_DIRECTORY);
        if (indexDirectory.exists()) {
            deleteDirectory(indexDirectory);
        }
    }

    /**
     * Cleans database files by deleting files with ".db" or ".db.trash" extensions.
     */
    public static void cleanDatabaseFiles() {
        File databaseFile = new File(PROJECT_CRUD_PATH);
        if (databaseFile.exists()) {
            File[] files = databaseFile.listFiles();
            for(int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(".db") || files[i].getName().endsWith(".db.trash")) {
                    files[i].delete();
                }
            }
        }
    }

    /**
     * Cleans the JSON indexes directory by deleting its contents.
     */
    public static void cleanJsonIndexesDirectory() {
        File indexDirectory = new File(JSON_INDEXES_DIRECTORY);
        if (indexDirectory.exists()) {
            deleteDirectory(indexDirectory);
        }
    }

    /**
     * Cleans JSON database files by deleting files with ".json" extension.
     */
    public static void cleanJsonDatabaseFiles() {
        File databaseFile = new File(JSON_FILES_DIRECTORY);
        if (databaseFile.exists()) {
            File[] files = databaseFile.listFiles();
            for(int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(".json")) {
                    files[i].delete();
                }
            }
        }
    }

    /**
     * Cleans the entire directory structure (temporary files, JSON files, and indexes).
     */
    public static void clean() {
        System.out.print("[WARNING] This will delete all the data in the database. Are you sure? (y/N): ");
        Scanner sc = new Scanner(System.in);
        String op = sc.nextLine().toLowerCase();
        sc.close();
        
        if(op.length() == 0 || op.charAt(0) != 'y' || op.length() > 1) {
            System.out.println("Aborting...");
        } else {
            System.out.println("Cleaning...");
            File temporaryDirectory = new File(TEMPORARY_FILES_DIRECTORY);
            if (temporaryDirectory.exists()) 
                deleteDirectory(temporaryDirectory);

            File jsonDirectory = new File(JSON_FILES_DIRECTORY);
            if (jsonDirectory.exists()) 
                deleteDirectory(jsonDirectory);

            File indexDirectory = new File(INDEXES_FILES_DIRECTORY);
            if (indexDirectory.exists()) 
                deleteDirectory(indexDirectory);

            System.out.println("Done.");
        }
    }

    // Private Methods

    /**
     * Recursively deletes a directory and its contents.
     *
     * @param directory The directory to be deleted.
     */
    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) 
                    deleteDirectory(file);
                else
                    file.delete();
            }
        }
        directory.delete();
    }
}
