package com.iconsulting.backend.common.constants;

/**
 * Constantes globales de l'API
 */
public class ApiConstants {

    // Base path de l'API
    public static final String API_BASE_PATH = "/api/v1";

    // Pagination par défaut
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    // Messages communs
    public static final String CREATED_SUCCESSFULLY = "Resource created successfully";
    public static final String UPDATED_SUCCESSFULLY = "Resource updated successfully";
    public static final String DELETED_SUCCESSFULLY = "Resource deleted successfully";
    public static final String NOT_FOUND = "Resource not found";

    private ApiConstants() {
        // Empêche l'instanciation
    }
}
