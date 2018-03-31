package com.abcd.paulboutot.cps406project;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public final int LOCATION_REQUEST = 500;
    private MapFragment mapFragment;

    ListView search_location;
    ArrayAdapter<String> adapter;

    private DrawerLayout aLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search_location = (ListView) findViewById(R.id.search_location);

        ArrayList<String> arrayLocation = new ArrayList<>();
        arrayLocation.addAll(Arrays.asList(getResources().getStringArray(R.array.my_locations)));

        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, arrayLocation);

        search_location.setAdapter(adapter);

        aLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, aLayout, R.string.open, R.string.close);
        aLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Gets the map fragment.
     * @return
     */
    public MapFragment getMapFragment() {
        return mapFragment;
    }

    /**
     * sets the map fragment.
     * @param mapFragment
     */
    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    /**
     * Gets the map that is being shown to the user.
     * @return the map that is shown in this activity.
     */
    public GoogleMap getMap() {
        return getMapFragment().getMap();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.search_location);
        SearchView searchView = (SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (getMap() != null) {
                    getMap().setMyLocationEnabled(true);
                }
                else {
                    throw new NullPointerException("setMap was never called properly in MapFragment.java" +
                            " or setMap was called somewhere else, and was set to null.");
                }
            }
        }
    }

}
