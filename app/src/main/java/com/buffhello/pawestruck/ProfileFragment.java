package com.buffhello.pawestruck;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import static android.content.Context.MODE_PRIVATE;
import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;

/**
 * Displays information about the user
 */
public class ProfileFragment extends Fragment {

    private final int MOBILE_REQUEST = 2, PLACE_REQUEST = 3, DP_SIZE = 450;

    private Context mContext;
    private HelperClass helperClass;
    private FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();
    private DocumentReference userDocument = FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(mFirebaseUser).getUid());
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private Uri mCropImageUri;
    private SharedPreferences mSharedPreferences;

    private ImageView profilePic;
    private ProgressBar progressBar;
    private TextView tvName, tvEmail, tvPhone, tvAddress;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        helperClass = new HelperClass(mContext);
        setHasOptionsMenu(true);
        Places.initialize(mContext, getString(R.string.google_maps_key));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        AppBarLayout appBarLayout = (AppBarLayout) toolbar.getParent();
        appBarLayout.setExpanded(true, false);

        if (mFirebaseUser != null) mFirebaseUser.reload();
        mSharedPreferences = getActivity().getSharedPreferences("mSharedPreferences", MODE_PRIVATE);
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        Button buttonSignOut = view.findViewById(R.id.prof_button_signout);
        ImageView ivPhoneVerify = view.findViewById(R.id.prof_iv_edit_phone);
        ImageView ivLocationVerify = view.findViewById(R.id.prof_iv_edit_address);
        ImageView ivEmailVerified = view.findViewById(R.id.prof_iv_email_status);
        FloatingActionButton changeProfilePic = view.findViewById(R.id.prof_fab_dp_change);
        profilePic = view.findViewById(R.id.prof_iv_dp);
        tvName = view.findViewById(R.id.prof_tv_name);
        tvEmail = view.findViewById(R.id.prof_tv_email);
        tvPhone = view.findViewById(R.id.prof_tv_phone);
        tvAddress = view.findViewById(R.id.prof_tv_address);
        progressBar = view.findViewById(R.id.prof_progress);
        if (mFirebaseUser.isEmailVerified())
            ivEmailVerified.setImageDrawable(getResources().getDrawable(R.drawable.prof_email_verified));
        else
            ivEmailVerified.setImageDrawable(getResources().getDrawable(R.drawable.prof_email_unverified));

        int maxWidth = getResources().getConfiguration().screenWidthDp;
        tvEmail.setMaxWidth(maxWidth);
        tvPhone.setMaxWidth(maxWidth);
        tvAddress.setMaxWidth(maxWidth);

        /* If SharedPreferences isn't available, get info from the user's document in Firestore.
        Else get info from SharedPreferences */
        if (mSharedPreferences.getString("name", "").equals("")) {
            userDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        UserDetails userDetails = task.getResult().toObject(UserDetails.class);
                        if (userDetails != null) {
                            tvName.setText(userDetails.getName());
                            tvEmail.setText(userDetails.getEmailId());
                            if (userDetails.getPhoneNum() != null)
                                tvPhone.setText(userDetails.getPhoneNum());
                            if (userDetails.getAddress() != null)
                                tvAddress.setText(userDetails.getAddress());
                            editor.putString("name", userDetails.getName());
                            editor.putString("emailID", userDetails.getEmailId());
                            editor.putString("phnum", userDetails.getPhoneNum());
                            editor.putString("address", userDetails.getAddress());
                            editor.apply();
                            if (userDetails.getProfilePicUrl() != null) {
                                Glide.with(mContext).load(userDetails.getProfilePicUrl()).apply(new RequestOptions().placeholder(R.drawable.profile).override(DP_SIZE).circleCrop()).into(profilePic);
                                editor.putString("profilePicUrl", userDetails.getProfilePicUrl());
                                editor.apply();
                            }
                        }
                    }
                }
            });
        } else {
            tvName.setText(mSharedPreferences.getString("name", ""));
            tvEmail.setText(mSharedPreferences.getString("emailID", ""));
            tvPhone.setText(mSharedPreferences.getString("phnum", "-"));
            tvAddress.setText(mSharedPreferences.getString("address", "-"));
            if (!mSharedPreferences.getString("profilePicUrl", "").equals(""))
                Glide.with(getActivity().getApplicationContext()).load(mSharedPreferences.getString("profilePicUrl", "")).apply(new RequestOptions().placeholder(R.drawable.profile).override(DP_SIZE).circleCrop()).into(profilePic);
        }

        // Pops up profile picture if available
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSharedPreferences.getString("profilePicUrl", "").equals("")) {
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(mSharedPreferences.getString("profilePicUrl", ""));
                    helperClass.imagePopup(arrayList, 0);
                }
            }
        });

        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.content_frame, new HomeFragment());
                ft.commit();
            }
        });


        ivPhoneVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), OTPActivity.class);
                startActivityForResult(i, MOBILE_REQUEST);
            }
        });

        // Opens Place Autocomplete Activity with just Indian places
        ivLocationVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helperClass.isNetworkAvailable()) {
                    List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).setCountry("IN").build(mContext);
                    startActivityForResult(intent, PLACE_REQUEST);
                } else helperClass.displayToast(R.string.no_internet);
            }
        });

        // Opens Image Cropper Activity
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCropImageActivity(null);
                if (!helperClass.isNetworkAvailable())
                    helperClass.displayToast(R.string.prof_no_internet_dp);
            }
        });

        return view;
    }

    /**
     * Uploads image to Storage with filename as the user's ID
     */
    private void uploadImage(Uri filePath) {
        progressBar.setVisibility(View.VISIBLE);
        if ((filePath != null)) {
            final StorageReference ref = mStorageReference.child("userDp/" + mFirebaseUser.getUid());
            ref.putFile(filePath).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull final Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final String imageUri = task.getResult().toString();

                        // Updates user's document with image URL
                        userDocument.update("profilePicUrl", imageUri).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putString("profilePicUrl", imageUri);
                                editor.apply();
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Image Cropper external library
     */
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1).setRequestedSize(1080, 1080).setAutoZoomEnabled(true).setMultiTouchEnabled(false).setActivityTitle(getResources().getString(R.string.title_choose_image_dp)).start(mContext, this);
    }

    /**
     * Starts Image Cropper if Storage permissions were granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startCropImageActivity(mCropImageUri);
        else helperClass.displayToast(R.string.allow_permission_storage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handles result of Image Chooser
        int IMAGE_REQ = 1;
        if (requestCode == IMAGE_REQ && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                Uri imageUri = CropImage.getPickImageResultUri(mContext, data);

                // Requests permissions and handles the result in onRequestPermissionsResult()
                if (CropImage.isReadExternalStoragePermissionsRequired(mContext, imageUri)) {
                    mCropImageUri = imageUri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                } else startCropImageActivity(imageUri);
            }
        }

        // Handles result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), result.getUri());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                    Glide.with(mContext).load(bitmap).apply(new RequestOptions().placeholder(R.drawable.placeholder).override(DP_SIZE).circleCrop()).into(profilePic);
                    uploadImage(result.getUri());
                } catch (Exception e) {
                    helperClass.displayToast(e.toString());
                }
            }
        }

        // Handles result of OTPActivity
        if (requestCode == MOBILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("phnum", mFirebaseUser.getPhoneNumber());
                editor.apply();
                tvPhone.setText(mFirebaseUser.getPhoneNumber());
            }
        }

        // Handles result of Place Autocomplete
        if (requestCode == PLACE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                String address = place.getAddress();
                LatLng coOrdinates = place.getLatLng();
                GeoPoint geoPoint = new GeoPoint(coOrdinates.latitude, coOrdinates.longitude);
                userDocument.update("address", address);
                userDocument.update("coOrdinates", geoPoint);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("address", address);
                editor.apply();
                tvAddress.setText(address);
            }
        }

    }

}

