package com.buffhello.pawestruck;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Appears when a signed in user clicks '+' on Home Fragment
 */
public class AddPostActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_REQ = 1, PLACE_REQUEST = 2;

    private static int imageindex = 0;
    private boolean isDoubleBack = false;
    private String petDocId = null, address = null, imageUri = null;
    private ArrayList<Uri> filepaths = new ArrayList<>();
    private Uri mCropImageUri;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private GeoPoint geoPoint;

    private HelperClass helperClass;
    private FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private CollectionReference petCollection = mFirestore.collection("pets");
    private CollectionReference userCollection = mFirestore.collection("users");
    private StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();

    private ProgressBar progressBar;
    private ImageView iv1, iv2, iv3, iv4, ivToSet, ivToVisible;
    private TextView tvPhone, tvAddress, tvEmail;
    private EditText etDescription;
    private Spinner spinnerAnimal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        Places.initialize(this, getString(R.string.google_maps_key));
        helperClass = new HelperClass(this);
        progressBar = findViewById(R.id.addp_progress);
        tvPhone = findViewById(R.id.addp_tv_phone);
        tvAddress = findViewById(R.id.addp_tv_address);
        tvEmail = findViewById(R.id.addp_tv_email);
        iv1 = findViewById(R.id.addp_iv_1);
        iv2 = findViewById(R.id.addp_iv_2);
        iv3 = findViewById(R.id.addp_iv_3);
        iv4 = findViewById(R.id.addp_iv_4);
        etDescription = findViewById(R.id.addp_et_description);
        spinnerAnimal = findViewById(R.id.addp_spinner_animal);
        Button buttonPost = findViewById(R.id.addp_button_post);
        ImageView ivLocationEdit = findViewById(R.id.addp_iv_edit_address);
        ConstraintLayout parent = findViewById(R.id.addp_parent);

        int maxWidth = getResources().getConfiguration().screenWidthDp;
        tvEmail.setMaxWidth(maxWidth);
        tvPhone.setMaxWidth(maxWidth);
        tvAddress.setMaxWidth(maxWidth);

        buttonPost.setOnClickListener(this);
        ivLocationEdit.setOnClickListener(this);
        iv1.setOnClickListener(this);
        iv2.setOnClickListener(this);
        iv3.setOnClickListener(this);
        iv4.setOnClickListener(this);
        parent.setOnClickListener(this);

        // Checks if user's profile has phone number and address
        userCollection.document(mFirebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                UserDetails userDetails = task.getResult().toObject(UserDetails.class);
                if (userDetails.getPhoneNum() != null) tvPhone.setText(userDetails.getPhoneNum());
                if (userDetails.getAddress() != null) tvAddress.setText(userDetails.getAddress());
                tvEmail.setText(userDetails.getEmailId());
                address = userDetails.getAddress();
                geoPoint = userDetails.getCoOrdinates();
            }
        });

    }

    /**
     * Creates a Firestore document, uploads images to Storage and adds post to user's bookmarks
     */
    private void uploadPost() {
        String description = etDescription.getText().toString();
        String animal = spinnerAnimal.getSelectedItem().toString();

        if ((filepaths.size() != 0) && !description.equals("") && !tvAddress.getText().toString().equals("") && !tvAddress.getText().toString().equals("-")) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.requestFocus();
            final PostDetails postDetails = new PostDetails(new ArrayList<String>(), description, mFirebaseUser.getUid(), address, new Timestamp(new Date()), geoPoint, animal, false);

            // Adds to Firestore
            petCollection.add(postDetails).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    petDocId = documentReference.getId();
                    DocumentReference userDocument = userCollection.document(mFirebaseUser.getUid());

                    // Bookmarks the post
                    userDocument.update("bookmarkIds", FieldValue.arrayUnion(documentReference.getId())).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // Adds each image to Storage
                            for (final Uri uri : filepaths) {
                                final StorageReference ref = mStorageReference.child("petPics/" + mFirebaseUser.getUid() + "_" + new Date().getTime());
                                Bitmap bitmap = null;
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                                ref.putBytes(baos.toByteArray()).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }
                                        return ref.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            imageUri = task.getResult().toString();
                                            DocumentReference postDocument = petCollection.document(petDocId);

                                            // Updates the post with image URLs
                                            postDocument.update("photoUrls", FieldValue.arrayUnion(imageUri)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    postDetails.getPhotoUrls().add(imageUri);

                                                    // Exits only when all images are uploaded
                                                    while (postDetails.getPhotoUrls().size() == filepaths.size()) {
                                                        progressBar.setVisibility(View.GONE);
                                                        setResult(RESULT_OK);
                                                        filepaths.clear();
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });
        } else if (filepaths.size() == 0) helperClass.displayToast(R.string.addp_error_image);
        else if (description.equals("")) helperClass.displayToast(R.string.addp_error_description);
        else if (tvAddress.getText().toString().equals("") || tvAddress.getText().toString().equals("-"))
            helperClass.displayToast(R.string.addp_error_location);
    }

    /**
     * Image Cropper external library
     */
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).setRequestedSize(1080, 1080).setAutoZoomEnabled(true).setMultiTouchEnabled(false).setActivityTitle(getResources().getString(R.string.title_choose_image_pet)).start(this);
    }

    /**
     * Images start Image Cropper, Edit Location starts PlacePicker, Post starts uploadPost()
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addp_button_post) {
            if (helperClass.isNetworkAvailable()) uploadPost();
            else helperClass.displayToast(R.string.no_internet);
        } else if (v.getId() == R.id.addp_iv_edit_address) {
            if (helperClass.isNetworkAvailable()) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).setCountry("IN").build(this);
                startActivityForResult(intent, PLACE_REQUEST);
            } else helperClass.displayToast(R.string.no_internet);
        } else if (v.getId() == R.id.addp_iv_1) {
            ivToSet = iv1;
            ivToVisible = iv2;
            imageindex = 0;
            startCropImageActivity(null);
        } else if (v.getId() == R.id.addp_iv_2) {
            ivToSet = iv2;
            ivToVisible = iv3;
            imageindex = 1;
            startCropImageActivity(null);
        } else if (v.getId() == R.id.addp_iv_3) {
            ivToSet = iv3;
            ivToVisible = iv4;
            imageindex = 2;
            startCropImageActivity(null);
        } else if (v.getId() == R.id.addp_iv_4) {
            ivToSet = iv4;
            imageindex = 3;
            startCropImageActivity(null);
        } else if (v.getId() == R.id.addp_parent) {
            helperClass.hideKeyboard(v);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handles result of Image Chooser
        if (requestCode == IMAGE_REQ && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                Uri imageUri = CropImage.getPickImageResultUri(this, data);

                // Requests permissions and handles the result in onRequestPermissionsResult()
                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
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
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());
                    ivToSet.setImageBitmap(bitmap);
                    ivToVisible.setVisibility(View.VISIBLE);

                    if (!filepaths.contains(result.getUri())) {
                        if (imageindex >= filepaths.size())
                            filepaths.add(imageindex, result.getUri());
                        else {
                            filepaths.remove(imageindex);
                            filepaths.add(imageindex, result.getUri());
                        }
                    }
                } catch (Exception e) {
                    helperClass.displayToast(e.toString());
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
                helperClass.displayToast(result.getError().toString());
        }

        // Handles result of Places Autocomplete
        if (requestCode == PLACE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                address = place.getAddress();
                LatLng coOrdinates = place.getLatLng();
                geoPoint = new GeoPoint(coOrdinates.latitude, coOrdinates.longitude);
                tvAddress.setText(address);
            }
        }
    }

    /**
     * Starts Image Cropper if Storage permissions are granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startCropImageActivity(mCropImageUri);
        else helperClass.displayToast(R.string.allow_permission_storage);
    }

    /**
     * On double click within 2 seconds- Exit Activity
     */
    @Override
    public void onBackPressed() {
        if (isDoubleBack) {
            super.onBackPressed();
            return;
        }
        isDoubleBack = true;
        Toast.makeText(this, R.string.addp_exit, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isDoubleBack = false;
            }
        }, 2000);
    }

}