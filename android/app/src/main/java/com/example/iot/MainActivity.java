package com.example.iot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public  static String net ="net";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText ipAddress = findViewById(R.id.ip_address);
        final EditText port = findViewById(R.id.port);
        findViewById(R.id.hider_btn).setOnClickListener(v -> {
            String ipAddressStr = ipAddress.getText().toString().trim();
            String portNum = port.getText().toString().trim();
            if (ipAddressStr.isEmpty() || portNum.isEmpty()) {
                Toast.makeText(this, "Please key in the ip address and port number of the server", Toast.LENGTH_SHORT).show();
                return;
            }
            final Loading loading = new Loading(this);
            loading.show();
            HiderNet hiderNet = new HiderNet(this, ipAddressStr, portNum);
            new Thread(() -> {
                try {
                    final int id = hiderNet.addHider();
                    runOnUiThread(() -> {
                        if(loading.isShowing()){
                            loading.dismiss();
                        }
                        if (id != -1) {
                            Intent i = new Intent(this, HiderActivity.class);
                            i.putExtra(net, hiderNet.hostName);
                            i.putExtra("id", id);
                            startActivity(i);
                        }
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        if(loading.isShowing()){
                            loading.dismiss();
                        }
                        Toast.makeText(this, "looks like some there connection problem or you key in the wrong ip address", Toast.LENGTH_LONG).show();
                    });
                }
            }).start();

        });
        findViewById(R.id.seeker_btn).setOnClickListener(v -> {
            String ipAddressStr = ipAddress.getText().toString().trim();
            String portNum = port.getText().toString().trim();
            if (ipAddressStr.isEmpty() || portNum.isEmpty()) {
                Toast.makeText(this, "Please key in the ip address and port number of the server", Toast.LENGTH_SHORT).show();
                return;
            }
            final Loading loading = new Loading(this);
            SeekerNet seekerNet = new SeekerNet(this, ipAddressStr, portNum);
            new Thread(() -> {
                try {
                    if (seekerNet.addSeeker()) {
                        runOnUiThread(() -> {
                            if(loading.isShowing()){
                                loading.dismiss();
                            }
                            Intent i = new Intent(this, SeekerActivity.class);
                            i.putExtra("net", seekerNet.hostName);
                            startActivity(i);
                        });
                    } else {
                        runOnUiThread(loading::dismiss);
                    }
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        if(loading.isShowing()){
                            loading.dismiss();
                        }
                        Toast.makeText(this, "looks like some there connection problem or you key in the wrong ip address", Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
    }
}