package com.example.hi5an.hw07;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;


public class ProfileFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    User user;
    DatabaseReference firebaseDatabase;
    String username, firstName, lastName, dateOfBirth;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();

        return fragment;
    }





    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((TextView)getActivity().findViewById(R.id.textViewUsername)).setText(user.getUsername());
         ((EditText) getActivity().findViewById(R.id.editTextFirstName)).setText(user.getFirstName());
        ((EditText) getActivity().findViewById(R.id.editTextLastName)).setText(user.getLastName());
        ((EditText) getActivity().findViewById(R.id.editTextDoB)).setText(user.getDateOfBirth());
        getActivity().findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFields()){
                    return;
                }
                final DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
                firebaseDatabase.child(user.getUserKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child(MainActivity.USER_DETAILS).getValue(User.class);
                        String firstName = ((EditText) getActivity().findViewById(R.id.editTextFirstName)).getText().toString().trim();
                        user.setFirstName(firstName);
                        String lastName = ((EditText) getActivity().findViewById(R.id.editTextLastName)).getText().toString().trim();
                        user.setLastName(lastName);
                        String dob = ((EditText) getActivity().findViewById(R.id.editTextDoB)).getText().toString().trim();
                        user.setDateOfBirth(dob);
                        firebaseDatabase.child(user.getUserKey()).child(MainActivity.USER_DETAILS).setValue(user);
                        Toast.makeText(getActivity(), "Info updated", Toast.LENGTH_SHORT).show();
                        HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
                        getActivity().getSupportFragmentManager().beginTransaction().
                                replace(R.id.mainActivityContainer,homeScreenFragment,"homeScreen")
                                .addToBackStack(null)
                                .commit();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private boolean validateFields(){
        firstName = ((EditText)getActivity().findViewById(R.id.editTextFirstName)).getText().toString().trim();
        lastName = ((EditText)getActivity().findViewById(R.id.editTextLastName)).getText().toString().trim();
        dateOfBirth = ((EditText)getActivity().findViewById(R.id.editTextDoB)).getText().toString().trim();
        if(firstName.equals("") || lastName.equals("") || dateOfBirth.equals("") ){
            Toast.makeText(getActivity(), "Please complete missing fields!", Toast.LENGTH_SHORT).show();
            return false;
        } if(!isDateValid(dateOfBirth)){
            Toast.makeText(getActivity(), "Check date format. Also you should be 13 or older.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }




    private boolean isDateValid(String dateOfBirth)
    {
        try {
            String month = dateOfBirth.substring(0,2);
            int monthDigit = Integer.parseInt(month);
            if (monthDigit<1 || monthDigit>12)
                return false;
            String slash = dateOfBirth.substring(2,3);
            if(!slash.equals("/"))
                return false;
            String date = dateOfBirth.substring(3,5);
            int dateDigit = Integer.parseInt(date);
            if (dateDigit<1 || dateDigit>31)
                return false;
            slash = dateOfBirth.substring(5,6);
            if(!slash.equals("/"))
                return false;
            String year = dateOfBirth.substring(6,dateOfBirth.length());
            int yearDigit = Integer.parseInt(year);
            if (yearDigit>2004 || yearDigit<1940)
                return false;
            //df.setLenient(false);
            //df.parse(month + "-" + date + "-" + year);
            return true;
        } catch (NumberFormatException e) {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

}
