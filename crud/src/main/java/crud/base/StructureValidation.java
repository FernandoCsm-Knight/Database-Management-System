package crud.base;

import java.io.File;

import logic.SystemSpecification;

public final class StructureValidation extends SystemSpecification {
    
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

}
