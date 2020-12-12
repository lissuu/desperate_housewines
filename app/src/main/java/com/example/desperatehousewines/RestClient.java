package com.example.desperatehousewines;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestClient {
    public enum API {
        FETCH_ITEMS("alko-lista", Request.Method.GET),

        FETCH_DRINKS("omat-juomat", Request.Method.GET),
        ADD_DRINK("omat-juomat", Request.Method.POST),
        REMOVE_DRINK("omat-juomat", Request.Method.DELETE),

        SEARCHES("haut", Request.Method.GET),
        CONSUMERS("kuluttajat", Request.Method.GET),
        SERVICES("palvelut", Request.Method.GET),
        NEW_USER("uusi_kayttaja", Request.Method.GET)
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

        public enum USER {
            USER("kayttaja"),
            SERVICE("palvelu");

            private final String dbStr;

            USER(String s) {
                this.dbStr = s;
            }

            public String str() {
                return this.dbStr;
            }
        }
    }

    public interface Callback {
        void onError(API api, VolleyError err);
    }

    public interface Response extends Callback {
        void onResponse(API api);
    }

    public interface ResponseObject extends Callback {
        void onResponse(API api, JSONObject resp);
    }

    public interface ResponseArray extends Callback {
        void onResponse(API api, JSONArray resp);
    }

    public interface AsyncData extends Callback {
        void onPreExecute();
        void onResponse(API api);
        void onTaskUpdate(int val);
        void onTaskDone();
    }

    private static String TAG = "REST";

    private static RestClient instance;
    private static Context context;

    private RequestQueue requestQueue;

    // Contains parsed data.
    private List<Item> items = new ArrayList<>();

    // item's id, database id
    private Map<Integer, Integer> userItems = new HashMap<Integer, Integer>();

    // "Logged user"
    private String userHash = "blankUserHash";

    // Singleton's constructor.
    private RestClient(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    // Call to fetch instance to use this class.
    public static synchronized RestClient getInstance(Context context) {
        if (instance == null)
            instance = new RestClient(context);

        return instance;
    }

    // Add requests to this queue.
    public RequestQueue getRequestQueue() {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        return requestQueue;
    }

    // Downloads JSON data and continues to parse it in an AsyncTask afterwards.
    public void fetchItemData(AsyncData cb) {
        Log.d(TAG, "get: " + API.FETCH_ITEMS.toString() + ", url: " + API.FETCH_ITEMS.url() + ", method: " + API.FETCH_ITEMS.method());

        cb.onPreExecute();

        getRequestQueue().add(new JsonArrayRequest(API.FETCH_ITEMS.method(), API.FETCH_ITEMS.url(),
            response -> {
                cb.onResponse(API.FETCH_ITEMS);
                new AsyncTaskRunner(response, cb).execute();
            },
            error -> cb.onError(API.FETCH_ITEMS, error)
        ));
    }

    // Overload for adding items for user
    public void addDrink(Response cb, int id) {
        addDrink(cb, id, -1, API.USER.SERVICE);
    }

    // Overload for adding item for services
    public void addDrink(Response cb, int id, int rating) {
        addDrink(cb, id, rating, API.USER.USER);
    }

    // Base method for aforementioned methods, not to be used outside this class.
    private void addDrink(Response cb, int id, int rating, API.USER user) {
        Log.d(TAG, "addItem id: " + id + ", rating: " + rating + ", api: " + user.toString());

        try {
            JSONObject data = new JSONObject();

            data.put("user_hash", userHash);
            data.put("juoma_id", id);
            data.put("kayttaja", user.str());
            data.put("arvio", rating);

            getRequestQueue().add(new JsonObjectRequest(API.ADD_DRINK.method(), API.ADD_DRINK.url(), data,
                    response -> {
                        cb.onResponse(API.ADD_DRINK);
                    },
                    error -> cb.onError(API.ADD_DRINK, error))
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Removes user/service drink by drink id which is then resolved to a database id.
    public void removeDrink(Response cb, int id) {
        Log.d(TAG, "removeDrink id: " + id);

        if (userItems.containsKey(id)) {
            int dbid = userItems.get(id);

            Log.d(TAG, "removing a drink with database id: " + dbid);

            getRequestQueue().add(new JsonObjectRequest(API.REMOVE_DRINK.method(), API.REMOVE_DRINK.url() + "/" + dbid,
                    response -> {
                        cb.onResponse(API.REMOVE_DRINK);
                    },
                    error -> cb.onError(API.REMOVE_DRINK, error))
            );
        } else {
            Log.e(TAG, "can't resolve database id, dumping userItems:\n" + getUserItemsAsString());
        }
    }

    // Downloads user's saved drinks and adds them to userItems hashmap.
    public void fetchUserItems(Response cb) {
        getRequestQueue().add(new JsonArrayRequest(API.FETCH_DRINKS.method(), API.FETCH_DRINKS.url() + "/" + userHash, response -> {
            Log.d(TAG, "clearing " + userItems.size() + " user items before fetching new");

            userItems.clear();
            int len = response.length();

            for (int i = 0; i < len; i++) {
                try {
                    JSONObject row = response.getJSONObject(i);
                    int id = row.getInt("juoma_id");
                    int dbId = row.getInt("id");

                    if (id >= 0 && dbId >= 0 && getItemById(id) != null) {
                        Log.d(TAG, "added user item with an id: " + id);
                        userItems.put(id, dbId);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "added " + userItems.size() + "/" + len + " user items");
            cb.onResponse(API.FETCH_DRINKS);
        }, error -> cb.onError(API.FETCH_DRINKS, error)));
    }

    public List<Item> getItems () {
        return items;
    }

    // Searches items list by name with 'contains' method. Case is ignored and a match can be at any
    // string position.
    public List<Item> getItemsByName (String search, List<Item> it) {
        List<Item> result = new ArrayList<Item>();

        if (search.equals(""))
            return result;

        Log.d(TAG, "name search called with '" + search + "'");

        for (Item i : it) {
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
    public List<Item> getItemsByYear (int search, List<Item> it) {
        List<Item> result = new ArrayList<Item>();

        Log.d(TAG, "year search called with '" + search + "'");

        for (Item i : it) {
            int item = i.getYear();

            if (item == search) {
                Log.d(TAG, "\t\t[" + result.size() + "]\t" + item);
                result.add(i);
            }
        }

        Log.d(TAG, "search returning with " + result.size() + " items");
        return result;
    }

    public Item getItemById (int id) {
        for (Item i : items) {
            int item = i.getId();

            if (item == id) {
                return i;
            }
        }

        return null;
    }

    public List<Item> getUserItems() {
        Log.d(TAG, "getUserItems, has " + userItems.size() + " items");

        List<Item> user = new ArrayList<>();

        userItems.forEach((k, v) -> {
            Log.d(TAG, "\tid: " + k + " (db id: " + v + ")");
            Item item = getItemById(k);

            if (item != null)
                user.add(item);
        });

        return user;
    }

    public boolean isUserItem (int id) {
        return userItems.containsKey(id);
    }

    // Debugging purposes only
    public void printItems (List<Item> itemList) {
        Log.d(TAG, "item list size: " + itemList.size());

        for (Item i : itemList)
            Log.d(TAG, i.toString());
    }

    public String getUserItemsAsString () {
        Log.d(TAG, "userItem list size: " + userItems.size());

        List<String> s = new ArrayList<>();
        userItems.forEach((k, v) -> s.add("\tkey: " + k + ", value: " + v));

        return TextUtils.join("\n", s);
    }

    // AsyncTask class to parse JSON blob generated from Alko_list table. This is a heavy process
    // and, depending on a phone, can take anywhere from 10 to 30 seconds.
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
