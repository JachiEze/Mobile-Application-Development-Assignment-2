package com.example.locationpinned;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.locationpinned.databinding.FragmentSecondBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private boolean newPin = false;
    private boolean useAddress = false;
    private boolean useCords = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get data from bundle
        String address = getArguments().getString("Address");
        String latitude = getArguments().getString("Latitude");
        String longitude = getArguments().getString("Longitude");
        int id = getArguments().getInt("ID");

        // clicked existing pin
        if (!address.equals("0") && !latitude.equals("0") && !longitude.equals("0") && id != -1) {
            // if pin has invalid coordinates
            if (latitude.equals("???") || longitude.equals("???")) {
                createMap(view, Float.parseFloat("43.653908"), Float.parseFloat("-79.384293"));
            } else {
                // set map to location pin
                createMap(view, Float.parseFloat(latitude), Float.parseFloat(longitude));
                Log.d("ID", String.valueOf(id));
                newPin = false;
            }
        }
        // clicked create new pin
        else {
            // set map to default location
            createMap(view, Float.parseFloat("43.653908"), Float.parseFloat("-79.384293"));

            // remove delete button
            binding.deleteButton.setVisibility(View.GONE);
            newPin = true;
        }

        // get text layouts
        TextInputLayout adr_layout = (TextInputLayout) view.findViewById(R.id.textField_Address);
        TextInputLayout lat_layout = (TextInputLayout) view.findViewById(R.id.textField_Latitude);
        TextInputLayout lon_layout = (TextInputLayout) view.findViewById(R.id.textField_Longitude);

        // get edit text
        TextInputEditText lat_edit_text = (TextInputEditText) view.findViewById(R.id.editText_Latitude);
        TextInputEditText lon_edit_text = (TextInputEditText) view.findViewById(R.id.editText_Longitude);
        TextInputEditText adr_edit_text = (TextInputEditText) view.findViewById(R.id.editText_Address);

        // initialize text fields
        if (!newPin) {
            adr_edit_text.setText(address);
            lat_edit_text.setText(latitude);
            lon_edit_text.setText(longitude);
        }

        // disable coordinates and enable address text field
        binding.adrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // enable address text input
                adr_layout.setEnabled(true);

                // disable address text input
                lat_layout.setEnabled(false);
                lon_layout.setEnabled(false);

                // clear coordinates text
                lat_edit_text.setText("");
                lon_edit_text.setText("");

                // set address text
                if (!newPin) {
                    adr_edit_text.setText(address);
                }

                useAddress = true;
                useCords = false;
            }
        });

        // disable address and enable coordinates text field
        binding.cordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get edit text
                TextInputEditText adr_edit_text = (TextInputEditText) view.findViewById(R.id.editText_Address);

                // enable coordinates text input
                lat_layout.setEnabled(true);
                lon_layout.setEnabled(true);

                // disable address text input
                adr_layout.setEnabled(false);

                // clear address text
                adr_edit_text.setText("");

                // set coordinates text
                if (!newPin) {
                    lat_edit_text.setText(latitude);
                    lon_edit_text.setText(longitude);
                }

                useAddress = false;
                useCords = true;
            }
        });

        // toggle coordinates button to set default behaviour
        binding.cordBtn.performClick();

        // delete location pin
        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // connect to database
                DatabaseManager db = new DatabaseManager(getContext());

                // create alert
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Alert");
                builder.setMessage("Do you want to delete this location pin?");

                // yes button, delete note
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // create toast message
                        Toast.makeText(getContext(), "Location Pin Deleted", Toast.LENGTH_SHORT).show();

                        // delete location
                        db.deleteLocation(id);

                        // move to next fragment
                        NavHostFragment.findNavController(SecondFragment.this).navigate(R.id.action_SecondFragment_to_FirstFragment);
                    }
                });

                // no button, close alert
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });

                // show alert
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                // close database
                db.close();
            }
        });

        // open maps in browser
        binding.mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useCords) {
                    // get coordinates
                    String lat = String.valueOf(binding.editTextLatitude.getText());
                    String lon = String.valueOf(binding.editTextLongitude.getText());

                    // if cords are not empty
                    if (!lat.isEmpty() && !lon.isEmpty()) {
                        // check if cords are valid
                        if (check_cords(lat, lon)) {
                            // open map at the specific location
                            String uri = String.format(Locale.ENGLISH, "geo:%s,%s", lat, lon);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            getContext().startActivity(intent);
                        }
                    } else {
                        // display helper message
                        Toast.makeText(getContext(), "Coordinates Not Specified", Toast.LENGTH_SHORT).show();
                    }
                }

                if (useAddress) {
                    String address = String.valueOf(binding.editTextAddress.getText());

                    String uri = "geo:0,0?q=" + Uri.encode(address);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    getContext().startActivity(intent);
                }
            }
        });
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // find fab
        FloatingActionButton fab = (FloatingActionButton) requireActivity().findViewById(R.id.fab);

        // update icon
        fab.setImageResource(R.drawable.ic_check);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open database
                DatabaseManager db = new DatabaseManager(getContext());

                // create new pin
                if (newPin) {
                    // generate coordinates from address
                    if (useAddress) {
                        String tmp = String.valueOf(binding.editTextAddress.getText());

                        if (!tmp.isEmpty()) {
                            // if address is not empty
                            db.addNewAddress(tmp);
                        } else {
                            // display helper message
                            Toast.makeText(getContext(), "No Address Specified", Toast.LENGTH_SHORT).show();
                            db.close();
                            return;
                        }
                    }

                    // generate address from coordinates
                    if (useCords) {
                        String lat = String.valueOf(binding.editTextLatitude.getText());
                        String lon = String.valueOf(binding.editTextLongitude.getText());

                        if (!lat.isEmpty() && !lon.isEmpty()) {
                            // if coordinates are not empty
                            db.addNewCoordinates(lat, lon);

                            // check if cords are valid
                            if (!check_cords(lat, lon)) {
                                db.close();
                                return;
                            }
                        } else {
                            // display helper message
                            Toast.makeText(getContext(), "Coordinates Not Specified", Toast.LENGTH_SHORT).show();
                            db.close();
                            return;
                        }
                    }
                }

                // edit pin
                if (!newPin) {
                    String adr = String.valueOf(binding.editTextAddress.getText());
                    String lat = String.valueOf(binding.editTextLatitude.getText());
                    String lon = String.valueOf(binding.editTextLongitude.getText());

                    // get id
                    int id = getArguments().getInt("ID");

                    // if fields are empty use original values
                    if (adr.isEmpty()) {
                        adr = getArguments().getString("Address");
                    }
                    if (lat.isEmpty()) {
                        lat = getArguments().getString("Latitude");
                    }
                    if (lon.isEmpty()) {
                        lon = getArguments().getString("Longitude");
                    }

                    // check if cords are valid
                    if (!check_cords(lat, lon)) {
                        db.close();
                        return;
                    }

                    // edit note
                    db.editLocation(id, adr, lat, lon);

                    // display helper message
                    Toast.makeText(getContext(), "Pin Updated", Toast.LENGTH_SHORT).show();
                }

                // close database
                db.close();

                // navigate to the first fragment
                NavHostFragment.findNavController(SecondFragment.this).navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // initializes map
    public void createMap(View view, float lat, float lon) {
        // create map
        Configuration.getInstance().setUserAgentValue("testing-2023-10-04-sd2ehjnxs");
        MapView mMap = (MapView) view.findViewById(R.id.osmmap);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setMultiTouchControls(true);

        // create geo point
        GeoPoint geoPoint = new GeoPoint(lat, lon);

        // set initial position
        IMapController controller = mMap.getController();
        controller.setZoom(19.0);
        controller.setCenter(geoPoint);

        // create location pin
        Marker marker = new Marker(mMap);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // add marker to map
        mMap.getOverlays().add(marker);
    }

    // check if lat and long are valid
    public boolean check_cords(String lat, String lon) {
        // convert to float
        float latitude = Float.parseFloat(lat);
        float longitude = Float.parseFloat(lon);

        // latitude is not within a valid range
        if (!(latitude >= -90) || !(latitude <= 90)) {
            Toast.makeText(getContext(), "Latitude is not Valid", Toast.LENGTH_SHORT).show();
            return false;
        }

        // longitude is not within a valid range
        if (!(longitude >= -180) || !(longitude <= 180)) {
            Toast.makeText(getContext(), "Longitude is not Valid", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}