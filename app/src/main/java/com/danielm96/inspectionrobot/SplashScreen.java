package com.danielm96.inspectionrobot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
SplashScreen

Klasa wywoływana przy uruchamianiu aplikacji. Jej zadaniem jest wyświetlenie ekranu powitalnego (splash screen),
a następnie uruchomienie MainActivity, czyli głównej aktywnośc aplikacji.

Klasa dziedziczy po klasie AppCompatActivity.
 */

public class SplashScreen extends AppCompatActivity {
    // Wywołanie metody onCreate
    // Metoda onCreate jest wywoływana zawsze przy tworzeniu aktywności.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Jako pierwsza instrukcja, wywoływana jest metoda onCreate z klasy nadrzędnej.
        // Jest to standardowe działanie dla języka Java.
        super.onCreate(savedInstanceState);
        /*
        Utworzenie obiektu klasy Intent
        Intent (intencja) stanowi abstrakcyjny opis akcji, jaka ma zostać wykonana.
        Może to być np. uruchomienie nowej aktywności lub komunikacja z usługą w tle.
        Konstruktor jako pierwszy parametr przyjmuje kontekst, jako drugi - klasę.
        */
        Intent intent = new Intent(this, MainActivity.class);
        // Uruchomienie nowej aktywności przy użyciu utworzonego obiektu intent
        startActivity(intent);
        // Zakończenie bieżącej aktywności
        finish();
    }
}
