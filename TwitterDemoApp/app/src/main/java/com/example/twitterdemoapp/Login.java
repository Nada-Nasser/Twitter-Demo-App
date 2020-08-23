package com.example.twitterdemoapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Login extends AppCompatActivity
{
    private final int RESULT_LOAD_IMAGE_CODE = 111;
    private final  int REQUEST_READ_STORAGE_CODE_PERMISSIONS = 123;

    private EditText usernameEditText;
    private  EditText userEmailEditText;
    private EditText userPasswordEditText;
    private ImageView userImageView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    final private String SERVER_PATH = "http://localhost:8080/TwitterServer/";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.etName);
        userEmailEditText =(EditText)findViewById(R.id.etEmail);
        userPasswordEditText =(EditText)findViewById(R.id.etPassword);
        userImageView =(ImageView) findViewById(R.id.ivUserImage);

        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckReadExternalStoragePermission();
            }
        });

        //TODO: user register into firebase
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    Log.i("AuthStateListener", "onAuthStateChanged: User Logged out");
                }
                else
                {
                    Log.i("AuthStateListener", "onAuthStateChanged: User Logged in : " + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };

        try {
            JSONObject jsonObject = new JSONObject("{\"msg\":\"failed\"}");
            Toast.makeText(getApplicationContext(),"DONE" , Toast.LENGTH_LONG).show();
        }catch (JSONException err){
            Log.d("Error", err.toString());
            Toast.makeText(getApplicationContext(),"NOT --- DONE" , Toast.LENGTH_LONG).show();
        }

    }

    void CheckReadExternalStoragePermission(){
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

            userImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        signInAnonymously();
    }

    private void signInAnonymously()
    {
        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                                Log.d("firebaseAuth", "signInAnonymously:success");
                            Toast.makeText(getApplicationContext(), "Authentication Succeed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.w("firebaseAuth", "signInAnonymously:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        hideProgressDialog();
    }

    public void buLogin(View view)
    {
        //TODO: user login
        try {
            showProgressDialog();

            DateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
            Date dateobj = new Date();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReferenceFromUrl("gs://mytwitter-64249.appspot.com");

            final String ImagePath = df.format(dateobj) + ".jpg";
            final StorageReference mountainsRef = storageRef.child("Images/" + ImagePath);

            // Get the data from an ImageView as bytes
            userImageView.setDrawingCacheEnabled(true);
            userImageView.buildDrawingCache();

            BitmapDrawable drawable = (BitmapDrawable) userImageView.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
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
                        String downloadUrl = mountainsRef.getDownloadUrl().toString();

                        Log.i("UploadImage", "onSuccess: " + downloadUrl);

                        String name = "";

                        //for space with name
                        name = java.net.URLEncoder.encode(usernameEditText.getText().toString(), "UTF-8");
                        downloadUrl = java.net.URLEncoder.encode(downloadUrl, "UTF-8");


                        //TODO:  login and register

                        String url= SERVER_PATH +"Register.php?name="+usernameEditText.getText().toString()
                                +"&email="+userEmailEditText.getText().toString()
                                +"&password="+userPasswordEditText.getText().toString()+"&pic="+downloadUrl;


                        new MyAsyncTasks().execute(url);

                        hideProgressDialog();
                    }catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                }
            });
        }catch (Exception EX)
        {
            EX.printStackTrace();
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

    class MyAsyncTasks extends AsyncTask<String,String,String>
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
                if (progress[0].contains("string"))
                {
                    int indexOf = progress[0].indexOf("string");
                    progress[0] = progress[0].substring(6);
                }

                Toast.makeText(getApplicationContext(),progress[0] , Toast.LENGTH_SHORT).show();

               JSONObject json = new JSONObject(progress[0]);

                String phpMsg = json.getString("msg");
                //display response data
                if (phpMsg == null)
                    return;

                if (phpMsg.equalsIgnoreCase("failed"))
                {
                    UpdateUiAfterLoginFailed();
                }

                if (phpMsg.equalsIgnoreCase("registered"))
                {
                    String url= SERVER_PATH + "Login.php?name="+
                            "&email="+userEmailEditText.getText().toString()
                            +"&password="+userPasswordEditText.getText().toString();

                    new MyAsyncTasks().execute(url);
                }

                if(phpMsg.equalsIgnoreCase("loggedIn"))
                {
                    UpdateUiAfterLogin(json);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void UpdateUiAfterLoginFailed()
    {
        Toast.makeText(getApplicationContext(),"You can not Login/Register using this Info" , Toast.LENGTH_SHORT).show();
        hideProgressDialog();
    }

    private void UpdateUiAfterLogin(JSONObject jsonObject)
    {
        try {
            JSONArray UserInfo = new JSONArray(jsonObject.getString("info"));
            JSONObject UserCredentials = UserInfo.getJSONObject(0);

            hideProgressDialog();
            SaveSettings saveSettings = new SaveSettings(getApplicationContext());
            saveSettings.SaveData(UserCredentials.getString("user_id"));
            finish(); //close this activity
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        hideProgressDialog();
    }


}
