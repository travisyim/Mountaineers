package com.travisyim.mountaineers.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.travisyim.mountaineers.MountaineersApp;
import com.travisyim.mountaineers.R;
import com.travisyim.mountaineers.adapters.BadgeAdapter;
import com.travisyim.mountaineers.objects.Mountaineer;
import com.travisyim.mountaineers.utils.PicassoCustom;

import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment {
    private Fragment mProfileEditFragment;
    private Mountaineer mMember;
    private GridView mGridView;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_MEMBER = "member";

    // Returns a new instance of this fragment for the given section number
    public static UserProfileFragment newInstance(int sectionNumber) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public UserProfileFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));

        // Get Mountaineer member from bundle
        mMember = (Mountaineer) getArguments().getSerializable(ARG_MEMBER);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ImageView imageView;
        TextView textViewName;
        TextView textViewRegistrationDate;
        TextView textViewBranch;

        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        imageView = (ImageView) rootView.findViewById(R.id.imageViewProfile);
        textViewName = (TextView) rootView.findViewById(R.id.textViewName);
        textViewRegistrationDate = (TextView) rootView.findViewById(R.id.textViewRegistrationDate);
        textViewBranch = (TextView) rootView.findViewById(R.id.textViewBranch);
        mGridView = (GridView) rootView.findViewById(R.id.badgeGrid);

        // Load profile image
        Picasso.with(getActivity()).load(mMember.getProfileImageUrl())
                .error(R.drawable.default_avatar)
                .transform(new PicassoCustom.CropCircleTransformation()).into(imageView);

        // Load user profile data
        textViewName.setText(mMember.getName());  // Name
        // Registration Date
        textViewRegistrationDate.setText
                (getActivity().getString(R.string.member_since) + mMember.getRegDate());
        // Branch
        textViewBranch.setText(getActivity().getString(R.string.branch) + mMember.getBranch());

        // Load badges - Since badges rarely ever get added do not allow updates
        // Create the List of String[] to hold the data
        List<String[]> badgeList = new ArrayList<String[]>();

        for (int i = 0; i < mMember.getBadgeInfo()[0].length; i++) {
            String[] badgeInfo = new String[2];
            badgeInfo[0] = mMember.getBadgeInfo()[0][i];
            badgeInfo[1] = mMember.getBadgeInfo()[1][i];

            badgeList.add(badgeInfo);
        }

        // Pass the list to the adapter
        BadgeAdapter adapter = new BadgeAdapter(getActivity(), badgeList);
        mGridView.setAdapter(adapter);

        // TODO: Is there any need to show anything if there are no badges?  Show Badges header and setup empty text view!
        mGridView.setEmptyView(getActivity().findViewById(R.id.textViewEmpty));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.user_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_editProfile:  // Edit Profile
                if (!((MainActivity) getActivity()).isDrawerOpen()) {
                    // Google Analytics tracking code - Edit user profile
                    Tracker t = ((MountaineersApp) getActivity().getApplication()).getTracker
                            (MountaineersApp.TrackerName.APP_TRACKER);
                    t.setScreenName("Edit Profile");
                    t.send(new HitBuilders.AppViewBuilder().build());

                    // Launch Profile Edit fragment to show user profile's edit webpage
                    if (mProfileEditFragment == null) {
                        // TODO: Fix up section numbering scheme
                        mProfileEditFragment = ProfileEditFragment.newInstance
                                ((float) (this.getArguments().getInt(ARG_SECTION_NUMBER) + 0.1),
                                        getActivity().getActionBar().getTitle().toString(),
                                        mMember.getMemberUrl() + "/edit");
                    }

                    // Update ActionBar title to show name
                    getActivity().getActionBar().setTitle(getString(R.string.title_profile_edit));

                    // Load activity details fragment
                    getFragmentManager().beginTransaction().replace
                            (R.id.container, mProfileEditFragment).addToBackStack(null).commit();
                }

                return true;
            case R.id.action_logOut:  // Log Out
                ParseUser.getCurrentUser().logOut();
                ((MainActivity) getActivity()).showLoginScreen();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}