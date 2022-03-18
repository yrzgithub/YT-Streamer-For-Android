package com.example.ytstreamer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.MenuItemCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.List;

public class ListViewActivity extends AppCompatActivity {

    SongsList sng_list_Adapter;
    ListView lst;
    List<PyObject> songs_list;
    PyObject main;
    Python py;
    String git = "https://github.com/yrzgithub/YT-Streamer-For-Android";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_list);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        SwipeRefreshLayout swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ListViewActivity.this.recreate();
            }
        });

        lst = findViewById(R.id.songs_list);

        py = Python.getInstance();
        main = py.getModule("main");

        sng_list_Adapter = new SongsList(ListViewActivity.this,null);
        lst.setAdapter(sng_list_Adapter);

        Intent intent = getIntent();
        String song = intent.getStringExtra("song");

        @SuppressLint("StaticFieldLeak")
        AsyncTask task = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                songs_list = main.callAttr("get_results",song).asList();
                sng_list_Adapter = new SongsList(ListViewActivity.this,songs_list);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {

                if(sng_list_Adapter.getViewTypeCount()<1) {
                    Toast.makeText(ListViewActivity.this,"No results found",Toast.LENGTH_SHORT).show();
                    Intent intent_main = new Intent(ListViewActivity.this,MainActivity.class);
                    intent_main.setAction(Intent.ACTION_SEND);
                    startActivity(intent_main);
                    finish();
                }

                else {
                    lst.setAdapter(sng_list_Adapter);
                    swipe.setRefreshing(false);
                    lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(ListViewActivity.this,MainActivity.class);
                            intent.putExtra("title",songs_list.get(position).asList().get(1).toString());
                            intent.putExtra("thumb_url",songs_list.get(position).asList().get(3).toString());
                            intent.putExtra("url",songs_list.get(position).asList().get(0).toString());
                            intent.putExtra("stream_url",songs_list.get(position).asList().get(2).toString());
                            intent.setAction("create_stream");
                            startActivity(intent);
                        }
                    });
                }
            }
        };

        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_activity,menu);

        MenuItem search = menu.findItem(R.id.search_sng);
        SearchView sView = (SearchView) MenuItemCompat.getActionView(search);

        sView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String song) {
                sView.clearFocus();
                sView.onActionViewCollapsed();
                Intent intent = new Intent(ListViewActivity.this,MainActivity.class);
                intent.putExtra("onSearch",song);
                intent.setAction("onSearch");
                startActivity(intent);
                finish();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch ((item.getItemId())) {

            case R.id.git:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(git));
                startActivity(intent);
                break;

            case R.id.contact:
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://api.whatsapp.com/send?phone=919047665729")));
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
