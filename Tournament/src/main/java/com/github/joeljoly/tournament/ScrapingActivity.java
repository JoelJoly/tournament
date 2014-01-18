package com.github.joeljoly.tournament;

import android.os.AsyncTask;
import android.os.Environment;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
            HttpRequestsFragment fragment = (HttpRequestsFragment)fragmentManager.findFragmentByTag(HttpRequestsFragment.TAG);
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

        private class TeamPlayerRequest extends AsyncTask<String, Integer, String> {
            private Exception mExceptionDuringBackground;

            public TeamPlayerRequest(AbstractHttpClient client) {
                mClient = client;
            }
            @Override
            protected String doInBackground(String... urls) {
                try
                {
                    File f = new File(Environment.getExternalStorageDirectory(), "tournament_dump.html");
                    if (f.exists())
                    {
                        f.setReadOnly();
                        Document doc = Jsoup.parse(f, null);
                        Elements forms = doc.select("form"); // find forms
                        List<FormElement> selectForm = forms.forms();
                        if (selectForm.isEmpty())
                            throw new RuntimeException("Cannot find form.");
                        FormElement firstForm = selectForm.get(0);
                        String formsElementsText = "";
                        Element lincenseNumberElement;
                        for (Element element: firstForm.elements()) {
                            formsElementsText = formsElementsText + element.toString();
                            if (element.val() == "precision") {

                            }
                        }
                        for (Connection.KeyVal keyVal : firstForm.formData()) {
                            formsElementsText = formsElementsText + keyVal.key() + " " + keyVal.value();
                        }
                        return formsElementsText;
                    }
                    else
                    {
                        HttpGet request = new HttpGet(urls[0]);
                        // execute request
                        HttpResponse response = mClient.execute(request);
                        // getting the first page is roughly 10% of the job
                        publishProgress(10);
                        int responseCode = response.getStatusLine().getStatusCode();
                        String htmlBody = "";
                        switch(responseCode)
                        {
                            case 200:
                                HttpEntity entity = response.getEntity();
                                if(entity != null)
                                {
                                    htmlBody = EntityUtils.toString(entity);
                                }
                                break;
                            default:
                                throw new RuntimeException("Cannot retrieve page '" +
                                        urls[0] + "' invalid response code: " +
                                        new Integer(responseCode).toString());
                        }
                        // TODO
                        // job's done
                        publishProgress(100);
                        return htmlBody;
                    }
                } catch (Exception e) {
                        mExceptionDuringBackground = e;
                        cancel(true);
                }
                return "";
            }
            @Override
            // get integer values as percentages
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                // convert from [0, 100] to [0, 10000]
                getActivity().setProgress(values[0] * 100);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Toast.makeText(getActivity(), "result " + result, Toast.LENGTH_LONG).show();
                writeToFile(result);
            }

            @Override
            protected void onCancelled(String result) {
                super.onCancelled(result);
                if (mExceptionDuringBackground != null)
                    Toast.makeText(getActivity(), "Connexion error: " + mExceptionDuringBackground.getMessage(), Toast.LENGTH_LONG).show();
            }
            private void writeToFile(String data) {
                try {
                    File f = new File(Environment.getExternalStorageDirectory(), "tournament_dump.html");
                    if (f.exists())
                        Toast.makeText(getActivity(), "result " + f.toString(), Toast.LENGTH_LONG).show();
                    BufferedWriter out = new BufferedWriter(new FileWriter(f));
                    out.write(data);
                    out.close();
                }
                catch (IOException e) {
                    Toast.makeText(getActivity(), "File write failed: " + e.toString(), Toast.LENGTH_LONG).show();
                }
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
            TeamPlayerRequest retrieveCookie = new TeamPlayerRequest(mClient);
            retrieveCookie.execute(getString(R.string.scraping_web_target_cookie));
            if (((ScrapingActivity)getActivity()).mHasFeatureProgress) {
            }
        }

    }

}
