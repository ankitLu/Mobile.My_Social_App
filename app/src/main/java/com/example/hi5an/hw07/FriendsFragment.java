package com.example.hi5an.hw07;

import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class FriendsFragment extends Fragment {

    GoogleSignInClient mGoogleSignInClient;
    TabLayout allTabs;

    public FriendsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setActionBar();
        setTabbedPanel();
        setCurrentTabFragment(0);

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setActionBar(){
        android.support.v7.widget.ActionMenuView toolBar = (android.support.v7.widget.ActionMenuView)getActivity().findViewById(R.id.toolBarFriendsMenuView);
        Menu toolBarMenu = toolBar.getMenu();
        getActivity().getMenuInflater().inflate(R.menu.tool_bar,toolBarMenu);
        toolBarMenu.getItem(0).setTitle(R.string.friends);
        toolBarMenu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return false;
            }
        });
        toolBarMenu.getItem(2).setIcon(R.drawable.home);
        toolBarMenu.getItem(2).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainActivityContainer,homeScreenFragment,"homeScreen")
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        /*getActivity().getMenuInflater().inflate(R.menu.tool_bar,menu);
        ActionMenuView toolBar = (ActionMenuView)getActivity().findViewById(R.id.toolbar);
        Menu toolBarMenu = toolBar.getMenu();*/
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        try{
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if(account!=null){
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(getActivity(),gso);
                mGoogleSignInClient.revokeAccess();
            } else if (firebaseAuth.getCurrentUser()!=null) {
                firebaseAuth.signOut();
            } LoginFragment loginFragment = new LoginFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainActivityContainer,loginFragment,"loginFragment")
                    .addToBackStack(null)
                    .commit();

        }catch (Exception e) {

        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.add(0,0,0,"Logout").setIcon(R.drawable.logout)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuInflater.inflate(R.menu.menu_dummy,menu);
    }

    public void setTabbedPanel(){
        allTabs = (TabLayout) getActivity().findViewById(R.id.tabs);
        allTabs.addTab(allTabs.newTab().setText("Friends"),true);
        allTabs.addTab(allTabs.newTab().setText("Add New Friend"));
        allTabs.addTab(allTabs.newTab().setText("Requests Pending"));
        allTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setCurrentTabFragment(int tabPosition)
    {
        switch (tabPosition)
        {
            case 0 : ShowFriendsFragment showFriendsFragment = new ShowFriendsFragment();
                replaceFragment(showFriendsFragment);
                break;
            case 1 : AddFriendFragment addFriendFragment = new AddFriendFragment();
                replaceFragment(addFriendFragment);
                break;

            case 2: PendingRequestsFragment pendingRequestsFragment = new PendingRequestsFragment();
            replaceFragment(pendingRequestsFragment);
                break;
        }
    }
    public void replaceFragment(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.friendsScreenLinearLayout, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }




}
