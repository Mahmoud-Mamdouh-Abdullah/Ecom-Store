package com.mahmoudjoe3.eComStore.repo;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mahmoudjoe3.eComStore.model.Admin;
import com.mahmoudjoe3.eComStore.model.AuthorizedUser;
import com.mahmoudjoe3.eComStore.prevalent.Prevalent;


public class FirebaseAuthRepo {

    private OnLoginListener mOnLoginListener;
    public void setOnLoginListener(OnLoginListener onLoginListener) {
        mOnLoginListener = onLoginListener;
    }



    public interface OnLoginListener {
        void onLogeInSuccess(Object user);
        void onLogeInDenied(String errorMsg);
        void onRemember(Object user);
    }

    private OnRegisterListener mOnRegisterListener;
    public void setOnRegisterListener(OnRegisterListener OnRegisterListener) {
        mOnRegisterListener = OnRegisterListener;
    }
    public interface OnRegisterListener {
        void onRegisterSuccess();
        void onRegisterExist();
        void onRegisterFailure();
    }


    static FirebaseAuthRepo instance;

    static DatabaseReference mReference;

    public static synchronized FirebaseAuthRepo getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthRepo();
        }
        mReference = FirebaseDatabase.getInstance().getReference();
        return instance;
    }

    public void RegisterUser(String name, String phone, String password,String date) {

        mReference = FirebaseDatabase.getInstance().getReference(Prevalent.refColName_User);

        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(phone).exists()) { //phone number is exist in database
                    if(mOnRegisterListener!=null){
                        mOnRegisterListener.onRegisterExist();
                    }
                }
                else {//safe to insert new user
                    insertNewUser(name, phone, password,date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void insertNewUser(String name, String phone, String password,String date) {
        AuthorizedUser user=new AuthorizedUser(name,phone,password,date);
        mReference.child(phone).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(mOnRegisterListener!=null){
                    mOnRegisterListener.onRegisterSuccess();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(mOnRegisterListener!=null){
                    mOnRegisterListener.onRegisterFailure();
                }
            }
        });

    }


    public void forgetPassword(boolean isAdmin, String phone, String newPassword) {
        String refCollectionName = (isAdmin) ? Prevalent.refColName_Admin : Prevalent.refColName_User;
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(refCollectionName).child(phone).exists()) { //access done
                    if (isAdmin) {
                        Admin admin = snapshot.child(refCollectionName).child(phone).getValue(Admin.class);
                        if (admin.getPhone().equals(phone)) {
                            admin.setPassword(newPassword);
                            mReference.child(refCollectionName).child(phone).setValue(admin);
                        }
                    }
                    else {
                        AuthorizedUser user = snapshot.child(refCollectionName).child(phone).getValue(AuthorizedUser.class);
                        if (user.getPhone().equals(phone)) {
                            user.setPassword(newPassword);
                            mReference.child(refCollectionName).child(phone).setValue(user);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void login(boolean admin, String phone, String password, boolean rememberMe) {

        String refCollectionName = (admin) ? Prevalent.refColName_Admin : Prevalent.refColName_User;

        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(refCollectionName).child(phone).exists()) { //access done
                    if(admin) {
                        Admin admin = snapshot.child(refCollectionName).child(phone).getValue(Admin.class);
                        if (admin.getPhone().equals(phone)) {
                            if (admin.getPassword().equals(password)) {
                                //pref
                                if (rememberMe) {
                                    if (mOnLoginListener != null) {
                                        mOnLoginListener.onRemember(admin);
                                    }
                                }
                                if (mOnLoginListener != null) {
                                    mOnLoginListener.onLogeInSuccess(admin);
                                }
                            } else {
                                if (mOnLoginListener != null) {
                                    mOnLoginListener.onLogeInDenied("password incorrect");
                                }
                            }
                        }
                    }
                    else {
                        AuthorizedUser user = snapshot.child(refCollectionName).child(phone).getValue(AuthorizedUser.class);
                        if (user.getPhone().equals(phone)) {
                            if (user.getPassword().equals(password)) {
                                //pref
                                if (rememberMe) {
                                    if (mOnLoginListener != null) {
                                        mOnLoginListener.onRemember(user);
                                    }
                                }
                                if (mOnLoginListener != null) {
                                    mOnLoginListener.onLogeInSuccess(user);
                                }
                            } else {
                                if (mOnLoginListener != null) {
                                    mOnLoginListener.onLogeInDenied("password incorrect");
                                }
                            }
                        }
                    }

                } else {  //access denied
                    if (mOnLoginListener != null) {
                        mOnLoginListener.onLogeInDenied("Login Failed, You should have account");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}



