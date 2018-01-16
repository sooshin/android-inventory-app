package com.example.android.inventory.utils;

/**
 * Store constants for the inventory app.
 */

public class Constants {

    /**
     * Creates a private constructor because no one should ever create a {@link Constants} object.
     */
    private Constants() {
    }

    /** Extract the key associated with the JSONObject and JSONArray */
    static final String JSON_KEY_ITEMS = "items";
    static final String JSON_KEY_VOLUME_INFO = "volumeInfo";
    static final String JSON_KEY_TITLE = "title";
    static final String JSON_KEY_AUTHORS = "authors";
    static final String JSON_KEY_INDUSTRY_IDENTIFIERS = "industryIdentifiers";
    static final String JSON_KEY_TYPE = "type";
    static final String JSON_KEY_ISBN_13 = "ISBN_13";
    static final String JSON_KEY_IDENTIFIER = "identifier";
    static final String JSON_KEY_PUBLISHER = "publisher";

    /** Read timeout for setting up the HTTP request */
    static final int READ_TIMEOUT = 10000; /* milliseconds */

    /** Connect timeout for setting up the HTTP request */
    static final int CONNECT_TIMEOUT = 15000; /* milliseconds */

    /** HTTP response code when the request is successful */
    static final int SUCCESS_RESPONSE_CODE = 200;

    /** Request method type "GET" for reading information from the server */
    static final String REQUEST_METHOD_GET = "GET";

    /** URL for the book data from the Google books data set */
    public static final String BOOK_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?";

    /** Default number to set the image on the top of the textView */
    public static final int DEFAULT_NUMBER = 0;
}
