package com.travisyim.mountaineers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.objects.FragmentListItem;
import com.travisyim.mountaineers.utils.PicassoCustom;

public class DrawerAdapter extends BaseAdapter {
    private Context mContext;
    private Object[] mDrawerItems;  // Contains drawer items attached to the adapter

    private static final int ITEM_VIEW_TYPE_FRAGMENT = 0;
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 1;
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    public DrawerAdapter(Context context, Object[] drawerItems) {
        mContext = context;
        mDrawerItems = drawerItems;
    }

    @Override
    public int getCount() {
        return mDrawerItems.length;
    }

    @Override
    public Object getItem(int position) {
        return mDrawerItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return (mDrawerItems[position] instanceof String) ?
                ITEM_VIEW_TYPE_SEPARATOR : ITEM_VIEW_TYPE_FRAGMENT;
    }

    @Override
    public boolean isEnabled(int position) {
        // A separator cannot be clicked !
        return getItemViewType(position) != ITEM_VIEW_TYPE_SEPARATOR;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textViewUpdateCounter;
        ImageView imageViewIcon;
        final int type = getItemViewType(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    type == ITEM_VIEW_TYPE_SEPARATOR ?
                            R.layout.drawer_separator : R.layout.drawer_item, null);
        }

        // Fill the list item view with the appropriate data
        if (type == ITEM_VIEW_TYPE_SEPARATOR) {
            ((TextView) convertView.findViewById(R.id.textViewHeader))
                    .setText((String) getItem(position));
        } else {
            final FragmentListItem drawerItem = (FragmentListItem) getItem(position);
            textViewUpdateCounter = (TextView) convertView.findViewById(R.id.textViewUpdateCounter);
            imageViewIcon = (ImageView) convertView.findViewById(R.id.imageViewIcon);

            // Drawer item title
            ((TextView) convertView.findViewById(R.id.textViewTitle)).setText(drawerItem.getTitle());

            // Image - See if there is a profile image attached
            if (drawerItem.getProfileImage() == null) {  // No - show icon
                Picasso.with(mContext).load(drawerItem.getIcon()).into(imageViewIcon);

                // Save original image height (icon height x 2) in the imageView's tag
                imageViewIcon.setTag(imageViewIcon.getHeight() * 2);
            }
            else {  // Yes - show profile image
                Picasso.with(mContext).load(drawerItem.getProfileImage())
                        .transform(new PicassoCustom.CropCircleTransformation())
                        .resize(0, new Integer(imageViewIcon.getTag().toString()))
                        .into(imageViewIcon);
            }

            // Update counter (only applies to Saved Searches and potentially Favorites)
            if (drawerItem.getUpdateCount() > 0) {  // Only show if there are some updates
                textViewUpdateCounter.setVisibility(View.VISIBLE);

                // Check how many updates there are
                if (drawerItem.getUpdateCount() <= 9) {  // Max is 9 (no double digits)
                    textViewUpdateCounter.setText(Integer.toString(drawerItem.getUpdateCount()));
                }
                else {  // More than 9 updates so shorten the text
                    textViewUpdateCounter.setText("9+");
                }
            }
            else {
                // Hide the counter if not representing saved search or if the count is 0
                textViewUpdateCounter.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}