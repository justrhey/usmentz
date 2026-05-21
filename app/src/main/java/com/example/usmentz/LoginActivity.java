package com.example.usmentz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.usmentz.databinding.ActivityLoginBinding;
import com.example.usmentz.databinding.DialogForgotPasswordBinding;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final String PREFS_NAME = "UsmentzPrefs";

    private ActivityLoginBinding binding;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupBottomSheet();
        setupGoogleSignIn();
        setupClickListeners();
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.slideCard);

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
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                binding.etEmail.setError("Email is required");
                return;
            }
            if (password.isEmpty()) {
                binding.etPassword.setError("Password is required");
                return;
            }

            loginWithEmail(email, password);
        });

        // Google Sign In
        binding.btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Sign Up link
        binding.tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // Forgot password
        binding.tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }

    private void showForgotPasswordDialog() {
        DialogForgotPasswordBinding dialogBinding = DialogForgotPasswordBinding.inflate(getLayoutInflater());

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSubmit.setOnClickListener(v -> {
            String email = dialogBinding.etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                dialogBinding.tilEmail.setError("Required");
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
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);
            binding.btnGoogle.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            binding.btnGoogle.setEnabled(true);
        }
    }

    private void showError(String message) {
        binding.tvError.setText(message);
        binding.tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.tvError.setVisibility(View.GONE);
    }
}
