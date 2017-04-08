package com.macapps.developer.maptrash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    //Todo list CCC Decodificar los puntos de la lista de FB;

    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    Object strRoute;
    private Object o;
    private String string;
    private Boolean ready = false;

    /*
    TODO ya se estableció como hacer las rutas, ahora necesito subir las rutas a la nube y que se descargue segun se requiera


    La geocodificación es una tarea que consume tiempo y recursos. Siempre que sea posible, realiza una geocodificación previa de las direcciones conocidas (usando la Google Maps Geocoding API que se describe aquí u otro servicio de geocodificación) y guarda tus resultados en un caché temporal de tu propio diseño.



     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");


        if (android.os.Build.VERSION.SDK_INT >= M) {
            checkLocationPermission();
        }
        // Initializing
        MarkerPoints = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                o = dataSnapshot.getValue(Object.class);
                string = o.toString();
                // Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
                ready = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        ///     .findFragmentById(R.id.map);
        /// mapFragment.getMapAsync(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        // Setting onclick event listener for the map

    }

    private class DrawFromServer extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... params) {
            if (ready) {
                JSONArray jsonArray;
                JSONArray jsonArray1;
                HashMap<String, String> hashMap = new HashMap<>();
                List<HashMap> hashMaps = new ArrayList<>();
                try {

                    jsonArray = new JSONArray(string);
                    jsonArray1 = jsonArray.getJSONArray(0);
                    String data = "";
                    for (int i = 0; i < jsonArray1.length(); i++) {
                        JSONObject jsonObject = jsonArray1.getJSONObject(i);
                        String lat = jsonObject.getString("lat");
                        String lng = jsonObject.getString("lng");
                        hashMap.put("lat", lat);
                        hashMap.put("lng", lng);
                        hashMaps.add(hashMap);
                    }
                    try {
                        Log.d("HshMAp", "Entrando...");
                        for (HashMap<String, String> hashMap1 : hashMaps) {
                            for (String str : hashMap1.values()) {
                                Double lat = Double.parseDouble(str);


                            }
                        }
                        //  textView.setText(data);

                    } catch (Exception e) {
                        Log.e("String Parse Error", e.toString());
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Parse Error", e.toString());
                }

            }
            return null;
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {//Entra una cadena y sale una lista de listas en HashMap

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                //   myRef3.setValue(jObject);
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            myRef.setValue(routes);
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            // String helper;
            //helper=myRef.

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
                //     getRoutes();
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    public void onClickSrvrDwnld(View view) {

        if (ready) {
            Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show();
            ArrayList<LatLng> latLngs;
            JSONArray jsonArray;
            JSONArray jsonArray1;

            List<HashMap> hashMaps = new ArrayList<>();
            try {

                jsonArray = new JSONArray(string);
                jsonArray1 = jsonArray.getJSONArray(0);

                for (int i = 0; i < jsonArray1.length(); i++) {
                    JSONObject jsonObject = jsonArray1.getJSONObject(i);
                    for (int j = 0; j < jsonObject.length(); j++) {
                        HashMap<String, Double> hashMap1 = new HashMap<>();
                        String lat = jsonObject.getString("lat");
                        String lng = jsonObject.getString("lng");

                        //Todo Hacer dos Puntos de prueba
                        hashMap1.put("lat", Double.parseDouble(jsonObject.getString("lat")));
                        hashMap1.put("lng", Double.parseDouble(jsonObject.getString("lng")));
                        hashMaps.add(hashMap1);
                    }

                }
                //Toast.makeText(this, "Parse Completed", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, hashMaps.toString(), Toast.LENGTH_LONG).show();
                try {
                    Log.i("HshMAp", "Entrando...");
                    latLngs = new ArrayList<>();
                    PolylineOptions polylineOptions;
                    polylineOptions = new PolylineOptions();

                    for (HashMap<String, Double> hashMap1 : hashMaps) {
                        //       Toast.makeText(this, "lat: "+hashMap1.get("lat")+ "lng: "+hashMap1.get("lng"), Toast.LENGTH_SHORT).show();
                        LatLng latLng=new LatLng(hashMap1.get("lat"),hashMap1.get("lng"));
                        latLngs.add(latLng);



                        ///TODO aqui Ya tenemos todos los puntos;

                    }
                    // LatLng latLng = new LatLng(2.454954, -76.597619);
                    //LatLng latLng1 = new LatLng(2.442008, -76.606899);
                    //  latLngs.add(latLng);
                    // latLngs.add(latLng1);
                    Toast.makeText(this, latLngs.toString(), Toast.LENGTH_SHORT).show();


                    polylineOptions.addAll(latLngs);
                    polylineOptions.width(10);
                    polylineOptions.color(Color.RED);
                    Toast.makeText(this, "Lat: " + latLngs.get(latLngs.size() - 1).latitude + "Long: " + latLngs.get(latLngs.size() - 1).longitude, Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "size" + latLngs.size(), Toast.LENGTH_SHORT).show();
                    if (polylineOptions != null) {

                        mMap.addPolyline(polylineOptions);
                        Toast.makeText(this, "Add Polyline", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(this, "Poly is null", Toast.LENGTH_SHORT).show();
                    }


                    //  textView.setText(data);

                } catch (Exception e) {
                    Log.e("String Parse Error", e.toString());
                    Toast.makeText(this, "Error" + e.toString(), Toast.LENGTH_SHORT).show();
                }


            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Parse Error", e.toString());
                Toast.makeText(this, "Error" + e.toString(), Toast.LENGTH_SHORT).show();

            }

        }

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    public void getRoutes() {
        myRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        strRoute = dataSnapshot.getValue();
                        Log.d("getRoutes", strRoute.toString());
                        Toast.makeText(MainActivity.this, strRoute.toString(), Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );


    }
}
