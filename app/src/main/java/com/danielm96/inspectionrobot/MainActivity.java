package com.danielm96.inspectionrobot;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    boolean socketStatus = false;

    Socket socket;

    String address = "192.168.43.220";

    int cameraQuality = 3;

    // int port = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startApp();
    }

    private void showSettings() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.settings_title);
        alert.setMessage(R.string.settings_message);

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(R.string.settings_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                address = input.getText().toString();
            }
        });

        alert.setNegativeButton(R.string.settings_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, R.string.settings_defaultip+address, Toast.LENGTH_SHORT).show();
            }
        });

        alert.create();
        alert.show();
    }

    private void showCameraSettings() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.camerasettings_title);

        final String[] qualityValues = {
                getString(R.string.camerasettings_quality0),
                getString(R.string.camerasettings_quality1),
                getString(R.string.camerasettings_quality2),
                getString(R.string.camerasettings_quality3),
                getString(R.string.camerasettings_quality4),
                getString(R.string.camerasettings_quality5),
                getString(R.string.camerasettings_quality6),
                getString(R.string.camerasettings_quality7),
                getString(R.string.camerasettings_quality8)
        };

        int checkedItem = cameraQuality;

        alert.setSingleChoiceItems(qualityValues, checkedItem, null);

        alert.setPositiveButton(R.string.settings_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView listView = ((AlertDialog)dialog).getListView();
                Object checkedItem = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                cameraQuality = (int)checkedItem;
            }
        });

        alert.setNegativeButton(R.string.settings_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, R.string.camerasettings_previousquality+qualityValues[cameraQuality],Toast.LENGTH_SHORT).show();
            }
        });

        alert.create();
        alert.show();
    }

    // Funkcja uruchamiająca właściwą część aplikacji
    private void startApp() {
        ImageButton btnForward,
                btnBackward,
                btnLeft,
                btnRight,
                btnSettings,
                btnCameraSettings;

        // Tworzenie przycisków
        btnForward = findViewById(R.id.btn_Forward);
        btnBackward = findViewById(R.id.btn_Backward);
        btnLeft = findViewById(R.id.btn_Left);
        btnRight = findViewById(R.id.btn_Right);
        btnSettings = findViewById(R.id.btn_Settings);
        btnCameraSettings = findViewById(R.id.btn_CameraSettings);

        if (socketStatus) {
            Toast.makeText(MainActivity.this, R.string.socketNotNull, Toast.LENGTH_SHORT).show();
        } else {
            socket = null;
            ClientTask connect = new ClientTask(address);
            connect.execute("CON");
        }

        final Handler mHandler = new Handler();

        final Runnable stopMove = new Runnable() {
            @Override
            public void run() {
                String msg = "STOP";
                ClientTask task = new ClientTask(address);
                task.execute(msg);
            }
        };

        final Runnable moveForward = new Runnable() {
            @Override
            public void run() {
                String msg = "FORWARD";
                ClientTask task = new ClientTask(address);
                task.execute(msg);
            }
        };

        final Runnable moveBackward = new Runnable() {
            @Override
            public void run() {
                String msg = "BACKWARD";
                ClientTask task = new ClientTask(address);
                task.execute(msg);
            }
        };

        final Runnable moveLeft = new Runnable() {
            @Override
            public void run() {
                String msg = "LEFT";
                ClientTask task = new ClientTask(address);
                task.execute(msg);
            }
        };

        final Runnable moveRight = new Runnable() {
            @Override
            public void run() {
                String msg = "RIGHT";
                ClientTask task = new ClientTask(address);
                task.execute(msg);
            }
        };

        btnForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mHandler.postDelayed(moveForward, 10);
                        mHandler.removeCallbacks(stopMove);
                        return true;

                    case MotionEvent.ACTION_UP:
                        mHandler.postDelayed(stopMove, 10);
                        mHandler.removeCallbacks(moveForward);
                        return true;
                }
                return true;
            }
        });

        btnBackward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mHandler.postDelayed(moveBackward, 10);
                        mHandler.removeCallbacks(stopMove);
                        return true;

                    case MotionEvent.ACTION_UP:
                        mHandler.postDelayed(stopMove, 10);
                        mHandler.removeCallbacks(moveBackward);
                        return true;
                }
                return true;
            }
        });

        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mHandler.postDelayed(moveLeft, 10);
                        mHandler.removeCallbacks(stopMove);
                        return true;

                    case MotionEvent.ACTION_UP:
                        mHandler.postDelayed(stopMove, 10);
                        mHandler.removeCallbacks(moveLeft);
                        return true;
                }
                return true;
            }
        });

        btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mHandler.postDelayed(moveRight, 10);
                        mHandler.removeCallbacks(stopMove);
                        return true;

                    case MotionEvent.ACTION_UP:
                        mHandler.postDelayed(stopMove, 10);
                        mHandler.removeCallbacks(moveRight);
                        return true;
                }
                return true;
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettings();
            }
        });

        btnCameraSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCameraSettings();
            }
        });

        // VideoView
        VideoView videoView;
        videoView = findViewById(R.id.videoView);

        String httpLiveUrl = address + "/stream";

        videoView.setVideoURI(Uri.parse(httpLiveUrl));
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
        videoView.start();
    }

    /**
     * ClientTask
     *
     * Klasa służąca do komunikacji za pośrednictwem protokołu HTTP.
     */
    public static class ClientTask extends AsyncTask<String, Void, String> {
        String server;

        ClientTask(String server) {
            this.server = server;
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder chain = new StringBuilder();

            final String val = params[0];
            final String p = "http://" + server + val;

            String serverResponse = "";

            try {
                URL url = new URL(p);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                InputStream inputStream = connection.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = rd.readLine()) != null) {
                    chain.append(line);
                }
                inputStream.close();

                System.out.println("Chain: "+chain.toString());

                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
                serverResponse = e.getMessage();
            }

            return serverResponse;
        }
    }
}
