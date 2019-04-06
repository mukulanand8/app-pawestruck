package com.buffhello.pawestruck;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.FeedViewHolder> {

    private static Context mContext;
    private final int DP_CACHE_PIXELS = 135;
    private final int POST_CACHE_PIXELS = 750;
    private HelperClass helperClass;
    private List<PostDetails> postDetailsList;
    private SparseBooleanArray itemStateArray = new SparseBooleanArray();
    private HashMap<Integer, UserDetailsDisplay> userDetailsDisplayHashMap = new HashMap<>();

    //Firebase Instances
    private FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference petCollection = firestore.collection("pets");
    private CollectionReference userCollection = firestore.collection("users");
    private CollectionReference reportedPostsCollection = firestore.collection("reportedPosts");
    private CollectionReference adoptedPetsCollection = firestore.collection("adoptedPets");
    private CollectionReference deletedPostsCollection = firestore.collection("deletedPosts");



    public PostAdapter(Context mContext, List<PostDetails> postDetailsList) {
        PostAdapter.mContext = mContext;
        this.postDetailsList = postDetailsList;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.feed_card, parent, false);
        return new FeedViewHolder(view);
    }

    /**
     * Binds the View with RecyclerAdapter (card feed)
     */
    @Override
    public void onBindViewHolder(@NonNull final FeedViewHolder holder, final int position) {

        final PostDetails postDetails = postDetailsList.get(position);
        helperClass = new HelperClass(mContext);

        holder.description.setText(postDetails.getDescription());
        if (postDetails.getAddress() != null) holder.location.setText(postDetails.getAddress());

        // Sets Report/Delete button
        if (mFirebaseUser != null) {
            if (postDetails.getPostedBy().equalsIgnoreCase(mFirebaseUser.getUid())) {
                holder.buttonRprtDel.setText(R.string.post_delete);
                holder.buttonRprtDel.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.card_delete, 0, 0, 0);
                holder.buttonAdopted.setVisibility(View.VISIBLE);
            } else {
                holder.buttonRprtDel.setText(R.string.post_report);
                holder.buttonRprtDel.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.card_report, 0, 0, 0);
                holder.buttonAdopted.setVisibility(View.GONE);
            }
        }

        // Already Bookmarked
        int adapterPosition = holder.getAdapterPosition();
        if (postDetails.isBookmarked()) {
            holder.fabBookmark.setImageDrawable(mContext.getResources().getDrawable(R.drawable.card_bookmark_on));
            itemStateArray.put(adapterPosition, true);
        } else {
            holder.fabBookmark.setImageDrawable(mContext.getResources().getDrawable(R.drawable.card_bookmark_off));
            itemStateArray.put(adapterPosition, false);
        }

        // Recycler Issue fix (State Retain)
        if (!userDetailsDisplayHashMap.containsKey(holder.getAdapterPosition())) {
            userCollection.document(postDetails.getPostedBy()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    // Populates four pics in card
                    for (int i = 0; i < 4; i++) {
                        if (i < postDetails.getPhotoUrls().size()) {
                            holder.photoUrls.get(i).setVisibility(View.VISIBLE);
                            final int popupPosition = i;
                            Glide.with(mContext).load(postDetails.getPhotoUrls().get(i)).apply(new RequestOptions().placeholder(R.drawable.placeholder).override(POST_CACHE_PIXELS)).into(holder.photoUrls.get(i));
                            holder.photoUrls.get(i).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    helperClass.imagePopup(postDetails.getPhotoUrls(), popupPosition);
                                }
                            });
                        } else holder.photoUrls.get(i).setVisibility(View.GONE);
                    }

                    final UserDetails userDetails = documentSnapshot.toObject(UserDetails.class);
                    UserDetailsDisplay userDetailsDisplay = new UserDetailsDisplay(userDetails.getProfilePicUrl(), userDetails.getPhoneNum(), userDetails.getName(), userDetails.getEmailId(), postDetails.getPhotoUrls());
                    userDetailsDisplayHashMap.put(holder.getAdapterPosition(), userDetailsDisplay);
                    if (userDetails.getProfilePicUrl() != null) {
                        Glide.with(mContext).load(userDetails.getProfilePicUrl()).apply(new RequestOptions().placeholder(R.drawable.profile).override(DP_CACHE_PIXELS).circleCrop()).into(holder.userPreview);
                        holder.userPreview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ArrayList<String> arrayList = new ArrayList<>();
                                arrayList.add(userDetails.getProfilePicUrl());
                                helperClass.imagePopup(arrayList, 0);
                            }
                        });
                    }
                    if (userDetails.getPhoneNum() != null)
                        holder.phoneNum.setText(userDetails.getPhoneNum());
                    holder.username.setText(userDetails.getName());
                    holder.email.setText(userDetails.getEmailId());
                }
            });
        }

        // Retains the values from userDetailsHashMap
        else {
            final UserDetailsDisplay userDetailsDisplay = userDetailsDisplayHashMap.get(holder.getAdapterPosition());
            holder.username.setText(userDetailsDisplay.getName());
            if (userDetailsDisplay.getPhonenum() != null && !userDetailsDisplay.getPhonenum().isEmpty())
                holder.phoneNum.setText(userDetailsDisplay.getPhonenum());
            holder.email.setText(userDetailsDisplay.getEmail());
            if (userDetailsDisplay.getPhotoURL() == null)
                holder.userPreview.setImageDrawable(mContext.getResources().getDrawable(R.drawable.profile));
            else {
                Glide.with(mContext).load(userDetailsDisplay.getPhotoURL()).apply(new RequestOptions().placeholder(R.drawable.profile).override(DP_CACHE_PIXELS).circleCrop()).into(holder.userPreview);
                holder.userPreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(userDetailsDisplay.getPhotoURL());
                        helperClass.imagePopup(arrayList, 0);
                    }
                });
            }

            // Populates four pics in card
            final ArrayList<String> photoUrlList = userDetailsDisplayHashMap.get(holder.getAdapterPosition()).getPhotourls();
            for (int i = 0; i < 4; i++) {
                if (i < photoUrlList.size()) {
                    holder.photoUrls.get(i).setVisibility(View.VISIBLE);
                    final int x = i;
                    Glide.with(mContext).load(photoUrlList.get(i)).apply(new RequestOptions().placeholder(R.drawable.placeholder).override(POST_CACHE_PIXELS)).into(holder.photoUrls.get(i));
                    holder.photoUrls.get(i).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            helperClass.imagePopup(photoUrlList, x);
                        }
                    });
                } else holder.photoUrls.get(i).setVisibility(View.GONE);
            }
        }

        // Checkbox States Retain
        if (!itemStateArray.get(position, false))
            holder.fabBookmark.setImageDrawable(mContext.getResources().getDrawable(R.drawable.card_bookmark_off));
        else
            holder.fabBookmark.setImageDrawable(mContext.getResources().getDrawable(R.drawable.card_bookmark_on));


        // Handles click of Bookmark FAB
        holder.fabBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFirebaseUser != null) {
                    if (!mFirebaseUser.getUid().equals(postDetails.getPostedBy())) {
                        int adapterPosition = holder.getAdapterPosition();

                        // Change bookmark state
                        if (!itemStateArray.get(adapterPosition, false)) {
                            holder.fabBookmark.setImageDrawable(mContext.getResources().getDrawable(R.drawable.card_bookmark_on));
                            itemStateArray.put(adapterPosition, true);
                        } else {
                            holder.fabBookmark.setImageDrawable(mContext.getResources().getDrawable(R.drawable.card_bookmark_off));
                            itemStateArray.put(adapterPosition, false);
                        }

                        // Adds bookmark
                        if (holder.fabBookmark.getDrawable().getConstantState().equals(mContext.getResources().getDrawable(R.drawable.card_bookmark_on).getConstantState())) {
                            holder.fabBookmark.setImageDrawable(mContext.getResources().getDrawable(R.drawable.card_bookmark_on));
                            DocumentReference addbookmark = userCollection.document(mFirebaseUser.getUid());
                            addbookmark.update("bookmarkIds", FieldValue.arrayUnion(postDetails.getDocId())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    postDetails.setBookmarked(true);
                                }
                            });
                        }

                        // Deletes bookmark
                        else {
                            holder.fabBookmark.setImageDrawable(mContext.getResources().getDrawable(R.drawable.card_bookmark_off));
                            DocumentReference userDocument = userCollection.document(mFirebaseUser.getUid());
                            userDocument.update("bookmarkIds", FieldValue.arrayRemove(postDetails.getDocId())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    postDetails.setBookmarked(false);
                                }
                            });
                        }
                    } else helperClass.displayToast(R.string.bookmark_own);
                } else {
                    holder.fabBookmark.setImageDrawable(mContext.getResources().getDrawable(R.drawable.card_bookmark_off));
                    helperClass.displayToast(R.string.acc_needed_bookmark);
                }
            }
        });

        // Phone call (Dial) intent
        holder.fabCallTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneIntent(holder.phoneNum.getText().toString());
            }
        });

        holder.fabCallBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneIntent(holder.phoneNum.getText().toString());
            }
        });

        //Email intent
        holder.fabEmailTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailIntent(holder.email.getText().toString());
            }
        });
        holder.fabEmailBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailIntent(holder.email.getText().toString());
            }
        });

        // Location(Map) intent
        holder.fabLocationTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationIntent(postDetails.getCoOrdinates(), postDetails.getAddress());
            }
        });

        holder.fabLocationBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationIntent(postDetails.getCoOrdinates(), postDetails.getAddress());
            }
        });

        holder.buttonAdopted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        // By default, the button shows "Report Post". Can report only if the user has signed in
        holder.buttonRprtDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Reports the post
                if (holder.buttonRprtDel.getText().equals(mContext.getResources().getString(R.string.post_report))) {
                    if (mFirebaseUser != null) {
                        final DocumentReference addpropicurl = reportedPostsCollection.document(postDetails.getDocId());
                        new AlertDialog.Builder(mContext).setTitle(R.string.post_report).setMessage(R.string.post_report_prompt).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                holder.progressBar.setVisibility(View.VISIBLE);
                                holder.progressBar.requestFocus();
                                addpropicurl.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        // Appends userID to the post document's user list field
                                        if (documentSnapshot.exists()) {
                                            addpropicurl.update("UserIds", FieldValue.arrayUnion(mFirebaseUser.getUid())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    holder.progressBar.setVisibility(View.GONE);
                                                    helperClass.displayToast(R.string.post_reported);
                                                    petCollection.document(postDetails.getDocId()).update("Reported", true);

                                                }
                                            });
                                        }

                                        // Creates a new document with user list field and appends userID
                                        else {
                                            Map<String, Object> newRprt = new HashMap<>();
                                            newRprt.put("UserIds", FieldValue.arrayUnion(mFirebaseUser.getUid()));
                                            addpropicurl.set(newRprt).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    helperClass.displayToast(R.string.post_reported);
                                                }
                                            });
                                        }
                                        holder.progressBar.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }).setNegativeButton(android.R.string.no, null).setIcon(R.drawable.card_report).show();
                    } else helperClass.displayToast(R.string.acc_needed_report);
                }

                // Deletes the post
                else if (holder.buttonRprtDel.getText().equals(mContext.getResources().getString(R.string.post_delete))) {
                    final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                    new AlertDialog.Builder(mContext).setTitle(R.string.post_delete).setMessage(R.string.post_delete_prompt).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            holder.progressBar.setVisibility(View.VISIBLE);
                            holder.progressBar.requestFocus();

                            moveFirestoreDocument(petCollection.document(postDetails.getDocId()), deletedPostsCollection.document(postDetails.getDocId()));

                            // Removes the post from user's bookmark list
                            userCollection.document(mFirebaseUser.getUid()).update("bookmarkIds", FieldValue.arrayRemove(postDetails.getDocId())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    holder.progressBar.setVisibility(View.GONE);
                                    postDetailsList.remove(postDetails);
                                    userDetailsDisplayHashMap.clear();
                                    notifyItemRemoved(position);
                                    notifyItemRangeRemoved(position, getItemCount());
                                }
                            });
                        }
                    }).setNegativeButton(android.R.string.no, null).setIcon(R.drawable.card_delete).show();
                }
            }
        });


        // Handles card expansion
        boolean isExpanded = postDetails.isExpanded();
        if (isExpanded) {
            holder.subItem.setVisibility(View.VISIBLE);
            holder.fabCallTop.setVisibility(View.GONE);
            holder.fabLocationTop.setVisibility(View.GONE);
            holder.fabEmailTop.setVisibility(View.GONE);
            holder.imgExpand.setImageResource(R.drawable.arrow_up);
        } else {
            holder.subItem.setVisibility(View.GONE);
            holder.fabCallTop.setVisibility(View.VISIBLE);
            holder.fabLocationTop.setVisibility(View.VISIBLE);
            holder.fabEmailTop.setVisibility(View.VISIBLE);
            holder.imgExpand.setImageResource(R.drawable.arrow_down);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isExpanded = postDetails.isExpanded();
                postDetails.setExpanded(!isExpanded);
                notifyDataSetChanged();
            }
        });

        holder.buttonAdopted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                petCollection.document(postDetails.getDocId()).update("adopted", true);
                copyFirestoreDocument(petCollection.document(postDetails.getDocId()), adoptedPetsCollection.document(postDetails.getDocId()));

            }
        });
    }

    /**
     * Map intent
     */
    private void locationIntent(GeoPoint coOrdinates, String address) {
        if (coOrdinates != null && address != null) {
            String uri = "http://maps.google.com/maps?q=loc:" + coOrdinates.getLatitude() + "," + coOrdinates.getLongitude() + " (" + address + ")";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            if (intent.resolveActivity(mContext.getPackageManager()) != null)
                mContext.startActivity(intent);
        } else helperClass.displayToast(R.string.no_address);
    }

    /**
     * Email intent that only email apps should handle
     */
    private void emailIntent(String emailid) {
        String emailIds[] = new String[1];
        emailIds[0] = emailid;
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, emailIds);
        if (intent.resolveActivity(mContext.getPackageManager()) != null)
            mContext.startActivity(intent);
    }

    /**
     * Call (Dial) intent
     */
    private void phoneIntent(String phoneNumber) {
        if (!phoneNumber.equals("-")) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            if (intent.resolveActivity(mContext.getPackageManager()) != null)
                mContext.startActivity(intent);
        } else helperClass.displayToast(R.string.no_phone);
    }

    @Override
    public int getItemCount() {
        return postDetailsList.size();
    }

    public void moveFirestoreDocument(final DocumentReference fromPath, final DocumentReference toPath) {
        fromPath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        toPath.set(document.getData())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        displayToast("DocumentSnapshot successfully written!");
                                        fromPath.delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        displayToast("DocumentSnapshot successfully deleted!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        displayToast("Error deleting document");
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        displayToast("Error writing document" + e.toString());
                                    }
                                });
                    } else {
                        displayToast("No such document");
                    }
                } else {
                    displayToast("get failed with " + task.getException().toString());
                }
            }
        });
    }


    public void copyFirestoreDocument(final DocumentReference fromPath, final DocumentReference toPath) {
        fromPath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        toPath.set(document.getData())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        displayToast("DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        displayToast("Error writing document");
                                    }
                                });
                    } else {
                        displayToast("No such document");
                    }
                } else {
                    displayToast("get failed with ");
                }
            }
        });
    }

    public void displayToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    /**
     * Class containing the components of a card
     */
    static class FeedViewHolder extends RecyclerView.ViewHolder {

        private ImageView petPreview1, petPreview2, petPreview3, petPreview4, imgExpand, userPreview;
        private TextView description, username, location, phoneNum, email;
        private LinearLayout subItem;
        private FloatingActionButton fabBookmark, fabEmailTop, fabCallTop, fabLocationTop, fabEmailBot, fabCallBot, fabLocationBot;
        private ArrayList<ImageView> photoUrls = new ArrayList<>();
        private Button buttonRprtDel, buttonAdopted;
        private ProgressBar progressBar;

        FeedViewHolder(View itemView) {
            super(itemView);
            userPreview = itemView.findViewById(R.id.card_iv_dp);
            petPreview1 = itemView.findViewById(R.id.card_iv_preview1);
            petPreview2 = itemView.findViewById(R.id.card_iv_preview2);
            petPreview3 = itemView.findViewById(R.id.card_iv_preview3);
            petPreview4 = itemView.findViewById(R.id.card_iv_preview4);
            photoUrls.add(petPreview1);
            photoUrls.add(petPreview2);
            photoUrls.add(petPreview3);
            photoUrls.add(petPreview4);
            description = itemView.findViewById(R.id.card_tv_description);
            username = itemView.findViewById(R.id.card_tv_name);
            phoneNum = itemView.findViewById(R.id.card_tv_phone);
            location = itemView.findViewById(R.id.card_tv_address);
            email = itemView.findViewById(R.id.card_tv_email);
            subItem = itemView.findViewById(R.id.card_sub_item);
            fabEmailTop = itemView.findViewById(R.id.card_fab_email_top);
            fabCallTop = itemView.findViewById(R.id.card_fab_call_top);
            fabLocationTop = itemView.findViewById(R.id.card_fab_location_top);
            fabEmailBot = itemView.findViewById(R.id.card_fab_email_bot);
            fabCallBot = itemView.findViewById(R.id.card_fab_call_bot);
            fabLocationBot = itemView.findViewById(R.id.card_fab_location_bot);
            imgExpand = itemView.findViewById(R.id.card_iv_expand);
            fabBookmark = itemView.findViewById(R.id.card_fab_bookmark);
            buttonRprtDel = itemView.findViewById(R.id.card_button_del_rep);
            buttonAdopted = itemView.findViewById(R.id.card_button_adopted);
            progressBar = itemView.findViewById(R.id.card_progress);

            int maxWidth = mContext.getResources().getConfiguration().screenWidthDp * 2;
            email.setMaxWidth(maxWidth);
            phoneNum.setMaxWidth(maxWidth);
            location.setMaxWidth(maxWidth);
        }
    }
}