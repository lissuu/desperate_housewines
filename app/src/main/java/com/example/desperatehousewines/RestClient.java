package com.example.desperatehousewines;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RestClient {
    public enum API {
        // GET
        LIST("alko-lista", Request.Method.GET),
        SEARCHES("haut", Request.Method.GET),
        CONSUMERS("kuluttajat", Request.Method.GET),
        DRINKS("omat-juomat", Request.Method.GET),
        SERVICES("palvelut", Request.Method.GET),
        NEW_USER("uusi_kayttaja", Request.Method.GET),

        // POST
        ADD_FAVORITE("omat-juomat", Request.Method.POST),

        // DELETE
        REMOVE_FAVORITE("omat-juomat", Request.Method.DELETE)
        ;

        private final String request;
        private final int method;

        public static String server = "http://91.145.114.251:8001/";

        API(String r, int m) {
            this.request = r;
            this.method = m;
        }

        String url() {
            return server + this.request;
        }

        int method() {
            return method;
        }
    }

    public interface Callback {
        void onError(VolleyError err);
    }

    public interface ResponseObject extends Callback {
        void onResponse(JSONObject resp);
    }

    public interface ResponseArray extends Callback {
        void onResponse(JSONArray resp);
    }

    public interface AsyncData extends Callback {
        void onPreExecute();
        void onResponse();
        void onTaskUpdate(int val);
        void onTaskDone();
    }

    private static String TAG = "REST";

    private static RestClient instance;
    private static Context context;

    private RequestQueue requestQueue;

    private List<Item> items;
    private String userHash = "blankUserHash";
    private String userName = "blankUserName";

    private RestClient(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized RestClient getInstance(Context context) {
        if (instance == null)
            instance = new RestClient(context);

        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        return requestQueue;
    }

    public void getList(AsyncData cb) {
        API api = API.LIST;
        Log.d(TAG, "get: " + api.toString() + ", url: " + api.url() + ", method: " + api.method());

        cb.onPreExecute();

        getRequestQueue().add(new JsonArrayRequest(api.method(), api.url(),
                response -> {
                    cb.onResponse();
                    new AsyncTaskRunner(response, cb).execute();
                },
                error -> cb.onError(error)
        ));
    }

    public void addFavorite(ResponseObject cb, int id, int rating) {
        try {
            JSONObject data = new JSONObject();

            data.put("user_hash", userHash);
            data.put("juoma_id", id);
            data.put("kayttaja", userName);
            data.put("arvio", rating);

            getObjectData(cb, API.ADD_FAVORITE, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeFavorite(ResponseObject cb, int id) {
        try {
            JSONObject data = new JSONObject();

            data.put("id", id);
            getObjectData(cb, API.REMOVE_FAVORITE, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getFavorites(ResponseArray cb) {
        getArrayData(cb, API.DRINKS);
    }

    public void getNewUser(ResponseObject cb) {
        getObjectData(cb, API.NEW_USER);
    }

    public void getSearches(ResponseArray cb) {
        getArrayData(cb, API.SEARCHES);
    }

    public void getUsers(ResponseArray cb) {
        getArrayData(cb, API.CONSUMERS);
    }
    
    public void getServices(ResponseArray cb) {
        getArrayData(cb, API.SERVICES);
    }

    private void getArrayData(ResponseArray cb, API api) {
        getArrayData(cb, api, null);
    }

    private void getArrayData(ResponseArray cb, API api, JSONObject data) {
        Log.d(TAG, "get: " + api.toString() + ", url: " + api.url() + ", method: " + api.method() + ", data: " + data.toString());

        getRequestQueue().add(data == null ?
            new JsonArrayRequest(api.method(), api.url(), response -> cb.onResponse(response), error -> cb.onError(error)) :
            new JsonArrayRequest(api.method(), api.url(), data, response -> cb.onResponse(response), error -> cb.onError(error))
        );
    }

    private void getObjectData(ResponseObject cb, API api) {
        getObjectData(cb, api, null);
    }

    private void getObjectData(ResponseObject cb, API api, JSONObject data) {
        Log.d(TAG, "get: " + api.toString() + ", url: " + api.url() + ", method: " + api.method() + ", data: " + data.toString());

        getRequestQueue().add(data == null ?
            new JsonObjectRequest(api.method(), api.url(), response -> cb.onResponse(response), error -> cb.onError(error)) :
            new JsonObjectRequest(api.method(), api.url(), data, response -> cb.onResponse(response), error -> cb.onError(error))
        );
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
        private JSONArray array;
        private AsyncData cb;

        public AsyncTaskRunner (JSONArray array, AsyncData cb) {
            this.array = array;
            this.cb = cb;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute");
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
            return "done";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: " + result);
            cb.onTaskDone();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            cb.onTaskUpdate(progress[0]);
        }
    }
}
