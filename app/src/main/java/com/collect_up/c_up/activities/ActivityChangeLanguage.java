/*
 * Created by Collect-up  on December 27, 2016.
 *
 * This is the source code of C-Up v1.0
 *
 * Copyright (c) 2016 C-Up. All rights reserved.
 */

package com.collect_up.c_up.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.collect_up.c_up.R;
import com.collect_up.c_up.adapters.LanguageListAdapter;
import com.collect_up.c_up.helpers.Files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityChangeLanguage extends AppCompatActivity {

    private LanguageListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_toolbar);
        }

        ListView listView = (ListView) findViewById(R.id.list_view_languages);

        List<String> languages = getLanguages();

        ArrayList<String> alreadySelectedLanguages = getIntent().getStringArrayListExtra("languages");

        mAdapter = new LanguageListAdapter(this, languages, alreadySelectedLanguages == null ? new ArrayList<String>() : alreadySelectedLanguages);

        listView.setAdapter(mAdapter);
    }

    /**
     * Get Countries which are read from file as List
     *
     * @return Countries as List
     */
    private List<String> getLanguages() {
        List<String> output = new ArrayList<>();
        String fileContent = null;
        try {
            fileContent = Files.readFileContentFromAssets(this, "languages.txt");
        } catch (IOException ignored) {

        }
        assert fileContent != null;
        String[] languages = fileContent.split("/");

        output.addAll(Arrays.asList(languages));

        return output;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_change_language, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_done:
                Intent intent = new Intent();
                intent.putExtra("languages", mAdapter.selectedLanguages);

                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return false;
    }
}
