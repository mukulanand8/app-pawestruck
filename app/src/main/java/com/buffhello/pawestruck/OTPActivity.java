package com.buffhello.pawestruck;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * To set, change and verify mobile number
 */
public class OTPActivity extends AppCompatActivity {

    /**
     * verificationCode is the number (otp) that is sent and otp is the number entered by the user
     */
    private String phoneNumber, otp, verificationCode;
    private boolean isCodeSent = false;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private FirebaseUser mUser;
    private DocumentReference documentReference;
    private HelperClass helperClass;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        Context mContext = getApplicationContext();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        documentReference = FirebaseFirestore.getInstance().collection("users").document(mUser.getUid());
        helperClass = new HelperClass(mContext);

        final CountryCodePicker ccp = findViewById(R.id.otp_country_picker);
        final EditText etOTP = findViewById(R.id.otp_et_code);
        progressBar = findViewById(R.id.otp_progress);
        EditText etMobile = findViewById(R.id.otp_et_mobile);
        Button buttonSend = findViewById(R.id.otp_button_send);
        Button buttonVerify = findViewById(R.id.otp_button_verify);

        // Links EditText mobile number field to Country Code Picker
        ccp.registerCarrierNumberEditText(etMobile);

        firebaseVerification();

        // Sends the OTP if Internet is available and entered mobile number is valid
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helperClass.isNetworkAvailable()) {
                    if (ccp.isValidFullNumber()) {
                        phoneNumber = ccp.getFullNumberWithPlus();
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.requestFocus();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, OTPActivity.this, mCallback);
                    } else helperClass.displayToast(R.string.otp_invalid_number);
                } else helperClass.displayToast(R.string.no_internet);
            }
        });

        // Checks if Internet is available, a code has been sent and entered OTP matches
        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helperClass.isNetworkAvailable()) {
                    otp = etOTP.getText().toString();
                    if (verificationCode != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.requestFocus();
                        link(PhoneAuthProvider.getCredential(verificationCode, otp));
                    } else helperClass.displayToast(R.string.otp_null);
                } else helperClass.displayToast(R.string.no_internet);
            }
        });
    }

    /**
     * Handles OTP-related functions (with callback variable mCallback)
     */
    private void firebaseVerification() {
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            /** Handles Instant Verification i.e. verification without sending OTP */
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                if (!isCodeSent) link(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                /** Assigns the code that was sent */
                verificationCode = s;
                isCodeSent = true;
                helperClass.displayToast(R.string.otp_sent);
                progressBar.setVisibility(View.GONE);
            }
        };
    }

    /**
     * Links mobile number to the user and updates the user's details in Firestore.
     * Refer Toast messages for explanation of Exceptions
     */
    private void link(final PhoneAuthCredential credential) {
        mUser.linkWithCredential(credential).addOnCompleteListener(OTPActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    documentReference.update("phoneNum", phoneNumber);
                    helperClass.displayToast(R.string.otp_link_success);
                    progressBar.setVisibility(View.GONE);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Exception e = task.getException();
                    progressBar.setVisibility(View.GONE);
                    if (e instanceof FirebaseAuthInvalidCredentialsException)
                        helperClass.displayToast(R.string.otp_verification_failure);
                    else if (e instanceof FirebaseAuthUserCollisionException)
                        helperClass.displayToast(R.string.otp_link_failure);
                    else if (e.toString().contains("User has already been linked to the given provider.")) {
                        mUser.updatePhoneNumber(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    documentReference.update("phoneNum", phoneNumber);
                                    helperClass.displayToast(R.string.otp_link_success);
                                    setResult(RESULT_OK);
                                    finish();
                                } else helperClass.displayToast(task.getException().toString());
                            }
                        });
                    } else helperClass.displayToast(e.toString());
                }
            }
        });
    }

}
