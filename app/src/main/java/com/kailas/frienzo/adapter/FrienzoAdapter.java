package com.kailas.frienzo.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.kailas.frienzo.model.Location;
import com.kailas.frienzo.model.User;
import com.kailas.frienzo.model.UserResponse;
import com.kailas.locationmapper.ApplicationController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by kailasl on 5/11/2015.
 */
public class FrienzoAdapter {

    private static final String BASE_URL = "http://192.168.0.104:8080/frienzoservice";
    Logger logger = Logger.getLogger(FrienzoAdapter.class.toString());


    private static class UserEndpoints {
        public static final String USER_SERVICE_URI_UPDATE = "/user/update";
        public static final String USER_SERVICE_URI_GET = "/user/{id}";
        public static final String USER_SERVICE_URI_FRIENDS = "/user/friends";
        public static final String USER_SERVICE_URI_UNSUBSCRIBE = "/user/friends/unsubscribe/{userId}/{friendId}";
        public static final String USER_SERVICE_URI_SUBSCRIBE = "/user/friends/subscribe/{userId}/{friendId}";

    }

    private Context context;
    public FrienzoAdapter(Context context) {
        this.context = context;
    }
    public FrienzoAdapter(){}
    private static class LocationEndpoints {
        public static final String LOCATION_SERVICE_URI_UPDATE = "/location/";
        public static final String LOCATION_SERVICE_URI_GET_ALL = "/location/all";
        public static final String LOCATION_SERVICE_URI_GET = "/location/{id}";
    }

    public  void updateUser(User user) throws JSONException {
        // Tag used to cancel the request
        String tag_json_obj = "update_user_req";
        Gson gson = new Gson();
        JSONObject jsonObject = new JSONObject(gson.toJson(user));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,  BASE_URL+UserEndpoints.USER_SERVICE_URI_UPDATE, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Toast.makeText(context, "User successfully added to DB", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(context, "Failed to add User  to DB", Toast.LENGTH_LONG).show();
            }
        });


        ApplicationController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj );
    }


    public void updateUserLocation(String name, double latitude, double longitude) throws JSONException {
        Location location = new Location();
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        String tag_json_obj = "update_user_loc_req";
        Gson gson = new Gson();
        JSONObject jsonObject = new JSONObject(gson.toJson(location));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,  BASE_URL+LocationEndpoints.LOCATION_SERVICE_URI_UPDATE+name, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                //TODO:AddLog
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //TODO:AddLog
            }
        });


        ApplicationController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_obj );
    }

    public void updateFriendLocations(final Map<String, Marker> markers, final GoogleMap googleMap, final String id) {
        String tag_json_obj = "update_friends_loc_req";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, BASE_URL+UserEndpoints.USER_SERVICE_URI_FRIENDS,  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                logger.info("getting update"+ response);
                Gson gson = new Gson();
                Type userMapType = new TypeToken<Map<String, User>>(){}.getType();
                Map<String, User> friendsmap = gson.fromJson(response, userMapType);
                    logger.info("update " + friendsmap + " " + response + " " + friendsmap.size());
                for(Map.Entry<String, User> entry: friendsmap.entrySet()) {
                    User user = entry.getValue();
                    String userId = entry.getKey();
                    logger.info("checking user id " + friendsmap + " " + response + " " + friendsmap.size());
                    if(userId.equals(id))
                        continue;
                    com.kailas.frienzo.model.Location location = user.getLastKnownLocation();
                    logger.info("checking location" + friendsmap + " " + response + " " + friendsmap.size());
                    if(location == null)
                        continue;
                    LatLng frndLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    logger.info("adding markers " + friendsmap + " " + response + " " + friendsmap.size());
                    if(markers.containsKey(userId)) {
                        markers.get(userId).remove();

                    }

                    logger.info("Adding marker at "+ frndLatLng.latitude + frndLatLng.longitude + " For User "+ userId );
                    Marker marker = googleMap.addMarker(new MarkerOptions().position(frndLatLng).visible(true).title(user.getName()).snippet("Email: "+ user.getEmail()).draggable(false));
                    markers.put(userId, marker);
                    marker.showInfoWindow();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });


        ApplicationController.getInstance().addToRequestQueue(stringRequest, tag_json_obj );
    }
}
