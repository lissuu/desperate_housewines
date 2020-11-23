package com.example.desperatehousewines;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

public class RestClient {
    public enum API {
        LIST("alko-lista", Request.Method.GET),
        SEARCHES("haut", Request.Method.GET),
        CONSUMERS("kuluttajat", Request.Method.GET),
        DRINKS("omat-juomat", Request.Method.GET),
        SERVICES("palvelut", Request.Method.GET);

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
        void onResponse(JSONArray array);
        void onError(VolleyError err);
    }

    private String TAG = "REST";

    private static RestClient instance;
    private RequestQueue requestQueue;
    private static Context ctx;
    private JsonArrayRequest jsonArrayRequest;

    private RestClient(Context context) {
        ctx = context;
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
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }

        return requestQueue;
    }

    // Call rest API with a callback interface, use the overloaded below if you don't want to use an interface.
    public void get(API api, Callback cb) {
        Log.d(TAG, "get: " + api.toString() + ", url: " + api.url() + ", method: " + api.method());

        jsonArrayRequest = new JsonArrayRequest(api.method(), api.url(),
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.d(TAG, "volley done, calling cb");
                    cb.onResponse(response);
                }
            },
            new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error){
                    Log.e(TAG, "volley error");
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
}
