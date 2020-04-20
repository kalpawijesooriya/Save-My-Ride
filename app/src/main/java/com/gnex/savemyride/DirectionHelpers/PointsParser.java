package com.gnex.savemyride.DirectionHelpers;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.gnex.savemyride.MapsActivity;
import com.gnex.savemyride.Models.MarkerModel;
import com.gnex.savemyride.R;
import com.gnex.savemyride.ReadMarkers;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Vishal on 10/20/2018.
 */

public class PointsParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    TaskLoadedCallback taskCallback;
    String directionMode = "driving";
  static   List <MarkerModel> markerModels = new ArrayList <MarkerModel>();
    static  MarkerModel mark;
    public
    PointsParser(Context mContext, String directionMode) {
        this.taskCallback = (TaskLoadedCallback) mContext;
        this.directionMode = directionMode;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            Log.d("mylog", jsonData[0]);
            DataParser parser = new DataParser();
            Log.d("mylog", parser.toString());

            // Starts parsing data
            routes = parser.parse(jObject);
            Log.d("mylog", "Executing routes");
            Log.d("mylog", routes.toString());

//            String rrr = routes.toString();
//            int maxLogSize = 1000;
//            for(int i = 0; i <= rrr.length() / maxLogSize; i++) {
//                int start = i * maxLogSize;
//                int end = (i+1) * maxLogSize;
//                end = end > rrr.length() ? rrr.length() : end;
//                Log.v("**********", rrr.substring(start, end));
//            }

        } catch (Exception e) {
            Log.d("mylog", e.toString());
            e.printStackTrace();
        }
        return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);
            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));


                LatLng position = new LatLng(lat, lng);
                isMarker(position);

                points.add(position);
            }
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            if (directionMode.equalsIgnoreCase("walking")) {
                lineOptions.width(10);
                lineOptions.color(Color.MAGENTA);
            } else {
                lineOptions.width(20);
                lineOptions.color(Color.rgb(20, 95, 190));
                lineOptions.clickable(true);

            }
            Log.d("mylog", "onPostExecute lineoptions decoded");
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            //mMap.addPolyline(lineOptions);
            try {
                taskCallback.onTaskDone(lineOptions,markerModels);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Log.d("mylog", "without Polylines drawn");
        }
    }

  private  void  isMarker(LatLng position  )
  {
      ReadMarkers rm=new ReadMarkers();
      JSONArray markers=null;
      try {
          JSONObject jsonObject = new JSONObject(rm.loadJSONFromAsset((Context) taskCallback));
          markers = (JSONArray) jsonObject.get("markers");
          for (int i = 0; i < markers.length(); ++i) {
              JSONObject rec = markers.getJSONObject(i);
              String type = rec.getString("type");
              String speed = rec.getString("speed");
              String lat   = rec.getString("lat");
              String lon   =rec.getString("lon");
              String name   =rec.getString("name");
              String road   =rec.getString("Road");
              LatLng makerlatlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
              double dist = Maker.getStraightLineDistance(position, makerlatlng);
              mark= new MarkerModel();
              if (dist <= 0.05) {
                  mark.setType(type);
                  mark.setLocation(makerlatlng);
                  mark.setStstus(false);
                  mark.setStstusPassed(false);
                  mark.setName(name);
                  mark.setRoad(road);
                  mark.setSpeed(speed);

                  if (!contains(markerModels,name))
                    markerModels.add(mark);
              }
          }
      } catch (JSONException e) {
          e.printStackTrace();
      }




  }

    boolean contains(List<MarkerModel> list, String name) {
        for (MarkerModel item : list) {
            if (item.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}