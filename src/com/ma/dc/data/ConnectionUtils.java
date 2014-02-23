package com.ma.dc.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.ma.dc.Common;
import com.ma.dc.util.LogHelper;

final class ConnectionUtils {
    
    static String getJsonDataFromUrl(final String url, final int timeout) throws MalformedURLException, IOException {    	
        
        URL u = new URL(url);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("Content-length", "0");
        c.setUseCaches(false);
        c.setAllowUserInteraction(false);
        c.setConnectTimeout(timeout);
        c.setReadTimeout(timeout);
        c.connect();
        int status = c.getResponseCode();
        
        LogHelper.logDebug(ConnectionUtils.class, Common.LOG_TAG_NETWORK, "getJsonDataFromUrl", "status: " + status);
        
        switch (status) {
        case 200:
        case 201:    
            BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            return sb.toString();
        }
        return null;
    }
    
    public interface FileDownloadCallback {
        public void downloadDone(String url, boolean statusOk, String failureMessage);
    }
    
    public static void getFileFromServer(final AQuery aq, final String url, final File target,
            final FileDownloadCallback callbackObject) {
        
        aq.download(url, target, new AjaxCallback<File>(){
            
            public void callback(String url, File file, AjaxStatus status) {
                boolean statusOk = status.getError() == null;
                callbackObject.downloadDone(url, statusOk, statusOk ? null : status.getError());
            }       
    });
    }

    static int postJsonDataToUrl(final String url, final int timeout, String jsonString) throws MalformedURLException,
            IOException {
        URL u = new URL(url);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod("POST");
        c.setDoInput(true);
        c.setDoOutput(true);
        c.setUseCaches(false);
        c.setRequestProperty("Content-Type", "application/json");
        c.setAllowUserInteraction(false);
        c.setConnectTimeout(timeout);
        c.setReadTimeout(timeout);
        c.connect();

        OutputStream stream = c.getOutputStream();
        stream.write(jsonString.getBytes());
        stream.flush();
        stream.close();

        return c.getResponseCode();
    }
}
