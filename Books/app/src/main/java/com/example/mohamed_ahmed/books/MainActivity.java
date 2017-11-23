package com.example.mohamed_ahmed.books;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    public final static String url = "https://www.googleapis.com/books/v1/volumes?q=";
    HashMap<String, List<ListItem>> map;
    ExtendableListAdapter ExpandableListView;
    List<String> Header;
    @BindView(R.id.search)
    SearchView searchView;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    @BindView(R.id.NoDataOrInternet)
    TextView NoDataOrInternet_textView;
    @BindView(R.id.extendList)
    ExpandableListView exList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (savedInstanceState != null) {
            map = (HashMap<String, List<ListItem>>) savedInstanceState.getSerializable("map");
            Header = savedInstanceState.getStringArrayList("header");
            ExpandableListView = new ExtendableListAdapter(this, Header, map);
            if (savedInstanceState.getString("NoInternetOrData") != null) {
                NoDataOrInternet_textView.setText(savedInstanceState.getString("NoInternetOrData"));
            }
            if (Header != null && map != null) {
                exList.setAdapter(ExpandableListView);
            }
        }
        searchView.setQueryHint("Search Books");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchQuery) {
                NoDataOrInternet_textView.setVisibility(View.INVISIBLE);
                if (!isConnected()) {
                    NoDataOrInternet_textView.setVisibility(View.VISIBLE);
                    NoDataOrInternet_textView.setText(R.string.NoInternet);
                    exList.setAdapter(new ExtendableListAdapter(MainActivity.this, new ArrayList<String>(), new HashMap<String, List<ListItem>>()));
                } else if ((searchQuery != null || !searchQuery.isEmpty())) {
                    new Async_task(MainActivity.this).execute(searchQuery);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public Boolean isConnected() {
        final ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("map", map);
        outState.putStringArrayList("header", (ArrayList) Header);
        if (!isConnected()) {
            outState.putString("NoInternetOrData", getString(R.string.NoInternet));
        } else if (Header == null) {
            outState.putString("NoInternetOrData", getString(R.string.NoData));
        } else {
            outState.putString("NoInternetOrData", null);
        }
        super.onSaveInstanceState(outState);
    }

    public class Async_task extends AsyncTask<String, Void, List<ListItem>> {
        private final String LOG_TAG = MainActivity.class.getName();
        Context _context;

        public Async_task(Context _context) {
            this._context = _context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<ListItem> listItems) {
            progressBar.setVisibility(View.INVISIBLE);
            Header = new ArrayList<>();
            map = new HashMap<>();
            if (listItems != null && !listItems.isEmpty()) {
                for (int i = 0; i < listItems.size(); i++) {
                    Header.add(listItems.get(i).getTitle());
                    Log.i(LOG_TAG, listItems.get(i).getTitle());
                    List<ListItem> item = new ArrayList<>();
                    item.add(listItems.get(i));
                    map.put(listItems.get(i).getTitle(), item);
                }
            } else {
                NoDataOrInternet_textView.setText(R.string.NoData);
                NoDataOrInternet_textView.setVisibility(View.VISIBLE);
            }
            ExpandableListView = new ExtendableListAdapter(_context, Header, map);
            exList.setAdapter(ExpandableListView);
        }

        @Override
        protected List<ListItem> doInBackground(String... urls) {
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }
            String Url = url + urls[0];
            List<ListItem> Result = null;
            try {
                QueryUtils queryUtils = new QueryUtils(MainActivity.this);
                Result = queryUtils.fetchBookData(Url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Result;
        }
    }
}
