package logic;

/**
 * @author Fernando Campos Silva Dal Maria & Rafael Fleury Barcellos Ceolin de Oliveira
 * @version 1.0.0
 */
public interface SystemSpecification {
    
    // Attributes

    static final int BLOCK_SIZE = 4096; // 4KB
    static final int PAGE_SIZE = BLOCK_SIZE * 10; // 40KB
    static final String PROJECT_PATH = "src/"; // Path of the project
    
    static final String PROJECT_CRUD_PATH = PROJECT_PATH + "data/"; // Path for the CRUD files
    static final String CSV_FILES_DIRECTORY = PROJECT_PATH + "data/bases/"; // Directory where the CSV files will be stored

    static final String JSON_FILES_DIRECTORY = PROJECT_PATH + "data/json/"; // Directory where the JSON files will be stored
    static final String JSON_INDEXES_DIRECTORY = PROJECT_PATH + "data/json/index/"; // Directory where the indexes will be stored
    static final String TEMPORARY_FILES_DIRECTORY = PROJECT_PATH + "data/tmp/"; // Directory where the temporary files will be stored
    static final String INDEXES_FILES_DIRECTORY = PROJECT_PATH + "data/index/"; // Directory where the indexes will be stored
    
    static final String DATABASE_FILE_PATH = PROJECT_PATH + "data/database.db"; // Path of the database file
    
    static final String TEMPORARY_FILES_EXTENSION = ".dat"; // Extension of the temporary files
}