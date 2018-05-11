package com.ramzi.messanger.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ramzi.messanger.R;
import com.ramzi.messanger.adapters.UserAdapter;
import com.ramzi.messanger.models.User;
import com.ramzi.messanger.utils.Const;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ramzi on 31-Mar-18.
 */

public class UserFragment extends Fragment implements ChildEventListener, UserAdapter.OnUserClicked {

    @BindView(R.id.listView)
    RecyclerView listView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private DatabaseReference ref;
    private ArrayList<User> users = new ArrayList<>();
    private UserAdapter userAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ChildEventListener childEventListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_layout, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Friends");
        linearLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(linearLayoutManager);
        listView.setHasFixedSize(true);
        userAdapter = new UserAdapter(getActivity(), users);
        userAdapter.setOnUserClicked(this);
        listView.setAdapter(userAdapter);
        ref = FirebaseDatabase.getInstance().getReference();
        childEventListener = ref.child(Const.USERS_TABLE).addChildEventListener(this);

    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        User user = dataSnapshot.getValue(User.class);
        if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user.getUserId())) {
            users.add(user);
            userAdapter.notifyItemInserted(users.size());
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        User user = dataSnapshot.getValue(User.class);
        if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user.getUserId())) {
            users.add(user);
            userAdapter.notifyItemInserted(users.size());
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public void onClick(User user) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Const.USERKEY, user);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(childEventListener);
    }
}
