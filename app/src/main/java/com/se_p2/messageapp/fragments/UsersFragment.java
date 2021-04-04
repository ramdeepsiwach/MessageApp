package com.se_p2.messageapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.se_p2.messageapp.Common;
import com.se_p2.messageapp.R;
import com.se_p2.messageapp.adapter.UserAdapter;
import com.se_p2.messageapp.model.UserModel;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> users;

    EditText search_users;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_users, container, false);

        search_users=view.findViewById(R.id.search_users);
        recyclerView=view.findViewById(R.id.recycler_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        users=new ArrayList<>();
        
        getUsersFromFirebase();

        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        
        return view;
    }

    private void searchUsers(String s) {
        FirebaseUser fUser=FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference(Common.USERS_REF)
                .orderByChild("name")
                .startAt(s)
                .endAt(s+"\uf0ff").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for(DataSnapshot snapshot1:snapshot.getChildren()){
                            UserModel user=snapshot1.getValue(UserModel.class);

                            if(!user.getUserId().equals(fUser.getUid())){
                                users.add(user);
                            }
                        }
                        userAdapter=new UserAdapter(getContext(),users,false);
                        recyclerView.setAdapter(userAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getUsersFromFirebase() {
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference(Common.USERS_REF)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (search_users.getText().toString().equals("")) {
                            users.clear();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                UserModel user = snapshot1.getValue(UserModel.class);
                                assert user != null;
                                assert firebaseUser != null;
                                if (!user.getUserId().equals(firebaseUser.getUid())) {
                                    users.add(user);
                                }
                            }

                            userAdapter = new UserAdapter(getContext(), users, false);
                            recyclerView.setAdapter(userAdapter);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}