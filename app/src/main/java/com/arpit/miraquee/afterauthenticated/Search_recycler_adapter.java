package com.arpit.miraquee.afterauthenticated;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arpit.miraquee.RequestNotif.ApiServe;
import com.arpit.miraquee.RequestNotif.Client;
import com.arpit.miraquee.RequestNotif.Data;
import com.arpit.miraquee.RequestNotif.MyResponse;
import com.arpit.miraquee.RequestNotif.NotificationSender;
import com.arpit.miraquee.R;
import com.arpit.miraquee.SharedPreferences.Preferences;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Search_recycler_adapter extends RecyclerView.Adapter<Search_recycler_adapter.myAdapter> {
    List<SearchModel> list;
    Context context;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String uid;
    Preferences prefs;
    ApiServe apiServe;
    StorageReference storageReference;

    public Search_recycler_adapter(List<SearchModel> list, Context context) {
        this.list = list;
        this.context = context;
        prefs = new Preferences(context);
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth =  FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        apiServe = Client.getClient("https://fcm.googleapis.com/").create(ApiServe.class);
    }

    @NonNull
    @Override
    public Search_recycler_adapter.myAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_search_element,
                        parent,
                        false);
        return new Search_recycler_adapter.myAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Search_recycler_adapter.myAdapter holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class myAdapter extends RecyclerView.ViewHolder {
        TextView name, username;
        CircularImageView image;
        Button addContact;
        public myAdapter(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(SearchModel searchModel) {
            name =itemView.findViewById(R.id.searchname);
            username = itemView.findViewById(R.id.searchusername);
            image = itemView.findViewById(R.id.searchChatCiv);
            addContact = itemView.findViewById(R.id.addContact);


            //to denote the users signs when start up
            db.collection("All Users").document(searchModel.getUid()).collection("Contacts")
                    .document(uid).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Boolean status = documentSnapshot.getBoolean("Status");
                            String SorR = documentSnapshot.getString("SentOrRecieved");
                            if(status == null){
                            }
                            else{
                                if(status == false) {
                                    if(SorR.equals("R")){
                                        addContact.setBackgroundResource(R.drawable.ic_request_sent);
                                    }
                                    if(SorR.equals("S")){
                                        addContact.setBackgroundResource(R.drawable.ic_request_received);
                                    }

                                }
                                if(status == true){
                                    addContact.setBackgroundResource(R.drawable.ic_friends);
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            });

            addContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //same thing but only when the user presses it
                    db.collection("All Users").document(searchModel.getUid()).collection("Contacts")
                            .document(uid).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Boolean status = documentSnapshot.getBoolean("Status");
                            if(status == null){
                                sendContactRequest(searchModel);
                            }
                            else{
                                if(status == false) {
                                    removeRequest(searchModel);
                                }
                                if(status == true){
                                    Toast.makeText(context, "Already Friends", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            //For Name, username, and Profile Pic
            name.setText(searchModel.getName());
            username.setText("@"+searchModel.getUsername());

            StorageReference profoleRef = storageReference.child("Profile Photos").child(searchModel.getUid());
            profoleRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d("Check", "Uri has been received");
                    Picasso.get().load(uri).into(image);
                }
            });
        }

        private void removeRequest(@NotNull SearchModel searchModel) {
            db.collection("All Users").document(searchModel.getUid()).collection("Contacts")
                    .document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String SorR = documentSnapshot.getString("SentOrRecieved");
                    if(SorR.equals("R")){
                        db.collection("All Users").document(searchModel.getUid()).collection("Contacts")
                                .document(uid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                removeFromMyContactList(searchModel);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                    if(SorR.equals("S")){
                        Toast.makeText(context, "This person has sent you the request! You can only accept it", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }

        private void removeFromMyContactList(@NotNull SearchModel searchModel) {
            db.collection("All Users").document(uid).collection("Contacts")
                    .document(searchModel.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    addContact.setEnabled(false);
                    addContact.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addContact.setEnabled(true);
                        }
                    }, 500);
                    addContact.setBackgroundResource(R.drawable.ic_person_add);
                    Toast.makeText(context, "Request Deleted", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void sendContactRequest(SearchModel searchModel) {

            DocumentReference documentReference = db.collection("All Users").document(searchModel.getUid()).collection("Contacts")
                    .document(uid);
            Map<String, Object> request = new HashMap<>();
            request.put("SentOrRecieved","R");
            request.put("OpponentUid",uid);
            request.put("Status",false);
            request.put("OpponentUsername", prefs.getData("username"));
            request.put("OpponentName", prefs.getData("usernameAdded"));
            documentReference.set(request, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    addContact.setBackgroundResource(R.drawable.ic_request_sent);
                    updateMyContactlist(searchModel);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void updateMyContactlist(SearchModel searchModel) {
            DocumentReference documentReference = db.collection("All Users").document(uid).collection("Contacts")
                    .document(searchModel.getUid());
            Map<String, Object> request = new HashMap<>();
            request.put("SentOrRecieved","S");
            request.put("OpponentUid",searchModel.getUid());
            request.put("Status",false);
            request.put("OpponentUsername",searchModel.getUsername());
            request.put("OpponentName", searchModel.getName());
            documentReference.set(request, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    addContact.setEnabled(false);
                    addContact.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addContact.setEnabled(true);
                            sendCloudNotification(searchModel);
                        }
                    }, 500);
                    Toast.makeText(context, "Request Sent", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void sendCloudNotification(SearchModel searchModel) {


            //get opponents token
            db.collection("All Users").document(searchModel.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String opponentsToken;
                    opponentsToken = documentSnapshot.getString("FCM_token");
                    if(opponentsToken!= null){
                        String Title = "New Request!";
                        String Message = "@"+prefs.getData("username") + " wants to be your Contact! Open Requests in the application to see all pending requests";
                        Data data = new Data(Title, Message);
                        NotificationSender sender= new NotificationSender(data, opponentsToken);
                        apiServe.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                if(response.code() == 200){
                                    if(response.body().success != 1){
                                        Toast.makeText(context, "Failed! The user is inactive", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
//                                        Toast.makeText(context, "FCM sent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Toast.makeText(context, "Failed!"+ t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }
}
