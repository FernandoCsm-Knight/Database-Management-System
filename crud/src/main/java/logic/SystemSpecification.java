package logic;

public abstract class SystemSpecification {
    protected static final int BLOCK_SIZE = 4096; // 4KB

    protected static final String JSON_FILES_DIRECTORY = "src/main/java/data/json/"; // Directory where the JSON files will be stored
    protected static final String TEMPORARY_FILES_DIRECTORY = "src/main/java/data/tmp"; // Directory where the temporary files will be stored
    
    protected static final String DATABASE_FILE_PATH = "src/main/java/data/database.db"; // Path of the database file
    protected static final String TEMPORARY_FILES_PATH = "src/main/java/data/tmp/tmp"; // Path of the temporary files
    
    protected static final String TEMPORARY_FILES_EXTENSION = ".tmp"; // Extension of the temporary files
}