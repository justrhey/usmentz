package com.example.usmentz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final String PREFS_NAME = "UsmentzPrefs";

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnCreateAccount;
    private MaterialButton btnGoogle;
    private TextView tvLogin, tvError;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupGoogleSignIn();
        setupClickListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvLogin = findViewById(R.id.tvLogin);
        tvError = findViewById(R.id.tvError);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Create Account
        btnCreateAccount.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Name is required");
                return;
            }
            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                return;
            }
            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }

            createAccount(name, email, password);
        });

        // Google Sign Up
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Login link
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void createAccount(String name, String email, String password) {
        showLoading(true);
        hideError();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Send verification email
                                user.sendEmailVerification();
                            }
                            saveUserSession(user, name);
                            Toast.makeText(RegisterActivity.this,
                                    "Account created! Please verify your email.",
                                    Toast.LENGTH_LONG).show();
                            goToHome();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            showError(getErrorMessage(task.getException()));
                            showLoading(false);
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
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserSession(user, user.getDisplayName());
                            goToHome();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            showError("Authentication failed. Please try again.");
                            showLoading(false);
                        }
                    }
                });
    }

    private String getErrorMessage(Exception e) {
        if (e == null) return "Registration failed";
        String message = e.getMessage();
        if (message == null) return "Registration failed";
        if (message.contains("email already")) return "This email is already registered";
        if (message.contains("weak")) return "Password is too weak";
        if (message.contains("invalid")) return "Invalid email address";
        return "Registration failed. Please try again.";
    }

    private void saveUserSession(FirebaseUser user, String name) {
        if (user != null) {
            sharedPreferences.edit()
                    .putString("user_id", user.getUid())
                    .putString("user_email", user.getEmail())
                    .putString("user_name", name != null ? name : (user.getDisplayName() != null ? user.getDisplayName() : ""))
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
            btnCreateAccount.setEnabled(false);
            btnGoogle.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnCreateAccount.setEnabled(true);
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