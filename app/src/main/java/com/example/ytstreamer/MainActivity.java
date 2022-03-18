package com.example.ytstreamer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.MenuItemCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    PyObject main;
    Python py;
    ImageView thumb_btn;
    TextView currentTime,duration,vid_title;
    ImageButton playRpause,forward,backward;
    Streamer current_streamer;
    Drawable img;
    SeekBar seek;
    String git = "https://github.com/yrzgithub/YT-Streamer-For-Android",current_song="Search any Song Or Video",sng;
    Intent intent;
    SearchView sview;
    Thread creator;
    Intent loading_intent;
    BroadcastReceiver broadCast;
    IntentFilter filter;
    MediaPlayer player;
    boolean stream_obj_created = false,backpressed=false,loading=false;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"SetTextI18n", "ResourceAsColor", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        player = new MediaPlayer();
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        player.setLooping(true);

        filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_MANAGE_NETWORK_USAGE);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        py = Python.getInstance();
        main = py.getModule("main");

        thumb_btn = findViewById(R.id.thumb);

        vid_title = findViewById(R.id.video_title);
        vid_title.setSelected(true);

        currentTime = findViewById(R.id.currentTime);
        duration = findViewById(R.id.duration);

        forward = findViewById(R.id.forward);
        playRpause = findViewById(R.id.pauseRplay);
        backward = findViewById(R.id.backward);

        seek = findViewById(R.id.seek);

        forward.setOnClickListener(this);
        playRpause.setOnClickListener(this);
        backward.setOnClickListener(this);

        intent = getIntent();
        String action = intent.getAction();

        if(action.equals("create_stream") && !stream_obj_created) {
            create_stream(intent.getStringExtra("url"));
        }

        else if(action.equals("onSearch") && !stream_obj_created) {
            String song = intent.getStringExtra("onSearch");
            onSearch(song);
        }

        else {
            showGif(R.drawable.yt);
        }

        broadCast = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()) {

                    case Intent.ACTION_HEADSET_PLUG:
                        if(intent.getIntExtra("state",-1)==0 && current_streamer!=null) {
                            change_img(R.drawable.play);
                            current_streamer.pause();
                        }
                        break;

                    case "android.net.conn.CONNECTIVITY_CHANGE":

                        if(!isConnected()) {
                            vid_title.setText("Please Connect To Internet");
                            vid_title.setTextColor(Color.RED);
                            showGif(R.drawable.no_internet);
                        }
                        else {
                            vid_title.setText(current_song);
                            vid_title.setTextColor(Color.BLACK);
                            if(loading)
                            {
                                showGif(R.drawable.loading);
                            }

                            else if(img==null) {
                                showGif(R.drawable.yt);
                            }

                            else {
                                thumb_btn.setImageDrawable(img);
                            }
                        }
                        break;
                }
            }
        };
        this.registerReceiver(broadCast,filter);
    }

    public void showGif(int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(MainActivity.this).load(id).into(thumb_btn);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        MenuItem search = menu.findItem(R.id.search);

        if(search!=null) {
            sview = (SearchView) MenuItemCompat.getActionView(search);
            sview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public boolean onQueryTextSubmit(String song) {
                    sview.clearFocus();
                    sview.onActionViewCollapsed();
                    sng = song;
                    if(!loading)
                        onSearch(song);
                    else
                        Toast.makeText(MainActivity.this,"Not available while loading",Toast.LENGTH_LONG).show();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("SetTextI18n")
    public void onSearch(String song) {
        if(!isConnected()) {
            vid_title.setText("Please Connect To Internet");
            vid_title.setTextColor(Color.RED);
            showGif(R.drawable.no_internet);
        }

        else if(current_streamer==null) {
            load_song(song);
        }

        else{
            if(current_streamer.isDownloading())
                Toast.makeText(MainActivity.this,"Downloading media...Please wait",Toast.LENGTH_LONG).show();
            else
                load_song(song);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId() ) {

            case R.id.git:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(git));
                startActivity(intent);
                break;

            case R.id.contact:
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://api.whatsapp.com/send?phone=919047665729")));
                break;

            case R.id.download:
                if(current_streamer!=null)
                {
                    String stream_url = current_streamer.getStreamUrl();
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle("Download Manager");
                    alert.setMessage("Enter media title");
                    alert.setIcon(R.mipmap.ic_launcher_foreground);
                    EditText text = new EditText(MainActivity.this);
                    text.setHint("Enter the file name");
                    text.setMaxLines(2);
                    text.setText(current_song.replaceAll(" [^a-zA-Z0-9]","").replaceAll(" {2}"," "));
                    alert.setView(text);
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            download(text.getText().toString(),item,stream_url);
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alert.show();
                }

                else {
                    Toast.makeText(MainActivity.this,"Media not loaded",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.openYt:
                if(current_streamer!=null) {
                    current_streamer.pause();
                    change_img(R.drawable.play);
                    Intent intent = current_streamer.browseYtUrl();
                    if(intent!=null) {
                        startActivity(intent);
                    }
                }

                else {
                    Toast.makeText(MainActivity.this,"Media not loaded",Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void create_stream(String url) {

        intent = getIntent();
        current_song = intent.getStringExtra("title");

        if(isConnected()) {

            vid_title.setText(current_song);

            loading = true;
            showGif(R.drawable.loading);

            creator = new Thread(new Runnable() {
                @Override
                public void run() {

                    current_streamer = new Streamer(main,url,current_song,player,sng);
                    load_img_from_web(intent.getStringExtra("thumb_url"));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            duration.setText(FormatTime(current_streamer.getDuration()));

                            Handler seekHandler = new Handler();
                            Runnable seekUpdateRunnable = new Runnable() {

                                int pos;

                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void run() {
                                    if(current_streamer!=null)
                                    {
                                        pos = current_streamer.getCurrentPosition();
                                        pos/=1000;
                                        Log.d("my tag",String.valueOf(pos));
                                        seek.setProgress(pos,true);
                                        currentTime.setText(FormatTime(pos));
                                    }
                                    seekHandler.postDelayed(this,1000);
                                }
                            };
                            seekUpdateRunnable.run();

                            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                                    if(current_streamer!=null)
                                    {
                                        if(fromUser) {
                                            current_streamer.setPosition(i);
                                        }
                                    }
                                    else
                                    {
                                        seek.setProgress(0);
                                    }
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {

                                }
                            });
                            seek.setMax(current_streamer.getDuration());
                            change_img(R.drawable.pause);

                            /*   current_streamer.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    current_streamer.pause();
                                    current_streamer.setPosition(0);
                                    change_img(R.drawable.play);
                                }
                            }); */

                            if(!backpressed) {
                                current_streamer.start();
                                stream_obj_created = true;
                            }
                            else
                            {
                                current_streamer.stop();
                            }
                        }
                    });
                }
            });
            creator.start();

        }
        else {
            Toast.makeText(MainActivity.this,"Stream Not Created",Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void change_img(int draw)
    {
        playRpause.setImageResource(draw);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void load_img_from_web(String url)
    {
        loading = false;
        try
        {
            InputStream is = (InputStream) new URL(url).getContent();
            img =  Drawable.createFromStream(is,"thumbnail");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    thumb_btn.setImageDrawable(img);
                }
            });
        }
        catch (IOException e)
        {
           showGif(R.drawable.yt);
        }
    }

    public String FormatTime(int totSec)
    {
        String min,sec;
        min = String.valueOf(totSec /60);
        sec = String.valueOf(totSec % 60);
        if(sec.length()==1) sec = "0" + sec;
        return String.format("%s.%s",min,sec);
    }

    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    @Override
    public void onClick(View view) {

        if(current_streamer==null)
            return;

        switch (view.getId()) {

            case R.id.pauseRplay:
                if(current_streamer.isPlaying())
                {
                    current_streamer.pause();
                    change_img(R.drawable.play);
                }
                else
                {
                    current_streamer.start();
                    change_img(R.drawable.pause);
                }
                break;

            case R.id.forward:
                current_streamer.forward();
                break;

            case R.id.backward:
                current_streamer.backward();
                break;
        }
    }

    public boolean isConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo =  connectivityManager.getActiveNetworkInfo();
        if(current_streamer!=null && !current_streamer.isStreamCreated()) {
            current_streamer = null;
        }
        return netInfo!=null && netInfo.isConnected();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBackPressed() {

        backpressed = true;

        if(current_streamer!=null)  {
            if(current_streamer.isDownloading()) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setIcon(R.mipmap.ic_launcher_foreground);
                dialog.setTitle("Download Manager");
                dialog.setMessage("Do you want to cancel the download?");
                dialog.setNegativeButton("wait", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        current_streamer.stop();
                        current_streamer.cancel_download();
                        MainActivity.super.onBackPressed();
                    }
                });
                dialog.show();
            }
            else {
                current_streamer.stop();
                MainActivity.super.onBackPressed();
            }
        }

        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        unregisterReceiver(broadCast);

        if(current_streamer!=null) {
            if(current_streamer.isDownloading()) {
                current_streamer.cancel_download();
            }
            player.release();
        }
        super.onDestroy();
    }

    public void download(String name,MenuItem item,String stream_url) {
        if(!name.equals(""))
            current_streamer.download(item,stream_url,name,MainActivity.this);
        else
            Toast.makeText(MainActivity.this,"Invalid file name",Toast.LENGTH_LONG).show();
    }

    public void load_song(String song) {
        if(current_streamer!=null) {
            current_streamer.stop();
        }
        vid_title.setText(String.format("Searching \"%s\"",song));
        new Thread(new Runnable() {
            @Override
            public void run() {
                loading_intent = new Intent(MainActivity.this,ListViewActivity.class);
                loading_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                loading_intent.putExtra("song",song);
                startActivity(loading_intent);
                finish();
            }
        }).start();
    }

}