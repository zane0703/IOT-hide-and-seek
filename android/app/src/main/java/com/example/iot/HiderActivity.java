package com.example.iot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

import io.socket.client.Socket;
import io.socket.client.IO;

public class HiderActivity extends AppCompatActivity {
    private TextView message = null;
    private Button button = null;
    private ProgressBar loadingOnScreen = null;
    private HiderNet hiderNet = null;
    private Loading loading = null;
    private Socket mSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hider);
        loading = new Loading(this);
        Intent intent = getIntent();
        final String hostName = intent.getStringExtra(MainActivity.net);
        final int id = intent.getIntExtra("id", -1);
        hiderNet = new HiderNet(this, hostName, id);
        hiderNet.setActivity(this);
        message = findViewById(R.id.message);
        button = findViewById(R.id.hider_btn);
        loadingOnScreen = findViewById(R.id.loading_on_screen);
        try {
            mSocket = IO.socket(hostName).connect();
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
            mSocket.once("start", o -> {
                runOnUiThread(() -> {
                    loadingOnScreen.setVisibility(View.INVISIBLE);
                    message.setText(new char[]{'P', 'l', 'e', 'a', 's', 'e', ' ', 'f', 'i', 'n', 'd', ' ', 'y', 'o', 'u', 'r', ' ', 'h', 'i', 'd', 'i', 'n', 'g', ' ', 's', 'p', 'o', 'r', 't'}, 0, 29);
                    button.setVisibility(View.VISIBLE);
                    button.setOnClickListener(v -> {
                        new AlertDialog.Builder(this)
                                .setTitle("Confirm hiding sport")
                                .setPositiveButton("Yes", (w2, d2) -> {
                                    loading.show();
                                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        new Thread(() -> {
                                            try {
                                                if (hiderNet.foundSport()) {
                                                    mSocket.once("allFound", o2 -> {
                                                        runOnUiThread(() -> {
                                                            loadingOnScreen.setVisibility(View.INVISIBLE);
                                                            message.setText(new char[]{'S', 't', 'a', 'y', ' ', 'Q', 'u', 'i', 'e', 't', ' ', 'a', 'n', 'd', ' ', 'O', 'u', 't', ' ', 'o', 'f', ' ', 'S', 'i', 'g', 'h', 't', '!'}, 0, 28);
                                                            button.setText(new char[]{'I', ' ', 'g', 'o', 't', ' ', 'c', 'a', 'u', 'g', 'h', 't', '!'}, 0, 13);
                                                            button.setOnClickListener(v2 -> {
                                                                new AlertDialog.Builder(this)
                                                                        .setTitle("been caught?")
                                                                        .setPositiveButton("Yes", (dialog, which) -> {
                                                                            loading.show();
                                                                            new Thread(() -> {
                                                                                try {
                                                                                    if (hiderNet.deleteHider(true)) {
                                                                                        runOnUiThread(() -> {
                                                                                            if(loading.isShowing()){
                                                                                                loading.dismiss();
                                                                                            }
                                                                                            message.setText(new char[]{'Y', 'o', 'u', ' ', 'h', 'a', 'v', 'e', ' ', 'b', 'e', 'e', 'n', ' ', 'c', 'a', 'u', 'g', 'h', 't', '!', ' ', 'P', 'l', 'e', 'a', 's', 'e', ' ', 'w', 'a', 'i', 't', ' ', 'f', 'o', 'r', ' ', 't', 'h', 'e', ' ', 'g', 'a', 'm', 'e', ' ', 't', 'o', ' ', 'b', 'e', ' ', 'e', 'n', 'd'}, 0, 56);
                                                                                            button.setVisibility(View.INVISIBLE);
                                                                                            loadingOnScreen.setVisibility(View.VISIBLE);
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

                                                                        })
                                                                        .setNegativeButton("No", null).show();
                                                            });
                                                            button.setVisibility(View.VISIBLE);
                                                        });
                                                    });
                                                    runOnUiThread(() -> {
                                                        loading.dismiss();
                                                        button.setVisibility(View.INVISIBLE);
                                                        message.setText(new char[]{'P', 'l', 'e', 'a', 's', 'e', ' ', 'w', 'a', 'i', 't', ' ', 'f', 'o', 'r', ' ', 'e', 'v', 'e', 'r', 'y', 'o', 'n', 'e'}, 0, 24);
                                                        loadingOnScreen.setVisibility(View.VISIBLE);
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
                                        if(loading.isShowing()){
                                            loading.dismiss();
                                        }
                                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                                    }
                                }).setNegativeButton("No", null).show();
                    });
                });
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Toast.makeText(this, "Looks like somthing wrong with socket", Toast.LENGTH_LONG).show();
            new Thread(() -> {
                try {
                    hiderNet.deleteHider(false);
                } catch (IOException ignored) {
                }
            }).start();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            button.callOnClick();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Quit game?")
                .setTitle("Are sure you want to quit game?")
                .setPositiveButton("Yes", (d, w) -> {
                    new Thread(() -> {
                        try {
                            hiderNet.deleteHider(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    finish();
                })
                .setNegativeButton("No", null).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.close();
    }
}