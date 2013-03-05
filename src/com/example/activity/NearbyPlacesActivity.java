package com.example.activity; //NearbyPlacesActivity

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.AlertDialogManager;
import com.example.ConnectionDetector;
import com.example.GPSTracker;
import com.example.GooglePlaces;

import com.example.R;
import com.example.Restaurant;
import com.example.RestaurantList;
import com.example.constants.Constants;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;


 
public class NearbyPlacesActivity extends Activity {
 
    // flag for Internet connection status
    Boolean isInternetPresent = false;
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    // Connection detector class
    ConnectionDetector cd;
 
    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();
 
    // Google Places
    GooglePlaces googlePlaces;
 
    // Places List
    RestaurantList nearPlaces;
 
    // GPS Location
    GPSTracker gps;
 
    // Button
    Button btnShowOnMap;
 
    // Progress dialog
    ProgressDialog pDialog;
 
    // Places Listview
    ListView lv;
 
    // ListItems data
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();
    private SharedPreferences mPrefs;
    // KEY Strings
    public static String KEY_RESTAURANT = "restaurant";
    public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_VICINITY = "vicinity"; // Place area name
    public static String GET_PLACES_URL="https://graph.facebook.com/search?type=place";
  private double _latitude;
  private double _longitude;
  private double _radius;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.places_container);
        mPrefs = getSharedPreferences(Constants.PREFERENCE_FILENAME, MODE_PRIVATE);
		String authtoken = mPrefs.getString("access_token", null);
        cd = new ConnectionDetector(getApplicationContext());
 
        // Check if Internet present
        isInternetPresent = cd.isConnectingToInternet();
        if (!isInternetPresent) {
            // Internet Connection is not present
            alert.showAlertDialog(NearbyPlacesActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }
 
        // creating GPS Class object
        gps = new GPSTracker(this);
 
        // check if GPS location can get
        if (gps.canGetLocation()) {
            _latitude=gps.getLatitude();
            _longitude=gps.getLongitude();
        } else {
            // Can't get user's current location
            alert.showAlertDialog(NearbyPlacesActivity.this, "GPS Status",
                    "Couldn't get location information. Please enable GPS",
                    false);
            // stop executing code by return
            return;
        }
 
        // Getting listview
        lv = (ListView) findViewById(R.id.list);
 
        // button show on map
        //btnShowOnMap = (Button) findViewById(R.id.btn_show_map);
 
        // calling background Async task to load Google Places
        // After getting places from Google all the data is shown in listview
        new LoadPlaces().execute();
 
        /** Button click event for shown on map */
//        btnShowOnMap.setOnClickListener(new View.OnClickListener() {
// 
//            @Override
//            public void onClick(View arg0) {
//                Intent i = new Intent(getApplicationContext(),
//                        PlacesMapActivity.class);
//                // Sending user current geo location
//                i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
//                i.putExtra("user_longitude", Double.toString(gps.getLongitude()));
// 
//                // passing near places to map activity
//                i.putExtra("near_places", nearPlaces);
//                // staring activity
//                startActivity(i);
//            }
//        });
 
        /**
         * ListItem click event
         * On selecting a listitem SinglePlaceActivity is launched
         * */
        lv.setOnItemClickListener(new OnItemClickListener() {
 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // getting values from selected ListItem
                String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();
                String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                Restaurant restaurant=new Restaurant();
                restaurant.setPlaceId(reference);
                restaurant.setName(name);
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),RestaurantActivity.class);
 
                // Sending place refrence id to single place activity
                // place refrence id used to get "Place full details"
                in.putExtra(KEY_REFERENCE, reference);
                in.putExtra(KEY_NAME, name);
                Bundle extras=new Bundle();
                extras.putSerializable(KEY_RESTAURANT, restaurant);
                in.putExtras(extras);
                startActivity(in);
            }
        });
    }
 
    /**
     * Background Async Task to Load Google places
     * */
    class LoadPlaces extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NearbyPlacesActivity.this);
            pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        public RestaurantList callGetPlacesSerivice(){
            String places = null;
            try {
            	String finalURL=GET_PLACES_URL+"&center="+_latitude+","+_longitude+"&distance=1000&method=GET&format=json&access_token="+mPrefs.getString("access_token", null);
            	
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet request = new HttpGet(finalURL);
                ResponseHandler<String> handler = new BasicResponseHandler();
                places = httpclient.execute(request, handler);
                System.out.println(places);
                
                //Toast.makeText(this, returned, Toast.LENGTH_LONG).show();
//            	HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
//                HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(GET_PLACES_URL));
//                
//                request.getUrl().put("type", "place");
//                request.getUrl().put("distance","2500"); // in meters
//                request.getUrl().put("center", "12.9833,77.5833");
//                request.getUrl().put("format", "json");
//                request.getUrl().put("access_token", "BAACEdEose0cBAHnJnfcJnk1qYyswHX1BirHAMf1105SAeTAqrHEBxY1J99rP57TzMJalJ314rsRN6TU4ZBQ5ER3y3G3L26NaZCxZAlFTB8oPGs8ndFW");
//               
     
//                RestaurantList list = request.execute().parseAs(RestaurantList.class);
                // Check log cat for places response status
//                Log.d("Places Status", "" + list.status);
                Restaurant list[] = new Restaurant[1000];
                try {
                    JSONObject json = new JSONObject(places) ;
                    JSONArray jsonArray=json.getJSONArray("data");
                    
                   // JSONObject objJson=json.getJSONObject(0);
                    
                 //   JSONArray json1=new JSONArray(objJson.toString());
                    
                    int i=0;
                    for(i=0;i < jsonArray.length();i++){
                        JSONObject objJson = jsonArray.getJSONObject(i);
                        
                        Restaurant restaurant = new Restaurant();
                        restaurant.setPlaceId(objJson.getString("id"));
                        restaurant.setName(objJson.getString("name"));
                        list[i]=restaurant;
                        
                    }
                    List<Restaurant>  list1=new ArrayList<Restaurant>(i);
                    for(int j=0;j<i;j++){
                    	list1.add(list[j]);
                    }
                    RestaurantList restaurantList=new RestaurantList();
                    restaurantList.status="OK";
                    restaurantList.results=list1;
                    return restaurantList;

                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                
                return null;

            }
            catch (ClientProtocolException e) {
                //Toast.makeText(this, "safdasf", 2);
            }
            catch (IOException e) {
                //Toast.makeText(this, "There was an IO issue Try again later", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return null;
            
        }
        
 
        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            
 
            try {
                
//                String types = "cafe|restaurant"; // Listing places only cafes, restaurants
//                double radius = 2500; // 1000 meters 
// 
//                // get nearest places
//                nearPlaces = googlePlaces.search(gps.getLatitude(),
//                        gps.getLongitude(), radius, types);
            	nearPlaces=callGetPlacesSerivice();
            	
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * and show the data in UI
         * Always use runOnUiThread(new Runnable()) to update UI from background
         * thread, otherwise you will get error
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            
        	pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    
                    
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                    // Get json response status
                    String status = nearPlaces.status;
 
                    // Check for all possible status
                    if(status.equals("OK")){
                        // Successfully got places details
                        if (nearPlaces.results != null) {
                            // loop through each place
                            for (Restaurant p : nearPlaces.results) {
                                HashMap<String, String> map = new HashMap<String, String>();
 
                                // Place reference won't display in listview - it will be hidden
                                // Place reference is used to get "place full details"
                                map.put(KEY_REFERENCE, p.getPlaceId());
 
                                // Place name
                                map.put(KEY_NAME, p.getName());
 
                                // adding HashMap to ArrayList
                                placesListItems.add(map);
                            }
                            // list adapter
                            ListAdapter adapter = new SimpleAdapter(NearbyPlacesActivity.this, placesListItems,
                                    R.layout.list_places,
                                    new String[] { KEY_REFERENCE, KEY_NAME}, new int[] {
                                            R.id.reference, R.id.name });
 
                            // Adding data into listview
                            lv.setAdapter(adapter);
                        }
                    }
                    else if(status.equals("ZERO_RESULTS")){
                        // Zero results found
                        alert.showAlertDialog(NearbyPlacesActivity.this, "Near Places",
                                "Sorry no places found. Try to change the types of places",
                                false);
                    }
                    else if(status.equals("UNKNOWN_ERROR"))
                    {
                        alert.showAlertDialog(NearbyPlacesActivity.this, "Places Error",
                                "Sorry unknown error occured.",
                                false);
                    }
                    else if(status.equals("OVER_QUERY_LIMIT"))
                    {
                        alert.showAlertDialog(NearbyPlacesActivity.this, "Places Error",
                                "Sorry query limit to google places is reached",
                                false);
                    }
                    else if(status.equals("REQUEST_DENIED"))
                    {
                        alert.showAlertDialog(NearbyPlacesActivity.this, "Places Error",
                                "Sorry error occured. Request is denied",
                                false);
                    }
                    else if(status.equals("INVALID_REQUEST"))
                    {
                        alert.showAlertDialog(NearbyPlacesActivity.this, "Places Error",
                                "Sorry error occured. Invalid Request",
                                false);
                    }
                    else
                    {
                        alert.showAlertDialog(NearbyPlacesActivity.this, "Places Error",
                                "Sorry error occured.",
                                false);
                    }
                }
            });
 
        }
 
    }
    
 
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.placesContainer, menu);
//        return true;
//    }
 
}