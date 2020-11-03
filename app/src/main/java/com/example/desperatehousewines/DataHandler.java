
package com.example.desperatehousewines;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public class DataHandler {
    final String TAG = "DATA";

    final static String DATA_URL = "https://www.alko.fi/INTERSHOP/static/WFS/Alko-OnlineShop-Site/-/Alko-OnlineShop/fi_FI/Alkon%20Hinnasto%20Tekstitiedostona/alkon-hinnasto-tekstitiedostona.xlsx";
    final static String DATA_FILE = "data.xlsx";

    public DataHandler(Context c, VolleyCallback cb) {
        getData(c, cb);
    }

    private void getData (Context c, VolleyCallback cb) {
        Log.d(TAG, "[getData] init");

        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, DATA_URL,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        Log.d(TAG, "[getData] => [onResponse]");

                        saveRawData(c, response);
                        parseSavedData(c);

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

    private void parseSavedData(Context c) {
        try {
            File file = new File(c.getFilesDir(), DataHandler.DATA_FILE);
            InputStream fis = new FileInputStream(file);
            ReadableWorkbook wb = new ReadableWorkbook(fis);
            Sheet sheet = wb.getFirstSheet();
            List<Row> rows = sheet.read();

            Log.d(TAG,"[parseSavedData]\n" +
                    "\nparsing file: " + file.toString() +
                    "\ncan read: " + file.canRead() +
                    "\n" + (file.length() > 0 ? "data size: " + Math.round(file.length() / 1024) + " kilobytes" : ", but it's size is zero") +
                    "\nrows: " + rows.size()
                );

            for (Row r : rows) {
                String rowprint = "[parseSavedData] [" + r.getRowNum() + "]: ";

                Iterator<Cell> iter = r.iterator();

                while (iter.hasNext()) {
                    Cell cell = iter.next();
                    rowprint += (cell != null ? cell.getRawValue() : "NULL") + "\t";
                }

                Log.d(TAG, rowprint);

                if (r.getRowNum() > 1000) {
                    Log.e(TAG, "[parseSavedData] parse exited early");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

interface VolleyCallback {
    void onSuccess();
    void onError();
}