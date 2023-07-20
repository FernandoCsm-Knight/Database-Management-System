package crud.base;

import java.io.File;
import java.util.Scanner;

import logic.SystemSpecification;

public final class StructureValidation implements SystemSpecification {
    
    public static void verifyDirectoryStructure() {
        createTemporaryDirectory();
        createJSONDirectory();
    }

    public static void createTemporaryDirectory() {
        File temporaryDirectory = new File(TEMPORARY_FILES_DIRECTORY);
        if (!temporaryDirectory.exists()) {
            temporaryDirectory.mkdir();
        }
    }

    public static void createJSONDirectory() {
        File jsonDirectory = new File(JSON_FILES_DIRECTORY);
        if (!jsonDirectory.exists()) {
            jsonDirectory.mkdir();
        }
    }

    public static void createJSONIndexDirectory() {
        createJSONDirectory();
        File indexDirectory = new File(JSON_INDEXES_DIRECTORY);
        if (!indexDirectory.exists()) {
            indexDirectory.mkdir();
        }
    }

    public static void createIndexesDirectory() {
        File indexDirectory = new File(INDEXES_FILES_DIRECTORY);
        if (!indexDirectory.exists()) {
            indexDirectory.mkdir();
        }
    }

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
