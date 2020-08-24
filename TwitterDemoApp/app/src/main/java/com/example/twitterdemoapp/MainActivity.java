package com.example.twitterdemoapp;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{

    private final int RESULT_LOAD_IMAGE_CODE = 222;
    private final  int REQUEST_READ_STORAGE_CODE_PERMISSIONS = 321;


    //adapter class
    ArrayList<AdapterItem> tweetsList = new ArrayList<AdapterItem>();
    TweetsCustomAdapter tweetsCustomAdapter;

    int StartFrom = 0;
    int UserOperation = SearchType.MyFollowing; // 0 my followers post 2- specifc user post 3- search post
    String Searchquery;
    int totalItemCountVisible = 0; //totalItems visible
    int SelectedUserID = 0;

    Button buFollow;
    SearchView searchView;
    Menu myMenu;
    LinearLayout ChannelInfo;
    TextView txtnamefollowers;

    String attachedImageURL = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ChannelInfo=(LinearLayout)findViewById(R.id.ChannelInfo) ;
        ChannelInfo.setVisibility(View.GONE);
        txtnamefollowers=(TextView)findViewById(R.id.txtnamefollowers) ;
        buFollow=(Button)findViewById(R.id.buFollow);


        SaveSettings saveSettings= new SaveSettings(getApplicationContext());
        saveSettings.LoadData();

        //TODO: set the adapter
        ListView tweetsListView=(ListView)findViewById(R.id.LVNews);
        tweetsCustomAdapter = new TweetsCustomAdapter(tweetsList,this);
        tweetsListView.setAdapter(tweetsCustomAdapter);//intisal with data

        tweetsList.add(new AdapterItem("add"));
        tweetsList.add(new AdapterItem("loading"));

        tweetsCustomAdapter.notifyDataSetChanged();
    }

    public void buFollowers(View view)
    {
        //TODO: add code s=for subscribe and un subscribe

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

                }
                //TODO: search in posts
                //LoadTweets(0,SearchType.SearchIn);// seearch
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //   searchView.setOnCloseListener(this);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.home:
                //TODO: main search
              //  LoadTweets(0,SearchType.MyFollowing);
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
                            // http://localhost/TwitterServer/AddTweet.php?user_id=1&tweet_text=Hello, This is my fisrt tweet&tweet_picture=photo.png

                            boolean flag = addTweetOnPhpServer(SaveSettings.UserID , postTextArea.getText().toString() , attachedImageURL);
                            if(flag)
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


            }

            return myView;
        }
    }

    final private String SERVER_PATH = "http://localhost:8080/TwitterServer/";
    private boolean addTweetOnPhpServer(String userID, String tweetText, String attachedImageURL)
    {
        // http://localhost/TwitterServer/
        // AddTweet.php?user_id=1&tweet_text=Hello, This is my fisrt tweet&tweet_picture=photo.png
        try {

            String url= SERVER_PATH +
                    "AddTweet.php?user_id="+userID+"&tweet_text="+tweetText+"&tweet_picture="+attachedImageURL;

            new TweetsAsyncTasks().execute(url);

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
            Uri selectedImageUri = data.getData();
            String[] dataPath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver()
                    .query(selectedImageUri,dataPath,null,null,null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(dataPath[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            //userImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            uploadImage(BitmapFactory.decodeFile(picturePath));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }




    private void uploadImage(Bitmap bitmap)
    {
        try {
            showProgressDialog();
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
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    hideProgressDialog();
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
                                attachedImageURL = mountainsRef.getDownloadUrl().toString();
                                attachedImageURL = java.net.URLEncoder.encode(attachedImageURL, "UTF-8");

                                hideProgressDialog();
                            }
                            catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    });

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        hideProgressDialog();
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

                Toast.makeText(getApplicationContext(),progress[0] , Toast.LENGTH_SHORT).show();

                JSONObject json = new JSONObject(progress[0]);

                String phpMsg = json.getString("msg");

                //display response data
                if (phpMsg == null)
                    return;

                if (phpMsg.equalsIgnoreCase("post added"))
                {
                    UpdateUiAfterPostAdded();
                }

                if(phpMsg.equalsIgnoreCase("post failed"))
                {
                    UpdateUiAfterPostFailed();
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void UpdateUiAfterPostAdded() {
        // TODO: LoadTweets(0,UserOperation);
        Log.i("ADD_POST", "UpdateUiAfterPostAdded: post added successfully");
    }

    private void UpdateUiAfterPostFailed() {
        // TODO: failed message
        Log.i("ADD_POST", "UpdateUiAfterPostAdded: failed ");
    }

}
