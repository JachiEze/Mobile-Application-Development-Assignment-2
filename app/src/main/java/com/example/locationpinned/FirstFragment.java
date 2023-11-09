package com.example.locationpinned;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationpinned.databinding.FragmentFirstBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    private RecyclerView rv;
    private LocationRVAdapter adapter;
    private ArrayList<LocationModal> locationModalList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);

        // make fragment participate in menu handling
        setHasOptionsMenu(true);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // connect to database and get objects
        DatabaseManager db = new DatabaseManager(getContext());
        locationModalList = db.readLocations();

        // get size of database
        int x = locationModalList.size();
        Log.d("SIZE", Integer.toString(x));

        // no location pins, read from json file to initialize database
        if (x == 0) {
            initializeDatabase(getContext());
            locationModalList = db.readLocations();
        }

        // add notes to recycler view
        rv = view.findViewById(R.id.locationRV);
        adapter = new LocationRVAdapter(locationModalList, getContext());

        // setting layout manager for our recycler view.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);
        rv.setAdapter(adapter);

        // close database
        db.close();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // find fab
        FloatingActionButton fab = (FloatingActionButton) requireActivity().findViewById(R.id.fab);

        // update icon
        fab.setImageResource(R.drawable.ic_add);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // navigate to the second fragment
                NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate menu
        inflater.inflate(R.menu.menu_main, menu);

        // get search view
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        // set hint
        searchView.setQueryHint(getString(R.string.search_hint));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // get the menu item for the search bar
        MenuItem searchItem = menu.findItem(R.id.action_search);

        // get the search view
        SearchView searchView = (SearchView) searchItem.getActionView();

        // set up the query text listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // called when fragment is about to turn visible
    @Override
    public void onResume() {
        super.onResume();

        // get top app bar
        MaterialToolbar toolbar = (MaterialToolbar) requireActivity().findViewById(R.id.toolbar);

        // clear current items on top app bar
        toolbar.getMenu().clear();

        // load items onto top app bar
        requireActivity().getMenuInflater().inflate(R.menu.menu_main, toolbar.getMenu());

        // get search view
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // set hint
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                Log.d("SEARCH", newText);
                return false;
            }
        });
    }

    // called when moving away from the fragment
    public void onPause() {
        super.onPause();

        // get toolbar
        MaterialToolbar toolbar = (MaterialToolbar) requireActivity().findViewById(R.id.toolbar);

        // clear items
        toolbar.getMenu().clear();
    }

    // add contents of json file to database
    public static void initializeDatabase(Context context) {
        try {
            String jsonString = loadJSON(context, "coordinates.json");
            JSONArray jsonArray = new JSONArray(jsonString);

            DatabaseManager db = new DatabaseManager(context);

            // for each value
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String latitude = jsonObject.getString("latitude");
                String longitude = jsonObject.getString("longitude");

                // add to database
                db.addNewCoordinates(latitude, longitude);
            }

            ArrayList<LocationModal> locationModalList = db.readLocations();
            for (LocationModal note : locationModalList) {
                Log.d("ID", String.valueOf(note.getId()));
                Log.d("Address", note.getAddress());
                Log.d("Latitude", note.getLatitude());
                Log.d("Longitude", note.getLongitude());
            }

            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // gets contents of json and return as string
    public static String loadJSON(Context context, String fileName) {
        String json = null;

        try {
            // open file
            InputStream input = context.getAssets().open(fileName);

            // get file size
            int size = input.available();

            // read file contents into buffer
            byte[] buffer = new byte[size];
            input.read(buffer);

            // close file
            input.close();

            // format file contents to string
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }

    // filter through location pins
    private void filter(String text) {
        ArrayList<LocationModal> filteredList = new ArrayList<LocationModal>();

        // check each location
        for (LocationModal item : locationModalList) {
            // check if the entered string matches the address
            if (item.getAddress().toLowerCase().contains(text.toLowerCase())) {
                // add to filtered list
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "No Location Pins Found", Toast.LENGTH_SHORT).show();
        } else {
            // pass filtered list to data
            adapter.filterList(filteredList);
        }
    }
}