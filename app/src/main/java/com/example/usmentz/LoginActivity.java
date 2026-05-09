package com.example.usmentz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final String PREFS_NAME = "UsmentzPrefs";

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle;
    private TextView tvSignUp, tvForgotPassword, tvError;
    private ProgressBar progressBar;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupBottomSheet();
        setupGoogleSignIn();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvError = findViewById(R.id.tvError);
        progressBar = findViewById(R.id.progressBar);
    }

private void setupBottomSheet() {
        View slideCard = findViewById(R.id.slideCard);
        bottomSheetBehavior = BottomSheetBehavior.from(slideCard);

        // Peek at header area only (180dp) - shows the drag handle and "Welcome Back"
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Cannot be fully hidden - sheet always stays visible
        bottomSheetBehavior.setHideable(false);

        // Drag down to minimize, drag up to expand full form
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // STATE_COLLAPSED = showing header only (180dp)
                // STATE_EXPANDED = showing full form
                // Never hidden - always visible at minimum
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Natural slide - no extra effects
            }
        });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            goToHome();
            return;
        }

        // Email Login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                return;
            }

            loginWithEmail(email, password);
        });

        // Google Sign In
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Sign Up link
        tvSignUp.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }

    private void showForgotPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);

        com.google.android.material.textfield.TextInputLayout tilEmail = dialogView.findViewById(R.id.tilEmail);
        com.google.android.material.textfield.TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                tilEmail.setError("Required");
                return;
            }
            dialog.dismiss();
            resetPassword(email);
        });

        dialog.show();
    }

    private void loginWithEmail(String email, String password) {
        showLoading(true);
        hideError();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserSession(user);
                            goToHome();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            showError("Invalid email or password");
                        }
                    }
                });
    }

    private void signInWithGoogle() {
        showLoading(true);
        hideError();

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                showLoading(false);
                showError("Google sign in failed. Please try again.");
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserSession(user);
                            goToHome();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            showError("Authentication failed. Please try again.");
                        }
                    }
                });
    }

    private void resetPassword(String email) {
        showLoading(true);
        hideError();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Password reset email sent to " + email,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Failed to send reset email. Check your email address.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserSession(FirebaseUser user) {
        if (user != null) {
            sharedPreferences.edit()
                    .putString("user_id", user.getUid())
                    .putString("user_email", user.getEmail())
                    .putString("user_name", user.getDisplayName() != null ? user.getDisplayName() : "")
                    .putString("user_photo", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "")
                    .putBoolean("is_logged_in", true)
                    .apply();
        }
    }

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnGoogle.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnGoogle.setEnabled(true);
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
}
