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

    /** Extract the key associated with the JSONObject */


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

}
