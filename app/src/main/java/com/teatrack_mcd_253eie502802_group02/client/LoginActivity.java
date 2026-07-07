package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.admin.AdminDashboard;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.util.AdminSessionHelper;
import com.teatrack_mcd_253eie502802_group02.util.GoogleSignInHelper;
import com.teatrack_mcd_253eie502802_group02.util.UserIdGenerator;
import com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity_Log";
    private static final String DATABASE_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> legacyGoogleSignInLauncher;
    private DatabaseReference databaseReference;

    private TextInputEditText edtLoginName, edtPassword;
    private TextInputLayout tilLoginName, tilPassword;
    private MaterialButton btnLogIn, btnGoogleSignIn;
    private TextView tvErrorMessage, tvSignUp, tvForgotPassword;
    private CheckBox cbRemember;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_ID = "userId";
    private static final long GOOGLE_DB_TIMEOUT_MS = 6000L;

    private final Handler googleHandler = new Handler(Looper.getMainLooper());
    private Runnable googleDbTimeoutRunnable;
    private boolean googleNavigationStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);
        initGoogleSignIn();
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("Users");
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        initViews();
        setupListeners();
        loadRememberedData();

        if (mAuth.getCurrentUser() != null) {
            String savedUserId = sharedPreferences.getString(KEY_USER_ID, null);
            if (savedUserId != null && !savedUserId.isEmpty()) {
                startActivity(new Intent(this, Homepage.class));
                finish();
            } else {
                resolveGoogleUser(mAuth.getCurrentUser(), true);
            }
        }

        ensureAdminAccount();
    }

    private void ensureAdminAccount() {
        databaseReference.orderByChild("name").equalTo("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    String adminId = databaseReference.push().getKey();
                    Map<String, Object> adminData = new HashMap<>();
                    adminData.put("id", adminId);
                    adminData.put("name", "admin");
                    adminData.put("fullName", "System Administrator");
                    adminData.put("email", "admin@teatrack.com");
                    adminData.put("role", "Admin");
                    adminData.put("status", "Active");
                    adminData.put("password", hashPassword("admin123"));
                    adminData.put("createdAt", String.valueOf(System.currentTimeMillis()));

                    if (adminId != null) {
                        databaseReference.child(adminId).setValue(adminData);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Admin check cancelled: " + error.getMessage());
            }
        });
    }

    private void initViews() {
        edtLoginName = findViewById(R.id.edtLoginName);
        edtPassword = findViewById(R.id.edtPassword);
        tilLoginName = findViewById(R.id.tilLoginName);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogIn = findViewById(R.id.btnLogIn);
        btnGoogleSignIn = findViewById(R.id.btnGoogle);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        cbRemember = findViewById(R.id.cbRemember);
    }

    private void setupListeners() {
        if (btnLogIn != null) btnLogIn.setOnClickListener(v -> handleLogin());
        if (btnGoogleSignIn != null) btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                startActivity(new Intent(this, ForgotPassword.class));
            });
        }

        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterActivity.class));
            });
        }

        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { hideError(); }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        if (edtLoginName != null) edtLoginName.addTextChangedListener(clearErrorWatcher);
        if (edtPassword != null) edtPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void setFieldError(TextInputLayout til, boolean isError) {
        if (til == null) return;
        til.setErrorEnabled(false);
        if (isError) {
            int red = android.graphics.Color.RED;
            android.content.res.ColorStateList redList = new android.content.res.ColorStateList(
                    new int[][] {
                            new int[] { android.R.attr.state_focused },
                            new int[] { -android.R.attr.state_focused },
                            new int[] {}
                    },
                    new int[] { red, red, red }
            );
            til.setBoxStrokeColorStateList(redList);
        } else {
            til.setBoxStrokeColorStateList(
                    ContextCompat.getColorStateList(this, R.color.til_stroke_color)
            );
        }
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        legacyGoogleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        setGoogleLoading(false);
                        Log.w(TAG, "Google picker dismissed, resultCode=" + result.getResultCode());
                        return;
                    }
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null && account.getIdToken() != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        } else {
                            setGoogleLoading(false);
                            Log.e(TAG, "Google account returned without idToken");
                            Toast.makeText(this, getString(R.string.error_google_token_missing), Toast.LENGTH_LONG).show();
                        }
                    } catch (ApiException e) {
                        setGoogleLoading(false);
                        Log.e(TAG, "Legacy Google sign-in failed: " + e.getStatusCode(), e);
                        if (e.getStatusCode() != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                            Toast.makeText(
                                    this,
                                    getString(R.string.error_google_signin_failed, e.getMessage()),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
        );
    }

    private void setGoogleLoading(boolean loading) {
        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setEnabled(!loading);
        }
    }

    private void signInWithGoogle() {
        googleNavigationStarted = false;
        setGoogleLoading(true);
        GoogleSignInHelper.getClient(this).signOut().addOnCompleteListener(task -> requestGoogleCredential(false));
    }

    private void requestGoogleCredential(boolean autoSelect) {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(autoSelect)
                .setAutoSelectEnabled(autoSelect)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        Executor mainExecutor = ContextCompat.getMainExecutor(this);

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                mainExecutor,
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        if (e instanceof NoCredentialException) {
                            if (autoSelect) {
                                requestGoogleCredential(false);
                            } else {
                                launchLegacyGoogleSignIn();
                            }
                            return;
                        }
                        Log.e(TAG, "Google credential error", e);
                        setGoogleLoading(false);
                        Toast.makeText(
                                LoginActivity.this,
                                getString(R.string.error_google_signin_failed, e.getMessage()),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void launchLegacyGoogleSignIn() {
        if (googleSignInClient == null || legacyGoogleSignInLauncher == null) {
            setGoogleLoading(false);
            Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(signOutTask ->
                googleSignInClient.revokeAccess().addOnCompleteListener(revokeTask -> {
                    if (isFinishing()) {
                        setGoogleLoading(false);
                        return;
                    }
                    legacyGoogleSignInLauncher.launch(googleSignInClient.getSignInIntent());
                })
        );
    }

    private void handleSignIn(Credential credential) {
        GoogleIdTokenCredential googleIdTokenCredential = extractGoogleCredential(credential);
        if (googleIdTokenCredential != null && googleIdTokenCredential.getIdToken() != null) {
            setGoogleLoading(true);
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
            return;
        }
        Log.e(TAG, "Unexpected credential type: " + credential.getClass().getName());
        launchLegacyGoogleSignIn();
    }

    @Nullable
    private GoogleIdTokenCredential extractGoogleCredential(@NonNull Credential credential) {
        if (credential instanceof GoogleIdTokenCredential) {
            return (GoogleIdTokenCredential) credential;
        }
        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential;
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                try {
                    return GoogleIdTokenCredential.createFrom(customCredential.getData());
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse Google CustomCredential", e);
                }
            }
        }
        return null;
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            setGoogleLoading(false);
            Toast.makeText(this, getString(R.string.error_google_token_missing), Toast.LENGTH_LONG).show();
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        resolveGoogleUser(mAuth.getCurrentUser(), false);
                    } else {
                        setGoogleLoading(false);
                        Exception error = task.getException();
                        Log.e(TAG, "Firebase Google auth failed", error);
                        String detail = error != null && error.getMessage() != null
                                ? error.getMessage()
                                : getString(R.string.error_auth_failed);
                        Toast.makeText(
                                this,
                                getString(R.string.error_google_firebase_auth_failed, detail),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void clearGoogleDbTimeout() {
        if (googleDbTimeoutRunnable != null) {
            googleHandler.removeCallbacks(googleDbTimeoutRunnable);
            googleDbTimeoutRunnable = null;
        }
    }

    private void resolveGoogleUser(FirebaseUser user, boolean silentRestore) {
        if (user == null) {
            setGoogleLoading(false);
            if (!silentRestore) {
                Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        final String uid = user.getUid();
        final String normalizedEmail = normalizeEmail(user.getEmail());
        final boolean[] resolved = {false};

        clearGoogleDbTimeout();
        if (!silentRestore) {
            googleDbTimeoutRunnable = () -> {
                if (resolved[0] || isFinishing()) {
                    return;
                }
                resolved[0] = true;
                Log.w(TAG, "Users lookup timed out, navigating with Firebase session");
                openHomeAfterGoogleAuth(user);
            };
            googleHandler.postDelayed(googleDbTimeoutRunnable, GOOGLE_DB_TIMEOUT_MS);
        }

        databaseReference.orderByChild("firebaseUid").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (resolved[0]) {
                            return;
                        }
                        if (snapshot.exists()) {
                            resolved[0] = true;
                            clearGoogleDbTimeout();
                            completeGoogleLogin(snapshot.getChildren().iterator().next(), silentRestore);
                            return;
                        }
                        lookupGoogleUserByEmail(user, uid, normalizedEmail, silentRestore, resolved);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (resolved[0]) {
                            return;
                        }
                        resolved[0] = true;
                        clearGoogleDbTimeout();
                        Log.e(TAG, "Failed to query Users by firebaseUid: " + error.getMessage());
                        handleGoogleLookupFailure(user, silentRestore);
                    }
                });
    }

    private void lookupGoogleUserByEmail(
            @NonNull FirebaseUser user,
            @NonNull String uid,
            @Nullable String normalizedEmail,
            boolean silentRestore,
            boolean[] resolved
    ) {
        if (normalizedEmail == null) {
            if (resolved[0]) {
                return;
            }
            resolved[0] = true;
            clearGoogleDbTimeout();
            handleGoogleUserNotFound(user, uid, silentRestore);
            return;
        }

        databaseReference.orderByChild("email").equalTo(normalizedEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (resolved[0]) {
                            return;
                        }
                        resolved[0] = true;
                        clearGoogleDbTimeout();
                        if (snapshot.exists()) {
                            linkFirebaseUidAndLogin(
                                    snapshot.getChildren().iterator().next(),
                                    uid,
                                    user,
                                    silentRestore
                            );
                            return;
                        }
                        handleGoogleUserNotFound(user, uid, silentRestore);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (resolved[0]) {
                            return;
                        }
                        resolved[0] = true;
                        clearGoogleDbTimeout();
                        Log.e(TAG, "Failed to query Users by email: " + error.getMessage());
                        handleGoogleLookupFailure(user, silentRestore);
                    }
                });
    }

    private void handleGoogleUserNotFound(@NonNull FirebaseUser user, @NonNull String uid, boolean silentRestore) {
        if (silentRestore) {
            mAuth.signOut();
            setGoogleLoading(false);
            return;
        }
        createGoogleUser(user, uid);
    }

    private void handleGoogleLookupFailure(@NonNull FirebaseUser user, boolean silentRestore) {
        if (silentRestore) {
            mAuth.signOut();
            setGoogleLoading(false);
            return;
        }
        openHomeAfterGoogleAuth(user);
    }

    private void openHomeAfterGoogleAuth(@NonNull FirebaseUser user) {
        if (googleNavigationStarted || isFinishing()) {
            createGoogleUserInBackground(user);
            return;
        }
        googleNavigationStarted = true;
        setGoogleLoading(false);
        UserProfileHelper.cacheProfile(
                getApplicationContext(),
                sharedPreferences.getString(KEY_USER_ID, ""),
                "Customer",
                user.getDisplayName(),
                null
        );
        sharedPreferences.edit()
                .putString("userRole", "Customer")
                .apply();
        startActivity(new Intent(this, Homepage.class));
        finish();
        createGoogleUserInBackground(user);
    }

    private void createGoogleUserInBackground(@NonNull FirebaseUser user) {
        final String uid = user.getUid();
        final String normalizedEmail = normalizeEmail(user.getEmail());
        DatabaseReference usersRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("Users");
        usersRef.orderByChild("firebaseUid").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            cacheGoogleProfileFromSnapshot(snapshot.getChildren().iterator().next());
                            return;
                        }
                        if (normalizedEmail == null) {
                            createGoogleUserRecordInBackground(usersRef, user, uid, normalizedEmail);
                            return;
                        }
                        usersRef.orderByChild("email").equalTo(normalizedEmail)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot emailSnapshot) {
                                        if (emailSnapshot.exists()) {
                                            DataSnapshot existing = emailSnapshot.getChildren().iterator().next();
                                            String key = existing.getKey();
                                            if (key != null) {
                                                usersRef.child(key).updateChildren(buildGoogleLinkUpdates(user));
                                            }
                                            cacheGoogleProfileFromSnapshot(existing);
                                            return;
                                        }
                                        createGoogleUserRecordInBackground(usersRef, user, uid, normalizedEmail);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "Background email lookup cancelled: " + error.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Background uid lookup cancelled: " + error.getMessage());
                    }
                });
    }

    private void createGoogleUserRecordInBackground(
            DatabaseReference usersRef,
            @NonNull FirebaseUser user,
            @NonNull String uid,
            @Nullable String normalizedEmail
    ) {
        UserIdGenerator.next(usersRef, new UserIdGenerator.Callback() {
            @Override
            public void onGenerated(String csId) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", csId);
                userMap.put("firebaseUid", uid);
                userMap.put("name", user.getDisplayName());
                userMap.put("fullName", user.getDisplayName());
                userMap.put("email", normalizedEmail);
                userMap.put("role", "Customer");
                userMap.put("status", "Active");
                userMap.put("provider", "google");
                userMap.put("createdAt", String.valueOf(System.currentTimeMillis()));
                usersRef.child(csId).setValue(userMap);
                getApplicationContext()
                        .getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                        .edit()
                        .putString(KEY_USER_ID, csId)
                        .putString("userRole", "Customer")
                        .apply();
                UserProfileHelper.cacheProfile(
                        getApplicationContext(),
                        csId,
                        "Customer",
                        user.getDisplayName(),
                        null
                );
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Background Google user create failed: " + message);
            }
        });
    }

    private Map<String, Object> buildGoogleLinkUpdates(@NonNull FirebaseUser user) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firebaseUid", user.getUid());
        updates.put("provider", "google");
        String displayName = user.getDisplayName();
        if (displayName != null && !displayName.trim().isEmpty()) {
            updates.put("fullName", displayName.trim());
            updates.put("name", displayName.trim());
        }
        return updates;
    }

    private void cacheGoogleProfileFromSnapshot(@NonNull DataSnapshot userSnapshot) {
        String userId = userSnapshot.getKey();
        if (userId == null) {
            return;
        }
        String role = userSnapshot.child("role").getValue(String.class);
        if (role == null || role.trim().isEmpty()) {
            role = "Customer";
        }
        String fullName = userSnapshot.child("fullName").getValue(String.class);
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = userSnapshot.child("name").getValue(String.class);
        }
        String phone = userSnapshot.child("phoneNumber").getValue(String.class);
        if (phone == null || phone.trim().isEmpty()) {
            phone = userSnapshot.child("phone").getValue(String.class);
        }
        UserProfileHelper.cacheProfile(getApplicationContext(), userId, role, fullName, phone);
        sharedPreferences.edit()
                .putString(KEY_USER_ID, userId)
                .putString("userRole", role)
                .apply();
    }

    private void linkFirebaseUidAndLogin(
            DataSnapshot existingUser,
            String uid,
            FirebaseUser firebaseUser,
            boolean silentRestore
    ) {
        String userId = existingUser.getKey();
        if (userId == null) {
            if (silentRestore) {
                mAuth.signOut();
            } else {
                createGoogleUser(firebaseUser, uid);
            }
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("firebaseUid", uid);
        updates.put("provider", "google");
        String displayName = firebaseUser.getDisplayName();
        if (displayName != null && !displayName.trim().isEmpty()) {
            String existingFullName = existingUser.child("fullName").getValue(String.class);
            if (existingFullName == null || existingFullName.trim().isEmpty()) {
                updates.put("fullName", displayName.trim());
            }
            String existingName = existingUser.child("name").getValue(String.class);
            if (existingName == null || existingName.trim().isEmpty()) {
                updates.put("name", displayName.trim());
            }
        }

        databaseReference.child(userId).updateChildren(updates)
                .addOnSuccessListener(unused -> completeGoogleLogin(existingUser, silentRestore))
                .addOnFailureListener(e -> completeGoogleLogin(existingUser, silentRestore));
    }

    private void completeGoogleLogin(DataSnapshot userSnapshot, boolean silentRestore) {
        setGoogleLoading(false);
        String userId = userSnapshot.getKey();
        if (userId == null) {
            if (!silentRestore) {
                Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String role = userSnapshot.child("role").getValue(String.class);
        if (role == null || role.trim().isEmpty()) {
            role = "Customer";
        }
        String fullName = userSnapshot.child("fullName").getValue(String.class);
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = userSnapshot.child("name").getValue(String.class);
        }
        String phone = userSnapshot.child("phoneNumber").getValue(String.class);
        if (phone == null || phone.trim().isEmpty()) {
            phone = userSnapshot.child("phone").getValue(String.class);
        }

        UserProfileHelper.cacheProfile(getApplicationContext(), userId, role, fullName, phone);
        sharedPreferences.edit()
                .putString(KEY_USER_ID, userId)
                .putString("userRole", role)
                .apply();

        if (googleNavigationStarted || isFinishing()) {
            return;
        }
        googleNavigationStarted = true;
        Intent destination = "Admin".equalsIgnoreCase(role)
                ? new Intent(this, AdminDashboard.class)
                : new Intent(this, Homepage.class);
        startActivity(destination);
        finish();
    }

    private void createGoogleUser(FirebaseUser user, String uid) {
        UserIdGenerator.next(databaseReference, new UserIdGenerator.Callback() {
            @Override
            public void onGenerated(String csId) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", csId);
                userMap.put("firebaseUid", uid);
                userMap.put("name", user.getDisplayName());
                userMap.put("fullName", user.getDisplayName());
                userMap.put("email", normalizeEmail(user.getEmail()));
                userMap.put("role", "Customer");
                userMap.put("status", "Active");
                userMap.put("provider", "google");
                userMap.put("createdAt", String.valueOf(System.currentTimeMillis()));
                databaseReference.child(csId).setValue(userMap)
                        .addOnSuccessListener(unused -> {
                            setGoogleLoading(false);
                            UserProfileHelper.cacheProfile(
                                    LoginActivity.this,
                                    csId,
                                    "Customer",
                                    user.getDisplayName(),
                                    null
                            );
                            sharedPreferences.edit()
                                    .putString(KEY_USER_ID, csId)
                                    .putString("userRole", "Customer")
                                    .apply();
                            googleNavigationStarted = true;
                            startActivity(new Intent(LoginActivity.this, Homepage.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            setGoogleLoading(false);
                            Log.e(TAG, "Create Google user failed, opening home anyway", e);
                            openHomeAfterGoogleAuth(user);
                        });
            }

            @Override
            public void onError(String message) {
                setGoogleLoading(false);
                Log.e(TAG, "Generate Google user id failed, opening home anyway: " + message);
                openHomeAfterGoogleAuth(user);
            }
        });
    }

    @Nullable
    private static String normalizeEmail(@Nullable String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.US);
    }

    private void handleLogin() {
        if (edtLoginName == null || edtPassword == null) return;

        String loginName = edtLoginName.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        hideError();

        if (TextUtils.isEmpty(loginName) || TextUtils.isEmpty(password)) {
            showError(getString(R.string.error_login_empty_fields));
            if (TextUtils.isEmpty(loginName)) setFieldError(tilLoginName, true);
            if (TextUtils.isEmpty(password)) setFieldError(tilPassword, true);
            return;
        }

        btnLogIn.setEnabled(false);
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            if (!isFinishing()) {
                btnLogIn.setEnabled(true);
                showError(getString(R.string.error_network_timeout));
                Log.e(TAG, "Login timeout reached");
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 10000);

        if (loginName.equalsIgnoreCase("admin") && password.equals("admin123")) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            hideError();
            loginAsAdminShortcut();
            return;
        }

        try {
            FirebaseDatabase db = FirebaseDatabase.getInstance(DATABASE_URL);
            DatabaseReference usersRef = db.getReference("Users");

            usersRef.orderByChild("name").equalTo(loginName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    btnLogIn.setEnabled(true);

                    if (!snapshot.exists()) {
                        showError(getString(R.string.error_invalid_credentials));
                        setFieldError(tilLoginName, true);
                        setFieldError(tilPassword, true);
                        return;
                    }

                    String userId = null;
                    boolean passwordMatched = false;
                    String hashedInput = hashPassword(password);

                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String storedPassword = userSnap.child("password").getValue(String.class);
                        if (hashedInput.equals(storedPassword)) {
                            passwordMatched = true;
                            userId = userSnap.getKey();
                            String role = userSnap.child("role").getValue(String.class);
                            String fullName = userSnap.child("fullName").getValue(String.class);
                            if (fullName == null || fullName.trim().isEmpty()) {
                                fullName = userSnap.child("name").getValue(String.class);
                            }
                            String phone = userSnap.child("phoneNumber").getValue(String.class);
                            if (phone == null || phone.trim().isEmpty()) {
                                phone = userSnap.child("phone").getValue(String.class);
                            }

                            sharedPreferences.edit()
                                    .putString(KEY_USER_ID, userId)
                                    .putString("userRole", role != null ? role : "Customer")
                                    .apply();
                            UserProfileHelper.cacheProfile(
                                    LoginActivity.this,
                                    userId,
                                    role != null ? role : "Customer",
                                    fullName,
                                    phone
                            );

                            if (cbRemember.isChecked()) {
                                saveLoginData(loginName, password, userId);
                            }

                            if ("Admin".equalsIgnoreCase(role)) {
                                startActivity(new Intent(LoginActivity.this, AdminDashboard.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, Homepage.class));
                            }
                            finish();
                            break;
                        }
                    }

                    if (!passwordMatched) {
                        showError(getString(R.string.error_invalid_credentials));
                        setFieldError(tilPassword, true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    btnLogIn.setEnabled(true);
                    showError(getString(R.string.error_system_general, error.getMessage()));
                }
            });
        } catch (Exception e) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            btnLogIn.setEnabled(true);
            showError(getString(R.string.error_firebase_connection));
        }
    }

    private void loginAsAdminShortcut() {
        databaseReference.orderByChild("name").equalTo("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                btnLogIn.setEnabled(true);
                if (!snapshot.exists()) {
                    showError(getString(R.string.error_invalid_credentials));
                    return;
                }
                DataSnapshot adminSnap = snapshot.getChildren().iterator().next();
                AdminSessionHelper.cacheAdminSnapshot(LoginActivity.this, adminSnap);
                Toast.makeText(LoginActivity.this, getString(R.string.msg_welcome_admin), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, AdminDashboard.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnLogIn.setEnabled(true);
                showError(getString(R.string.error_system_general, error.getMessage()));
            }
        });
    }

    private void loadRememberedData() {
        boolean remember = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (remember) {
            cbRemember.setChecked(true);
            edtLoginName.setText(sharedPreferences.getString(KEY_USERNAME, ""));
            edtPassword.setText(sharedPreferences.getString(KEY_PASSWORD, ""));
        }
    }

    private void saveLoginData(String username, String password, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMEMBER, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    private void showError(String message) {
        if (tvErrorMessage != null) {
            tvErrorMessage.setText(message);
            tvErrorMessage.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideError() {
        if (tvErrorMessage != null) {
            tvErrorMessage.setVisibility(View.GONE);
        }
        setFieldError(tilLoginName, false);
        setFieldError(tilPassword, false);
    }
}