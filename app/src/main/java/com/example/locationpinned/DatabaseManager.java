package com.example.locationpinned;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Geocoder";
    private static final int DATABASE_VERSION = 1;
    private Context context;

    // stores notes
    private static final String TABLE_NAME = "Location";
    private static final String ID_COL = "id";
    private static final String ADDRESS_COL = "address";
    private static final String LATITUDE_COL = "latitude";
    private static final String LONGITUDE_COL = "longitude";

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ADDRESS_COL + " TEXT,"
                + LATITUDE_COL + " TEXT,"
                + LONGITUDE_COL + " TEXT)";

        // execute query
        db.execSQL(query);
    }

    public void addNewAddress(String address) {
        // open database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        // used to update data
        ContentValues values = new ContentValues();

        List<Address> addresses; // holds locations matching the address
        Geocoder geocoder = new Geocoder(context); // used to find matching locations

        try {
            // get at most one pair of coordinates that matches the address
            addresses = geocoder.getFromLocationName(address, 1);

            String latitude = "???";
            String longitude = "???";

            // if coordinates are found
            if (addresses.size() > 0) {
                // get coordinates
                double lat_tmp = addresses.get(0).getLatitude();
                double long_tmp = addresses.get(0).getLongitude();

                latitude = Double.toString(lat_tmp);
                longitude = Double.toString(long_tmp);
            }

            // assign values
            values.put(ADDRESS_COL, address);
            values.put(LATITUDE_COL, latitude);
            values.put(LONGITUDE_COL, longitude);

            // add row to table
            db.insert(TABLE_NAME, null, values);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // close database
        db.close();
    }

    public void addNewCoordinates(String latitude, String longitude) {
        // open database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        // used to update data
        ContentValues values = new ContentValues();

        List<Address> addresses; // holds locations matching the coordinates
        Geocoder geocoder = new Geocoder(context); // used to find matching locations

        try {
            // convert to float
            float lat = Float.parseFloat(latitude);
            float lon = Float.parseFloat(longitude);

            // default address
            String address = "???";

            // if valid
            if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                // get at most one address that matches the coordinates
                addresses = geocoder.getFromLocation(lat, lon, 1);

                // if address is found
                if (addresses.size() > 0) {
                    // get address
                    address = addresses.get(0).getAddressLine(0);
                }
            }

            // assign values
            values.put(ADDRESS_COL, address);
            values.put(LATITUDE_COL, latitude);
            values.put(LONGITUDE_COL, longitude);

            // add row to table
            db.insert(TABLE_NAME, null, values);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // close database
        db.close();
    }

    public void editLocation(int id, String newAddress, String newLatitude, String newLongitude) {
        // open database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        // used to update data
        ContentValues values = new ContentValues();

        // assign new values
        values.put(ADDRESS_COL, newAddress);
        values.put(LATITUDE_COL, newLatitude);
        values.put(LONGITUDE_COL, newLongitude);

        // update row in table
        db.update(TABLE_NAME, values, ID_COL + " = ?", new String[]{String.valueOf(id)});

        // close database
        db.close();
    }

    public void deleteLocation(int id) {
        // open database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        // delete specific location
        db.delete(TABLE_NAME, ID_COL + " = ?", new String[]{String.valueOf(id)});

        // close database
        db.close();
    }

    public void deleteAllLocations() {
        // open the database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        // delete all rows from the locations table
        db.delete(TABLE_NAME, null, null);

        // close the database
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // check if table exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // get locations from database and return it as an array of objects
    public ArrayList<LocationModal> readLocations() {
        // open database for reading
        SQLiteDatabase db = this.getReadableDatabase();

        // get data
        Cursor cursorCourses = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // create arraylist to store rows
        ArrayList<LocationModal> locationModalArrayList = new ArrayList<>();

        // moving cursor to first row
        if (cursorCourses.moveToFirst()) {
            do {
                // add data to array list
                locationModalArrayList.add(new LocationModal(
                        cursorCourses.getInt(0),
                        cursorCourses.getString(1),
                        cursorCourses.getString(2),
                        cursorCourses.getString(3)));
            } while (cursorCourses.moveToNext());
            // move to next row
        }

        // close cursor
        cursorCourses.close();

        // return array list
        return locationModalArrayList;
    }
}