package com.example.iot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.socket.client.Socket;
import io.socket.client.IO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class SeekerActivity extends AppCompatActivity {
    private TextView message = null;
    private Button button = null;
    private ProgressBar loadingOnScreen = null;
    private SeekerNet seekerNet = null;
    private Loading loading = null;
    private Socket mSocket;
    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loading = new Loading(this);
        String hostName = getIntent().getStringExtra(MainActivity.net);
        seekerNet = new SeekerNet(this, hostName);
        try {
            mSocket = IO.socket(hostName).connect();
            setContentView(R.layout.activity_seeker);
            message = findViewById(R.id.message);
            button = findViewById(R.id.seeker_btn);
            loadingOnScreen = findViewById(R.id.loading_on_screen);
            button.setOnClickListener(v -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    final LocationTracker locationTracker = new LocationTracker(this);
                    if (locationTracker.canGetLocation()) {
                        loading.show();
                        new Thread(() -> {
                            try {
                                locationTracker.getLocation();
                                if (seekerNet.startGame(locationTracker.getLatitude(),locationTracker.getLongitude())) {
                                    mSocket.once("allFound", o2 -> {
                                        mSocket.once("gameEnd", o -> {
                                            final JSONObject jsonObject = (JSONObject) o[0];
                                            runOnUiThread(() -> {
                                                try {
                                                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                finish();
                                            });
                                        });
                                        runOnUiThread(() -> {
                                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                            loadingOnScreen.setVisibility(View.INVISIBLE);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                                            } else {
                                                //deprecated in API 26
                                                vibrator.vibrate(1000);
                                            }
                                            message.setText(new char[]{'A', 'l', 'l', ' ', 'h', 'i', 'd', 'e', 'r', 's', ' ', 'h', 'a', 'v', 'e', ' ', 'f', 'o', 'u', 'n', 'd', ' ', 't', 'h', 'e', 'i', 'r', ' ', 'h', 'i', 'd', 'i', 'n', 'g', ' ', 's', 'p', 'o', 'r', 't', '!', ' ', 'F', 'i', 'n', 'd', ' ', 't', 'h', 'e', 'm', '!'}, 0, 52);
                                            timer = new Timer();
                                            timer.scheduleAtFixedRate(new TimerTask() {
                                                private final String latitude ="latitude";
                                                private final String longitude ="longitude";
                                                private final String seekerPosition = "seekerPosition";
                                                @Override
                                                public void run() {
                                                    locationTracker.getLocation();
                                                    JSONObject jsonObject = new JSONObject();
                                                    try {
                                                        jsonObject.put(latitude, locationTracker.getLatitude());
                                                        jsonObject.put(longitude, locationTracker.getLongitude());
                                                        mSocket.emit(seekerPosition, jsonObject);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            }, 0, 2000);


                                        });
                                    });
                                    runOnUiThread(() -> {
                                        if(loading.isShowing()){
                                            loading.dismiss();
                                        }
                                        message.setText(new char[]{'P', 'l', 'e', 'a', 's', 'e', ' ', 'w', 'a', 'i', 't', ' ', 'f', 'o', 'r', ' ', 'a', 'l', 'l', ' ', 'h', 'i', 'd', 'e', 'r', 's', ' ', 't', 'o', ' ', 'f', 'i', 'n', 'd', ' ', 't', 'h', 'e', 'i', 'r', ' ', 'h', 'i', 'd', 'i', 'n', 'g', ' ', 's', 'p', 'o', 't'}, 0, 52);
                                        loadingOnScreen.setVisibility(View.VISIBLE);
                                        button.setVisibility(View.INVISIBLE);
                                    });
                                } else {
                                    runOnUiThread(loading::dismiss);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                runOnUiThread(() -> {
                                    if(loading.isShowing()){
                                        loading.dismiss();
                                    }
                                    Toast.makeText(this, "looks like some there connection problem or you key in the wrong ip address", Toast.LENGTH_LONG).show();
                                });
                            }
                        }).start();
                    } else {
                        Toast.makeText(this, "looks like your gps is disabled", Toast.LENGTH_LONG).show();
                    }

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Toast.makeText(this, "Looks like something wrong with socket", Toast.LENGTH_LONG).show();
            new Thread(() -> {
                try {
                    seekerNet.deleteSeeker();
                } catch (IOException ignored) {
                }
            }).start();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Quit game?")
                .setMessage("By exiting you are ending the game are sure you want to end the game?")
                .setPositiveButton("Yes", (d, w) -> {
                    endGame(null);
                })
                .setNegativeButton("No", null).show();

    }

    public void endGame(View v) {
        new Thread(() -> {
            try {
                seekerNet.endGame();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.close();
        if(timer!=null){
            timer.cancel();
        }
    }
}