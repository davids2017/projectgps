package pl.dawidszczesniak.rejestrator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    String url;
    String user;
    String pass;

    private GoogleMap mMap;
    private ProgressDialog pg;
    private ProgressDialog pg1;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    public double cLatitude;
    public double cLongitude;

    public ArrayList<String> names = new ArrayList<>();
    public int t = names.size();

    public ArrayList<Double> latitude = new ArrayList<>();
    public int lat = latitude.size();

    public ArrayList<Double> longitude = new ArrayList<>();
    public int lng = longitude.size();

    public ArrayList<LatLng> points = new ArrayList<>();
    public int p = points.size();

    public ArrayList<Marker> Markers = new ArrayList<>();
    public int m = Markers.size();

    public Marker mMarker;

    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;
    private static final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 102;
    private boolean permissionIsGranted = false;

    Button downloadB, addB, sendB, removeB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        url = getIntent().getExtras().getString("url_value");
        user = getIntent().getExtras().getString("user_value");
        pass = getIntent().getExtras().getString("pass_value");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pg = new ProgressDialog(this);
        pg.setMessage("Wysyłanie...");

        pg1 = new ProgressDialog(this);
        pg1.setMessage("Pobieranie...");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        downloadB = (Button) findViewById(R.id.downloadB);
        addB = (Button) findViewById(R.id.addB);
        removeB = (Button) findViewById(R.id.removeButton);
        sendB = (Button) findViewById(R.id.sendB);

        addB.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))

                {

                    if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected())

                    {

                        mGoogleApiClient.connect();

                    }

                }

                else

                {
                    infoGPS();
                }

            }
        });

        removeB.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                if (points != null && !points.isEmpty()) {
                    points.remove(p);

                }

                if (Markers != null && !Markers.isEmpty()) {
                    Markers.get(m).remove();
                    Markers.remove(m);
                }

                if (latitude != null && !latitude.isEmpty()) {
                    latitude.remove(lat);
                }

                if (longitude != null && !longitude.isEmpty()) {
                    longitude.remove(lng);
                }

                if (names != null && !names.isEmpty()) {
                    names.remove(t);
                }

            }
        });

        sendB.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                ConnectivityManager cm = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                boolean isConn = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if (isConn == true)

                {
                    pg.show();

                    Toast.makeText(MapsActivity.this, "Wysyłam dane", Toast.LENGTH_SHORT).show();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sDB();
                        }
                    }, 1000);
                }

                else

                {
                    infoNet();
                }
            }
        });

        downloadB.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                ConnectivityManager conne = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = conne.getActiveNetworkInfo();

                boolean isConn = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if (isConn == true)

                {

                    pg1.show();

                    Toast.makeText(MapsActivity.this, "Pobieram dane", Toast.LENGTH_SHORT).show();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dDB();

                        }
                    }, 1000);

                }

                else

                {
                    infoNet();
                }

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_REQUEST_FINE_LOCATION);

            } else {
                permissionIsGranted = true;
            }
            return;
        }

        LatLng curr = new LatLng(50.866588, 20.626626);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curr, 5));
        mMap.setMyLocationEnabled(true);
    }

    public void NLocation(Location location)

    {

        cLatitude = location.getLatitude();
        cLongitude = location.getLongitude();

        final LatLng latLng = new LatLng(cLatitude, cLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        latitude.add(lat, cLatitude);
        longitude.add(lng, cLongitude);

        points.add(p, latLng);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setMessage("Wprowadź nazwę punktu");

        final EditText editText = new EditText(MapsActivity.this);

        alertDialog.setView(editText);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editText.getText().toString();
                if (name.trim().compareTo("")==0) {
                    Toast.makeText(MapsActivity.this, "Wprowadź nazwe", Toast.LENGTH_SHORT).show();

                }

                else{
                    MarkerOptions options = new MarkerOptions()
                            .position(latLng)
                            .title(name)
                            .visible(true);

                    names.add(t, name);
                    Markers.add(m, mMarker = mMap.addMarker(options));
                    mMarker.showInfoWindow();
                }
            }
        });
        alertDialog.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();

        mGoogleApiClient.disconnect();
    }

    public void sDB()

    {

        Connection conn = null;
        Statement stmt = null;

        try

        {
            StrictMode.ThreadPolicy db = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(db);

            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(url, user, pass);

            stmt = conn.createStatement();

            int size = points.size();

            for(int i=size-1;i>=0;i--) {

                Double la = latitude.get(i);
                Double lo = longitude.get(i);
                String tt = names.get(i);

                String sql1 = "INSERT wspolrzedne (szerokosc_geograficzna, dlugosc_geograficzna, nazwa) VALUES('" + la + "', '" + lo + "', '" + tt + "')";
                stmt.executeUpdate(sql1);
            }

            pg.cancel();
            Toast.makeText(MapsActivity.this, "Dane zostały wysłane", Toast.LENGTH_SHORT).show();

        } catch (SQLException se){
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    conn.close();
            } catch (SQLException se) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public void dDB()

    {

        Connection conn = null;
        Statement stmt = null;

        try {
            StrictMode.ThreadPolicy db = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(db);

            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(url, user, pass);

            stmt = conn.createStatement();

            String sql = "SELECT szerokosc_geograficzna, dlugosc_geograficzna, nazwa FROM wspolrzedne";

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                Double lat2 = rs.getDouble("szerokosc_geograficzna");
                Double lon2 = rs.getDouble("dlugosc_geograficzna");
                String tt = rs.getString("nazwa");

                latitude.add(lat, rs.getDouble("szerokosc_geograficzna"));
                longitude.add(lng, rs.getDouble("dlugosc_geograficzna"));
                names.add(t, rs.getString("nazwa"));

                LatLng latLng = new LatLng(lat2, lon2);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                points.add(p, latLng);

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .visible(true)
                        .title(tt);

                Markers.add(m, mMarker = mMap.addMarker(options));
                mMarker.showInfoWindow();

            }
            rs.close();
            pg1.cancel();
            Toast.makeText(MapsActivity.this, "Dane zostały pobrane", Toast.LENGTH_SHORT).show();

        } catch (SQLException se){
            se.printStackTrace();
            infoCorrect();
        } catch (Exception e) {
            e.printStackTrace();
            infoCorrect();
        } finally {
            try {
                if (stmt != null)
                    conn.close();
            } catch (SQLException se) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

    }

    private void goToFirstActivity() {

        Intent intent = new Intent(this, FirstActivity.class);

        startActivity(intent);

    }

    private void infoCorrect() {
        AlertDialog.Builder info = new AlertDialog.Builder(MapsActivity.this);
        info.setMessage("Niepoprawne dane logowania").setCancelable(false)
                .setPositiveButton("Przejdz do ustawień", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToFirstActivity();
                    }
                });

        AlertDialog alert = info.create();
        alert.show();
    }

    private void infoGPS() {
        AlertDialog.Builder info = new AlertDialog.Builder(MapsActivity.this);
        info.setMessage("Aplikacja wymaga włączonej usługi GPS").setCancelable(false)
                .setPositiveButton("Przejdz do ustawień", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        turnGPSOn();
                    }
                })
                .setNegativeButton("Zamknij", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = info.create();
        alert.setTitle("Wymagany GPS");
        alert.show();
    }

    private void infoNet() {
        AlertDialog.Builder info = new AlertDialog.Builder(MapsActivity.this);
        info.setMessage("Aplikacja wymaga włączonej transmisji danych").setCancelable(false)
                .setPositiveButton("Przejdz do ustawień", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        turnNETon();
                    }
                })
                .setNegativeButton("Zamknij", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = info.create();
        alert.setTitle("Wymagana transmisja danych");
        alert.show();
    }

    private void infoLocation() {
        AlertDialog.Builder info = new AlertDialog.Builder(MapsActivity.this);
        info.setMessage("Pobieranie lokalizacji...").setCancelable(false);
        final AlertDialog alert = info.create();

        alert.show();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                alert.cancel();
                mGoogleApiClient.connect();
            }
        }, 100);

    }

    private void turnGPSOn() {

        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(callGPSSettingIntent);

    }

    private void turnNETon() {

        Intent callGPSSettingIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        startActivity(callGPSSettingIntent);

    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {

            infoLocation();
            mGoogleApiClient.disconnect();


        }

        else

        {

            NLocation(location);
            mMap.setMyLocationEnabled(true);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    permissionIsGranted = true;
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    LatLng curr = new LatLng(50.866588, 20.626626);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curr, 5));

                } else {
                    permissionIsGranted = false;
                    finishAffinity();
                }

        }

    }

}
