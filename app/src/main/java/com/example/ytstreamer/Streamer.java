package com.example.ytstreamer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.chaquo.python.PyObject;

import java.io.IOException;
import java.util.List;

public class Streamer {

    MediaPlayer player;
    int duration;
    Downloader downloader;
    String stream_url,yt_url;
    String song_name;
    String title;
    Context context;
    boolean streamCreated=false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Streamer(PyObject main, String yt_url,String title,MediaPlayer player,String song_name) {
        this.player = player;
        this.title = title;
        this.yt_url = yt_url;
        this.song_name = song_name;

        List<PyObject> pafy_data = main.callAttr("get_vid_data",yt_url).asList();

        if(pafy_data.size()==0)
        {
            streamCreated = false;
        }

        else
        {
            stream_url = pafy_data.get(1).toString();
            duration = pafy_data.get(2).toInt();

            try {
                player.setDataSource(stream_url);
                player.prepare();
                streamCreated = true;
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        player.start();
    }

    public void pause() {
        player.pause();
    }

    public  boolean isPlaying() {
        if(player!=null)
            return player.isPlaying();
        else
            return false;
    }

    public void stop() {
        player.reset();
        player.stop();
        player.release();
        player = null;
    }

    public Intent browse(String url)
    {
        Intent browser = new Intent(Intent.ACTION_VIEW);
        browser.setData(Uri.parse(url));
        return browser;
    }

    public Intent browseYtUrl() {
        if(this.isStreamCreated())
            return browse(yt_url);
        else
            return null;
    }

    public void forward()
    {
        int currentPos = player.getCurrentPosition();
        int delPosition = currentPos + 5000;
        player.seekTo(delPosition);
    }

    public void backward()
    {
        int currentPos = player.getCurrentPosition();
        int delPosition = currentPos - 5000;
        player.seekTo(delPosition);
    }

    public void setPosition(int pos)
    {
        pos*=1000;
        player.seekTo(pos);
    }

    public int getDuration()
    {
        return duration;
    }

    public int getCurrentPosition()
    {
        if(player!=null)
            return player.getCurrentPosition();
        else
            return 0;
    }

    public void download(MenuItem item,String stream_url,String name,Context context)
    {
        if(downloader==null || !downloader.isDownloading()) {
            downloader = new Downloader(stream_url, name, item, context);
            downloader.execute();
            this.context = context;
        }
    }

    public void cancel_download() {
        if(downloader!=null) {
            Toast.makeText(context,"Download cancelled",Toast.LENGTH_LONG).show();
            downloader.onCancelled();
        }
    }

    public boolean isDownloading() {
        if(downloader!=null) return downloader.isDownloading();
        else return false;
    }

    public boolean isStreamCreated()
    {
        return streamCreated;
    }

    public String getStreamUrl() {
        return stream_url;
    }

    public void reset() {
        player.reset();
    }
}
