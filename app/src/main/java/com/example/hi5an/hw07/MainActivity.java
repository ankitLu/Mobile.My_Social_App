package com.example.hi5an.hw07;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static final String ADD_FRIEND = "addFriends";
    static final String FRIEND_LIST = "showFriends";
    static final String PENDING_REQ = "pendingRequests";
    static final String USER_DETAILS = "UserDetails";
    static final String SENT_REQ_DETAILS = "sentRequests";
    static final String POST_DETAILS = "postDetails";
    static final String USER_KEY = "USER_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar menu = getSupportActionBar();
        //menu.setDisplayShowHomeEnabled(true);
        menu.setIcon(getResources().getDrawable(R.drawable.app_icon));
        menu.setTitle(R.string.my_social_app);
        LoginFragment loginFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainActivityContainer, loginFragment, "loginFragment")
                .addToBackStack(null)
                .commit();
    }


    public User validateSession() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            // get user details from firebase
            User user = getUserFromFireBase(firebaseAuth.getCurrentUser().getUid());
            if (user==null || user.getUsername() == null || user.getUsername().equals("")){
                user.setUsername(firebaseAuth.getCurrentUser().getEmail());
                user.setUserKey(firebaseAuth.getCurrentUser().getUid());
                user.setFirstName(firebaseAuth.getCurrentUser().getDisplayName());
                user.setLastName("");
                user.setUserKey(firebaseAuth.getCurrentUser().getUid());
            }
            return user;
        } else if (account != null) {
            User user = new User();
            user.setUsername(account.getEmail());
            user.setUserKey(account.getId());
            user.setFirstName(account.getGivenName());
            user.setLastName(account.getFamilyName());
            user.setStatus("google");
            user.setDateOfBirth("");
            return user;
        } else
        Toast.makeText(this, "Invalid session, please login!", Toast.LENGTH_SHORT).show();
        return null;
    }


    public void checkNetwork() {
        final ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnectedOrConnecting()) {
            //Toast.makeText(this, "Wifi", Toast.LENGTH_LONG).show();
        } else if (mobile.isConnectedOrConnecting()) {
            //Toast.makeText(this, "Mobile 3G ", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Check your Network ", Toast.LENGTH_LONG).show();
        }
    }


    public void clearBackStack() {
       /** FragmentManager fm = MainActivity.this.getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        } **/
    }

    public User getUserFromFireBase(final String key) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final User user = new User();
        databaseReference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        User user1 = dataSnapshot.child(key)
                                .child(MainActivity.USER_DETAILS)
                                .getValue(User.class);
                        if (user1 == null)
                            return;
                        user.setLastName(user1.getLastName());
                        user.setFirstName(user1.getFirstName());
                        user.setUserKey(user1.getUserKey());
                        user.setDateOfBirth(user1.getDateOfBirth());
                        user.setUsername(user1.getUsername());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        return user;
    }


}
