package com.aplikacja.weather_appandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

//Stała przechowująca klucz API, który jest używany do autoryzacji żądań do API OpenWeatherMap.
    final String APP_ID = "ca94fdd312e9f99104b0636d30b4ac21";
    //Stała przechowująca adres URL do API OpenWeatherMap, służącego do pobierania danych pogodowych.
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
//Stała określająca minimalny czas w milisekundach pomiędzy aktualizacjami lokalizacji przez LocationManager.
    final long MIN_TIME = 5000;
    //Stała określająca minimalną odległość w metrach, po której nastąpi aktualizacja lokalizacji przez LocationManager.
    final float MIN_DISTANCE = 1000;
    //Stała reprezentująca kod żądania uprawnień do lokalizacji, który jest używany w funkcji
    final int REQUEST_CODE = 101;

//Zmienna przechowująca nazwę dostawcy lokalizacji, w tym przypadku
    String Location_Provider = LocationManager.GPS_PROVIDER;
//Deklaracje zmiennych reprezentujących różne widoki (TextView, ImageView, RelativeLayout) znajdujące się w układzie (layout)
    TextView NameofCity, weatherState, Temperature;
    ImageView mweatherIcon;

    RelativeLayout mCityFinder;

//Obiekt LocationManager, który zarządza dostępem do usług lokalizacyjnych urządzenia.
    LocationManager mLocationManager;
    //Obiekt LocationListener, który nasłuchuje zmian w lokalizacji i reaguje na nie.
    LocationListener mLocationListner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//Widoki są pobierane za pomocą metody findViewById, a następnie przypisywane do odpowiednich zmiennych.
        weatherState = findViewById(R.id.weatherCondition);
        Temperature = findViewById(R.id.temperature);
        mweatherIcon = findViewById(R.id.weatherIcon);
        mCityFinder = findViewById(R.id.cityFinder);
        NameofCity = findViewById(R.id.cityName);

// Obsługa kliknięcia na przycisk "mCityFinder", przeniesienie do ekranu wyszukiwania miasta
        mCityFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CityFinder.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent mIntent=getIntent();
        //pobieranie nazwy miasta
        String city= mIntent.getStringExtra("City");
        if(city!=null)
        {
            // Jeśli miasto zostało wybrane z ekranu wyszukiwania, pobierz pogodę dla nowego miasta
            getWeatherForNewCity(city);
        }
        else
        {
            // W przeciwnym razie pobierz pogodę dla bieżącej lokalizacji
            getWeatherForCurrentLocation();
        }


    }

    // Metoda pobierająca pogodę dla wybranego miasta
    private void getWeatherForNewCity(String city)
    {
        //służy do przekazywania parametrów do żądania HTTP. Jest to część biblioteki loopj Android Async HTTP,
        //która umożliwia wykonywanie asynchronicznych żądań HTTP w aplikacji Android.
        RequestParams params=new RequestParams();
        //q to nazwa miasta dla ktorego chemy pobrac informacje o pogodzie
        params.put("q",city);
        //"appid" to klucz dostępu do API pogodowego.
        params.put("appid",APP_ID);
        letsdoSomeNetworking(params);

    }



    // Metoda pobierająca pogodę dla bieżącej lokalizacji
    private void getWeatherForCurrentLocation() {

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                String Latitude = String.valueOf(location.getLatitude());
                String Longitude = String.valueOf(location.getLongitude());

                RequestParams params =new RequestParams();
                params.put("lat" ,Latitude);
                params.put("lon",Longitude);
                params.put("appid",APP_ID);
                letsdoSomeNetworking(params);




            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                //nie mozna dostac lokalizacji
            }
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Brak uprawnień do dostępu do lokalizacji, należy poprosić użytkownika o przyznanie uprawnień
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListner);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if(requestCode==REQUEST_CODE)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this,"Locationget Succesffully",Toast.LENGTH_SHORT).show();
                getWeatherForCurrentLocation();
            }
            else
            {
                // Użytkownik odrzucił uprawnienia
            }
        }


    }

    //tutaj łaczy sie z api
    // Metoda wykonująca żądanie sieciowe i pobierająca dane pogodowe
    private  void letsdoSomeNetworking(RequestParams params)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL,params,new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Toast.makeText(MainActivity.this,"Data Get Success",Toast.LENGTH_SHORT).show();

                weatherData weatherD=weatherData.fromJson(response);
                updateUI(weatherD);

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // Niepowodzenie pobierania danych
            }
        });



    }
    // Metoda aktualizująca interfejs użytkownika na podstawie danych pogodowych
    private  void updateUI(weatherData weather){


        Temperature.setText(weather.getmTemperature());
        NameofCity.setText(weather.getMcity());
        weatherState.setText(weather.getmWeatherType());
        int resourceID=getResources().getIdentifier(weather.getMicon(),"drawable",getPackageName());
        mweatherIcon.setImageResource(resourceID);


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager!=null)
        {
            mLocationManager.removeUpdates(mLocationListner);
        }
    }
}