package com.travisyim.mountaineers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.travisyim.mountaineers.R;

import java.util.List;

public class DrawerAdapter extends ArrayAdapter<String[]> {
    private Context mContext;
    private List<String[]> mDrawerItems;  // Contains drawer items attached to the adapter

    public DrawerAdapter(Context context, List<String[]> drawerItems) {
        super(context, R.layout.activity_item, drawerItems);

        mContext = context;
        mDrawerItems = drawerItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String[] drawerItem;

        // Check to see if view for item exists
        if (convertView == null) { // No
            convertView = LayoutInflater.from(mContext).inflate(R.layout.drawer_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.textViewName = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(holder);
        }
        else { // Yes
            holder = (ViewHolder) convertView.getTag();
        }

        drawerItem = mDrawerItems.get(position);

        holder.textViewName.setText(drawerItem[0]);  // Drawer item title
        holder.imageView.setImageResource(Integer.parseInt(drawerItem[1]));  // Image

        return convertView;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView textViewName;
    }
}