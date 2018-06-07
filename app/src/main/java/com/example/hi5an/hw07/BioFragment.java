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
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;


public class BioFragment extends Fragment {

    User user;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    String userKeyforPosts;
    public static final String WALL_SCREEN = "wallScreen";
    GoogleSignInClient mGoogleSignInClient;


    private OnFragmentInteractionListener mListener;

    public BioFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        user = ((MainActivity)getActivity()).validateSession();
        if (user==null){
            LoginFragment loginFragment = new LoginFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainActivityContainer, loginFragment, "loginFragment")
                    .addToBackStack(null)
                    .commit();
        }
        userKeyforPosts = getArguments().getString(MainActivity.USER_KEY);
        if(userKeyforPosts==null || userKeyforPosts.trim().equals("")){
            HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
            Toast.makeText(getActivity(), "Unable to load posts, Contact developer", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.mainActivityContainer,homeScreenFragment,"homeScreen")
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bio, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        android.support.v7.widget.ActionMenuView toolBar = (android.support.v7.widget.ActionMenuView)getActivity().findViewById(R.id.toolBarMenuView);
        Menu toolBarMenu = toolBar.getMenu();
        getActivity().getMenuInflater().inflate(R.menu.tool_bar,toolBarMenu);
        String name = user.getFirstName() + " " + user.getLastName();
        if (name==null || name.trim().equals(""))
            name = user.getUsername();
        toolBarMenu.getItem(0).setTitle(name);
        toolBarMenu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return false;
            }
        });


        if (user.getUserKey().equals(user.getUserKey())){
            toolBarMenu.getItem(1).setVisible(true);
            toolBarMenu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ProfileFragment profileFragment = new ProfileFragment();
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.mainActivityContainer,profileFragment,"bioFragment")
                            .commit();
                    return true;
                }
            });
        }

        toolBarMenu.getItem(2).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                FriendsFragment friendsFragment = new FriendsFragment();
                Bundle args = new Bundle();
                args.putSerializable(getString(R.string.user_key),user);
                friendsFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainActivityContainer,friendsFragment,"friendsScreen")
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });





        /**
         * Getting posts from DB
         */
        readDatabase();

    }



    public void readDatabase() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final ArrayList<Post> postArrayList = new ArrayList<Post>();
        databaseReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // get my own posts
                        Iterable<DataSnapshot> myOwnPosts = dataSnapshot.child(userKeyforPosts)
                                .child(MainActivity.POST_DETAILS).getChildren();
                        for (DataSnapshot dataSnapshot1: myOwnPosts){
                            Post aPost = dataSnapshot1.getValue(Post.class);
                            if (aPost!=null)
                                postArrayList.add(aPost);
                        }


                        Collections.sort(postArrayList, new Comparator<Post>() {

                            @Override
                            public int compare(Post p1, Post p2) {
                                try {
                                    Date date1 = new Date(p1.getPostTime());
                                    Date date2 = new Date(p2.getPostTime());
                                    return date1.compareTo(date2);
                                } catch (Exception e) {
                                    return 0;
                                }
                            }
                        });
                        Collections.reverse(postArrayList);

                        loadPostsRecyclerView(postArrayList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }


    public void loadPostsRecyclerView(ArrayList<Post> postArrayList) {
        if(!isAdded())
            return;
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.showPostsRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter
        mAdapter = new PostsAdapter(postArrayList, user, WALL_SCREEN);
        mRecyclerView.setAdapter(mAdapter);
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
