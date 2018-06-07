package com.example.hi5an.hw07;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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


public class AddFriendFragment extends Fragment {

    User user;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private OnFragmentInteractionListener mListener;
    GoogleSignInClient mGoogleSignInClient;

    public AddFriendFragment() {
        // Required empty public constructor
    }


    public static AddFriendFragment newInstance(String param1, String param2) {
        AddFriendFragment fragment = new AddFriendFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = ((MainActivity)getActivity()).validateSession();
        if (user==null){
            ((MainActivity)getActivity()).clearBackStack();
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
        return inflater.inflate(R.layout.fragment_add_friend, container, false);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        readDatabase();
    }

    public void readDatabase() {
        final ArrayList<User> userArrayList = new ArrayList<User>();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        userArrayList.clear();
                        ArrayList<String> pendingReqList = new ArrayList<String>();
                        Iterable<DataSnapshot> myPendingRequests = dataSnapshot.child(user.getUserKey())
                                .child(MainActivity.PENDING_REQ).getChildren();
                        for (DataSnapshot dataSnapshot1: myPendingRequests){
                            String key = dataSnapshot1.getKey();
                            pendingReqList.add(key);
                        }
                        ArrayList<String> sentReqList = new ArrayList<String>();
                        Iterable<DataSnapshot> mySentRequests = dataSnapshot.child(user.getUserKey())
                                .child(MainActivity.SENT_REQ_DETAILS).getChildren();
                        for (DataSnapshot dataSnapshot2: mySentRequests){
                            String key = dataSnapshot2.getKey();
                            sentReqList.add(key);
                        }
                        ArrayList<String> alreadyFriendList = new ArrayList<String>();
                        Iterable<DataSnapshot> myFriends = dataSnapshot.child(user.getUserKey())
                                .child(MainActivity.FRIEND_LIST).getChildren();
                        for (DataSnapshot dataSnapshot3: myFriends){
                            String key = dataSnapshot3.getKey();
                            alreadyFriendList.add(key);
                        }

                        Iterable<DataSnapshot> allUsers = dataSnapshot.getChildren();
                        // iterate all users
                        for (DataSnapshot eachUser : allUsers) {
                            // user should not be myself
                            if (!eachUser.getKey().equals(user.getUserKey())) {
                                String userKey = eachUser.getKey();
                                if (pendingReqList != null) {
                                    if (!pendingReqList.contains(userKey)) {
                                        if (alreadyFriendList != null) {
                                            if (!alreadyFriendList.contains(userKey)) {
                                                if (sentReqList != null) {
                                                    if (!sentReqList.contains(userKey)) {
                                                        User person = eachUser.child(MainActivity.USER_DETAILS)
                                                                .getValue(User.class);
                                                        if (person!=null)
                                                            userArrayList.add(person);
                                                    } else {
                                                        User person = eachUser.child(MainActivity.USER_DETAILS)
                                                                .getValue(User.class);
                                                        person.setStatus(MainActivity.PENDING_REQ);
                                                        if (person!=null)
                                                            userArrayList.add(person);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        loadShowFriendsRecyclerView(userArrayList);
                        //startFragmentAgain();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    public void loadShowFriendsRecyclerView(ArrayList<User> userArrayList) {
        if(!isAdded()){
            userArrayList.clear();
            return;
        }
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.showFriendsRecyclerView);
        if (mRecyclerView==null)
            return;
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter
        mAdapter = new FriendsAdapter(userArrayList, MainActivity.ADD_FRIEND,user);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
