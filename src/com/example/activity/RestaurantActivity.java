package com.example.activity;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.R;
import com.example.Restaurant;

public class RestaurantActivity extends Activity{
	public static String KEY_REFERENCE = "reference"; // id of the place
    public static String KEY_NAME = "name"; // name of the place
    public static String KEY_RESTAURANT = "restaurant";
	private String reference;
	private String name;
    private  Restaurant restaurant;
    private String GET_PLACES_DETAILS_URL="https://graph.facebook.com/";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant);
        Intent intent = getIntent();
        reference = intent.getStringExtra(KEY_REFERENCE);
        name = intent.getStringExtra(KEY_NAME);
        restaurant=new Restaurant();
        restaurant=(Restaurant)intent.getExtras().get(KEY_RESTAURANT);
        
        setTitle(name);
        new LoadPlaceDetails().execute();
        
    }
    public void populateRestaurantData(){
//    	String likes=jsonObj.getString("likes");
//		TextView likesView=(TextView) findViewById(R.id.likes);
//		likesView.setText(likes+" People liked this");
//		String talk=jsonObj.getString("talking_about_count");
//		TextView talkView=(TextView) findViewById(R.id.talk);
//		likesView.setText(talk+" people are talking about this");
//		String fbcheckins=jsonObj.getString("were_here_count");
//		TextView fbciView=(TextView) findViewById(R.id.fbcheckin);
//		likesView.setText(fbcheckins+" people have been here");
    }
    
   
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {    
       switch (item.getItemId()) 
       {        
          case R.id.checkin:            
             Intent intent = new Intent(this, CheckInActivity.class);            
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
             Bundle extras=new Bundle();
             extras.putSerializable(KEY_RESTAURANT, restaurant);
             intent.putExtras(extras);
             startActivity(intent);            
             return true;        
          default:            
             return super.onOptionsItemSelected(item);    
       }
    }
 
    /** Callback function */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.restaurant_menu, menu);
        return super.onCreateOptionsMenu(menu);
        
    }
    private void getPlaceDetails(){
    
    	
    	}
    class LoadPlaceDetails extends AsyncTask<String, String, String>{
    	TextView likesView;
    	TextView talkView;
    	TextView fbCheckInView;
    	String likes;
		String talks;
		String checkIns;
    	protected void onPreExecute(){
    		likesView=(TextView) findViewById(R.id.likes);
    		talkView=(TextView) findViewById(R.id.talk);
    		fbCheckInView=(TextView) findViewById(R.id.fbcheckin);
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			String placeDetails=null;
			
			
	    	try{
	    	String finalURL=GET_PLACES_DETAILS_URL+reference;
	    	
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpGet request = new HttpGet(finalURL);
	        ResponseHandler<String> handler = new BasicResponseHandler();
	        placeDetails = httpclient.execute(request, handler);
	        System.out.println(placeDetails);
	    	}
	    	catch(Exception ex){
	    		ex.printStackTrace();
	    	}
	    	try{

	    		JSONObject jsonObj=new JSONObject(placeDetails);
	    		likes=jsonObj.getString("likes");
	    		talks=jsonObj.getString("talking_about_count");
	    		checkIns=jsonObj.getString("were_here_count");
	    		
	  
	    		
	    	}
	    	catch(JSONException ex){
	    		ex.printStackTrace();
	    	}
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					likesView.setText(likes+" people like this place");
					talkView.setText(talks+" people are talking about this");
					fbCheckInView.setText(checkIns+" people have been here");
					
				}
			});
			return null;
		}
		
		protected void onPostExecute(){
			
			
		}
    	
    	
    }
}
