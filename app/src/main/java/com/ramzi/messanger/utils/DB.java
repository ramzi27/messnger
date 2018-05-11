package com.ramzi.messanger.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ramzi.messanger.models.User;

/**
 * Created by Ramzi on 31-Mar-18.
 */

public class DB {
    private static DatabaseReference mDatabase;

    public static void saveUser(User user) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase.child(Const.USERS_TABLE).child(firebaseUser.getUid() + "").setValue(user);
    }
}
