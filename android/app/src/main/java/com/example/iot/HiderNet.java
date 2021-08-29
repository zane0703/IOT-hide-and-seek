package com.example.iot;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class HiderNet implements  java.io.Serializable {
    private Activity activity;
    public  final String hostName;
    private final int id;
    private final String POST = "POST";
    private final String APP_JSON ="application/json; charset=utf-8";
    private final String CONTENT_TYPE = "Content-Type";
    HiderNet(@NonNull Activity activity,String hostName,String port){
        this.activity = activity;
        this.hostName = new StringBuilder("http://").append(hostName).append(':').append(port).toString();
        this.id =0;
    }
    HiderNet(@NonNull Activity activity,String hostName,int id){
        this.activity = activity;
        this.hostName = hostName;
        this.id = id;
    }
    public int addHider() throws IOException {
        HttpURLConnection conn =  (HttpURLConnection) new URL(new StringBuilder(hostName).append(new char[]{'/', 'a', 'p', 'i', '/', 'h', 'i', 'd', 'e', 'r'},0,10).toString()).openConnection();
        conn.setRequestMethod(POST);
        switch (conn.getResponseCode()){
            case 201:
                Scanner in = new Scanner(conn.getInputStream());
                int id = in.nextInt();
                in.close();
                return id;
            case 409:
                activity.runOnUiThread(()-> Toast.makeText(activity, "The game already started", Toast.LENGTH_LONG).show());
                return -1;
            default:
                activity.runOnUiThread(()->Toast.makeText(activity, "looks like something wrong with our server", Toast.LENGTH_SHORT).show());
                return -1;
        }
    }
    public void setActivity(Activity activity){
        this.activity =activity;
    }
    public boolean foundSport() throws IOException{
        LocationTracker locationTracker = new LocationTracker(activity);
        if(locationTracker.canGetLocation()){
            locationTracker.getLocation();
            HttpURLConnection conn = (HttpURLConnection) new URL(new StringBuilder(hostName)
                    .append(new char[]{'/', 'a', 'p', 'i', '/', 'h', 'i', 'd', 'e', 'r', '/'},0,11)
                    .append(id)
                    .append(new char[]{'/', 'l', 'o', 'c', 'a', 't', 'i', 'o', 'n'},0,9).toString()).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(false);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty(CONTENT_TYPE, APP_JSON);
            OutputStream out = conn.getOutputStream();
            out.write( new byte[] { 123, 34, 108, 97, 116, 105, 116, 117, 100, 101, 34, 58 },0,12);
            out.write(Double.toString(locationTracker.getLatitude()).getBytes());
            out.write(new byte[] { 44, 34, 108, 111, 110, 103, 105, 116, 117, 100, 101, 34, 58 },0,13);
            out.write(Double.toString(locationTracker.getLongitude()).getBytes());
            out.write(125);
            out.close();
            switch (conn.getResponseCode()){
                case 204 :
                    return true;
                case 422:
                    activity.runOnUiThread(()->Toast.makeText(activity, "Your Hiding sport too far\nPlease find a new hiding sport", Toast.LENGTH_SHORT).show());
                    return false;
                default:
                    activity.runOnUiThread(()->Toast.makeText(activity, "looks like something wrong with our server", Toast.LENGTH_SHORT).show());
                    return false;
            }
        }else{
            activity.runOnUiThread(()->Toast.makeText(activity, "looks like your gps is disabled", Toast.LENGTH_LONG).show());
            return false;
        }

    }
    public boolean deleteHider(boolean caught)throws IOException{
        HttpURLConnection conn =  (HttpURLConnection) new URL(new StringBuilder(hostName).append(new char[]{'/', 'a', 'p', 'i', '/', 'h', 'i', 'd', 'e', 'r','/'},0,11).append(id).toString()).openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(false);
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty(CONTENT_TYPE, APP_JSON);
        OutputStream out = conn.getOutputStream();
        out.write(new  byte[] { 123, 34, 99, 97, 117, 103, 104, 116, 34, 58 },0,10);
        out.write(caught?new byte[] { 116, 114, 117, 101 }:new byte[] { 102, 97, 108, 115, 101 });
        out.write(125);
        out.flush();
        out.close();
        if(conn.getResponseCode()==204){
            return true;
        }else{
            activity.runOnUiThread(()->Toast.makeText(activity, "looks like something wrong with our server", Toast.LENGTH_SHORT).show());
            return false;
        }


    }
}
