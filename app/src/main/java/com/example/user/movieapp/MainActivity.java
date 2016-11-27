package com.example.user.movieapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnMovieSelectedListener{

    private boolean mTwoPane;
    private String DFTAG = DetailsActivityFragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        mTwoPane = findViewById(R.id.fragment_frame) != null;

        //initialize a preference holding the favourites if not yet created.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.contains(getString(R.string.pref_sort_fav))) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            Set<String> hs = new HashSet<>();
            edit.putStringSet(getString(R.string.pref_sort_fav), hs);
            edit.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //launching the settings activity
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelected(String response) {
        //this function gets called when a user clicks on an item,
        if (mTwoPane){
            //if in two pane mode, contact the fragment directly and replace the right frame with it.
            DetailsActivityFragment fragment = new DetailsActivityFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Intent.EXTRA_TEXT, response);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame,fragment).commit();
        }else{
            //if in one pane mode, open the detailsActivity which will contact the fragment.
            Intent intent = new Intent(this, DetailsActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, response);
            startActivity(intent);
        }
    }
}
