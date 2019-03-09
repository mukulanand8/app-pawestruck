package com.buffhello.pawestruck;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


/**
 * For the user to Login, Sign Up or Reset Password
 */
public class SignInActivity extends AppCompatActivity {

    private static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * isLogin to check if the display specifies Login or Sign Up,
     * hasForgot to check if the display specifies Forgot Password or not
     */
    boolean isLogin = true, hasForgot = false;

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private FirebaseAuth mAuth;
    private HelperClass helperClass;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mContext = getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        mSharedPreferences = getSharedPreferences("mSharedPreferences", MODE_PRIVATE);
        helperClass = new HelperClass(mContext);

        progressBar = findViewById(R.id.sign_progress);
        final TextView tvAccountYN = findViewById(R.id.sign_tv_has_account);
        final TextView tvForgotPassword = findViewById(R.id.sign_tv_forgot_pass);
        final EditText etUsername = findViewById(R.id.sign_et_name);
        final EditText etEmail = findViewById(R.id.sign_et_email);
        final TextInputLayout tilPassword = findViewById(R.id.sign_til_password);
        final TextInputEditText tietPassword = findViewById(R.id.sign_tiet_password);
        final Button buttonSignIn = findViewById(R.id.sign_button_signin);

        // Screen values for View animation
        int maxWidth = getResources().getConfiguration().screenWidthDp;
        final float left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -maxWidth, getResources().getDisplayMetrics());
        final float right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxWidth, getResources().getDisplayMetrics());
        final float mid = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());

        tvAccountYN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Brings up Views for Sign Up page */
                if (isLogin) {
                    etUsername.setTranslationX(left);
                    etUsername.setVisibility(View.VISIBLE);
                    tvForgotPassword.animate().translationX(right).setDuration(200).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            tvForgotPassword.setVisibility(View.GONE);
                        }
                    });
                    etUsername.animate().translationX(mid).setDuration(300);
                    tvAccountYN.setText(R.string.sign_have_account);
                    buttonSignIn.setText(R.string.sign_signup);
                    isLogin = false;
                }

                // Brings up Views for Login page
                else {
                    tvForgotPassword.setTranslationX(left);
                    tvForgotPassword.setVisibility(View.VISIBLE);
                    etUsername.animate().translationX(right).setDuration(200).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            etUsername.setVisibility(View.GONE);
                        }
                    });
                    tvForgotPassword.animate().translationX(mid).setDuration(300);
                    tvAccountYN.setText(R.string.sign_no_account);
                    buttonSignIn.setText(R.string.sign_login);
                    isLogin = true;
                }
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Brings up Views for Login page
                if (hasForgot) {
                    tilPassword.setTranslationX(left);
                    tilPassword.setVisibility(View.VISIBLE);
                    tilPassword.animate().translationX(mid).setDuration(300);
                    tvForgotPassword.setText(R.string.sign_forgot_password);
                    buttonSignIn.setText(R.string.sign_login);
                    tvAccountYN.setVisibility(View.VISIBLE);
                    hasForgot = false;
                }

                // Brings up Views for Reset Password page
                else {
                    tilPassword.animate().translationX(right).setDuration(200).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            tilPassword.setVisibility(View.GONE);
                        }
                    });
                    tvForgotPassword.setText(R.string.sign_remember_password);
                    buttonSignIn.setText(R.string.sign_reset_password);
                    tvAccountYN.setVisibility(View.GONE);
                    hasForgot = true;
                }
            }
        });

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new HelperClass(mContext).isNetworkAvailable()) {
                    String email = etEmail.getText().toString().trim();
                    String password = tietPassword.getText().toString().trim();
                    String username = etUsername.getText().toString().trim();

                    /* Checks if the button is for Login, Sign Up or Reset Password.
                     * "x" and "xxxxxxxx" are used to pass the validity check by default */
                    if (hasForgot && isLogin) {
                        email = etEmail.getText().toString().trim();
                        if (validCredentials(email, "xxxxxxxx", "x")) forgotPassword(email);
                    } else if (isLogin && validCredentials(email, password, "x"))
                        loginUser(email, password);
                    else if (validCredentials(email, password, username))
                        signUpUser(email, password, username);
                } else helperClass.displayToast(R.string.no_internet);
            }
        });

    }

    /**
     * Checks if the entered text can be accepted
     */
    private boolean validCredentials(String email, String password, String username) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            helperClass.displayToast(R.string.sign_bad_email);
            return false;
        }
        if (password.length() < PASSWORD_MIN_LENGTH) {
            helperClass.displayToast(R.string.sign_bad_password);
            return false;
        }
        if (username.length() < 1) {
            helperClass.displayToast(R.string.sign_bad_username);
            return false;
        }
        return true;
    }

    /**
     * Sends a link to the email address to reset password
     */
    private void forgotPassword(String email) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    helperClass.displayToast(R.string.sign_reset_password_sent);
                else helperClass.displayToast(task.getException().toString());
            }
        });
    }

    /**
     * Logs the user in if email and password match
     */
    private void loginUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.requestFocus();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Resets SharedPreferences in case another user had logged in
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("name", "");
                    editor.putString("emailID", "");
                    editor.putString("profilePicUrl", "");
                    editor.putString("phnum", "");
                    editor.putString("address", "");
                    editor.apply();

                    progressBar.setVisibility(View.GONE);
                    setResult(RESULT_OK);
                    finish();
                } else if (task.getException() instanceof FirebaseAuthInvalidUserException)
                    helperClass.displayToast(R.string.sign_incorrect);
                else helperClass.displayToast(task.getException().toString());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Creates an account and signs the user in
     */
    private void signUpUser(final String email, String password, final String username) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.requestFocus();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser mUser = mAuth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                    mUser.updateProfile(profileUpdates);

                    //Adds details to the user collection on the Firestore
                    UserDetails userDetails = new UserDetails(username, email);
                    FirebaseFirestore.getInstance().collection("users").document(mUser.getUid()).set(userDetails);

                    //Resets SharedPreferences for the new user
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("name", "");
                    editor.putString("emailID", "");
                    editor.putString("profilePicUrl", "");
                    editor.putString("phnum", "");
                    editor.putString("address", "");
                    editor.apply();

                    // Sends a link to the email address to verify the account
                    mUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            helperClass.displayToast(R.string.sign_email_verify);
                        }
                    });

                    progressBar.setVisibility(View.GONE);
                    setResult(RESULT_OK);
                    finish();
                } else helperClass.displayToast(task.getException().toString());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}