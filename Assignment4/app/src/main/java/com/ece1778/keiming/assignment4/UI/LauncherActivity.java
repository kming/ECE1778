package com.ece1778.keiming.assignment4.UI;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ece1778.keiming.assignment4.BuildConfig;
import com.ece1778.keiming.assignment4.InternalClasses.TableEntry;
import com.ece1778.keiming.assignment4.Managers.DatabaseManager;
import com.ece1778.keiming.assignment4.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class LauncherActivity extends ActionBarActivity {
    private static final String TAG = LauncherActivity.class.getName();
    private static final String mDefaultURL = "http://www.eecg.utoronto.ca/~jayar/PeopleList";
    private static Context mContext = null;
    private static boolean mFetching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setTitle("Assignment 4");
        setContentView(R.layout.activity_launcher);
        mContext = this;
        onDatabaseUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_database_button:
                onLoadClicked();
                break;
            case R.id.view_database_button:
                Intent intent = new Intent(this, ViewActivity.class);
                startActivity(intent);
                break;
            case R.id.clear_database_button:
                DatabaseManager.getManager(this).deleteDatabase();
                onDatabaseUpdate();
                break;
        }
    }

    private void loadDatabase(String url) {
        if (!mFetching) {
            new AsyncHTTPRequest().execute(url);
        }
    }

    private void onDatabaseUpdate () {
        int databaseCount = DatabaseManager.getManager(mContext).getValuesCount();
        if (BuildConfig.DEBUG) { Log.d(TAG, Integer.toString(databaseCount)); }
        if (databaseCount == 0) {
            Button viewButton = (Button) findViewById(R.id.view_database_button);
            viewButton.setEnabled(false);
            viewButton.setClickable(false);
            viewButton.setAnimation(new AlphaAnimation(1.0f, 0.45f));
            Button clearButton = (Button) findViewById(R.id.clear_database_button);
            clearButton.setEnabled(false);
            clearButton.setClickable(false);
            clearButton.setAnimation(new AlphaAnimation(1.0f, 0.45f));
        } else {
            Button viewButton = (Button) findViewById(R.id.view_database_button);
            viewButton.setEnabled(true);
            viewButton.setClickable(true);
            viewButton.setAnimation(new AlphaAnimation(0.45f, 1.0f));
            Button clearButton = (Button) findViewById(R.id.clear_database_button);
            clearButton.setEnabled(true);
            clearButton.setClickable(true);
            clearButton.setAnimation(new AlphaAnimation(0.45f, 1.0f));
        }
    }
    // Function for updating backend Database
    private void addToDatabase(String baseUrl, String inputString) {
        if (!inputString.isEmpty()) {
            // process picture url path
            DatabaseManager manager = DatabaseManager.getManager(mContext);
            URI baseUri = null;
            try {
                URI uri = new URI(baseUrl);
                baseUri = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
            } catch (URISyntaxException e) {
                //TODO: handle exception
            }
            // Adding entries to the database
            String[] data = inputString.split("\n");
            for (int i = 0; i < data.length; i = i + 3) {
                // Name, Location to Picture, Note/Bio
                // adds the relative path to the base Url.
                manager.addValue(new TableEntry(data[i], baseUri.resolve(data[i+2]).toString(), data[i+1]));
            }
        }
    }

    // Determine what to do when the load button is clicked.
    private void onLoadClicked () {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Enter Database URL");
        alert.setMessage("Click OK for default url");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setHint(mDefaultURL);
        alert.setView(input);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String url = input.getText().toString();
                if (url.isEmpty()) {
                    loadDatabase(mDefaultURL);
                } else {
                    loadDatabase(url);
                }
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    // Private asynchronous task to load the database with the data
    private class AsyncHTTPRequest extends AsyncTask<String, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            mFetching = true;
            dialog = new ProgressDialog(LauncherActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Loading");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        // Performs HTTP request in background.
        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(url));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }

            addToDatabase(url, responseString);
            return null;
        }

        protected void onPostExecute(Void result) {
            onDatabaseUpdate();
            mFetching = false;
            dialog.dismiss();
            Toast.makeText(mContext, "Database Loaded", Toast.LENGTH_SHORT).show();
        }
    }

}
