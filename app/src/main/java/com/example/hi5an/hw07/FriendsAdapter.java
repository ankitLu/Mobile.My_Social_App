package com.example.hi5an.hw07;

import android.support.v7.widget.RecyclerView;

/**
 * Created by hi5an on 11/19/2017.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Assignment # InClass 08
 * Group #01
 * Created by Ankit Luthra & Zach Graves on 10/23/2017.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder>
{
    ArrayList<User> mData;
    Context context;
    int position;
    String key;
    String tabType;
    User currentUser;

    public FriendsAdapter(ArrayList<User> friendsArrayList, String tabType, User currentUser){
        this.mData = friendsArrayList;
        this.tabType = tabType;
        this.currentUser = currentUser;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_friends_recycler_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, mData, position);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        User app = mData.get(position);
        holder.context = this.context;
        holder.textViewUsername.setText(app.getFirstName() +" "+ app.getLastName());
        if (tabType.equals(MainActivity.ADD_FRIEND)){
           if(app.getStatus()!=null && app.getStatus().equals(MainActivity.PENDING_REQ)){
               holder.imageViewFriendButton.setBackgroundResource(R.drawable.pending_requests);
               holder.imageViewFriendButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                       alertDialog.setTitle(R.string.confirm);
                       alertDialog.setMessage("Would you like to cancel sent request?");
                       alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               // add request to selected user
                               final User requested = mData.get(position);
                               final String id = requested.getUserKey();
                               holder.databaseReference.child(currentUser.getUserKey()).child(MainActivity.SENT_REQ_DETAILS)
                                       .child(id).removeValue();
                               holder.databaseReference.child(id).child(MainActivity.PENDING_REQ).child(currentUser.getUserKey())
                                       .removeValue();
                               holder.imageViewFriendButton.setBackgroundResource(R.drawable.add_new_friend);
                               mData.remove(position);
                               notifyDataSetChanged();

                           }
                       });
                       alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               return;
                           }
                       });
                       alertDialog.show();
                   }
               });
           } else{
               holder.imageViewFriendButton.setBackgroundResource(R.drawable.add_new_friend);
               holder.imageViewFriendButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                       alertDialog.setTitle(R.string.confirm);
                       alertDialog.setMessage("Would you like to send a friend request?");
                       alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               // add request to selected user
                               final User requested = mData.get(position);
                               final String id = requested.getUserKey();
                               holder.databaseReference.child(currentUser.getUserKey()).child(MainActivity.SENT_REQ_DETAILS)
                                       .child(id).setValue(id);
                               holder.databaseReference.child(id).child(MainActivity.PENDING_REQ).child(currentUser.getUserKey())
                                       .setValue(currentUser.getUserKey());
                               holder.imageViewFriendButton.setBackgroundResource(R.drawable.pending_requests);
                               mData.remove(position);
                               notifyItemRemoved(position);
                               notifyDataSetChanged();
                               holder.imageViewFriendButton.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       //do nothing
                                   }
                               });
                           }
                       });
                       alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               return;
                           }
                       });
                       alertDialog.show();
                   }
               });
           }
        }

        if (tabType.equals(MainActivity.FRIEND_LIST)){
            holder.imageViewFriendButton.setBackgroundResource(R.drawable.remove_friend);
            holder.imageViewFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(R.string.confirm);
                    alertDialog.setMessage("Would you like to delete?");
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mData.size() > 0) {
                                User requested = mData.get(position);
                                String id = requested.getUserKey();
                                holder.databaseReference.child(currentUser.getUserKey()).child(MainActivity.FRIEND_LIST)
                                        .child(id).removeValue();
                                holder.databaseReference.child(id).child(MainActivity.FRIEND_LIST).child(currentUser.getUserKey())
                                        .removeValue();
                                mData.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                            } else {
                                return;
                            }
                        }
                    });
                    alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    alertDialog.show();
                }
            });
        }

        if (tabType.equals(MainActivity.PENDING_REQ)){
            holder.imageViewAccept.setVisibility(View.VISIBLE);
            holder.imageViewAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    User requested = mData.get(position);
                    String id = requested.getUserKey();
                    holder.databaseReference.child(currentUser.getUserKey()).child(MainActivity.FRIEND_LIST)
                            .child(id).setValue(id);
                    holder.databaseReference.child(id).child(MainActivity.FRIEND_LIST).child(currentUser.getUserKey())
                            .setValue(currentUser.getUserKey());
                    mData.remove(position);
                    notifyDataSetChanged();
                    notifyItemRemoved(position);
                    holder.databaseReference.child(currentUser.getUserKey()).child(MainActivity.PENDING_REQ)
                            .child(id).removeValue();
                    holder.databaseReference.child(id).child(MainActivity.SENT_REQ_DETAILS)
                            .child(currentUser.getUserKey()).removeValue();
                }
            });
            holder.imageViewFriendButton.setBackgroundResource(R.drawable.decline);
            holder.imageViewFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    User requested = mData.get(position);
                    String id = requested.getUserKey();
                    holder.databaseReference.child(currentUser.getUserKey()).child(MainActivity.PENDING_REQ)
                            .child(id).removeValue();
                    holder.databaseReference.child(id).child(MainActivity.SENT_REQ_DETAILS)
                            .child(currentUser.getUserKey()).removeValue();
                    mData.remove(position);
                    notifyDataSetChanged();
                    notifyItemRemoved(position);
                }
            });
        }

        holder.app = app;
        holder.position = position;

    }

    public void removeItemFromList(ArrayList<User> appList, final int position) {
        this.mData = (ArrayList<User>) appList;
        // remove a friend from my friend list
        User friend = mData.get(position);
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        //myRef.child(currentUser.getUserKey()).child("USER_DETAILS").setValue(user);


        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        key = myRef.child(firebaseUser.getUid()).getKey();
        DataSnapshot dataSnapshot;
        final Query applesQuery = myRef.child(key).orderByChild("email").equalTo(mData.get(position).getUsername());
        myRef.child(firebaseUser.getUid()).child(key).removeValue();
        notifyDataSetChanged();
        this.mData.remove(position);
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                    notifyDataSetChanged();
                    notifyItemRemoved(position);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", "onCancelled", databaseError.toException());
            }
        });
        notifyItemRemoved(position);
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        User app;
        TextView textViewUsername;
        ImageView imageViewFriendButton, imageViewAccept;
        Context context;
        int position;
        ArrayList<User> arrayList;
        DatabaseReference databaseReference;

        public ViewHolder(final View itemView, ArrayList<User> mData, final int position) {
            super(itemView);
            this.app = app;
            textViewUsername = (TextView) itemView.findViewById(R.id.textViewFriend);
            imageViewFriendButton = (ImageView) itemView.findViewById(R.id.imageViewRemoveFriend);
            imageViewAccept = (ImageView) itemView.findViewById(R.id.imageViewAccept);
            this.databaseReference = FirebaseDatabase.getInstance().getReference();
            this.context = context;
            this.position = position;
            this.arrayList = mData;

        }
    }
}

