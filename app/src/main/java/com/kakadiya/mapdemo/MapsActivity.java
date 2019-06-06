package com.kakadiya.mapdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    PlaceAutocompleteFragment autocompleteFragment;
    private static final String TAG = MapsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Auto complete search - start
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d("Maps", "Place selected: " + place.getName());
                LatLng latLng = place.getLatLng();
                drawPolygon(latLng);
                mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.currentlocation_icon))
                        .title(String.valueOf(place.getName())));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });
        //Auto complete search - end

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Code block to remove all Google generated labels on the map - start
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle_remove_labels));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        //Code block to remove all Google generated labels on the map - end

        //Set default camera view on top of the Jibestream Building
        LatLng currentLocation = getLatLng("455 Dovercourt Rd Suite 101, Toronto, ON M6H 2W3");
        mMap.addMarker(new MarkerOptions().position(currentLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_my_location))
                .title("Jibestream Building"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    //Method to draw star shaped polygon on the searched location
    public void drawPolygon(LatLng latLng){
        double ave =3;
        double min = (20*ave)/100;
        double max = (60*ave)/100;
        mMap.addPolygon(new PolygonOptions()
                .addAll(createStar(latLng, ave, min,max))
                .strokeColor(Color.rgb(255, 204, 0))
                .fillColor(Color.rgb(255, 204, 0)));
    }

    //Method to get Latitude and Longitude from given address
    public LatLng getLatLng(String location) {
        List<Address> addressList = null;
        LatLng latLng = null;
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            latLng = new LatLng(address.getLatitude(), address.getLongitude());
        }
        return latLng;
    }

    //Method to create list of Latitude and Longitude to draw star shaped Polygon
    private List<LatLng> createStar(LatLng center, double average, double min, double max) {
        return Arrays.asList(
                new LatLng(center.latitude - min, center.longitude),
                new LatLng(center.latitude- average, center.longitude + average +min),
                new LatLng(center.latitude + min, center.longitude + average),
                new LatLng(center.latitude + average, center.longitude + (average*2)),
                new LatLng(center.latitude + average, center.longitude + max),
                new LatLng(center.latitude + (average*2), center.longitude),
                new LatLng(center.latitude + average, center.longitude - max),
                new LatLng(center.latitude + average, center.longitude - (average*2)),
                new LatLng(center.latitude + min, center.longitude - average),
                new LatLng(center.latitude - average, center.longitude - average - min),
                new LatLng(center.latitude - min, center.longitude));
    }
}
