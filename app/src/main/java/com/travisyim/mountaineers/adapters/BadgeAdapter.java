package com.travisyim.mountaineers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.travisyim.mountaineers.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BadgeAdapter extends ArrayAdapter {
    protected Context mContext;
    protected List<String[]> mBadges;

    public BadgeAdapter(Context context, List<String[]> badges) {
        super(context, R.layout.badge_item, badges);

        mContext = context;
        mBadges = badges;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Check to see if view for item exists
        if (convertView == null) { // No
            convertView = LayoutInflater.from(mContext).inflate(R.layout.badge_item, null);
            holder = new ViewHolder();
            holder.imageViewBadge = (ImageView) convertView.findViewById(R.id.imageViewBadge);
            holder.textViewBadge = (TextView) convertView.findViewById(R.id.textViewBadge);
            convertView.setTag(holder);
        }
        else { // Yes
            holder = (ViewHolder) convertView.getTag();
        }

        // Populate icon image and sender name for specific item view
        String[] badge = mBadges.get(position);

        Picasso.with(mContext).load(badge[1]).into(holder.imageViewBadge);

        holder.textViewBadge.setText(badge[0]);

        return convertView;
    }

    private class ViewHolder {
        ImageView imageViewBadge;
        TextView textViewBadge;
    }
}
