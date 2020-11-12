package com.example.sensair;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.sensair.ui.historyFragments.InfoFragment;
import com.mukesh.MarkdownView;

public class InfoActivity extends AppCompatActivity {

    public MarkdownView mdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mdView = new MarkdownView(this);
        mdView.findViewById(R.id.mdInfo);
        setContentView(mdView);
        mdView.loadMarkdownFromAssets("INFO.md");

        getSupportActionBar().setDisplayShowHomeEnabled(true);  // shows back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // enables back button

    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}