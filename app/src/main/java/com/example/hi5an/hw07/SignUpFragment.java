package com.example.hi5an.hw07;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.provider.Settings.System.DATE_FORMAT;


public class SignUpFragment extends Fragment {

DatabaseReference firebaseDatabase;
String username, firstName, lastName, dateOfBirth, password, confirmPass;


    private OnFragmentInteractionListener mListener;

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().findViewById(R.id.buttonSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseDatabase = FirebaseDatabase.getInstance().getReference();
                if (!validateFields()){
                    return;
                }
                firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if(user!=null){
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(firstName + " "+ lastName).build();
                            user.updateProfile(profileUpdates);
                        }
                    }
                });
                firebaseAuth.createUserWithEmailAndPassword (username,password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    String id = task.getResult().getUser().getUid();
                                    User user = new User();
                                    // set user object
                                    user.setFirstName(firstName);
                                    user.setLastName(lastName);
                                    user.setDateOfBirth(dateOfBirth);
                                    user.setUsername(username);
                                    user.setUserKey(id);

                                    // save user object

                                    firebaseDatabase.child(id).child(getString(R.string.user_details)).setValue(user);
                                    Toast.makeText(getActivity(), "user created successfully", Toast.LENGTH_SHORT).show();
                                    LoginFragment loginFragment= new LoginFragment();
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.mainActivityContainer, loginFragment,"loginFragment")
                                            .addToBackStack(null)
                                            .commit();
                                }else {
                                    Toast.makeText(getActivity(), "Failed to create user: "+
                                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }



                        });


            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private boolean validateFields(){
        username = ((EditText)getActivity().findViewById(R.id.editTextUsername)).getText().toString().trim();
        firstName = ((EditText)getActivity().findViewById(R.id.editTextFirstName)).getText().toString().trim();
        lastName = ((EditText)getActivity().findViewById(R.id.editTextLastName)).getText().toString().trim();
        dateOfBirth = ((EditText)getActivity().findViewById(R.id.editTextDoB)).getText().toString().trim();
        password = ((EditText)getActivity().findViewById(R.id.editTextPassword)).getText().toString().trim();
        confirmPass = ((EditText)getActivity().findViewById(R.id.editTextPasswordConfirm)).getText().toString().trim();
        if(username.equals("") || firstName.equals("") || lastName.equals("") || dateOfBirth.equals("") || password.equals("")){
            Toast.makeText(getActivity(), "Please complete missing fields!", Toast.LENGTH_SHORT).show();
            return false;
        } if(!isDateValid(dateOfBirth)){
            Toast.makeText(getActivity(), "Check date format. Also you should be 13 or older.", Toast.LENGTH_SHORT).show();
            return false;
        } if (!(password.length() >= 8)){
            Toast.makeText(getActivity(), "Password should be minimum 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        } if (!password.equalsIgnoreCase(confirmPass)) {
            Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
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
