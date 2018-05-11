package com.ramzi.messanger.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mvc.imagepicker.ImagePicker;
import com.ramzi.messanger.R;
import com.ramzi.messanger.adapters.ConversationAdapter;
import com.ramzi.messanger.models.Message;
import com.ramzi.messanger.models.User;
import com.ramzi.messanger.utils.Const;
import com.ramzi.messanger.utils.ImageUtil;
import com.ramzi.messanger.utils.RoundTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import petrov.kristiyan.colorpicker.ColorPicker;

public class ChatActivity extends AppCompatActivity implements ChildEventListener, ColorPicker.OnChooseColorListener, ValueEventListener {
    @BindView(R.id.msgText)
    EditText msgText;
    @BindView(R.id.sendButton)
    ImageButton sendButton;
    @BindView(R.id.msgRecycleView)
    RecyclerView msgRecycleView;
    @BindView(R.id.imageButton)
    ImageButton imageButton;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.chatProgress)
    ProgressBar chatProgressBar;
    @BindView(R.id.noMessages)
    TextView noMessages;

    private User user;
    private String conversationId;
    private ChildEventListener childEventListener;
    private ConversationAdapter conversationAdapter;
    private ValueEventListener valueEventListener;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Message> messages = new ArrayList<>();
    private long initTime;
    private boolean soundPlayed = false, sendClicked = false;
    private int color = -14654801;
    private DatabaseReference conversationRef;
    private DatabaseReference colorRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initTime = System.currentTimeMillis();
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        getConvKey();
        initDataBase();
        if (!sendClicked)
            sendButton.setOnClickListener(view -> sendMsg());
        imageButton.setOnClickListener(view -> pickImage());
    }

    private void pickImage() {
        ImagePicker.pickImage(this, "select an image");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
        if (bitmap != null) {
            String base64 = ImageUtil.convertToBase64(bitmap);
            sendMessage(Const.IMAGE_MSG, base64);
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_LONG).show();
        }
    }

    private synchronized void sendMessage(int type, String base64) {
        Message message = new Message();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        message.setSenderId(userId);
        String photo;
        Uri pho = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        photo = pho == null ? null : pho.toString();
        String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (name.contains(" ")) {
            name = name.split(" ")[0];
        }
        message.setSenderAvatar(photo);
        message.setSenderName(name);
        if (type == Const.TEXT_MSG) {
            String msg = msgText.getText().toString();
            message.setContent(msg);
        } else if (type == Const.IMAGE_MSG)
            message.setBase64Image(base64);
        conversationRef.push().setValue(message)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "success", Toast.LENGTH_LONG).show();
                        msgText.setText("");
                        sendClicked = false;
                    } else
                        Toast.makeText(this, "failed", Toast.LENGTH_LONG).show();
                });
    }

    private void sendMsg() {
        sendClicked = true;
        sendMessage(Const.TEXT_MSG, null);
    }

    private void initDataBase() {
        String mPath = String.format(Const.CONVERSATIONS_MESSAGES_PATH, conversationId);
        String cPath = String.format(Const.CONVERSATIONS_COLOR_PATH, conversationId);
        conversationRef = FirebaseDatabase.getInstance().getReference(mPath);
        childEventListener = conversationRef.addChildEventListener(this);
        colorRef = FirebaseDatabase.getInstance().getReference(cPath);
        colorRef.keepSynced(true);
        valueEventListener = colorRef.addValueEventListener(this);
    }

    private void getConvKey() {
        String s1 = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String s2 = user.getUserId();
        ArrayList<String> strings = new ArrayList<>();
        strings.add(s1);
        strings.add(s2);
        Collections.sort(strings);
        conversationId = strings.get(0) + "-" + strings.get(1);
    }

    private void init() {
        user = (User) getIntent().getExtras().getSerializable(Const.USERKEY);
        msgText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    sendButton.setClickable(true);
                    sendButton.setEnabled(true);
                    sendButton.setImageResource(R.drawable.ic_send_enabled);
                    DrawableCompat.wrap(sendButton.getDrawable()).setTint(ChatActivity.this.color);
                } else if (charSequence.length() == 0) {
                    sendButton.setClickable(false);
                    sendButton.setImageResource(R.drawable.ic_send_disabled);
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        if (user != null) {
            getSupportActionBar().setTitle(user.getName());
            if (user.getImgUrl() != null) {
                Single.create(emitter -> {
                    Bitmap bitmap = Picasso.get().load(user.getImgUrl()).transform(new RoundTransform()).get();
                    Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                    emitter.onSuccess(drawable);
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(o -> {
                            getSupportActionBar().setIcon((Drawable) o);
                        });
            } else {
                getSupportActionBar().setIcon(R.drawable.ic_account_placeholder);
            }
        }
        linearLayoutManager = new LinearLayoutManager(this);
        msgRecycleView.setHasFixedSize(true);
        msgRecycleView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.i("childAdded", "child");
        if (dataSnapshot.getValue() == null)
            return;
        if (messages.size() == 0) {
            noMessages.setVisibility(View.VISIBLE);
            msgRecycleView.setVisibility(View.GONE);
            chatProgressBar.setVisibility(View.GONE);
            conversationAdapter = new ConversationAdapter(this, messages, this.color);
            msgRecycleView.setAdapter(conversationAdapter);
        }
        Message message = dataSnapshot.getValue(Message.class);
        messages.add(message);
        conversationAdapter.notifyItemInserted(messages.size());
        linearLayoutManager.scrollToPosition(messages.size() - 1);
        if (!soundPlayed && !message.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && initTime < message.getTimeStamp())//new messages play sound
            playSound();
        if (messages.size() > 0) {
            noMessages.setVisibility(View.GONE);
            msgRecycleView.setVisibility(View.VISIBLE);
            chatProgressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {
            this.color = Integer.valueOf(dataSnapshot.getValue().toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                changeAllColors();
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }


    @Override
    protected void onDestroy() {
        conversationRef.removeEventListener(childEventListener);
        colorRef.removeEventListener(valueEventListener);
        super.onDestroy();
    }

    private synchronized void playSound() {
        soundPlayed = true;
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sound);
        mediaPlayer.start();
        soundPlayed = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.color_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.colorItem) {
            new ColorPicker(this)
                    .setRoundColorButton(true)
                    .setColors(R.array.default_colors)
                    .setColumns(5)
                    .setTitle("select a color for this conversation")
                    .setOnChooseColorListener(this)
                    .show();

        } else if (item.getItemId() == android.R.id.home)
            this.onBackPressed();
        return true;
    }

    @Override
    public void onChooseColor(int position, int color) {
        colorRef.setValue(color).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                Toast.makeText(this, "color changed", Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(this, "color not changed", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCancel() {
        Toast.makeText(this, "no color selected", Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void changeAllColors() {
        toolbar.setBackgroundColor(this.color);
        conversationAdapter.changeColor(this.color);
        if (msgText.getText().toString().length() > 0)
            DrawableCompat.wrap(sendButton.getDrawable()).setTint(this.color);
        getWindow().setStatusBarColor(this.color + 40);
        DrawableCompat.wrap(imageButton.getDrawable()).setTint(this.color);
    }


}
