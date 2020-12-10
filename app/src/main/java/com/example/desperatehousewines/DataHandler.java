
package com.example.desperatehousewines;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DataHandler {
    final String TAG = "DATA";

    final static String DATA_URL = "https://www.alko.fi/INTERSHOP/static/WFS/Alko-OnlineShop-Site/-/Alko-OnlineShop/fi_FI/Alkon%20Hinnasto%20Tekstitiedostona/alkon-hinnasto-tekstitiedostona.xlsx";
    final static String DATA_FILE = "data.xlsx";

    ArrayList<Item> parsedData;

    public DataHandler(Context c, VolleyCallback cb) {
        fetchData(c, cb);
    }

    public boolean hasItemNamed (String name) {
        if (parsedData == null) {
            Log.e(TAG, "can't call hasItemNamed() without fetching data");
            return false;
        }

        String lower = name.toLowerCase();

        for (Item i : parsedData) {
            if (i.getName() == lower)
                return true;
        }

        return false;
    }

    public Item getItemNamed (String name) {
        if (parsedData == null) {
            Log.e(TAG, "can't call getItemNamed() without fetching data");
            return null;
        }

        String lower = name.toLowerCase();

        for (Item i : parsedData) {
            if (i.getName() == lower)
                return i;
        }

        return null;
    }



    private void fetchData(Context c, VolleyCallback cb) {
        Log.d(TAG, "[getData] init");

        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, DATA_URL,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        Log.d(TAG, "[getData] => [onResponse]");

                        saveRawData(c, response);
                        parsedData = parseSavedData(c);
                        cb.onSuccess();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "[getData] => [onErrorResponse]");

                error.printStackTrace();
                cb.onError();
            }
        }, null);

        RequestQueue mRequestQueue = Volley.newRequestQueue(c, new HurlStack());
        mRequestQueue.add(request);
    }

    private void saveRawData(Context c, byte[] resp) {
        Log.d(TAG, "[saveRawData]");

        try {
            FileOutputStream outputStream;
            outputStream = c.openFileOutput(DataHandler.DATA_FILE, Context.MODE_PRIVATE);
            outputStream.write(resp);
            outputStream.close();
            Log.d(TAG, "[saveRawData] wrote file to internal storage");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Item> parseSavedData(Context c) {
        try {
            File file = new File(c.getFilesDir(), DataHandler.DATA_FILE);
            InputStream fis = new FileInputStream(file);
            ReadableWorkbook wb = new ReadableWorkbook(fis);
            Sheet sheet = wb.getFirstSheet();
            List<Row> rows = sheet.read();
            ArrayList<Item> data = new ArrayList<Item>(rows.size());

            Log.d(TAG, "[parseSavedData]:" +
                "\nfile\t\t: " + file.toString() +
                "\ncan read\t: " + file.canRead() +
                "\ndata size\t: " + (file.length() > 0 ? Math.round(file.length() / 1024) + " kilobytes" : "EMPTY") +
                "\nrows\t\t: " + rows.size()
            );

            for (Row r : rows) {
                Item i = new Item(r);

                if (i.isValid()) {
                    data.add(new Item(r));
                } else {
                    Log.d(TAG, "[parseSavedData] row " + r.getRowNum() + " is invalid :: " + i.toString());
                }
            }

            Log.d(TAG, "[parseSavedData] parsing DONE, valid data rows: " + data.size() + " (invalid: " + (rows.size() - data.size()) + ")");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<Item>();
    }
}

interface VolleyCallback {
    void onSuccess();
    void onError();
}