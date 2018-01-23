package com.example.android.inventory.utils;

import android.text.TextUtils;
import android.util.Log;

import com.example.android.inventory.Book;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving book data from the Google Books.
 */

public class QueryUtils {

    /** Tag for the log messages */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     */
    private QueryUtils() {
    }

    /**
     * Query the Google Books data set and return a list of {@link Book} objects.
     */
    public static List<Book> fetchBookData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Book}s
        List<Book> books = extractFeatureFromJSON(jsonResponse);

        // Return the list of {@link Book}s
        return books;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL.", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT /* milliseconds */);
            urlConnection.setConnectTimeout(Constants.CONNECT_TIMEOUT /* milliseconds */);
            urlConnection.setRequestMethod(Constants.REQUEST_METHOD_GET);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == Constants.SUCCESS_RESPONSE_CODE) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies that an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Book}s objects that has been built up from
     * parsing the given JSON response. In this app, returns only {@link Book} that matches ISBN
     * number.
     */
    private static List<Book> extractFeatureFromJSON(String bookJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Book> bookList = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(bookJSON);
            if (baseJsonResponse.has(Constants.JSON_KEY_ITEMS)) {
                // Extract the JSONArray associated with the key called "items"
                JSONArray bookArray = baseJsonResponse.getJSONArray(Constants.JSON_KEY_ITEMS);

                // Extract the first JSONObject in the bookArray
                JSONObject firstItemObject = bookArray.getJSONObject(0);

                // Extract the JSONObject associated with the key called "volumeInfo"
                JSONObject volumeInfoObject = firstItemObject.getJSONObject(Constants.JSON_KEY_VOLUME_INFO);

                // For a given book, extract the value for the key called "title"
                String title = volumeInfoObject.getString(Constants.JSON_KEY_TITLE);

                // For a given book, extract the JSONArray associated with the key called "authors"
                JSONArray authorsArray = volumeInfoObject.getJSONArray(Constants.JSON_KEY_AUTHORS);

                // For a given book, if there are authors, extract the value in the first
                String author = null;
                if (authorsArray.length() != 0) {
                    author = authorsArray.getString(0);
                }

                // For a given book, extract the JSONArray associated with the key called "industryIdentifiers"
                JSONArray industryIdentifiersArray =
                        volumeInfoObject.getJSONArray(Constants.JSON_KEY_INDUSTRY_IDENTIFIERS);
                String isbn = null;
                // If there is element in the JSONArray, for each element create a JSONObject.
                if (industryIdentifiersArray.length() != 0) {
                    for (int i = 0; i < industryIdentifiersArray.length(); i++) {
                        JSONObject currentObject = industryIdentifiersArray.getJSONObject(i);
                        if (currentObject.has(Constants.JSON_KEY_TYPE)) {
                            // Extract the value for the key "type" and the value will be "ISBN_13" or
                            // "ISBN_10".
                            String isbnType = currentObject.getString(Constants.JSON_KEY_TYPE);
                            // If the value for the key "type" is "ISBN_13", extract the value for the
                            // key "identifier"
                            if (isbnType.equals(Constants.JSON_KEY_ISBN_13)) {
                                isbn = currentObject.getString(Constants.JSON_KEY_IDENTIFIER);
                            }
                        }
                    }
                }

                // For a given book, if it contains the key called "publisher", extract the value for the
                // key called "publisher"
                String publisher = null;
                if (volumeInfoObject.has(Constants.JSON_KEY_PUBLISHER)) {
                    publisher = volumeInfoObject.getString(Constants.JSON_KEY_PUBLISHER);
                }

                // Create a new {@link Book} object with the title, author, ISBN, and publisher from
                // the JSON response.
                Book book = new Book(title, author, isbn, publisher);

                // Add the new {@link Book} to the list of books.
                bookList.add(book);
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
        }

        // Returns the list of books. In this app, since searching for the book that matches the ISBN number,
        // returns only one book that matches the ISBN number.
        return bookList;
    }
}
