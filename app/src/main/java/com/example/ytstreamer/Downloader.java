package com.example.ytstreamer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


@SuppressLint("StaticFieldLeak")
public class Downloader extends AsyncTask<String,String,String> {

    String stream_url,song_name,path,yt_directory="/storage/emulated/0/Download/";
    int length,percent;
    long tot;
    boolean downloading = false,downloaded = false;
    MenuItem item;
    Activity main;

    public Downloader(String stream_url, String song_name,MenuItem item,Context context)
    {
        this.stream_url = stream_url;
        this.song_name = song_name + ".mp3";
        this.item = item;
        this.main = (Activity) context;
    }

    @Override
    protected String doInBackground(String... strings) {

        try
        {
            URL url = new URL(stream_url);
            URLConnection connection = url.openConnection();
            connection.connect();

            length = connection.getContentLength();

            if(length==0) {
                return null;
            }

            downloading = true;

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(path);

            byte[] data = new byte[1024];

            int count;

            while ((count = input.read(data))!=-1)
            {
                tot+=count;
                percent = (int) ((tot * 100/length));
                main.runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        item.setTitle(String.format("Downloading(%d)",percent));
                    }
                });

                output.write(data,0,count);
            }

            downloading = false;
            downloaded = true;

            output.flush();
            output.close();
            input.close();
        }

        catch (IOException ignored)
        {
            downloading = false;
            downloaded = false;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {

        if(downloaded) {
            downloading = false;
            Toast.makeText(main,"Download Completed",Toast.LENGTH_LONG).show();
        }

        else {
            Toast.makeText(main, "Can't Download Media", Toast.LENGTH_LONG).show();
        }

        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                item.setTitle("Download");
            }
        });
        super.onPostExecute(s);
    }

    @Override
    protected void onPreExecute() {

        path = yt_directory + song_name;

        if(new File(path).exists()) {
            Toast.makeText(main,"Filename already exists",Toast.LENGTH_SHORT).show();
            this.cancel(true);
        }

        else {
            Toast.makeText(main,"Download Started",Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }
    }

    @Override
    protected void onCancelled() {
        File dFile = new File(path);
        if(dFile.exists()) {
            dFile.delete();
        }
        super.cancel(true);
    }

    public boolean isDownloading()
    {
        return downloading;
    }
}
