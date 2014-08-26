package com.inspiredo.inspiredo;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Make call to url for JSON response
 */
public class AsyncJSON extends AsyncTask<String, String, String> {

    public static final int METHOD_GET = 0;
    public static final int METHOD_POST = 1;

    private String mURL;
    private int mMethod;
    private JSONParser mParser;
    private BasicNameValuePair[] mParams;

    public AsyncJSON(String url, int method, JSONParser parser, BasicNameValuePair[] params) {
        mURL = url;
        mMethod = method;
        mParams = params;
        mParser = parser;
    }

    @Override
    protected void onPostExecute(String s) {
        JSONObject json;

        try {
            json = new JSONObject(s);
        } catch (JSONException e) {
            json = null;
        } catch (NullPointerException e) {
            json = null;
        }

        mParser.parseJSON(json);
    }

    @Override
    protected String doInBackground(String... strings) {
        String line = "";
        String response = "";
        String query = "";

        if (mParams.length > 0)
            query = "?";
            for (BasicNameValuePair pair : mParams) {
                query += pair.getName() + "="+ pair.getValue() + "&";
            }


        HttpUriRequest request = null;
        HttpClient client = new DefaultHttpClient();


        if (mMethod == METHOD_GET) {
            request = new HttpGet(mURL + query);
        } else {
            request = new HttpPost(mURL + query);

        }

        try {

            HttpResponse httpResponse = client.execute(request);

            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) { // Ok
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(httpResponse.getEntity().getContent()));

                while ((line = rd.readLine()) != null) {
                    response += line;
                }
            }
        } catch (ClientProtocolException e) {
            Log.d("JSON", "Client");
            return null;
        } catch (IOException e) {
            Log.d("JSON", "IO");
            return null;
        } catch (IllegalStateException e) {
            Log.d("JSON", "State");
            return null;
        }
        Log.d("JSON", response);

        return response;
    }

    interface JSONParser {
        public void parseJSON(JSONObject json);
    }
}
