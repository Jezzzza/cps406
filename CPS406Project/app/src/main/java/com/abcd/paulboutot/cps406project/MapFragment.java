package com.abcd.paulboutot.cps406project;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    public static final float MIN_ZOOM = 15.0f;
    public static final float MAX_ZOOM = 21.0f;

    public static final LatLng Ryerson = new LatLng(43.6576892007, -79.3782325814);
    public static final LatLngBounds RyersonBounds = new LatLngBounds(
            new LatLng(43.652778, -79.390278),
            new LatLng(43.663056, -79.374167)
    );

    private GoogleMap mMap;

    private LatLng origin;
    private LatLng destination;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        origin = null;
        destination = null;

        // Received from https://www.youtube.com/watch?v=vg9OWm4JV4U on 03/18/2018
        // Code START:
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment;
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        // if there is no map fragment, create a new one.
        if (mapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        return  view;
        // Code END.
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ((MainActivity) getActivity()).setMapFragment(this);

        // Retrieved from https://www.youtube.com/watch?v=jg1urt3FGCY&t=342s&index=31&list=WL on 03/18/2018
        // Code START:
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ((MainActivity) getActivity()).LOCATION_REQUEST);
            }

            return;
        }
        // Code END

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setMinZoomPreference(MIN_ZOOM);
        mMap.setMaxZoomPreference(MAX_ZOOM);
        mMap.setLatLngBoundsForCameraTarget(RyersonBounds);

        moveCameraToRyerson();

        // TODO: remove this after debugging.
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (isOriginAndDestinationSet()) {
                    setOrigin(null, false);
                    setDestination(null, false);
                    clearMap();
                }

                if (getOrigin() == null) {
                    setOrigin(latLng, true);
                } else {
                    setDestination(latLng, true);
                }

                if (isOriginAndDestinationSet()) {
                    showPath();
                }
            }
        }); // OnMapLongClickListener
    } // OnMapReady

    /**
     * gets the map.
     * @return the map being shown to the user.
     */
    public GoogleMap getMap() {
        return mMap;
    }

    /**
     * gets the location that was set as the beginning of the path.
     * @return the origin.
     */
    public LatLng getOrigin() {
        return origin;
    }

    /**
     * sets the location of the beginning of the path.
     * @param origin the beginning of the path.
     * @param showOnMap if true, show it on the map as a marker; false, don't show it on the map.
     */
    public void setOrigin(LatLng origin, boolean showOnMap) {
        this.origin = origin;

        if (showOnMap) {
            showMarker(origin, "Origin", BitmapDescriptorFactory.HUE_GREEN);
        }
    }

    /**
     * gets the location that was set as the end of the path.
     * @return the destination.
     */
    public LatLng getDestination() {
        return destination;
    }

    /**
     * sets the location of the end of the path.
     * @param destination the end of the path.
     * @param showOnMap if true, show it on the map as a marker; false, don't show it on the map.
     */
    public void setDestination(LatLng destination, boolean showOnMap) {
        this.destination = destination;

        if (showOnMap) {
            showMarker(destination, "Destination", BitmapDescriptorFactory.HUE_RED);
        }
    }

    /**
     * moves the camera to focus on ryerson university.
     */
    public void moveCameraToRyerson() {
        float zoomLevel = 16.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Ryerson, zoomLevel));
    }

    /**
     * shows a marker on the map.
     * @param latLng the location of the marker in the map.
     * @param colour the colour of the marker
     *               (Use BitmapDescriptorFactory.HUE_"Colour Name" to get a colour as a float).
     */
    public void showMarker(LatLng latLng, String title, float colour) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        markerOptions.title(title);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(colour));

        mMap.addMarker(markerOptions);
    }

    /**
     * returns true if both origin and destination have been set to a value.
     * @return true if both origin and destination are set; false otherwise.
     */
    public boolean isOriginAndDestinationSet() {
        return origin != null && destination != null;
    }

    /**
     * shows a path from the current location to the destination.
     */
    public void showPath() {
        showPath(getOrigin(), getDestination());
    }

    /**
     * shows a path using additional options.
     * @param options options for finding paths, in google's url format (not including
     *                the origin, destination, sensor, mode parameters).
     */
    public void showPath(String... options) {
        showPath(getOrigin(), getDestination(), options);
    }

    /**
     * shows a path from a passed origin and passed destination.
     * @param origin the location that the path starts at.
     * @param destination the location that the path ends at.
     */
    public void showPath(LatLng origin, LatLng destination) {
        if (isOriginAndDestinationSet()) {
            RequestDirections( getRequestUrl(origin, destination) );
        } else {
            String message = "origin and/or destination not set.";
            Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * shows a path from a passed origin and passed destination using additional options.
     * @param origin the location that the path starts at.
     * @param destination the location that the path ends at.
     * @param options options for finding paths, in google's url format (not including
     *                the origin, destination, sensor, mode parameters).
     */
    public void showPath(LatLng origin, LatLng destination, String... options) {
        if (isOriginAndDestinationSet()) {
            RequestDirections( getRequestUrl(origin, destination, options) );
        } else {
            String message = "origin and/or destination not set.";
            Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Starts a task to get and show directions.
     * @param url the url to request the directions from.
     */
    private void RequestDirections(String url) {
        TaskRequestDirections taskRequestDirections = new TaskRequestDirections(this);
        taskRequestDirections.execute(url);
    }

    /**
     * clears any path or points from the map.
     */
    public void clearMap() {
        mMap.clear();
    }

    /**
     * creates a url for getting directions from origin to destination.
     * @param origin the location that the path starts at.
     * @param destination the location that the path ends at.
     * @return a request url
     */
    private String getRequestUrl(LatLng origin, LatLng destination) {
        // Retrieved from https://www.youtube.com/watch?v=jg1urt3FGCY&t=342s&index=31&list=WL on 03/18/2018
        // Code START:
        String str_org  = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;

        String sensor = "sensor=false";
        String mode   = "mode=walking";

        String param  = str_org + "&" + str_dest + "&" +sensor+ "&" + mode;
        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        // Code END
    }

    /**
     * creates a url for getting directions from origin to destination with extra options.
     * @param origin the location that the path starts at.
     * @param destination the location that the path ends at.
     * @param options options excluding the origin, destination, sensor, and mode option.
     * @return returns a url to the server that gives directions from one location to another.
     */
    private  String getRequestUrl(LatLng origin, LatLng destination, String... options) {
        String extraParams = "&";
        StringBuilder stringBuilder = new StringBuilder(extraParams);

        for (String option : options) {
            stringBuilder.append(option);
            stringBuilder.append("&");
        }

        extraParams = stringBuilder.toString();
        return getRequestUrl(origin, destination) + extraParams;
    }

    /**
     * request directions
     * @param reqUrl the url to request the directions from.
     * @return the response string from google maps
     */
    private String requestDirection(String reqUrl) throws IOException {
        // Retrieved from https://www.youtube.com/watch?v=jg1urt3FGCY&t=342s&index=31&list=WL on 03/18/2018
        // Code START:
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL(reqUrl);

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            // Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            responseString = stringBuilder.toString();
            bufferedReader.close();
            inputStreamReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        return responseString;
        // Code END
    } // requestDirection

    /**
     * Request directions from a url, and gets the response string.
     */
    public static class TaskRequestDirections extends AsyncTask<String, Void, String> {
        MapFragment parent;

        TaskRequestDirections(MapFragment parent) {
            this.parent = parent;
        }

        @Override
        protected String doInBackground(String... strings) {
            // Retrieved from https://www.youtube.com/watch?v=jg1urt3FGCY&t=342s&index=31&list=WL on 03/18/2018
            // Code START:
            String responseString = "";
            try {
                responseString = parent.requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
            // Code END
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // Parsing Json file
            TaskParser taskParser = new TaskParser(parent);
            taskParser.execute(s);
        }
    }

    /**
     * Takes a directions json string and interprets it, and shows it on the map.
     */
    public static class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {
        MapFragment parent;

        TaskParser(MapFragment parent) {
            this.parent = parent;
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            // Retrieved from https://www.youtube.com/watch?v=jg1urt3FGCY&t=342s&index=31&list=WL on 03/18/2018
            // Code START:
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
            // Code END
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            // Retrieved from https://www.youtube.com/watch?v=jg1urt3FGCY&t=342s&index=31&list=WL on 03/18/2018
            // Code START:
            // Get List route and display it into the map
            ArrayList<LatLng> points;
            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList<>();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat, lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions != null) {
                parent.getMap().addPolyline(polylineOptions);
            } else {
                Context context = parent.getActivity().getApplicationContext();
                Toast.makeText(context, "Directions not found", Toast.LENGTH_SHORT).show();
            }
            // Code END
        }
    }

}
