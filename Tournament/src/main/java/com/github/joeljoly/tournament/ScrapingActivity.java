package com.github.joeljoly.tournament;

import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.List;

public class ScrapingActivity extends ActionBarActivity {

    /**
     * Flag to know if the activity supports a progress bar.
     */
    boolean mHasFeatureProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHasFeatureProgress = requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_scraping);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scraping, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        static int httpRequestIntentCode = 1;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(HttpRequestsFragment.TAG);
            if (fragment == null) {
                fragment = new HttpRequestsFragment();
                fragment.setTargetFragment(this, httpRequestIntentCode);
                fragmentManager.beginTransaction().add(fragment,
                        HttpRequestsFragment.TAG).commit();
            }

            View rootView = inflater.inflate(R.layout.fragment_scraping, container, false);
            return rootView;
        }
    }

    /**
     * A headless retained fragment for handling HTTP transactions.
     */
    public static class HttpRequestsFragment extends Fragment {

        static String TAG = "HttpRequestsFragment.instance";
        /**
         * The HTTP client used in all transactions.
         */
        private AbstractHttpClient mClient;

        private class CookieRetrieval extends AsyncTask<String, Integer, List<Cookie>> {
            private Exception mExceptionDuringBackground;

            public CookieRetrieval(AbstractHttpClient client) {
                mClient = client;
            }
            @Override
            protected List<Cookie> doInBackground(String... urls) {
                HttpGet request = new HttpGet(urls[0]);
                try {
                    HttpResponse response = mClient.execute(request);
                } catch (Exception e) {
                    mExceptionDuringBackground = e;
                    cancel(true);
                }
                return mClient.getCookieStore().getCookies();
            }
            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                getActivity().setProgress(values[0] * 1000);
            }

            @Override
            protected void onPostExecute(List<Cookie> cookies) {
                super.onPostExecute(cookies);
                for (Cookie cookie : cookies) {
                    Toast.makeText(getActivity(), "cookie " + cookie.getName() + " = " + cookie.getValue(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected void onCancelled(List<Cookie> cookies) {
                super.onCancelled(cookies);
                if (mExceptionDuringBackground != null)
                    Toast.makeText(getActivity(), "Connexion error error" + mExceptionDuringBackground.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        HttpRequestsFragment() {
            mClient = new DefaultHttpClient();
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // headless and retained fragment to not reset on mode changes
            setRetainInstance(true);
            CookieRetrieval retrieveCookie = new CookieRetrieval(mClient);
            retrieveCookie.execute(getString(R.string.scraping_web_target));
            if (((ScrapingActivity)getActivity()).mHasFeatureProgress) {
            }
        }

    }

}
