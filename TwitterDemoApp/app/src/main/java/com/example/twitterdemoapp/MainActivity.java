package com.example.twitterdemoapp;


import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity
{

    private final int RESULT_LOAD_IMAGE_CODE = 222;
    private final  int REQUEST_READ_STORAGE_CODE_PERMISSIONS = 321;
    final private String SERVER_PATH = "http://localhost:8080/TwitterServer/";

    //adapter class
    ArrayList<AdapterItem> tweetsList = new ArrayList<AdapterItem>();
    TweetsCustomAdapter tweetsCustomAdapter;

    int StartFrom = 0;
    int UserOperation = SearchType.MyFollowing; // 0 my followers post 2- specifc user post 3- search post
    String Searchquery;
    int totalItemCountVisible = 0; //totalItems visible
    int SelectedUserID = 0;


    SearchView searchView;
    Menu myMenu;

    LinearLayout ChannelInfo;
    TextView txtnamefollowers;
    Button buFollow;
    ImageView selectedUserImage;

    String attachedImageURL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ChannelInfo=(LinearLayout)findViewById(R.id.ChannelInfo) ;
        ChannelInfo.setVisibility(View.GONE);

        txtnamefollowers=(TextView)findViewById(R.id.txtnamefollowers) ;
        buFollow=(Button)findViewById(R.id.buFollow);
        selectedUserImage = findViewById(R.id.iv_channel_icon);

        SaveSettings saveSettings= new SaveSettings(getApplicationContext());
        saveSettings.LoadData();

        ListView tweetsListView=(ListView)findViewById(R.id.LVNews);
        tweetsCustomAdapter = new TweetsCustomAdapter(tweetsList,this);
        tweetsListView.setAdapter(tweetsCustomAdapter);//intisal with data

        tweetsList.add(new AdapterItem("add"));

        tweetsCustomAdapter.notifyDataSetChanged();

        LoadTweets(0,SearchType.MyFollowing);
    }

    public void buFollowers(View view)
    {
        String url = SERVER_PATH;
        int op = 0;
        // http://localhost:8080/TwitterServer/UserFollowing.php?user_id=1&following_user_id=2&op=1
        // op == 1 -> [user id]  follow [following user id]
        // op == 2 ->  //[user id] unfollow [user id]

        if(buFollow.getText().toString().equalsIgnoreCase("Follow")) {
            op = 1;
            buFollow.setText("un Follow");
        }
        else if (buFollow.getText().toString().equalsIgnoreCase("un Follow")) {
            op = 2;
            buFollow.setText("Follow");
        }

        url+= "UserFollowing.php?user_id="+SaveSettings.UserID+"&following_user_id="+SelectedUserID+"&op="+op;

        new TweetsAsyncTasks().execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        myMenu=menu;
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (android.widget.SearchView) menu.findItem(R.id.searchbar).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //final Context co=this;
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast.makeText(co, query, Toast.LENGTH_LONG).show();
                Searchquery=null;
                try {
                    //for space with name
                    Searchquery = java.net.URLEncoder.encode(query , "UTF-8");
                 } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                LoadTweets(0,SearchType.SearchIn);// seearch
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose()
            {
                LoadTweets(0,SearchType.MyFollowing);// seearch
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.home:
                LoadTweets(0,SearchType.MyFollowing);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class TweetsCustomAdapter extends BaseAdapter
    {
        ArrayList<AdapterItem> adapterItems ;
        Context context;

        public TweetsCustomAdapter(ArrayList<AdapterItem> adapterItems, Context context)
        {
            this.adapterItems = adapterItems;
            this.context = context;
        }

        @Override
        public int getCount() {
            return adapterItems.size();
        }

        @Override
        public Object getItem(int i) {
            return adapterItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup)
        {
            final AdapterItem adapterItem = adapterItems.get(index);
            LayoutInflater mInflater = getLayoutInflater();
            View myView = null;

            if(adapterItem.TAG.equalsIgnoreCase("add"))
            {
                myView = mInflater.inflate(R.layout.tweet_add, null);

                final EditText postTextArea = myView.findViewById(R.id.POST_TEXT_AREA);
                ImageView sendPostView = myView.findViewById(R.id.SEND_POST);
                ImageView attachImageView = myView.findViewById(R.id.ATTACH_IMAGE);

                sendPostView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Write post in php server..
                        if(postTextArea.getText().toString()!=null)
                        {
                            String postText = postTextArea.getText().toString();
                            try {
                                //for space with name
                                postText = URLEncoder.encode(postText , "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                                    boolean flag = addTweetOnPhpServer(SaveSettings.UserID, postText, attachedImageURL);
                                    if (flag)
                                        tweetsCustomAdapter.notifyDataSetChanged();

                        }
                    }
                });

                attachImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckReadExternalStoragePermissionAndPickImage();
                    }
                });

            }
            else if(adapterItem.TAG.equalsIgnoreCase("noTweet"))
            {
                myView = mInflater.inflate(R.layout.tweet_msg, null);

            }
            else if (adapterItem.TAG.equalsIgnoreCase("loading"))
            {
                myView = mInflater.inflate(R.layout.tweet_loading, null);

            }
            else if (adapterItem.TAG.equalsIgnoreCase("tweet"))
            {
                myView = mInflater.inflate(R.layout.tweet_item, null);

                TextView txtUserName = (TextView) myView.findViewById(R.id.txtUserName);
                txtUserName.setText(adapterItem.first_name);

                TextView txt_tweet = (TextView) myView.findViewById(R.id.txt_tweet);
                txt_tweet.setText(adapterItem.tweet_text);

                TextView txt_tweet_date = (TextView) myView.findViewById(R.id.txt_tweet_date);
                txt_tweet_date.setText(adapterItem.tweet_date);

                final ImageView buLikePost = myView.findViewById(R.id.iv_share);

                buLikePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        buLikePost.setImageResource(R.drawable.love_icon);
                    }
                });


                txtUserName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {

                        SelectedUserID = Integer.parseInt(adapterItem.user_id);
                        LoadTweets(0,SearchType.OnePerson);

                        txtnamefollowers.setText(adapterItem.first_name);
                        String imageURL = adapterItem.picture_path;

                        ImageFromFirebaseToImageView(imageURL,selectedUserImage);

                        if(SelectedUserID == Integer.parseInt(SaveSettings.UserID))
                        {
                            buFollow.setVisibility(View.GONE);
                        }
                        else
                        {
                            buFollow.setVisibility(View.VISIBLE);

                        }

                        //http://localhost/TwitterServer/isFollowing.php?user_id=1&following_user_id=2

                        String url = SERVER_PATH + "isFollowing.php?user_id=" + SaveSettings.UserID
                                + "&following_user_id="+adapterItem.user_id;

                        new  TweetsAsyncTasks().execute(url);
                    }
                });


                try {
                    Picasso.get().setLoggingEnabled(true);
                    final ImageView tweet_picture = (ImageView) myView.findViewById(R.id.tweet_picture);
                    final ImageView picture_path = (ImageView) myView.findViewById(R.id.picture_path);

                    if(adapterItem.tweet_picture != null)
                    {
                        FirebaseStorage.getInstance().getReference(adapterItem.tweet_picture).getDownloadUrl()
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                      //  exception.printStackTrace();
                                        tweet_picture.setVisibility(View.GONE);
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadUrl) {
                                        //do something with downloadurl
                                        Picasso.get().load(downloadUrl).into(tweet_picture);
                                        Log.i("TAG", "onSuccess: " + downloadUrl + " ----> " + downloadUrl.toString());
                                    }
                                });

                    }

                    if(adapterItem.picture_path != null)
                    {
                        FirebaseStorage.getInstance().getReference(adapterItem.picture_path).getDownloadUrl()
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                     //   exception.printStackTrace();
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadUrl) {
                                        //do something with downloadurl
                                        Picasso.get().load(downloadUrl).into(picture_path);
                                        Log.i("TAG", "onSuccess: " + downloadUrl + " ----> " + downloadUrl.toString());
                                    }
                                });
                    }

                }
                catch (Exception e){
                   // e.printStackTrace();
                    Log.i("Firebase images", "getView: " +  e.getMessage());
                }
            }
            return myView;
        }
    }

    Boolean ImageFromFirebaseToImageView(String url , final ImageView imgView)
    {
        final boolean[] flag = {true};
        try {
            Picasso.get().setLoggingEnabled(true);

            if(url != null)
            {
                FirebaseStorage.getInstance().getReference(url).getDownloadUrl()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                exception.printStackTrace();
                                flag[0] = false;

                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUrl) {
                                //do something with downloadurl
                                Picasso.get().load(downloadUrl).into(imgView);
                                Log.i("TAG", "onSuccess: " + downloadUrl + " ----> " + downloadUrl.toString());
                                flag[0] = true;
                            }
                        });
                return flag[0];
            }
            else
            {
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean addTweetOnPhpServer(String userID, String tweetText, String attachedImageURL)
    {
        // http://localhost/TwitterServer/AddTweet.php?user_id=1&tweet_text=Hello, This is my fisrt tweet&tweet_picture=photo.png
        try {

            String imgURL = attachedImageURL;

            String url= SERVER_PATH +
                    "AddTweet.php?user_id="+userID+"&tweet_text="+tweetText+"&tweet_picture="+imgURL;

            new TweetsAsyncTasks().execute(url);

            this.attachedImageURL = null;

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    void CheckReadExternalStoragePermissionAndPickImage(){
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_STORAGE_CODE_PERMISSIONS);
                return ;
            }
        }
        pickImage();// init the contact list
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_STORAGE_CODE_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage();// init the contact list
                } else {
                    // Permission Denied
                    Toast.makeText( this,"your message" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void pickImage() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == RESULT_LOAD_IMAGE_CODE && resultCode == RESULT_OK && null != data)
        {
            try {
                showProgressDialog();

                Uri selectedImageUri = data.getData();
                String[] dataPath = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver()
                        .query(selectedImageUri, dataPath, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(dataPath[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                //userImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

                uploadImage(BitmapFactory.decodeFile(picturePath));
            }catch (Exception Ex)
            {
                Ex.printStackTrace();
                Toast.makeText(getApplicationContext() , "Couldnot upload the image" ,Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void uploadImage(Bitmap bitmap)
    {
        try {
            showProgressDialog();

            DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
            Date dateobj = new Date();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReferenceFromUrl("gs://mytwitter-64249.appspot.com");

            final String ImagePath = SaveSettings.UserID+ "_" + df.format(dateobj) + ".jpg";

            final StorageReference mountainsRef = storageRef.child("Images/" + ImagePath);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = mountainsRef.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception)
                {
                    Toast.makeText(getApplicationContext(),"couldn't Attach the image " + exception.getMessage()  , Toast.LENGTH_SHORT).show();
                }
            })
             .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
             {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        hideProgressDialog();
                    }
             })
             .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
             {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    try {
                        // load into tweet pic.

                        //attachedImageURL = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                        attachedImageURL = mountainsRef.getPath();
                        Toast.makeText(getApplicationContext(),"Image Attached" , Toast.LENGTH_SHORT).show();
                        //attachedImageURL = mountainsRef.getDownloadUrl().toString();
                       // attachedImageURL = java.net.URLEncoder.encode(attachedImageURL, "UTF-8");


                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

             });

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Couldn't upload the image" , Toast.LENGTH_LONG).show();
        }
    }

    // loading display
    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    class TweetsAsyncTasks extends AsyncTask<String,String,String>
    {

        @Override
        protected void onPreExecute() {
            //before works
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... parameters)
        {
            try {
                String NewsData;
                //define the url we have to connect with
                URL url = new URL(parameters[0]);
                //make connect with url and send request
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //waiting for 7000ms for response
                urlConnection.setConnectTimeout(7000);//set timeout to 5 seconds

                try {
                    //getting the response data
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    //convert the stream to string
                    Operations operations=new Operations(getApplicationContext());
                    NewsData = operations.ConvertInputToStringNoChange(in);
                    //send to display data
                    publishProgress(NewsData);
                } finally {
                    //end connection
                    urlConnection.disconnect();
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }

            return null;
        }

        protected void onPostExecute(String  result2){
            hideProgressDialog();
        }

        protected void onProgressUpdate(String... progress) {

            try {

                Log.i("json", "onProgressUpdate: " + progress[0]);

              //  Toast.makeText(getApplicationContext(),progress[0] , Toast.LENGTH_SHORT).show();

                JSONObject json = new JSONObject(progress[0]);

                String phpMsg = json.getString("msg");

                //display response data
                if (phpMsg == null)
                    return;

                if (phpMsg.equalsIgnoreCase("post added")) // load my following tweets
                {
                    UpdateUiAfterPostAdded();
                }

                if(phpMsg.equalsIgnoreCase("post failed")) // do nothing
                {
                    Log.i("ADD_POST", "UpdateUiAfterPostAdded: failed ");
                }

                if(phpMsg.equalsIgnoreCase("hasTweets"))
                {
                    UpdateUiAfterHasTweetsMsg(json);
                }

                if(phpMsg.equalsIgnoreCase("noTweets"))
                {
                    //remove we are loading now
                    if(StartFrom == 0) // refresh tweets list view.
                    {
                        tweetsList.clear();
                        tweetsList.add(new AdapterItem("add"));
                    }
                    else {

                        //remove we are loading now
                        tweetsList.remove(tweetsList.size()-1);
                    }

                    tweetsList.add(new AdapterItem("noTweet"));
                    tweetsCustomAdapter.notifyDataSetChanged();
                }


                if (phpMsg.equalsIgnoreCase("following"))
                {
                    buFollow.setText("un Follow");
                }

                if (phpMsg.equalsIgnoreCase("not following"))
                {
                    buFollow.setText("Follow");
                }

            }
            catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void UpdateUiAfterHasTweetsMsg(JSONObject json)
    {
        if(StartFrom == 0) // for refreshing
        {
            tweetsList.clear();
            tweetsList.add(new AdapterItem("add"));
        }
        else { // when i just loading more
            //remove we are loading now
            tweetsList.remove(tweetsList.size()-1);
        }

        try // load tweets that are  results from the php server and add it to the list view
        {
            JSONArray tweetsInfo = new JSONArray(json.getString("tweets"));

            for(int i = 0 ; i < tweetsInfo.length() ; i++)
            {
                JSONObject tweet = tweetsInfo.getJSONObject(i);

                //add data and view it
                tweetsList.add(new AdapterItem(tweet.getString("tweet_id"),
                        tweet.getString("tweet_text"),tweet.getString("tweet_picture") ,
                        tweet.getString("tweet_date") ,tweet.getString("user_id") ,tweet.getString("first_name")
                        ,tweet.getString("picture_path") ));
            }

            tweetsCustomAdapter.notifyDataSetChanged();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void UpdateUiAfterPostAdded()
    {
        Log.i("ADD_POST", "UpdateUiAfterPostAdded: post added successfully");
        LoadTweets(0,SearchType.MyFollowing);
    }

    void LoadTweets(int startFrom , int op) // op = 1 , 2 or 3
    {
        this.StartFrom=startFrom;
        this.UserOperation = op;

        //display loading
        if(StartFrom==0) // add loading at beggining
            tweetsList.add(0,new AdapterItem("loading"));
        else // add loading at end
            tweetsList.add(new AdapterItem("loading"));

        tweetsCustomAdapter.notifyDataSetChanged();

        String url = SERVER_PATH + "listTweets.php?";

        if (UserOperation == SearchType.MyFollowing) // 1
             url+= "user_id="+ SaveSettings.UserID + "&StartFrom="+StartFrom + "&op="+ UserOperation; // 1

        if(UserOperation == SearchType.OnePerson) // 2
            url+= "user_id="+ SelectedUserID + "&StartFrom="+StartFrom + "&op="+ UserOperation;

        if (UserOperation == SearchType.SearchIn) // 3
            url+= "&StartFrom="+StartFrom + "&op="+ UserOperation + "&query="+ Searchquery;


        new TweetsAsyncTasks().execute(url);


        if (UserOperation==SearchType.OnePerson)
            ChannelInfo.setVisibility(View.VISIBLE);
        else
            ChannelInfo.setVisibility(View.GONE);

    }

}
