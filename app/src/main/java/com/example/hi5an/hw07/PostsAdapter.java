package com.example.hi5an.hw07;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by hi5an on 11/19/2017.
 */

/**
 * Assignment # InClass 08
 * Group #01
 * Created by Ankit Luthra & Zach Graves on 10/23/2017.
 */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder>
{
    ArrayList<Post> mData;
    Context context;
    int position;
    String key;
    User currentUser;
    String screen;

    public PostsAdapter(ArrayList<Post> postArrayList, User user, String screen){
        this.mData = postArrayList;
        this.currentUser = user;
        this.screen = screen;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_posts_recycler_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, mData, position);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Post app = mData.get(position);
        holder.context = this.context;
        holder.textViewName.setText(app.getPostedBy());
        holder.textViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BioFragment bioFragment = new BioFragment();
                Bundle bundle = new Bundle();
                bundle.putString(MainActivity.USER_KEY, app.getPostedByKey());
                bioFragment.setArguments(bundle);
                ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainActivityContainer,bioFragment)
                        .commit();
            }
        });
        holder.textViewMsg.setText(app.getPostMsg());
        PrettyTime p  = new PrettyTime();
        Date date = new Date(app.getPostTime());
        String datetime= p.format(date);
        holder.textViewTime.setText(datetime);
        if (this.screen.equals(HomeScreenFragment.HOME_SCREEN)){

        } else  if (this.screen.equals(BioFragment.WALL_SCREEN)){
            if (app.getPostedByKey().equals(currentUser.getUserKey())){
                holder.imageViewDelete.setVisibility(View.VISIBLE);
                holder.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeItemFromList(mData, position);
                    }
                });
            }
        }

        holder.app = app;
        holder.position = position;

    }

    public void removeItemFromList(final ArrayList<Post> appList, final int position) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(R.string.confirm);
        alertDialog.setMessage("Would you like to delete this post?");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Post aPost = appList.get(position);
                mData = appList;
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child(currentUser.getUserKey()).child(MainActivity.POST_DETAILS)
                        .child(aPost.getPostId())
                        .removeValue();
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

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        Post app;
        TextView textViewName, textViewTime, textViewMsg;
        ImageView imageViewDelete;
        Context context;
        int position;
        ArrayList<Post> arrayList;
        DatabaseReference databaseReference;

        public ViewHolder(final View itemView, ArrayList<Post> mData, final int position) {
            super(itemView);
            this.app = app;
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            textViewTime = (TextView) itemView.findViewById(R.id.textViewTime);
            textViewMsg = (TextView) itemView.findViewById(R.id.textViewMsg);
            imageViewDelete = (ImageView) itemView.findViewById(R.id.imageViewDelete);
            this.databaseReference = FirebaseDatabase.getInstance().getReference();
            this.context = context;
            this.position = position;
            this.arrayList = mData;

        }
    }
}

