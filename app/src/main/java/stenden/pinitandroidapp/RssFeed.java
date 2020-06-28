package stenden.pinitandroidapp;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import stenden.pinitandroidapp.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Jeroen on 22-12-2016.
 */

public class RssFeed extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private EditText mEditText;
    private Button mFetchFeedButton;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView mFeedTitleTextView;
    private TextView mFeedLinkTextView;
    private TextView mFeedDescriptionTextView;
    private static final String TAG = "RssFeed";
    private List<RssFeedModel> mFeedModelList;
    private String mFeedTitle;
    private String mFeedLink;
    private String mFeedDescription;
    private Menu menu;

    //Creates a new rss feed.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_feed);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);                 // The recycleview refreshes the feed in combination with the swipe layout
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        new FetchFeedTask().execute((Void) null);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute((Void) null);
            }
        });
    }

    //Loads the list item from the rss feed model, with this it will input the xml from the feed
    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();

        // parses the xml
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if (name == null)
                    continue;

                if (eventType == XmlPullParser.END_TAG) {
                    if (name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;
                }

                if (title != null && link != null && description != null) {
                    if (isItem) {
                        RssFeedModel item = new RssFeedModel(title, link, description);
                        items.add(item);
                    } else {
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = description;
                    }

                    title = null;
                    link = null;
                    description = null;
                    isItem = false;
                }
            }

            return items;
        } finally {
            inputStream.close();
        }
    }

    // makes a new task witch can refresh the layout by swyping.
    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            urlLink = "feeds.arstechnica.com/arstechnica/index";
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            try {
                if (!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                mFeedModelList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);

            if (success) {

                mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList));
            } else {
                Toast.makeText(RssFeed.this,
                        "Enter a valid Rss feed url",
                        Toast.LENGTH_LONG).show();
            }
        }


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(stenden.pinitandroidapp.R.menu.main, menu);

        this.menu = menu;

        MenuItem exitRss = menu.findItem(R.id.exitRss);
        exitRss.setVisible(true);
        MenuItem available = menu.findItem(R.id.available);
        available.setVisible(false);
        MenuItem post = menu.findItem(R.id.post_page);
        post.setVisible(false);
        MenuItem exitApp = menu.findItem(R.id.exit_app);
        exitApp.setVisible(false);
        MenuItem mainPage = menu.findItem(R.id.main_page);
        mainPage.setVisible(false);
        MenuItem rssFeed = menu.findItem(R.id.rss_feed);
        rssFeed.setVisible(false);
        MenuItem presencePage = menu.findItem(R.id.presence_page);
        presencePage.setVisible(false);

        return true;

    }

    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //Goes to the available layout when pressed
        if (id == R.id.exitRss) {
            Log.d("yay meer","debuggen");
            final Context context = this;
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }
}


