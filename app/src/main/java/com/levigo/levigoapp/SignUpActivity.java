package com.levigo.levigoapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseFirestore levigoDb = FirebaseFirestore.getInstance();
    private CollectionReference invitationCodesRef = levigoDb.collection("invitation_codes");
    private CollectionReference networksRef = levigoDb.collection("networks");

    private LinearLayout emailPasswordLayout;
    private Button submitInvitationCode;
    private TextInputLayout invitationCodeLayout;
    private TextInputEditText invitationCodeBox;
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    private TextInputEditText confirmPasswordField;
    private Button signUpButton;
    private TextView networkNameTextView;
    private TextView siteNameTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        networkNameTextView = findViewById(R.id.signup_network_name);
        siteNameTextView = findViewById(R.id.signup_site_name);

        emailPasswordLayout = findViewById(R.id.signup_email_password_layout);
        emailField = findViewById(R.id.signup_email);
        passwordField = findViewById(R.id.signup_password);
        confirmPasswordField = findViewById(R.id.signup_password_confirm);
        signUpButton = findViewById(R.id.signup_button);

        emailPasswordLayout.setVisibility(View.GONE);
        signUpButton.setEnabled(false);

        submitInvitationCode = findViewById(R.id.submit_invitation_code_button);
        submitInvitationCode.setEnabled(false);
        invitationCodeLayout = findViewById(R.id.textInputLayout_invitationCode);
        invitationCodeBox = findViewById(R.id.et_InvitationCode);

        // Disable submit invitation code when it's empty
        invitationCodeBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    submitInvitationCode.setEnabled(false);
                } else {
                    submitInvitationCode.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        submitInvitationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String invitationCode = invitationCodeBox.getText().toString();
                Log.d(TAG, "INVITATION CODE: " + invitationCode);

                final DocumentReference docRef = invitationCodesRef.document(invitationCode);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                                String networkId = document.get("network").toString();
//                                String siteId = document.get("site").toString();
//                                Log.d(TAG, "====================" + networksRef.document(networkId).get().toString());
//                                Log.d(TAG, networkId + "|" + siteId);

                                networkNameTextView.setText(document.get("network_name").toString());
                                siteNameTextView.setText(document.get("site_name").toString());
                                emailPasswordLayout.setVisibility(View.VISIBLE);
                                invitationCodeLayout.setEnabled(false);
                            } else {
                                // TODO add notification for invalid invitation code
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
        });

//        ImwdlM5c1FoqpDwbsRSU
        TextWatcher emailPasswordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String e = emailField.getText().toString();
                String p = passwordField.getText().toString();
                String cp = confirmPasswordField.getText().toString();
//                Log.d(TAG, e + "|" + p + "|" + cp);

                if (!p.equals(cp)){
                    signUpButton.setEnabled(false);
                    //TODO display warning sign next to confirm password
                }

                if (e.length() == 0 || p.length() == 0 || cp.length()==0){
                    signUpButton.setEnabled(false);
                } else {
                    signUpButton.setEnabled(true);
                }
            }
        };
        emailField.addTextChangedListener(emailPasswordWatcher);
        passwordField.addTextChangedListener(emailPasswordWatcher);
        confirmPasswordField.addTextChangedListener(emailPasswordWatcher);
        
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password);

                //TODO sign in user; delete invitation code
/*
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
//                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(getApplicationContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
//                                    updateUI(null);
                                }

                                // ...
                            }
                        });
*/
            }
        });
    }
}
