package com.example.usmentz.firestore;

import android.util.Log;
import androidx.annotation.Nullable;

import com.example.usmentz.category.Category;
import com.example.usmentz.date.DateLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {

    private static final String TAG = "FirestoreHelper";
    private static final String COL_CATEGORIES = "categories";
    private static final String COL_MOMENTS = "date_locations";
    private static final String COL_EXPENSES = "expenses";

    private static FirestoreHelper instance;
    private final FirebaseFirestore db;

    private FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    @Nullable
    private String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // ─────────────────────────────────────────────
    // Callbacks
    // ─────────────────────────────────────────────

    public interface SyncCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface SyncListCallback<T> {
        void onSuccess(List<T> list);
        void onFailure(Exception e);
    }

    // ─────────────────────────────────────────────
    // Categories
    // ─────────────────────────────────────────────

    public void saveCategory(Category category, SyncCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onFailure(new Exception("Not logged in"));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("localId", category.getId());
        data.put("name", category.getName());
        data.put("iconName", category.getIconName());
        data.put("color", category.getColor());
        data.put("itemCount", category.getItemCount());
        data.put("userId", userId);

        if (category.getId() > 0) {
            // Update: use localId to find document
            db.collection(COL_CATEGORIES)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("localId", category.getId())
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (snap != null && !snap.isEmpty()) {
                            String docId = snap.getDocuments().get(0).getId();
                            db.collection(COL_CATEGORIES).document(docId).set(data)
                                    .addOnSuccessListener(v -> {
                                        if (callback != null) callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "saveCategory", e);
                                        if (callback != null) callback.onFailure(e);
                                    });
                        } else {
                            // Not found, create new
                            db.collection(COL_CATEGORIES).add(data)
                                    .addOnSuccessListener(v -> {
                                        if (callback != null) callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "saveCategory", e);
                                        if (callback != null) callback.onFailure(e);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "saveCategory", e);
                        if (callback != null) callback.onFailure(e);
                    });
        } else {
            // New category
            db.collection(COL_CATEGORIES).add(data)
                    .addOnSuccessListener(v -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "saveCategory", e);
                        if (callback != null) callback.onFailure(e);
                    });
        }
    }

    public void deleteCategory(int localId, SyncCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onFailure(new Exception("Not logged in"));
            return;
        }

        db.collection(COL_CATEGORIES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("localId", localId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap != null && !snap.isEmpty()) {
                        String docId = snap.getDocuments().get(0).getId();
                        db.collection(COL_CATEGORIES).document(docId).delete()
                                .addOnSuccessListener(v -> {
                                    if (callback != null) callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "deleteCategory", e);
                                    if (callback != null) callback.onFailure(e);
                                });
                    } else {
                        if (callback != null) callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "deleteCategory", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void loadCategories(SyncListCallback<Category> callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onFailure(new Exception("Not logged in"));
            return;
        }

        db.collection(COL_CATEGORIES)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Category> list = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            Category cat = mapToCategory(doc);
                            if (cat != null) list.add(cat);
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadCategories", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    // ─────────────────────────────────────────────
    // Moments
    // ─────────────────────────────────────────────

    public void saveMoment(DateLocation moment, SyncCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onFailure(new Exception("Not logged in"));
            return;
        }

        Map<String, Object> data = momentToMap(moment);
        data.put("userId", userId);

        if (moment.getId() > 0) {
            // Update: find by localId
            db.collection(COL_MOMENTS)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("localId", moment.getId())
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (snap != null && !snap.isEmpty()) {
                            String docId = snap.getDocuments().get(0).getId();
                            db.collection(COL_MOMENTS).document(docId).set(data)
                                    .addOnSuccessListener(v -> {
                                        if (callback != null) callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "saveMoment", e);
                                        if (callback != null) callback.onFailure(e);
                                    });
                        } else {
                            db.collection(COL_MOMENTS).add(data)
                                    .addOnSuccessListener(v -> {
                                        if (callback != null) callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "saveMoment", e);
                                        if (callback != null) callback.onFailure(e);
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "saveMoment", e);
                        if (callback != null) callback.onFailure(e);
                    });
        } else {
            db.collection(COL_MOMENTS).add(data)
                    .addOnSuccessListener(v -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "saveMoment", e);
                        if (callback != null) callback.onFailure(e);
                    });
        }
    }

    public void deleteMoment(int localId, SyncCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onFailure(new Exception("Not logged in"));
            return;
        }

        db.collection(COL_MOMENTS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("localId", localId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap != null && !snap.isEmpty()) {
                        String docId = snap.getDocuments().get(0).getId();
                        db.collection(COL_MOMENTS).document(docId).delete()
                                .addOnSuccessListener(v -> {
                                    if (callback != null) callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "deleteMoment", e);
                                    if (callback != null) callback.onFailure(e);
                                });
                    } else {
                        if (callback != null) callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "deleteMoment", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void loadMoments(SyncListCallback<DateLocation> callback) {
        String userId = getUserId();
        if (userId == null) {
            if (callback != null) callback.onFailure(new Exception("Not logged in"));
            return;
        }

        db.collection(COL_MOMENTS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<DateLocation> list = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            DateLocation m = mapToMoment(doc);
                            if (m != null) list.add(m);
                        }
                    }
                    if (callback != null) callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadMoments", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    // ─────────────────────────────────────────────
    // Map conversions
    // ─────────────────────────────────────────────

    private Map<String, Object> momentToMap(DateLocation moment) {
        Map<String, Object> m = new HashMap<>();
        m.put("localId", moment.getId());
        m.put("name", moment.getName());
        m.put("address", moment.getAddress());
        m.put("description", moment.getDescription());
        m.put("categoryId", moment.getCategoryId());
        m.put("isCompleted", moment.isCompleted());
        m.put("rating", moment.getRating());
        m.put("review", moment.getReview());
        m.put("photoPath", moment.getPhotoPath());
        m.put("position", moment.getPosition());
        if (moment.getDate() != null) {
            m.put("date", moment.getDate().getTime());
        }
        return m;
    }

    @Nullable
    private Category mapToCategory(DocumentSnapshot doc) {
        try {
            Category cat = new Category();
            cat.setId(doc.getLong("localId") != null ? doc.getLong("localId").intValue() : 0);
            cat.setName(doc.getString("name"));
            cat.setIconName(doc.getString("iconName"));
            cat.setColor(doc.getLong("color") != null ? doc.getLong("color").intValue() : 0);
            cat.setItemCount(doc.getLong("itemCount") != null ? doc.getLong("itemCount").intValue() : 0);
            return cat;
        } catch (Exception e) {
            Log.e(TAG, "mapToCategory", e);
            return null;
        }
    }

    @Nullable
    private DateLocation mapToMoment(DocumentSnapshot doc) {
        try {
            DateLocation m = new DateLocation();
            m.setId(doc.getLong("localId") != null ? doc.getLong("localId").intValue() : 0);
            m.setName(doc.getString("name"));
            m.setAddress(doc.getString("address"));
            m.setDescription(doc.getString("description"));
            m.setCategoryId(doc.getLong("categoryId") != null ? doc.getLong("categoryId").intValue() : 0);
            m.setCompleted(doc.getBoolean("isCompleted") != null ? doc.getBoolean("isCompleted") : false);
            Double ratingVal = doc.getDouble("rating");
            m.setRating(ratingVal != null ? ratingVal.floatValue() : 0f);
            m.setReview(doc.getString("review"));
            m.setPhotoPath(doc.getString("photoPath"));
            m.setPosition(doc.getLong("position") != null ? doc.getLong("position").intValue() : 0);
            Long dateTs = doc.getLong("date");
            if (dateTs != null) {
                m.setDate(new Date(dateTs));
            }
            return m;
        } catch (Exception e) {
            Log.e(TAG, "mapToMoment", e);
            return null;
        }
    }
}