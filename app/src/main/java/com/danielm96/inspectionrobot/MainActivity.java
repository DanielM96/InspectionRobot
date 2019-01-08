package com.danielm96.inspectionrobot;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    // Utworzenie podstawowych obiektów

    // Stan socketu HTTP
    boolean socketStatus = false;

    // Socket HTTP - wykorzystywany do wysyłania danych do serwera
    Socket socket;

    // Domyślny adres IP robota
    String address = "192.168.43.220";

    // Części składowe żądania wysyłanego do serwera podczas zmiany ustawień kamery
    final String messagePrefix = "stream";
    final String messagePlen = "?plen=undefined";
    final String messageCameraQl = "&ql=";

    // Domyślna jakość kamery
    int cameraQuality = 3;

    // Port, na którym uruchomiony jest serwer - musi być zgodny z programem dla ArduCam
    // Tutaj używany jest port domyślny
    // int port = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Uruchomienie aplikacji
        startApp();
    }

    // Wyśwletlenie okna z ustawieniem adresu IP serwera
    private void showSettings() {
        // Utworzenie okna dialogowego typu AlertDialog
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        // Ustawienie tytułu okna i tekstu opisu
        alert.setTitle(R.string.settings_title);
        alert.setMessage(R.string.settings_message);

        // Utworzenie pola tekstowego
        final EditText input = new EditText(this);
        alert.setView(input);

        // Akcja wykonywana po naciśnięciu przycisku OK
        alert.setPositiveButton(R.string.settings_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Pobierz wpisany adres z pola tekstowego i zamień na String
                address = input.getText().toString();

                // Czyszczenie socketu przed ponownym użyciem
                socket = null;
                // Utworzenie obiektu klasy ClientTask i wykonanie metody execute (wysłanie żądania)
                ClientTask connect = new ClientTask(address);
                connect.execute("CON");
                Log.d("showSettings", "Sent command CON");

                // Utworzenie obiektu klasy VideoView i jego uruchomienie po zmianie adresu IP
                VideoView videoView;
                videoView = findViewById(R.id.videoView);
                startVideoView(videoView);
            }
        });

        // Akcja wykonywana po naciśnięciu przycisku Anuluj
        alert.setNegativeButton(R.string.settings_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, getString(R.string.settings_defaultip)+address, Toast.LENGTH_SHORT).show();
                Log.d("showSettings", "No IP changes commited");
            }
        });

        // Utworzenie i wyświetlenie okna
        alert.create();
        alert.show();
    }

    // Wyświetlenie okna z ustawieniem rozdzielczości kamery
    private void showCameraSettings() {
        // Utworzenie okna dialogowego
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        // Ustawienie tytułu
        alert.setTitle(R.string.camerasettings_title);

        // Tablica zawierająca dostępne rozdzielczości
        // Rozdzielczości znajdują się w zasobach aplikacji (katalog res\strings)
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

        // Pomocniczy obiekt przechowujący wybraną opcję
        final int[] selectedItem = new int[1];

        // Pomocnicza zmienna przechowująca tekst odpowiadający wybranej rozdzielczości
        int checkedItem = cameraQuality;

        // Utworzenie listy pojedynczego wyboru
        // Pierwszy parametr to tablica z wartościami, które mają zostać wyświetlone
        // Drugi parametr to wartość domyślna
        // Trzeci parametr to obiekt, który implementuje metodę onClick, reagującą na kliknięcie przycisku
        alert.setSingleChoiceItems(qualityValues, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do pomocniczej tablicy zapisz wartość numeru pola na liście
                selectedItem[0] = i;
            }
        });

        // Akcja wykonywana po naciśnięciu przycisku OK
        alert.setPositiveButton(R.string.settings_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Pobieranie z listy wyboru etykiety (tekstu) wybranego elementu
                ListView listView = ((AlertDialog)dialog).getListView();
                Object checkedItem = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                Log.d("showCameraSettings", "Selected quality "+checkedItem);
                // Zmiana aktualnej rozdzielczości kamery
                cameraQuality = selectedItem[0];

                // Budowa żądania wysyłanego do serwera
                // Żądanie wygląda następująco:
                // stream?plen=undefined&ql=x, gdzie x = cameraQuality
                String message = messagePrefix + messagePlen + messageCameraQl + cameraQuality;
                // Wysłanie żądania
                ClientTask changeCameraQuality = new ClientTask(address);
                changeCameraQuality.execute(message);

                // Wyświetlenie dymku z informacją o nowej rozdzielczości
                Toast.makeText(MainActivity.this, getString(R.string.camerasettings_newquality)+checkedItem.toString(),Toast.LENGTH_SHORT).show();

                Log.d("showCameraSettings", "Message sent: "+message);
                Log.d("showCameraSettings", "Sent camera quality change command");
            }
        });

        // Akcja wykonywana po naciśnięciu przycisku Anuluj
        alert.setNegativeButton(R.string.settings_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, getString(R.string.camerasettings_previousquality)+qualityValues[cameraQuality],Toast.LENGTH_SHORT).show();
                Log.d("showCameraSettings", "No camera changes commited");
            }
        });

        // Utworzenie i wyświetlenie okna
        alert.create();
        alert.show();
    }

    // Funkcja uruchamiająca podgląd wideo
    private void startVideoView(VideoView view) {
        // Adres, pod którym jest transmisja
        String httpLiveUrl = address + "/" + messagePrefix;

        // Ustawienie adresu
        view.setVideoURI(Uri.parse(httpLiveUrl));
        // Utworzenie obiektu klasy MediaController
        MediaController mediaController = new MediaController(this);
        // Ustawienie kontrolera
        view.setMediaController(mediaController);
        // Zażadanie fokusu na VideoView
        view.requestFocus();
        // Uruchomienie transmisji
        view.start();
        Log.d("startApp", "Started VideoView");
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

        // Sprawdzenie, czy socket HTTP nie jest pusty
        if (socketStatus) {
            // Socket nie jest pusty
            Toast.makeText(MainActivity.this, getString(R.string.socketNotNull), Toast.LENGTH_SHORT).show();
        } else {
            // Wyczyść socket
            socket = null;
            // Wysyłanie komendy informującej serwer o połączeniu z klientem
            ClientTask connect = new ClientTask(address);
            connect.execute("CON");
            Log.d("startApp", "Sent command CON");
        }

        // Tworzenie wątku, który zostanie wykorzystany później
        final Handler mHandler = new Handler();

        // Wątki wykonywane przy naciskaniu bądź puszczaniu przycisków
        // Dla pozostałych obiektów typu Runnable wykonywane są te same instrukcje, żądanie jest inne

        // Zatrzymanie ruchu
        final Runnable stopMove = new Runnable() {
            @Override
            public void run() {
                // Utworzenie żądania
                String msg = "STOP";
                // Wysłanie żądania
                ClientTask task = new ClientTask(address);
                task.execute(msg);
                Log.d("startApp", "Sent command "+msg);
            }
        };

        // Ruch do przodu
        final Runnable moveForward = new Runnable() {
            @Override
            public void run() {
                String msg = "FORWARD";
                ClientTask task = new ClientTask(address);
                task.execute(msg);
                Log.d("startApp", "Sent command "+msg);
            }
        };

        // Ruch do tyłu
        final Runnable moveBackward = new Runnable() {
            @Override
            public void run() {
                String msg = "BACKWARD";
                ClientTask task = new ClientTask(address);
                task.execute(msg);
                Log.d("startApp", "Sent command "+msg);
            }
        };

        // Ruch w lewo
        final Runnable moveLeft = new Runnable() {
            @Override
            public void run() {
                String msg = "LEFT";
                ClientTask task = new ClientTask(address);
                task.execute(msg);
                Log.d("startApp", "Sent command "+msg);
            }
        };

        // Ruch w prawo
        final Runnable moveRight = new Runnable() {
            @Override
            public void run() {
                String msg = "RIGHT";
                ClientTask task = new ClientTask(address);
                task.execute(msg);
                Log.d("startApp", "Sent command "+msg);
            }
        };

        // Zdefiniowanie akcji wykonywanych podczas dotykania bądź puszczania przycisków
        // Dla pozostałych przycisków odpowiedzialnych za sterowanie, instrukcje są analogiczne
        btnForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    // Przycisk został wciśnięty
                    case MotionEvent.ACTION_DOWN:
                        // Po 10 ms wykonaj wątek moveForward
                        mHandler.postDelayed(moveForward, 10);
                        // Usuń wszystkie oczekujące w kolejce instancje wątku stopMove
                        mHandler.removeCallbacks(stopMove);
                        return true;

                    // Przycisk został puszczony
                    case MotionEvent.ACTION_UP:
                        // Po 10 ms wykonaj wątek stopMove
                        mHandler.postDelayed(stopMove, 10);
                        // Usuń wszystkie oczekujące w kolejce instancje wątku moveForward
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

        // Ustawienie akcji wykonywanej po naciśnięciu przycisku ustawień
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Wyświetl dialog z ustawieniem adresu IP
                showSettings();
            }
        });

        btnCameraSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Wyświetl dialog z ustawieniem rozdzielczości kamery
                showCameraSettings();
            }
        });

        // Tworzenie obiektu klasy VideoView, który pozwoli na wyświetlenie obrazu z kamery
        VideoView videoView;
        videoView = findViewById(R.id.videoView);

        // Przygotowanie i uruchomienie podglądu
        startVideoView(videoView);
    }

    /**
     * ClientTask
     *
     * Klasa służąca do komunikacji za pośrednictwem protokołu HTTP.
     *
     * Klasa dziedziczy po klasie AsyncTask, która pozwala na wykonywanie w tle krótkich operacji,
     * bez konieczności bezpośredniego operowania na wątkach (jest dla nich klasą pomocniczą).
     *
     * Parametry:
     * 1. Typ danych wysyłanych do zadania,
     * 2. Typ danych zwracanych podczas wykonywania zadania, jako miara jego postępu (Void - nieużywane),
     * 3. Typ danych zwracanych przez zadanie.
     *
     * Wywoływanie:
     * new ClientTask(ip_address).execute("String");
     */
    public static class ClientTask extends AsyncTask<String, Void, String> {
        String server;

        // Konstruktor - jako parametr przyjmowany jest adres IP serwera
        ClientTask(String server) {
            this.server = server;
        }

        // Operacje wykonywane w tle
        // Metoda doInBackground nie może być wywołana ręcznie.
        @Override
        protected String doInBackground(String... params) {
            // Alternatywna metoda budowania łańcucha tekstowego
            StringBuilder chain = new StringBuilder();

            // Pobieranie pierwszego z parametrów metody i dodanie go łańcucha z adresem
            final String val = params[0];
            final String p = "http://" + server + "/" + val;

            // Zmienna przechowująca odpowiedź serwera
            String serverResponse = "";

            // Wysyłanie żądania do serwera
            try {
                // Rzutowanie łancucha p na URL
                URL url = new URL(p);
                // Otwieranie połączenia
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                // Ustawienie metody żądania na GET
                connection.setRequestMethod("GET");
                // Nawiązywanie połączenia
                connection.connect();
                // Tworzenie strumienia wejścia
                InputStream inputStream = connection.getInputStream();

                // Odczytywanie odpowiedzi
                // Utworzenie obiektu klasy BufferedReader, który pozwala na odczyt tekstu liniami
                BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = rd.readLine()) != null) {
                    chain.append(line);
                }
                // Zamykanie strumienia wejścia
                inputStream.close();

                System.out.println("Chain: "+chain.toString());

                // Rozłączanie z serwerem
                connection.disconnect();

            } catch (IOException e) {
                // Obsługa wyjątku IOException
                // Wyjątek jest zwracany, gdy nie jest możliwa komunikacja (głównie brak sieci)
                Log.e("AsyncTask", "NO NETWORK!");
                serverResponse = e.getMessage();
            }

            return serverResponse;
        }
    }
}
