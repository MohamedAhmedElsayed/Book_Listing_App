package com.example.mohamed_ahmed.books;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

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

public class QueryUtils {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static Context context;

    public QueryUtils(Context context) {
        this.context = context;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                }
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<ListItem> extractFeatureFromJson(String BooksJSON) throws JSONException {
        String title = "", previewLink = "", description = "", language = "", authors = "";
        if (TextUtils.isEmpty(BooksJSON)) {
            return null;
        }
        List<ListItem> Books = new ArrayList<>();
        JSONObject object = new JSONObject(BooksJSON);
        JSONArray BookItems = null;
        if (object.has("items")) {
            BookItems = object.getJSONArray("items");
            for (int i = 0; i < BookItems.length(); i++) {
                JSONObject volumeInfo = BookItems.optJSONObject(i).optJSONObject("volumeInfo");
                title = volumeInfo.optString("title");
                JSONArray authorsJson = volumeInfo.optJSONArray("authors");
                if (authorsJson != null) {
                    authors = authorsJson.get(0).toString();
                }
                description = volumeInfo.optString("description");
                language = volumeInfo.optString("language");
                previewLink = volumeInfo.optString("previewLink");
                Books.add(new ListItem(title, previewLink, description, language, authors));
            }
        }
        return Books;
    }

    public static List<ListItem> fetchBookData(String requestUrl) throws JSONException {
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        List<ListItem> Books = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            return Books;
        }
        Books = extractFeatureFromJson(jsonResponse);
        return Books;
    }

    public Boolean isConnected() {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
