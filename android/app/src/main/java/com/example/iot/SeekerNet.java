package com.example.iot;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class SeekerNet implements  java.io.Serializable {
    private Activity activity;
    public final String hostName;
    private final String POST ="POST";
    SeekerNet(@NonNull Activity activity, String hostName, String port) {
        this.activity = activity;
        this.hostName = new StringBuilder("http://").append(hostName).append(':').append(port).toString();
    }
    SeekerNet(@NonNull Activity activity, String hostName) {
        this.activity = activity;
        this.hostName = hostName;
    }
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public boolean addSeeker() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(new StringBuilder(hostName).append(new char[]{'/', 'a', 'p', 'i', '/', 's', 'e', 'e', 'k', 'e','r'}, 0, 11).toString()).openConnection();
        conn.setDoInput(false);
        conn.setRequestMethod(POST);
        switch (conn.getResponseCode()){
            case 201:
                return true;
            case 403:
                activity.runOnUiThread(()->Toast.makeText(activity, "There already have seeker", Toast.LENGTH_SHORT).show());
                return false;
            default:
                activity.runOnUiThread(()->Toast.makeText(activity, "looks like something wrong with our server", Toast.LENGTH_SHORT).show());
                return false;
        }
    }
    public boolean startGame(final double latitude,final double longitude) throws  IOException{
        HttpURLConnection conn =(HttpURLConnection) new URL(new StringBuilder(hostName).append(new char[]{'/', 'a', 'p', 'i', '/', 's', 'e', 'e', 'k', 'e', 'r', '/', 's', 't', 'a', 'r', 't'},0,17).toString()).openConnection();
        conn.setDoInput(false);
        conn.setDoOutput(true);
        conn.setRequestMethod(POST);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        OutputStream out = conn.getOutputStream();
        out.write( new byte[] { 123, 34, 108, 97, 116, 105, 116, 117, 100, 101, 34, 58 },0,12);
        out.write(Double.toString(latitude).getBytes());
        out.write(new byte[] { 44, 34, 108, 111, 110, 103, 105, 116, 117, 100, 101, 34, 58 },0,13);
        out.write(Double.toString(longitude).getBytes());
        out.write(125);
        out.close();
        if(conn.getResponseCode()==204){
            return true;
        }else{
            activity.runOnUiThread(()->Toast.makeText(activity, "looks like something wrong with our server", Toast.LENGTH_SHORT).show());
            return false;
        }
    }
    public boolean endGame() throws  IOException{
        HttpURLConnection conn =(HttpURLConnection) new URL(new StringBuilder(hostName).append(new char[]{'/', 'a', 'p', 'i', '/', 's', 'e', 'e', 'k', 'e', 'r', '/', 'e', 'n', 'd'},0,15).toString()).openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(false);
        if(conn.getResponseCode()==204){
            return true;
        }else{
            activity.runOnUiThread(()->Toast.makeText(activity, "looks like something wrong with our server", Toast.LENGTH_SHORT).show());
            return false;
        }

    }
    public boolean deleteSeeker()throws IOException{
        HttpURLConnection conn =  (HttpURLConnection) new URL(new StringBuilder(hostName).append(new char[]{'/', 'a', 'p', 'i', '/', 'h', 'i', 'd', 'e', 'r'},0,10).toString()).openConnection();
        conn.setDoInput(false);
        conn.setRequestMethod("DELETE");
        if(conn.getResponseCode()==204){
            return true;
        }else{
            activity.runOnUiThread(()->Toast.makeText(activity, "looks like something wrong with our server", Toast.LENGTH_SHORT).show());
            return false;
        }


    }
}