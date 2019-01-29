package com.webmons.disono.rtmpandrtspstreamer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: https://webmons.com
 * 
 * Created at: 1/09/2018
 */

public class CommentListAdapter extends ArrayAdapter<Comments> {
    private ArrayList<Comments> itemComments;
    private Context mContext;
    private int mLayout;

    // View lookup cache
    private static class ViewHolder {
        ImageView imgCommentAvatar;
        TextView txtCommentUsername;
        TextView txtCommentContent;
    }

    public CommentListAdapter(Activity context, int layout, ArrayList<Comments> items) {
        super(context, layout, items);
        mLayout = layout;
        mContext = context;

        itemComments = items;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Comments comment = itemComments.get(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(mLayout, parent, false);

            viewHolder.imgCommentAvatar = convertView.findViewById(_getResource("imgCommentAvatar", "id"));
            viewHolder.txtCommentUsername = convertView.findViewById(_getResource("txtCommentUsername", "id"));
            viewHolder.txtCommentContent = convertView.findViewById(_getResource("txtCommentContent", "id"));

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        new DownloadImageTask(viewHolder.imgCommentAvatar).execute(comment.getImgCommentAvatar());
        viewHolder.txtCommentUsername.setText(comment.getTxtCommentUsername());
        viewHolder.txtCommentContent.setText(comment.getTxtCommentContent());

        return convertView;
    }

    private int _getResource(String name, String type) {
        String package_name = mContext.getApplicationContext().getPackageName();
        Resources resources = mContext.getApplicationContext().getResources();
        return resources.getIdentifier(name, type, package_name);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;

            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                // to rounded bitmap
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
