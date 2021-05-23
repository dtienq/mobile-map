package com.hcmus.androidmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    boolean locationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    Button btnSearchLocation;
    ImageButton btnCurrent;
    View mapView;
    AutocompleteSupportFragment txtSearchStart;
    LatLng current;
    private List<Polyline> polylines=null;
    LatLng start, end;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    AutocompleteSupportFragment autocompleteFragment;
    List<LatLng> markers = new ArrayList<>();
    Marker oldMarker = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Demo location

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // get all view of activity
        mapView = mapFragment.getView();
        getAllView();
    }

    private void getAllView() {
        btnSearchLocation = mapView.findViewById(R.id.btnSearchLocation);
        btnSearchLocation.setTextColor(Color.parseColor("#28a745"));

        btnCurrent = mapView.findViewById(R.id.btnCurrent);
        btnCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveCameraToCurrent();
            }
        });
        btnSearchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = current;
                FindRoutes(start,end);
            }
        });

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.txtSearchStart);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                LatLng selected = place.getLatLng();

                if(selected != null) {
                    if(oldMarker != null) {
                        oldMarker.remove();
                    }
                    end = selected;
                    oldMarker = mMap.addMarker( new MarkerOptions().position(selected));
                    markers.add(selected);
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng marker : markers) {
                        builder.include(marker);
                    }
                    LatLngBounds bounds = builder.build();

                    int padding = 200; // offset from edges of the map in pixels
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cameraUpdate);
                }
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                System.out.println(status);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //get permission from android machine
        boolean isGetCurrentLocation = true;
        getLocationPermission(isGetCurrentLocation);
    }

    private void getLocationPermission(boolean isGetCurrentLocation) {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            loadCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }



    private void loadCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                current = new LatLng(location.getLatitude(), location.getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location));
                                markerOptions.position(current);
                                mMap.addMarker(markerOptions);
                                markers.add(current);
                                moveCameraToCurrent();
                                Log.d("success",location.getLatitude() +", " +location.getLongitude());
                            }
                        }
                    });
        }
    }

    private void moveCameraToCurrent() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 16.0F));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadCurrentLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        markers = new ArrayList<>();
        markers.add(current);

        if (resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            autocompleteFragment.setText(place.getAddress());
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // TODO: Handle the error.
            Status status = Autocomplete.getStatusFromIntent(data);
        } else if (resultCode == RESULT_CANCELED) {
            // The user canceled the operation.
        }
    }
    public void FindRoutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(MapsActivity.this,"Không tìm thấy địa điểm", Toast.LENGTH_LONG).show();
        }
        else
        {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key(getString(R.string.google_maps_key))
                    .build();
            routing.execute();
        }
    }
    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(MapsActivity.this,"Đang tìm...",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if (polylines != null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng = null;
        LatLng polylineEndLatLng = null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i < routes.size(); i++) {

            if (i == shortestRouteIndex) {
                polyOptions.color(ContextCompat.getColor(this,R.color.black));
                polyOptions.width(7);
                polyOptions.addAll(routes.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylineStartLatLng = polyline.getPoints().get(0);
                int k = polyline.getPoints().size();
                polylineEndLatLng = polyline.getPoints().get(k - 1);
                polylines.add(polyline);

            }
        }
        //Điểm bắt đầu
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("Start");
        mMap.addMarker(startMarker);

        //Điểm kết thúc - Có thể bỏ qua vì điểm này đã được đánh Marker từ trước.
//        MarkerOptions endMarker = new MarkerOptions();
//        endMarker.position(polylineEndLatLng);
//        endMarker.title("End");
//        mMap.addMarker(endMarker);
    }

    @Override
    public void onRoutingCancelled() {
    }
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}