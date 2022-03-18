package com.example.ytstreamer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.chaquo.python.PyObject;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SongsList extends BaseAdapter {

    Context context;
    String title,uploader,url,thumb;
    List<PyObject> search_data;
    RequestBuilder<Drawable> listVgif;

    public  SongsList(Context context, List<PyObject> search_data) {
        this.search_data = search_data;
        this.context = context;
        listVgif = Glide.with(context).load(R.drawable.list);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        if(search_data!=null)
            return search_data.size();
        else
            return  19;
    }

    @Override
    public List<PyObject> getItem(int position) {
        if(search_data!=null)
            return search_data.get(position).asList();
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(search_data!=null) {
            List<PyObject> data = search_data.get(position).asList();

            title = data.get(1).toString();
            uploader = data.get(2).toString();
            url = data.get(0).toString();
            thumb = data.get(3).toString();

            if(convertView==null)
            {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.custom_list,null);
                TextView vTitle = convertView.findViewById(R.id.list_title);
                ImageView vThumb = convertView.findViewById(R.id.list_view_img);
                TextView vUploader = convertView.findViewById(R.id.list_uploader);

                listVgif.into(vThumb);
                Picasso.with(context).load(thumb).into(vThumb);

                vTitle.setText(title);
                vUploader.setText(uploader);
            }
            return convertView;
        }
        else {
            if(convertView==null)
            {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.custom_list,null);

                TextView vTitle = convertView.findViewById(R.id.list_title);
                ImageView vThumb = convertView.findViewById(R.id.list_view_img);
                TextView vUploader = convertView.findViewById(R.id.list_uploader);

                listVgif.into(vThumb);
                vTitle.setText("Loading title");
                vUploader.setText("Loading channel");
            }
            return convertView;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
