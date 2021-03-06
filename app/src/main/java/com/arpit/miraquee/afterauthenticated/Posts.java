 package com.arpit.miraquee.afterauthenticated;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arpit.miraquee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Posts extends Fragment{


    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    Context context;
    int flag;

    public Posts() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth =  FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.afragment_posts, container, false);

        recyclerView = view.findViewById(R.id.post_recycleView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(
                R.color.colorPrimaryDark
        ));
        swipeRefreshLayout.setOnRefreshListener(this::getPost);

        getPost();
        return  view;
    }

    private void getPost() {
        swipeRefreshLayout.setRefreshing(true);
        final List<PostModel> list = new ArrayList<>();
        db.collection("All Posts").orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult() != null && task.isSuccessful())
                        {
                            list.clear();
                            for(QueryDocumentSnapshot snap: task.getResult()) {
                                String profilePic, postPic;
                                String name,text,time,uid;
                                String docId = snap.getId();
                                name = snap.getString("name");
                                text = snap.getString("Text of Post");
                                time = snap.getLong("time").toString();
                                uid = snap.getString("userid");
                                profilePic = snap.getString("profileUrl");
                                postPic = snap.getString("postUrl");

                                list.add(new PostModel(
                                        name,
                                        text,
                                        time,
                                        profilePic,
                                        postPic,
                                        uid,
                                        docId
                                ));
                            }
                            Log.d("XXX", "Recycler View Created items:" + list.size());
                            recyclerView.setAdapter(new Post_recycler_adapter(list,context));
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }
}