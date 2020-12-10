package com.example.desperatehousewines;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RestClient {
    public enum API {
        LIST("alko-lista", Request.Method.GET, "Ladataan tietoja..."),
        SEARCHES("haut", Request.Method.GET, ""),
        CONSUMERS("kuluttajat", Request.Method.GET, ""),
        DRINKS("omat-juomat", Request.Method.GET, ""),
        SERVICES("palvelut", Request.Method.GET, "");

        private final String request;
        private final int method;
        private final String dialogMessage;

        public static String server = "http://91.145.114.251:8001/";

        API(String r, int m, String d) {
            this.request = r;
            this.method = m;
            this.dialogMessage = d;
        }

        String url() {
            return server + this.request;
        }
        int method() {
            return method;
        }
        String msg() { return dialogMessage; }
        boolean hasMsg() { return dialogMessage.length() > 0; }
    }

    public interface Callback {
        void onResponse();
        void onError(VolleyError err);
    }

    private String TAG = "REST";

    private static RestClient instance;
    private static Context context;

    private RequestQueue requestQueue;
    private JsonArrayRequest jsonArrayRequest;
    private ProgressDialog progressBar;

    private List<Item> items;

    private RestClient(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized RestClient getInstance(Context context) {
        if (instance == null) {
            instance = new RestClient(context);
        }

        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }

        return requestQueue;
    }

    // Call rest API with a callback interface, use the overloaded below if you don't want to use an interface.
    public void get(API api, Callback cb, Context newContext) {
        Log.d(TAG, "get: " + api.toString() + ", url: " + api.url() + ", method: " + api.method());

        progressBar = new ProgressDialog(newContext);

        if (api.hasMsg()) {
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgressNumberFormat(null);
            progressBar.setMessage(api.msg());
            progressBar.show();
        }

        jsonArrayRequest = new JsonArrayRequest(api.method(), api.url(),
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.d(TAG, "volley done");
                    progressBar.dismiss();

                    switch (api) {
                        case LIST:
                            new AsyncTaskRunner(newContext, response, cb).execute();
                            break;
                        case DRINKS:
                        case SEARCHES:
                        case SERVICES:
                        case CONSUMERS:
                            cb.onResponse();
                            break;
                        default:
                            Log.e(TAG, "api switch defaults");
                    }
                }
            },
            new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error){
                    Log.e(TAG, "volley error");
                    progressBar.dismiss();
                    cb.onError(error);
                }
            }
        );

        getRequestQueue().add(jsonArrayRequest);
    }

    // Call rest API without a callback interface.
    public void get(API api, Response.Listener<JSONArray> onResponse, Response.ErrorListener onError) {
        Log.d(TAG, "get: " + api.toString() + ", url: " + api.url() + ", method: " + api.method());

        jsonArrayRequest = new JsonArrayRequest(
            api.method(),
            api.url(),
            onResponse,
            onError
        );

        getRequestQueue().add(jsonArrayRequest);
    }

    public List<Item> getItems () {
        return items;
    }

    // Searches items list by name with 'contains' method. Case is ignored and a match can be at any
    // string position.
    public List<Item> getItemsByName (String search) {
        List<Item> result = new ArrayList<Item>();

        if (search.equals(""))
            return result;

        Log.d(TAG, "name search called with '" + search + "'");

        for (Item i : items) {
            String itemName = i.getName().toLowerCase();

            if (itemName.contains(search)) {
                Log.d(TAG, "\t\t[" + result.size() + "]\t" + itemName);
                result.add(i);
            }
        }

        Log.d(TAG, "search returning with " + result.size() + " items");
        return result;
    }

    // Searches items list by a year.
    // Note: alko's api has a lot of blanks for this information
    public List<Item> getItemsByYear (int search) {
        List<Item> result = new ArrayList<Item>();

        Log.d(TAG, "year search called with '" + search + "'");

        for (Item i : items) {
            int item = i.getYear();

            if (item == search) {
                Log.d(TAG, "\t\t[" + result.size() + "]\t" + item);
                result.add(i);
            }
        }

        Log.d(TAG, "search returning with " + result.size() + " items");
        return result;
    }

    // Debugging purposes only
    public void itemsToString (List<Item> itemList) {
        Log.d(TAG, "item list size: " + itemList.size());

        for (Item i : itemList)
            Log.d(TAG, i.toString());
    }

    private class AsyncTaskRunner extends AsyncTask<String, Integer, String> {
        private Context context;
        private JSONArray array;
        private ProgressDialog progressBar;
        private Callback cb;

        public AsyncTaskRunner (Context context, JSONArray array, Callback cb) {
            this.context = context;
            this.array = array;
            this.cb = cb;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute");

            // Initialize progress bar.
            progressBar = new ProgressDialog(context);
            progressBar.setMessage("Käsitellään tietoja...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.setProgressNumberFormat(null);
            progressBar.setProgress(0);
            progressBar.setMax(100);
            progressBar.show();
        }

        @Override
        protected String doInBackground(String... params) {
            items = new ArrayList<Item>();

            int len = array.length();
            int prevProgress = -1;
            int nowProgress = -1;
            int notValidItems = 0;

            // Loop through data and update progress.
            for (int i = 0; i < len; i++) {
                try {
                    JSONObject row = array.getJSONObject(i);
                    Item item = new Item(row);

                    if (item.isValid()) {
                        items.add(item.createCardTitle());
                    } else {
                        notValidItems++;
                    }

                    nowProgress = (int) Math.round((double) i / len * 100);

                    // No need to update progress 10000 times when there is only 100 different progress values.
                    if (prevProgress != nowProgress) {
                        prevProgress = nowProgress;
                        publishProgress(nowProgress);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Sleeping a thread can cause an exception and thus needs to be enclosed in try catch.
            try {
                // The user needs to see 100%. For some reason.
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d(TAG, "parsed " + items.size() + " valid items and dismissed " + notValidItems + " items");
            progressBar.dismiss();
            return "done";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: " + result);
            cb.onResponse();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressBar.setProgress(progress[0]);
        }
    }
}
