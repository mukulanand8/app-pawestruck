package com.buffhello.pawestruck;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.app.Activity.RESULT_OK;

/**
 * Homepage where all posts show up. All back actions lead to this page
 */
public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int ADDPREQCODE = 123;
    private boolean isRefreshing = false;
    private double latitude = -1, longitude = -1;
    private int filterCache = -1, animalCache = -1, sortCache = -1;

    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference petCollection = firestore.collection("pets");
    private CollectionReference userCollection = firestore.collection("users");
    private Context mContext;
    private HelperClass helperClass;
    private SharedPreferences sharedPreferences;
    private List<PostDetails> postDetailsList;
    private ArrayList<String> bookmarkIds = new ArrayList<>();
    private HashMap<String, String> userWithEmailIdHashMap = new HashMap<>();

    private SwipeRefreshLayout swipeLayout;
    private CardView cardWelcome;
    private TextView tvNone;
    private AutoCompleteTextView actvUser;
    private RecyclerView recyclerView;
    private View globalView;

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getContext();
        helperClass = new HelperClass(mContext);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    /**
     * Check if user is signed in
     */
    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Handles action bar item clicks. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as the parent activity is specified in AndroidManifest.xml.
     */
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.add_post) {
            // User needs to sign in to post
            if (mFirebaseUser != null) {

                // Checks whether Email or Phone number is verified. Only then one can post
                if (mFirebaseUser.isEmailVerified() || (mFirebaseUser.getPhoneNumber() != null && !mFirebaseUser.getPhoneNumber().equals(""))) {
                    Intent intent = new Intent(getActivity(), AddPostActivity.class);
                    startActivityForResult(intent, ADDPREQCODE);
                } else helperClass.displayToast(R.string.acc_verify);

            } else {
                helperClass.displayToast(R.string.acc_needed_post);
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
            }
        }

        // Brings up the filter dialog box
        else if (id == R.id.filter) {
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_filter, null);
            actvUser = dialogView.findViewById(R.id.filt_actv_user);
            LinearLayout parent = dialogView.findViewById(R.id.filt_parent);
            final RadioGroup rgSortOrder = dialogView.findViewById(R.id.filt_rg_sort);
            final RadioGroup rgAnimal = dialogView.findViewById(R.id.filt_rg_animal);
            final Spinner filtSpinner = dialogView.findViewById(R.id.filt_spinner);

            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    helperClass.hideKeyboard(v);
                }
            });

            if (filterCache != -1) filtSpinner.setSelection(filterCache);
            if (animalCache != -1) rgAnimal.check(animalCache);
            if (sortCache != -1) rgSortOrder.check(sortCache);

            filtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Object item = parent.getSelectedItem().toString();
                    if (item.equals("Animal")) {
                        actvUser.setVisibility(View.GONE);
                        rgAnimal.setVisibility(View.VISIBLE);
                    } else if (item.equals("User")) {
                        actvUser.setVisibility(View.VISIBLE);
                        rgAnimal.setVisibility(View.GONE);
                        fillActvUser();
                    } else if (item.equals("Address")) {
                        actvUser.setVisibility(View.VISIBLE);
                        rgAnimal.setVisibility(View.GONE);
                        actvUser.setHint(R.string.filter_address);
                    } else if (item.equals("Radius (Testing)")) {
                        actvUser.setVisibility(View.VISIBLE);
                        rgAnimal.setVisibility(View.GONE);
                        getLocation();
                        actvUser.setHint(R.string.filter_radius);
                    } else if (item.equals("Breed")) {
                        Intent intent = new Intent(mContext, DiscourageActivity.class);
                        intent.putExtra("isGender", false);
                        startActivity(intent);
                    } else if (item.equals("Gender")) {
                        Intent intent = new Intent(mContext, DiscourageActivity.class);
                        intent.putExtra("isGender", true);
                        startActivity(intent);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            dialogBuilder.setView(dialogView);
            dialogBuilder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {

                /** Calls appropriate functions onClick */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String filterType = filtSpinner.getSelectedItem().toString();
                    final String value = actvUser.getText().toString().trim();
                    int selectedId = rgSortOrder.getCheckedRadioButtonId();
                    sortCache = selectedId;
                    RadioButton rbOrder = dialogView.findViewById(selectedId);
                    final String sortOrder = rbOrder.getText().toString();
                    switch (filterType) {
                        case "Animal":
                            int animalID = rgAnimal.getCheckedRadioButtonId();
                            animalCache = animalID;
                            filterCache = 0;
                            if (animalID != -1) {
                                RadioButton rbAnimal = dialogView.findViewById(animalID);
                                String animal = rbAnimal.getText().toString();
                                feedFillAnimal(animal, sortOrder);
                            } else feedFillBasic(sortOrder);
                            break;
                        case "User":
                            filterCache = 1;
                            if (value.equals("")) feedFillBasic(sortOrder);
                            else feedFillUser(value, sortOrder);
                            break;
                        case "Address":
                            filterCache = 2;
                            if (value.equals("")) feedFillBasic(sortOrder);
                            else feedFillAddress(value, sortOrder);
                            break;
                        case "Radius (Testing)":
                            filterCache = 3;
                            if (value.equals("")) feedFillBasic(sortOrder);
                            else {
                                getLocation();
                                if (!helperClass.isNetworkAvailable())
                                    helperClass.displayToast(R.string.no_internet);
                                else if (!helperClass.isLocationAvailable())
                                    helperClass.displayToast(R.string.no_gps);
                                else if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()).getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                                    helperClass.displayToast(R.string.allow_permission_location);
                                else if (latitude == -1 && longitude == -1)
                                    helperClass.displayToast(R.string.error_fetch_location);
                                else feedFillDistance(value, sortOrder);
                            }
                            break;
                    }
                }
            });
            dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Filters the feed based on the Animal and displays
     */
    private void feedFillAnimal(final String animal, final String sortOrder) {
        initView();
        petCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                final List<PostDetails> postDetailsList = new ArrayList<>();
                for (DocumentSnapshot doc : documentSnapshots) {
                    PostDetails postDetails = doc.toObject(PostDetails.class);
                    if (postDetails.getAnimal() != null && postDetails.getAnimal().equals(animal)) {
                        postDetails.setDocId(doc.getId());
                        postDetailsList.add(postDetails);
                    }
                }
                responseForQuery(postDetailsList);

                getSorted(postDetailsList, sortOrder);

                if (mFirebaseUser != null) integrateBookmarkInfo(postDetailsList);
                else attachAdapter(postDetailsList);
            }
        });
    }

    /**
     * Helper method to calculate distance (km) between two co-ordinates (lat,long)
     */
    private double distanceFrom(final double latitude, final double longitude) {
        double R = 6371; // km
        double dLat = Math.toRadians(this.latitude - latitude);
        double dLon = Math.toRadians(this.longitude - longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(this.latitude)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Fills the feed with the posts that are inside the entered radius
     */
    private void feedFillDistance(String value, final String sortOrder) {
        int reqKm = 0;
        try {
            reqKm = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            helperClass.displayToast(R.string.home_error_distance);
        }
        initView();
        final int finalReqKm = reqKm;
        petCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) return;
                final List<PostDetails> postDetailsList = new ArrayList<>();

                // Fetches each document's coOrdinates field and checks if it's inside the entered radius
                for (DocumentSnapshot doc : documentSnapshots) {
                    PostDetails postDetails = doc.toObject(PostDetails.class);
                    if (distanceFrom(postDetails != null ? postDetails.getCoOrdinates().getLatitude() : 0, postDetails != null ? postDetails.getCoOrdinates().getLongitude() : 0) <= finalReqKm) {
                        if (postDetails != null) {
                            postDetails.setDocId(doc.getId());
                            postDetailsList.add(postDetails);
                        }
                    }
                }
                responseForQuery(postDetailsList);
                getSorted(postDetailsList, sortOrder);
                if (mFirebaseUser != null) integrateBookmarkInfo(postDetailsList);
                else attachAdapter(postDetailsList);
            }
        });
    }

    /**
     * Fills the feed with the posts whose address contains the entered address
     */
    private void feedFillAddress(String address, final String sortOrder) {
        initView();
        address = address.toLowerCase();
        final String finalAddress = address;
        petCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) return;
                final List<PostDetails> postDetailsList = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    PostDetails postDetails = doc.toObject(PostDetails.class);
                    if (postDetails.getAddress().toLowerCase().contains(finalAddress)) {
                        postDetails.setDocId(doc.getId());
                        postDetailsList.add(postDetails);
                    }
                }
                responseForQuery(postDetailsList);
                getSorted(postDetailsList, sortOrder);
                if (mFirebaseUser != null) integrateBookmarkInfo(postDetailsList);
                else attachAdapter(postDetailsList);
            }
        });
    }

    /**
     * Fills the feed with the posts which are posted by the entered username
     */
    private void feedFillUser(String userid, final String sortOrder) {
        initView();
        final String docid = userWithEmailIdHashMap.get(userid);
        petCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (e != null) return;
                List<PostDetails> postDetailsList = new ArrayList<>();
                for (DocumentSnapshot doc : documentSnapshots) {
                    PostDetails postDetails = doc.toObject(PostDetails.class);
                    if (postDetails.getPostedBy().equals(docid)) {
                        postDetails.setDocId(doc.getId());
                        postDetailsList.add(postDetails);
                    }
                }
                responseForQuery(postDetailsList);
                getSorted(postDetailsList, sortOrder);
                if (mFirebaseUser != null) integrateBookmarkInfo(postDetailsList);
                else attachAdapter(postDetailsList);
            }
        });
    }


    /**
     * Fills the feed with all posts posted so far
     */
    private void feedFillBasic(final String sortOrder) {
        if (!helperClass.isNetworkAvailable()) helperClass.displayToast(R.string.home_no_internet);
        initView();
        petCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                final List<PostDetails> postDetailsList = new ArrayList<>();
                if (queryDocumentSnapshots.size() != 0) {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        PostDetails postDetails = doc.toObject(PostDetails.class);
                        if (postDetails != null) {
                            postDetails.setDocId(doc.getId());
                            postDetailsList.add(postDetails);
                        }
                    }
                    responseForQuery(postDetailsList);
                    getSorted(postDetailsList, sortOrder);
                    if (mFirebaseUser != null) {
                        integrateBookmarkInfo(postDetailsList);
                        stopRefreshing();
                    } else {
                        attachAdapter(postDetailsList);
                        stopRefreshing();
                    }
                } else {
                    attachAdapter(postDetailsList);
                    stopRefreshing();
                }
            }
        });
    }

    /**
     * Initializes the views required by the feedFill methods
     */
    private void initView() {
        postDetailsList.clear();
        recyclerView = globalView.findViewById(R.id.home_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(globalView.getContext()));
        recyclerView.setItemViewCacheSize(10);
    }

    /**
     * Displays 'Nothing' TextView if list is empty
     */
    private void responseForQuery(List<PostDetails> postDetailsList) {
        if (postDetailsList.size() == 0) tvNone.setVisibility(View.VISIBLE);
        else tvNone.setVisibility(View.GONE);
    }

    /**
     * Stops the UI Refresh action of the feed
     */
    private void stopRefreshing() {
        if (isRefreshing) {
            swipeLayout.setRefreshing(false);
            isRefreshing = false;
        }
    }

    /**
     * Sets Boomarked=>True if it is a bookmarked post
     */
    private void integrateBookmarkInfo(final List<PostDetails> postDetailsList) {
        userCollection.document(mFirebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                UserDetails userDetails = task.getResult().toObject(UserDetails.class);
                bookmarkIds = userDetails != null ? userDetails.getBookmarkIds() : null;
                for (PostDetails posts : postDetailsList) {
                    if (bookmarkIds.contains(posts.getDocId())) posts.setBookmarked(true);
                    else posts.setBookmarked(false);
                }
                attachAdapter(postDetailsList);
            }
        });
    }

    /**
     * Attaches the Recycler Adapter
     */
    private void attachAdapter(List<PostDetails> postDetailsList) {
        PostAdapter postAdapter = new PostAdapter(globalView.getContext(), postDetailsList);
        recyclerView.setAdapter(postAdapter);
    }

    /**
     * Sorts according to Timestamp
     */
    private void getSorted(List<PostDetails> postDetailsList, final String sortOrder) {
        Collections.sort(postDetailsList, new Comparator<PostDetails>() {
            @Override
            public int compare(PostDetails o1, PostDetails o2) {
                if (sortOrder.equals("Descending"))
                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                else return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });
    }

    /**
     * Helper method to get the current location of the user (Used is Radius Filter)
     */
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations, this can be null.
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    helperClass.displayToast(R.string.location_detected);
                }
            }
        });
    }

    /**
     * Fills the AutoCompleteTextView in Filter with usernames
     */
    private void fillActvUser() {
        actvUser.setHint(R.string.filter_user);
        final ArrayList<String> users_list = new ArrayList<>();
        userCollection.orderBy("name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {
                    users_list.add(snapshot.get("name").toString());
                    userWithEmailIdHashMap.put(snapshot.get("name").toString(), snapshot.getId());
                }
                String[] userArr = new String[users_list.size()];
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, users_list.toArray(userArr));
                actvUser.setThreshold(1);
                actvUser.setAdapter(adapter);
            }
        });
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        globalView = view;
        tvNone = view.findViewById(R.id.home_tv_none);

        // Uses SharedPreferences to display the first time open Welcome Card
        cardWelcome = view.findViewById(R.id.home_card_welcome);
        if (sharedPreferences.getBoolean("isFirstTime", true)) {
            cardWelcome.setVisibility(View.VISIBLE);
            SharedPreferences.Editor sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            sharedPreferencesEditor.putBoolean("isFirstTime", false);
            sharedPreferencesEditor.apply();
        } else cardWelcome.setVisibility(View.GONE);

        postDetailsList = new ArrayList<>();

        feedFillBasic("Descending");
        swipeLayout = view.findViewById(R.id.home_swipe_refresh);
        swipeLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onRefresh() {
        cardWelcome.setVisibility(View.GONE);
        postDetailsList = new ArrayList<>();
        isRefreshing = true;
        feedFillBasic("Descending");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        // Returns response from AddPost Activity (To refresh the Homepage)
        if (requestCode == ADDPREQCODE && resultCode == RESULT_OK) onRefresh();
    }
}