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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sj on 1/14/2018.
 */

public class QueryUtils {

    /** Tag for the log messages */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {

    }

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

        // Extract relevant fields from the JSON response and create a list of {@link Book}
        List<Book> book = extractFeatureFromJSON(jsonResponse);

        // Return
        return book;
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
                    Charset.forName(StandardCharsets.UTF_8.name()));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<Book> extractFeatureFromJSON(String bookJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        List<Book> bookList = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            JSONArray bookArray = baseJsonResponse.getJSONArray("items");



            JSONObject firstItemObject = bookArray.getJSONObject(0);

            JSONObject volumeInfoObject = firstItemObject.getJSONObject("volumeInfo");
            // title
            String title = volumeInfoObject.getString("title");

            Log.e(LOG_TAG, "queryUtils title: " + title);
            // author
            JSONArray authorsArray = volumeInfoObject.getJSONArray("authors");

            String author = null;
            if (authorsArray.length() != 0) {
                author = authorsArray.getString(0);
            }
            // isbn_13

            JSONArray industryIdentifiersArray = volumeInfoObject.getJSONArray("industryIdentifiers");
            String isbn = null;
            if (industryIdentifiersArray.length() != 0) {
                 for (int i = 0; i < industryIdentifiersArray.length(); i++) {
                    JSONObject currentObject = industryIdentifiersArray.getJSONObject(i);
                    if (currentObject.has("type")){
                       String isbnType = currentObject.getString("type");
                       if (isbnType.equals("ISBN_13")) {
                            isbn = currentObject.getString("identifier");
                       }

                    }

                 }

            }

/*
                JSONObject secondObject = industryIdentifiersArray.getJSONObject(1);
                if (secondObject.getString("type").equalsIgnoreCase("ISBN_13")
                        && secondObject.has("identifier") ) {
                    isbn = secondObject.getString("identifier");
                }
            }
*/
            // publisher
            String publisher = null;
            if (volumeInfoObject.has("publisher")) {
                publisher = volumeInfoObject.getString("publisher");
            }

            Book book = new Book(title, author, isbn, publisher);

            bookList.add(book);


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
        }

        return bookList;
    }
}
