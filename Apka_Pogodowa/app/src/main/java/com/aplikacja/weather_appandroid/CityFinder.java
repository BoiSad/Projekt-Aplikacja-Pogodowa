package com.aplikacja.weather_appandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CityFinder extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_finder);
        // Inicjalizacja widoków
        final EditText editText=findViewById(R.id.searchCity);
        ImageView backButton=findViewById(R.id.backButton);
        // Obsługa kliknięcia przycisku "backButton", zakończenie aktywności i powrót do poprzedniego ekranu
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Obsługa naciśnięcia przycisku "Enter" na klawiaturze
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Pobranie wpisanego miasta z pola tekstowego
                String newCity= editText.getText().toString();
                // Przekazanie wybranego miasta do aktywności MainActivity
                Intent intent=new Intent(CityFinder.this,MainActivity.class);
                intent.putExtra("City",newCity);
                startActivity(intent);



                return false;
            }
        });





    }
}