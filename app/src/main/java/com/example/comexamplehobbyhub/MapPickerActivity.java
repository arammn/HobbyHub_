package com.example.comexamplehobbyhub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapPickerActivity extends FragmentActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private MapView mapView;
    private Marker selectedMarker;
    private ImageButton applyButton, currentLocationButton;
    private GeoPoint selectedPoint = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map_picker);

        mapView = findViewById(R.id.mapView);
        applyButton = findViewById(R.id.applyButton);
        currentLocationButton = findViewById(R.id.currentLocationButton);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);

        GeoPoint defaultPoint = new GeoPoint(34.1425, -118.2551);
        mapController.setCenter(defaultPoint);

        mapView.setOnTouchListener((v, event) -> {
            GeoPoint touchedPoint = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
            if (selectedMarker != null) {
                mapView.getOverlays().remove(selectedMarker);
            }

            selectedPoint = touchedPoint;
            selectedMarker = new Marker(mapView);
            selectedMarker.setPosition(touchedPoint);
            selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(selectedMarker);
            mapView.invalidate();
            return false;
        });

        applyButton.setOnClickListener(v -> {
            if (selectedPoint != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedPoint.getLatitude());
                resultIntent.putExtra("longitude", selectedPoint.getLongitude());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Tap on the map to select a location", Toast.LENGTH_SHORT).show();
            }
        });

        currentLocationButton.setOnClickListener(v -> getCurrentLocation());
        requestLocationPermission();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        android.location.LocationManager locationManager = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
        if (location != null) {
            GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
            mapView.getController().animateTo(point);

            if (selectedMarker != null) mapView.getOverlays().remove(selectedMarker);

            selectedMarker = new Marker(mapView);
            selectedMarker.setPosition(point);
            selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(selectedMarker);
            mapView.invalidate();

            selectedPoint = point;
        } else {
            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
        }
    }
}
