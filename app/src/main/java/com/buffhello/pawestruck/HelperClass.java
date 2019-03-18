package com.buffhello.pawestruck;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.util.ArrayList;

/**
 * Contains general modules for the app
 */
class HelperClass {

    private Context mContext;

    HelperClass(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Checks if Internet Connection (Mobile Data/WiFi) is available
     */
    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Checks if GPS is turned on
     */
    boolean isLocationAvailable() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null)
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return false;
    }

    /**
     * Displays the string resource as Toast for 3.5 seconds
     */
    void displayToast(int stringResource) {
        Toast.makeText(mContext, stringResource, Toast.LENGTH_LONG).show();
    }

    /**
     * Displays the message as Toast for 3.5 seconds
     */
    void displayToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows the clicked image in a popup. arrayList contains the images to show.
     * startPosition is the index of the image to popup from arrayList
     */
    void imagePopup(ArrayList<String> arrayList, int startPosition) {
        new StfalconImageViewer.Builder<>(mContext, arrayList, new ImageLoader<String>() {
            @Override
            public void loadImage(ImageView imageView, String image) {
                Glide.with(mContext).load(image).apply(new RequestOptions().placeholder(R.drawable.placeholder)).into(imageView);
            }
        }).withHiddenStatusBar(false).withStartPosition(startPosition).show();
    }

    /**
     * Hides the soft keyboard when clicked elsewhere
     */
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
