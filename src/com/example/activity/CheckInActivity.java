package com.example.activity;
import com.example.constants.Constants;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.example.*;
import com.facebook.android.Facebook;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class CheckInActivity extends Activity{
	final int PICTURE_ACTIVITY = 1;
	Intent takePictureIntent;
    
	String GET_DISHES_URL = "http://ec2-23-20-146-25.compute-1.amazonaws.com/sakath-oota-backend/rest/fooditems/all";
	String SAVE_ITEM_URL = "http://ec2-23-20-146-25.compute-1.amazonaws.com/sakath-oota-backend/rest/items/item/";	
	String UPLOAD_IMAGE_URL = "http://ec2-23-20-146-25.compute-1.amazonaws.com/sakath-oota-backend/ImageUpload";

	private String listview_array[] = new String[10];
	public static String KEY_RESTAURANT = "restaurant";
	
	private Uri imageUri;

	private User userObj = new User();

	private String itemSelected;
	
	

	private static String APP_ID =Constants.APP_ID;
	
	private String uuid;

	RatingBar ratingBar;
	Restaurant restaurant;

	//AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_dishes);

	EditText mEdit;

	private SharedPreferences mPrefs;

	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkin);
        setTitle(R.string.checkin_title);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        listview_array = getDishesItems();
        Intent intent=getIntent();
        Bundle extras = new Bundle();
        restaurant=new Restaurant();
        restaurant=(Restaurant)intent.getExtras().get(KEY_RESTAURANT);
        
      
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, listview_array);
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_dishes);
       textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        	public void onItemClick(AdapterView<?> parent, View view,

        	int position, long id) {

        	itemSelected = parent.getItemAtPosition(position).toString(); 

        	}});

        	textView.setAdapter(adapter);


    }
 
    /** Callback function */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.checkin_menu, menu);
        return super.onCreateOptionsMenu(menu);
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {    
       switch (item.getItemId()) 
       {        
          case R.id.camera:
        	  dispatchTakePictureIntent(PICTURE_ACTIVITY);
        	  return true;
          case R.id.save:
		try {
			saveItem();
			return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          default:            
             return super.onOptionsItemSelected(item);    
       }
       
    }
    
    private void dispatchTakePictureIntent(int actionCode) {
        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, actionCode);
        
    }
    protected void onActivityResult(int requestCode, int resultCode,Intent data){
        Bundle extras = data.getExtras();
      Bitmap mImageBitmap = (Bitmap) extras.get("data");
      ImageView mImageView = (ImageView)findViewById(R.id.dish_photo);
      mImageView.setImageBitmap(mImageBitmap);
          ByteArrayOutputStream bytes = new ByteArrayOutputStream();
          mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
          uuid = UUID.randomUUID().toString();
          File file = new File(Environment.getExternalStorageDirectory()+File.separator +uuid+".jpg");
          try {
              file.createNewFile();
              FileOutputStream fo = new FileOutputStream(file);
              fo.write(bytes.toByteArray());
              fo.close();
              Toast.makeText(this, "<<<< success",
            Toast.LENGTH_LONG).show();
          } catch (IOException e) {
              // TODO Auto-generated catch block
               Toast.makeText(this, "<<<< error",
                  Toast.LENGTH_LONG).show();
              e.printStackTrace();
          }
      }

    public String[] getDishesItems(){
        String result = callGetDishesSerivice();
        String list[] = new String[1000];
        try {
            JSONArray jsonArray = new JSONArray(result) ;
            int i=0;
            for(i=0;i < jsonArray.length();i++){
                JSONObject objJson = jsonArray.getJSONObject(i);
                String name =objJson.getString("name");
                //TO DO populate the ids''s
                list[i] = name;
            }
            String list1[]=new String[i];
            for(int j=0;j<i;j++){
            	list1[j]=list[j];
            }
            return list1;

        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    
    //Get list of all the dishes
    public String callGetDishesSerivice(){
        String dishes = null;
        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet(GET_DISHES_URL);
            ResponseHandler<String> handler = new BasicResponseHandler();
            dishes = httpclient.execute(request, handler);
            System.out.println(dishes);
            
            //Toast.makeText(this, returned, Toast.LENGTH_LONG).show();

        }
        catch (ClientProtocolException e) {
            Toast.makeText(this, "There was an issue Try again later", Toast.LENGTH_LONG).show();
        }
        catch (IOException e) {
            Toast.makeText(this, "There was an IO issue Try again later", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return dishes.toString();
    }
    private void saveItem() throws  JSONException, Exception {
        Context context = getApplicationContext();
        CharSequence text = "Your item is saved!!!";
        int duration = Toast.LENGTH_SHORT;
        boolean isSuccess = false;
        
        String imageFilename =  Environment.getExternalStorageDirectory().getPath()+"/"+uuid+".jpg";
        int responseValue= uploadFile(imageFilename);
        if(responseValue == 200 ){
                /*Toast.makeText(this, "<<<< Success: "+response+"",
                          Toast.LENGTH_LONG).show();*/
        }
        if(responseValue != 200 ){
         /*Toast.makeText(this, "<<<< Not success:"+response+" ",
                   Toast.LENGTH_LONG).show();*/
        }

        
        //Construct the shared object and post it

        SharedItem sharedItem = new SharedItem();
        sharedItem = getSharedObject();
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(sharedItem,SharedItem.class);

        org.apache.http.HttpResponse response = postItemObject(SAVE_ITEM_URL,json);
        if(response!=null && response.getStatusLine().getStatusCode()==200)
              isSuccess = true;
        else 
              isSuccess = false;
        Log.i("MyApp*******", "AfterResponse");
        
        if(isSuccess)
              text = "Your item is saved!!!";
        else
              text ="Sorry! there was an error saving item";
        
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
     }
     
     
     
     private SharedItem getSharedObject() throws  JSONException, Exception
     {
        SharedItem sharedItem = new SharedItem();
        RatingBar ratingBar = (RatingBar) findViewById(R.id.widget36);
        mEdit   = (EditText)findViewById(R.id.widget37);
        String description = mEdit.getText().toString();
        FoodItem item = new FoodItem();
        
        item.setId("5121dd5ae4b0d5889fdf3b3d");
        item.setName(itemSelected);
        
        
        sharedItem.setItem(item);
        
        sharedItem.setEmotionId((int)ratingBar.getRating());
        sharedItem.setImageUrl("http://ec2-23-20-146-25.compute-1.amazonaws.com/images/"+uuid+".jpg");
        sharedItem.setDescription(description);

        
        sharedItem.setRestaurant(restaurant);
        Date date = new Date();
        sharedItem.setDate(null);
     
        User user = new User();
        user = getUserObject();

        sharedItem.setUser(user);
        
        
        return sharedItem;
     }
     private User getUserObject() throws  JSONException, IOException
     {
        mPrefs = getSharedPreferences(Constants.PREFERENCE_FILENAME,MODE_PRIVATE);
        Facebook facebook = new Facebook(APP_ID);
        Bundle bundle = new Bundle();
        bundle.putString("access_token", facebook.getAccessToken());
         JSONObject me  = new JSONObject(facebook.request("me"));
        /*Facebook facebook = new Facebook(APP_ID);
         String access_token = mPrefs.getString("access_token", null);
         JSONObject me = new JSONObject(facebook.request("me?access_token="+access_token));*/
         
         if(me.get("error")!=null){
              userObj.setName(null);
              userObj.setFaceboookId(null); // My ID
              userObj.setAccessToken(null);
              userObj.setDeviceToken(null);    
         }
         else {
              String id = me.getString("id");
              userObj.setName(me.getString("name"));
              userObj.setFaceboookId(id); // My ID
              userObj.setAccessToken(facebook.getAccessToken());
              userObj.setDeviceToken("12343465");     
         }
        return userObj;     
     }
     
     private Restaurant getRestaurantObject()
     {
        Restaurant restaurant = new Restaurant();
        //To DO
        return restaurant;
     }
     private org.apache.http.HttpResponse postItemObject(String uri, String json)
     {
         try {
              HttpPost httpPost = new HttpPost(uri);
              httpPost.setEntity(new StringEntity(json));
              httpPost.setHeader("Accept", "application/json");
              httpPost.setHeader("Content-type", "application/json");
              return new DefaultHttpClient().execute(httpPost);
          } catch (UnsupportedEncodingException e) {
               System.out.println("Failure");
               e.printStackTrace();
          } catch (ClientProtocolException e) {
               System.out.println("Failure");
               e.printStackTrace();
          } catch (IOException e) {
               System.out.println("Failure");
              e.printStackTrace();
             
          }
        return null;
     }
     
     public int uploadFile(String sourceFileUri) {

         String fileName = sourceFileUri;
         HttpURLConnection conn = null;
         DataOutputStream dos = null; 
         String lineEnd = "\r\n";
         String twoHyphens = "--";
         String boundary = "*****";
         int bytesRead, bytesAvailable, bufferSize;
         byte[] buffer;
         int maxBufferSize = 1 * 1024 * 1024;
         File sourceFile = new File(sourceFileUri);
         int serverResponseCode = 0;

         if (!sourceFile.isFile())
         {
          Log.e("uploadFile", "Source File Does not exist");
          return 0;
         }
             try { // open a URL connection to the Servlet
              FileInputStream fileInputStream = new FileInputStream(sourceFile);
              URL url = new URL(UPLOAD_IMAGE_URL);
              conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
              conn.setDoInput(true); // Allow Inputs
              conn.setDoOutput(true); // Allow Outputs
              conn.setUseCaches(false); // Don't use a Cached Copy
              conn.setRequestMethod("POST");
              conn.setRequestProperty("Connection", "Keep-Alive");
              conn.setRequestProperty("ENCTYPE", "multipart/form-data");
              conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
              conn.setRequestProperty("imageName", fileName);

              dos = new DataOutputStream(conn.getOutputStream());
              dos.writeBytes(twoHyphens + boundary + lineEnd);
              dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + lineEnd);
              dos.writeBytes(lineEnd);
              bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size
              bufferSize = Math.min(bytesAvailable, maxBufferSize);
              buffer = new byte[bufferSize];

              // read file and write it into form...
              bytesRead = fileInputStream.read(buffer, 0, bufferSize); 

                while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);              
               }

              // send multipart form data necesssary after file data...
              dos.writeBytes(lineEnd);
              dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

              // Responses from the server (code and message)
              serverResponseCode = conn.getResponseCode();
              String serverResponseMessage = conn.getResponseMessage();
              Log.e("server response codde",""+serverResponseCode);
              Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
              fileInputStream.close();
              dos.flush();
              dos.close();

         } catch (MalformedURLException ex) { 
             ex.printStackTrace();
             Log.e("Upload file to server", "error: " + ex.getMessage(), ex); 
         } catch (Exception e) {
             e.printStackTrace();
             Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e); 
         }
         return serverResponseCode; 
        }


}