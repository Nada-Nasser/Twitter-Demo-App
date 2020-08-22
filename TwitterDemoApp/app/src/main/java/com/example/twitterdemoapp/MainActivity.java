package com.example.twitterdemoapp;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //adapter class
    ArrayList<AdapterItems>    listnewsData = new ArrayList<AdapterItems>();
    int StartFrom=0;
    int UserOperation=SearchType.MyFollowing; // 0 my followers post 2- specifc user post 3- search post
    String Searchquery;
    int totalItemCountVisible=0; //totalItems visible
    LinearLayout ChannelInfo;
    TextView txtnamefollowers;
    int SelectedUserID=0;
    Button buFollow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         ChannelInfo=(LinearLayout)findViewById(R.id.ChannelInfo) ;
        ChannelInfo.setVisibility(View.GONE);
        txtnamefollowers=(TextView)findViewById(R.id.txtnamefollowers) ;
          buFollow=(Button)findViewById(R.id.buFollow);
        //TODO: load user data setting
        //SaveSettings saveSettings= new SaveSettings(getApplicationContext());
       // saveSettings.LoadData();
        //TODO: set the adapter

        ListView lsNews=(ListView)findViewById(R.id.LVNews);
       // lsNews.setAdapter(myadapter);//intisal with data


    }

    public void buFollowers(View view) {
//TODO: add code s=for subscribe and un subscribe


    }


  SearchView searchView;
    Menu myMenu;
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







}
