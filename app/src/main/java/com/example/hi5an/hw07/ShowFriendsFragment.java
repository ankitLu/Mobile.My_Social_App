package com.example.hi5an.hw07;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;


public class ShowFriendsFragment extends Fragment {


    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    User user;
    GoogleSignInClient mGoogleSignInClient;

    public ShowFriendsFragment() {
        // Required empty public constructor
    }


    public static ShowFriendsFragment newInstance(String param1, String param2) {
        ShowFriendsFragment fragment = new ShowFriendsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = ((MainActivity)getActivity()).validateSession();
        if (user==null){
            LoginFragment loginFragment = new LoginFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainActivityContainer, loginFragment, "loginFragment")
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_friends, container, false);
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        readDatabase();
    }


    public void readDatabase() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final ArrayList<User> userArrayList = new ArrayList<User>();
        final ArrayList<String> friendIdList = new ArrayList<String>();
        databaseReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        userArrayList.clear();
                        friendIdList.clear();
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        boolean isFound = false;
                        for (DataSnapshot child : children) {
                            // check out who is signed in user
                            if (isFound)
                                break;
                            String id = child.getKey();
                            if (id.equals(user.getUserKey())){
                                isFound = true;
                                Iterable<DataSnapshot> friendIds = child.child(MainActivity.FRIEND_LIST).getChildren();
                                for (DataSnapshot eachFriendID : friendIds){
                                    String eachFriendIDKey = eachFriendID.getKey();
                                    friendIdList.add(eachFriendIDKey);
                                }
                            }

                        }
                        children = dataSnapshot.getChildren();
                        for (DataSnapshot child : children) {
                            for(String friendId : friendIdList){
                                if (child.getKey().equals(friendId)){
                                    User user = child.child(MainActivity.USER_DETAILS).getValue(User.class);
                                    userArrayList.add(user);
                                }
                            }
                        }

                       loadShowFriendsRecyclerView(userArrayList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }


    public void loadShowFriendsRecyclerView(ArrayList<User> userArrayList) {
        if(!isAdded())
            return;
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.showFriendsRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter
        mAdapter = new FriendsAdapter(userArrayList, MainActivity.FRIEND_LIST, user);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.

            //updateUI(null);
        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.add(0,0,0,"Logout").setIcon(R.drawable.logout)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuInflater.inflate(R.menu.menu_dummy,menu);

    }
}
