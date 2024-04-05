package com.example.snapparkuser;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

public class Alerts extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView tvName;
    private TextView tvVehicleNo;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    String vehicleNumber;

    private Button btnLogout;
    private Button btnNav;
    private Button btnVerify;
    private Double Navlongitude;
    private Double Navlatitude;
    private androidx.cardview.widget.CardView alertCard;

    private LatLng currentVehicleLocation;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alerts);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();





        btnLogout = findViewById(R.id.btnLogout);
        btnNav = findViewById(R.id.btnNav);

        alertCard = findViewById(R.id.alertCard);

        // Initialize EditText fields
        tvName = findViewById(R.id.tvName);
        tvVehicleNo = findViewById(R.id.tvVehicleNo);
        alertCard.setVisibility(View.INVISIBLE);

        btnLogout.setOnClickListener(v -> logout());

        // Fetch and display user data
        fetchUserData();






        btnNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLocation(Navlatitude, Navlongitude);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




    }






    private void fetchUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Retrieve user data and set EditText fields
                                String name = document.getString("name");
                                String vehicleNo = document.getString("vehicleNo");
                                vehicleNumber = vehicleNo;
                                getLocation(vehicleNo);
                                tvName.setText(name);
                                tvVehicleNo.setText(vehicleNo);


                            }
                        }
                    });
        }


    }
    // Override onRequestPermissionsResult to handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Get the location.
                    // Call onCreate again to start obtaining location updates
                    onCreate(new Bundle());
                }
                break;
        }

    }

    private void logout() {
        mAuth.signOut(); // Sign out the current user
        // Redirect to login screen or any other desired behavior
        Intent intent = new Intent(Alerts.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close the current activity
    }
    private void getLocation(String vehicleNo) {
        db.collection("reports")
                .whereEqualTo("vehicleNo", vehicleNo)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                DocumentSnapshot document = querySnapshot.getDocuments().get(0); // Get the first document
                                if (document.exists()) {
                                    // Document exists, retrieve latitude and longitude
                                    Double latitude = document.getDouble("latitude");
                                    Double longitude = document.getDouble("longitude");
                                    currentVehicleLocation = new LatLng(latitude,longitude);
                                    findNearestLocation();

                                    // Check if latitude and longitude are not null
                                    if (latitude != null && longitude != null) {
                                        // Show alertCard and handle latitude/longitude data
                                        alertCard.setVisibility(View.VISIBLE);
                                        // Handle latitude and longitude data here
                                    } else {
                                        // Latitude or longitude is null, handle accordingly
                                        Toast.makeText(Alerts.this, "Latitude or longitude is null", Toast.LENGTH_SHORT).show();
                                        alertCard.setVisibility(View.INVISIBLE);
                                    }
                                } else {
                                    // Document does not exist
                                    Toast.makeText(Alerts.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                                    alertCard.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                // QuerySnapshot is empty
                                Toast.makeText(Alerts.this, "No reports found for vehicle number: " + vehicleNo, Toast.LENGTH_SHORT).show();
                                alertCard.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            // Error occurred while fetching data
                            Toast.makeText(Alerts.this, "Failed to retrieve data: " + task.getException(), Toast.LENGTH_SHORT).show();
                            alertCard.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }
    private void navigateToLocation(Double latitude, Double longitude) {
        // Create a Uri from the latitude and longitude values
        Toast.makeText(this,"Nearest Location: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);

        // Create an Intent to launch Google Maps with navigation
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Check if there's an app to handle the Intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            // Start Google Maps activity
            startActivity(mapIntent);

            // After navigation, delete relevant vehicle data from Firestore
            deleteVehicleDataFromFirestore();
        } else {
            // If Google Maps app is not found, display a toast message
            Toast.makeText(this, "Google Maps app not found", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteVehicleDataFromFirestore() {
        // Delete the relevant vehicle data from the Firestore "reports" collection
        db.collection("reports")
                .whereEqualTo("vehicleNo", vehicleNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            // Delete each document found
                            db.collection("reports").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Deletion successful
                                        Toast.makeText(Alerts.this, "Vehicle data deleted successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Error handling if deletion fails
                                        Toast.makeText(Alerts.this, "Failed to delete vehicle data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // Error handling if query fails
                        Toast.makeText(Alerts.this, "Failed to query vehicle data for deletion: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void findNearestLocation() {

        CollectionReference locationsRef = db.collection("locations");

        locationsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                    if (!documents.isEmpty()) {
                        LatLng nearestLocation = null;
                        double minDistance = Double.MAX_VALUE;

                        for (DocumentSnapshot document : documents) {
                            double latitude = document.getDouble("latitude");
                            double longitude = document.getDouble("longitude");
                            LatLng location = new LatLng(latitude, longitude);

                            // Calculate distance between current location and location from Firestore
                            double distance = calculateDistance(currentVehicleLocation, location);

                            // Update nearest location if this location is closer
                            if (distance < minDistance) {
                                minDistance = distance;
                                nearestLocation = location;
                            }
                        }

                        if (nearestLocation != null) {
                            Navlatitude = nearestLocation.latitude;
                            Navlongitude = nearestLocation.longitude;
                            // Print or use the nearest location
                            Toast.makeText(this,"Nearest Location: " + Navlatitude + ", " + Navlongitude, Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        Toast.makeText(this,"No locations found in Firestore.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {

                Toast.makeText(this,"Failed to retrieve locations from Firestore: "+ task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateDistance(LatLng location1, LatLng location2) {
        // Haversine formula to calculate distance between two locations
        double earthRadius = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(location2.latitude - location1.latitude);
        double lonDistance = Math.toRadians(location2.longitude - location1.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(location1.latitude)) * Math.cos(Math.toRadians(location2.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c; // Distance in kilometers
    }

    }

