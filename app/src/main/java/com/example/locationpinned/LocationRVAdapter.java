package com.example.locationpinned;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LocationRVAdapter extends RecyclerView.Adapter<LocationRVAdapter.ViewHolder> {
    private ArrayList<LocationModal> locationModals;
    private Context context;
    private int count = 1;

    // constructor
    public LocationRVAdapter(ArrayList<LocationModal> locationModals, Context context) {
        this.locationModals = locationModals;
        this.context = context;
    }

    @NonNull
    @Override
    // inflate layout
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_recycleview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // set data to recycler view item
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        LocationModal modal = locationModals.get(index);

        holder.id = modal.getId();
        holder.container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.light_green));
        holder.address.setText(modal.getAddress());
        holder.latitude.setText(modal.getLatitude());
        holder.longitude.setText(modal.getLongitude());
    }

    @Override
    public int getItemCount() {
        return locationModals.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView address, latitude, longitude;
        private CardView container;
        private int id;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // get views
            container = itemView.findViewById(R.id.locationContainer);
            address = itemView.findViewById(R.id.address);
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);

            // move to second fragment
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get text
                    String adr = address.getText().toString();
                    String lat = latitude.getText().toString();
                    String lon = longitude.getText().toString();

                    // put data in a bundle
                    Bundle bundle = new Bundle();
                    bundle.putString("Address", adr);
                    bundle.putString("Latitude", lat);
                    bundle.putString("Longitude", lon);
                    bundle.putInt("ID", id);

                    // move and send data to second fragment
                    Navigation.findNavController(v).navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
                }
            });
        }
    }

    // filtering items
    public void filterList(ArrayList<LocationModal> filteredList) {
        locationModals = filteredList;

        // notify adapter of change in recycler view data
        notifyDataSetChanged();
    }
}
