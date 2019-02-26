package com.apps.hinoir.reto1map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    //--------------------------------------------
    //Atributos
    //--------------------------------------------
    private static final int REQUEST_CODE = 11;
    private LocationManager manager;
    private GoogleMap mMap;
    private LatLng usuario;
    private List<MarkerOptions> markerOptions;
    private Dialog dialog;
    private EditText nombre;
    private Button guardar;
    private String nomb;
    private LatLng coordenadas;
    private Marker markerActual;
    private EditText cercano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        dialog = new Dialog(this);
        markerOptions = new ArrayList<>();
        cercano = findViewById(R.id.cercano);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);

        // Add a marker in Cali and move the camera
        usuario = new LatLng(3, -76);
        mMap.addMarker(new MarkerOptions().position(usuario).title("Marker in Cali"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(usuario));

        //Agregar un listener para el marker
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);



        //Agregar un listener de ubicacion
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mMap.clear();
                for(int i=0;i<markerOptions.size();i++){
                    mMap.addMarker(markerOptions.get(i));
                }
                usuario=new LatLng(location.getLatitude(),location.getLongitude());
                markerActual =  mMap.addMarker(new MarkerOptions().position(usuario).title("Yo"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(usuario));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }


//
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        Geocoder geo = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geo.getFromLocation(marker.getPosition().latitude,marker.getPosition().longitude,1);
            if(addresses.isEmpty()){
                Toast.makeText(getApplicationContext(),"Esperando...",Toast.LENGTH_LONG).show();
            }
            else{
                if(addresses.size()>0){
                    marker.setSnippet("Direccion: "+addresses.get(0).getAddressLine(0)
                            +addresses.get(0).getAdminArea());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        coordenadas=latLng;

        mostrarVentana();

    }

    public void mostrarVentana(){
        dialog.setContentView(R.layout.dialog_save);
        nombre=dialog.findViewById(R.id.nombre);
        guardar=dialog.findViewById(R.id.guardar);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nomb = nombre.getText().toString();

                MarkerOptions options = new MarkerOptions().title(nomb)
                        .position(new LatLng(coordenadas.latitude,coordenadas.longitude));
                markerOptions.add(options);
                mMap.addMarker(markerOptions.get(markerOptions.size()-1));
                dialog.dismiss();
                calcularCercano();
            }
        });
        dialog.show();

    }

    public void calcularCercano(){
        Location act = new Location("actual");
        act.setLatitude(usuario.latitude);
        act.setLongitude(usuario.longitude);
        List<Float> distancias = new ArrayList<>();
        float referencia = 1000000000;
        int indice = 0;
        for(int i=0;i<markerOptions.size();i++){
            Location lc = new Location("n"+i);
            lc.setLatitude(markerOptions.get(i).getPosition().latitude);
            lc.setLongitude(markerOptions.get(i).getPosition().longitude);
            float dist = act.distanceTo(lc);
            if(dist<=referencia){
                referencia=dist;
                indice=i;
            }
            else if(i==markerOptions.size()){
                referencia=dist;
                indice=i;
            }
        }
        float refCer = 1001;
        if(referencia<refCer){
            cercano.setText("El lugar mas cercano es: "+markerOptions.get(indice).getTitle()+ " a "+referencia+" metros");
        }
        else{
            cercano.setText("El lugar mas cercano es: "+markerOptions.get(indice).getTitle());
        }

    }

}
