package com.buffhello.pawestruck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Shows the posts that a user has bookmarked.
 * Posts by the user are also bookmarked by default and can't be removed
 */
public class BookmarksFragment extends Fragment {

    private FirebaseUser currentUser;

    public BookmarksFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        final CollectionReference petCollection = mFirestore.collection("pets");
        final CollectionReference userCollection = mFirestore.collection("users");

        final TextView tvNoBookmarks = view.findViewById(R.id.mark_tv_none);
        final RecyclerView recyclerView = view.findViewById(R.id.mark_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // Retrieves user's details from Firestore and gets the list of bookmarks
        userCollection.document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                UserDetails userDetails = task.getResult().toObject(UserDetails.class);
                final ArrayList<String> bookmarkList = userDetails.getBookmarkIds();
                final List<PostDetails> postDetailsList = new ArrayList<>();

                /* For each bookmarked post, checks if it has deleted by the one who posted.
                Deletes from the user's bookmark list if it has been deleted */
                for (final int[] i = {0}; i[0] < bookmarkList.size(); i[0]++) {
                    final String petuid = bookmarkList.get(i[0]);
                    petCollection.document(petuid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            if (documentSnapshot.exists()) {
                                PostDetails postDetails = documentSnapshot.toObject(PostDetails.class);
                                postDetails.setBookmarked(true);
                                postDetails.setDocId(documentSnapshot.getId());
                                postDetailsList.add(postDetails);
                            }

                            /* Updates RecyclerView with each item of postAdapter and
                            sorts the bookmarks in the descending order of timestamp */
                            while (postDetailsList.size() == bookmarkList.size()) {
                                if (!postDetailsList.isEmpty()) {
                                    Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                                        @Override
                                        public int compare(PostDetails o1, PostDetails o2) {
                                            return o2.getTimestamp().compareTo(o1.getTimestamp());
                                        }
                                    });
                                    tvNoBookmarks.setVisibility(View.GONE);
                                    PostAdapter postAdapter = new PostAdapter(getContext(), postDetailsList);
                                    recyclerView.setAdapter(postAdapter);
                                } else tvNoBookmarks.setVisibility(View.VISIBLE);
                                bookmarkList.clear();
                            }
                        }
                    });
                }
            }
        });
        return view;
    }
}
