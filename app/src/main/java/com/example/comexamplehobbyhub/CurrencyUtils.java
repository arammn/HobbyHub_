package com.example.comexamplehobbyhub;

import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CurrencyUtils {

    public static void updateCurrencyCounter(TextView currencyCounter) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long coins = documentSnapshot.getLong("sparkCoins");
                    if (coins != null) {
                        currencyCounter.setText(String.valueOf(coins));
                    }
                });
    }
}
