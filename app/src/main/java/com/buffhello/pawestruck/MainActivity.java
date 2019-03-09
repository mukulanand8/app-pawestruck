package com.buffhello.pawestruck;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * Consists of the behaviour of the menu including fragment changes
 */
public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    /**
     * requestCode for SignInActivity
     */
    private static final int RC_SIGNIN = 2;

    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private HelperClass helperClass;

    private boolean isMenuOpen = false;
    private boolean isDoubleBack = false;
    private AppBarLayout appBarLayout;
    private FloatingActionButton fabMenu, fabProfile, fabHome, fabBookmarks, fabSupport;
    private Rect rectProfile, rectHome, rectBookmarks, rectSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Updates mUser when a user signs in or signs out
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = firebaseAuth.getCurrentUser();
            }
        };

        init();

        // Goes to HomeFragment on opening the app
        changeFragment(fabHome);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Updates Firebase variables
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    /**
     * Initializes the variables in MainActivity
     */
    private void init() {
        helperClass = new HelperClass(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        appBarLayout = (AppBarLayout) toolbar.getParent();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        fabMenu = findViewById(R.id.main_fab_menu);
        fabProfile = findViewById(R.id.main_fab_profile);
        fabHome = findViewById(R.id.main_fab_home);
        fabBookmarks = findViewById(R.id.main_fab_bookmarks);
        fabSupport = findViewById(R.id.main_fab_support);

        rectProfile = new Rect();
        rectHome = new Rect();
        rectBookmarks = new Rect();
        rectSupport = new Rect();

        fabMenu.setOnTouchListener(this);
        fabProfile.setOnTouchListener(this);
        fabHome.setOnTouchListener(this);
        fabBookmarks.setOnTouchListener(this);
        fabSupport.setOnTouchListener(this);
    }

    /**
     * Takes to the Fragment corresponding to the FloatingActionButton.
     * Takes to SignInActivity if a user with no account goes to Profile or Bookmarks Fragment
     */
    private void changeFragment(FloatingActionButton fab) {
        String tag = "";
        Fragment frag = null;
        if (fab.equals(fabProfile)) {
            if (mUser != null) {
                frag = new ProfileFragment();
                tag = "Profile";
            } else {
                closeMenu();
                helperClass.displayToast(R.string.acc_needed_profile);
                fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                Intent intent = new Intent(this, SignInActivity.class);
                startActivityForResult(intent, RC_SIGNIN);
            }
        } else if (fab.equals(fabHome)) {
            frag = new HomeFragment();
            tag = "Home";
        } else if (fab.equals(fabBookmarks)) {
            if (mUser != null) {
                frag = new BookmarksFragment();
                tag = "Bookmarks";
            } else {
                closeMenu();
                helperClass.displayToast(R.string.acc_needed_bookmark);
                fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                Intent intent = new Intent(this, SignInActivity.class);
                startActivityForResult(intent, RC_SIGNIN);
            }
        } else if (fab.equals(fabSupport)) {
            frag = new SupportFragment();
            tag = "Support";
        }

        if (frag != null) {
            appBarLayout.setExpanded(true, false);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, frag, tag);
            ft.commit();
            closeMenu();
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }
    }

    /**
     * Updates the boundaries (Rect) of the FloatingActionButtons and sets up to listen for events
     */
    private void touchHandleCollection(MotionEvent event) {
        fabProfile.getGlobalVisibleRect(rectProfile);
        fabHome.getGlobalVisibleRect(rectHome);
        fabBookmarks.getGlobalVisibleRect(rectBookmarks);
        fabSupport.getGlobalVisibleRect(rectSupport);

        touchHandle(event, rectProfile, fabProfile);
        touchHandle(event, rectHome, fabHome);
        touchHandle(event, rectBookmarks, fabBookmarks);
        touchHandle(event, rectSupport, fabSupport);
    }

    /**
     * Highlights the button on hover and calls changeFragment(fab) on release
     */
    private void touchHandle(MotionEvent event, Rect rect, FloatingActionButton fab) {
        if (rect.contains((int) event.getRawX(), (int) event.getRawY()) && isMenuOpen) {
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryLight)));
            if (event.getAction() == MotionEvent.ACTION_UP) changeFragment(fab);
        } else
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
    }

    /**
     * Contains visual changes- Animation, Opacity, Colour, Drawable- and updates isMenuOpen when menu is opened
     */
    private void openMenu() {
        float x1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, getResources().getDisplayMetrics());
        float x2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 31, getResources().getDisplayMetrics());
        float y1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -18, getResources().getDisplayMetrics());
        float y2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -60, getResources().getDisplayMetrics());
        fabMenu.setAlpha(1f);
        fabMenu.setImageResource(R.drawable.menu_open);
        fabProfile.animate().translationX(-x1).translationY(y1).setDuration(200).alpha(1);
        fabHome.animate().translationX(-x2).translationY(y2).setDuration(200).alpha(1);
        fabBookmarks.animate().translationX(x2).translationY(y2).setDuration(200).alpha(1);
        ViewCompat.animate(fabSupport).translationX(x1).translationY(y1).setDuration(200).alpha(1).withEndAction(new Runnable() {
            @Override
            public void run() {
                isMenuOpen = true;
            }
        });
    }

    /**
     * Contains visual changes- Animation, Opacity, Colour, Drawable- and updates isMenuOpen when menu is closed
     */
    private void closeMenu() {
        isMenuOpen = false;
        fabProfile.animate().translationX(0).translationY(-30).setDuration(200).alpha(0);
        fabHome.animate().translationX(0).translationY(-30).setDuration(200).alpha(0);
        fabBookmarks.animate().translationX(0).translationY(-30).setDuration(200).alpha(0);
        fabSupport.animate().translationX(0).translationY(-30).setDuration(200).alpha(0);
        fabMenu.setImageResource(R.drawable.menu_closed);
        fabMenu.setAlpha(0.8f);
    }

    /**
     * Sets up the FloatingActionButtons to listen when any one in pressed
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.main_fab_menu) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isMenuOpen) closeMenu();
                else openMenu();
                return true;
            }
            touchHandleCollection(event);
            return true;
        } else {
            touchHandleCollection(event);
            return true;
        }
    }

    /**
     * On single back pressed elsewhere- brings up HomeFragment.
     * If current fragment is HomeFragment, one click- Toast, double click within 2 seconds- Exit
     */
    @Override
    public void onBackPressed() {
        appBarLayout.setExpanded(true, false);
        Fragment frag = getSupportFragmentManager().findFragmentByTag("Home");
        if (frag != null && frag.isVisible()) {
            if (isDoubleBack) {
                super.onBackPressed();
                return;
            }
            isDoubleBack = true;
            Toast.makeText(getApplicationContext(), R.string.home_exit, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isDoubleBack = false;
                }
            }, 2000);
        } else changeFragment(fabHome);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Goes to Profile Fragment on success and Bookmarks Fragment on failure
        if (requestCode == RC_SIGNIN) {
            Fragment frag;
            if (resultCode == RESULT_OK) frag = new ProfileFragment();
            else frag = new HomeFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, frag);
            ft.commit();
        }
    }

}
