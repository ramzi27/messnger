package com.ramzi.messanger.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ramzi.messanger.R;
import com.ramzi.messanger.models.Message;
import com.ramzi.messanger.ui.ImageViewer;
import com.ramzi.messanger.utils.Const;
import com.ramzi.messanger.utils.ImageUtil;
import com.ramzi.messanger.utils.RoundTransform;
import com.ramzi.messanger.utils.SharedPref;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Ramzi on 02-Apr-18.
 */

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context c;
    private List<Message> messages;
    private int color;

    public ConversationAdapter(Context c, List<Message> messages, int color) {
        this.c = c;
        this.color = color;
        this.messages = messages;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == Const.MY_TEXT_MESSAGE) {
            v = LayoutInflater.from(c).inflate(R.layout.my_msg_row, parent, false);
            viewHolder = new MyMsgViewHolder(v);
        } else if (viewType == Const.FRIEND_TEXT_MESSAGE) {
            v = LayoutInflater.from(c).inflate(R.layout.friend_msg_row, parent, false);
            viewHolder = new FriendMsgViewHolder(v);
        } else if (viewType == Const.FRIEND_IMAGE) {
            v = LayoutInflater.from(c).inflate(R.layout.friend_msg_image, parent, false);
            viewHolder = new FriendImageHolder(v);
        } else if (viewType == Const.MY_IMAGE) {
            v = LayoutInflater.from(c).inflate(R.layout.my_msg_image, parent, false);
            viewHolder = new MyImageHolder(v);
        }
        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Calendar calendar = Calendar.getInstance();
        Message message = messages.get(position);
        calendar.setTimeInMillis(message.getTimeStamp());
        long currenTime = System.currentTimeMillis();
        String st;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (currenTime - message.getTimeStamp() < 86400000) {//less than one day
                st = android.icu.text.DateFormat.getTimeInstance(android.icu.text.DateFormat.SHORT).format(calendar.getTime());
            } else {
                String d = android.icu.text.DateFormat.getDateInstance(android.icu.text.DateFormat.MEDIUM).format(calendar.getTime()).split(",")[0];
                String t = android.icu.text.DateFormat.getTimeInstance(android.icu.text.DateFormat.SHORT).format(calendar.getTime());
                st = d + " AT " + t;
            }
        } else {
            st = "";
        }
        if (holder instanceof MyMsgViewHolder) {
            ((MyMsgViewHolder) holder).msg.setBackgroundTintList(ColorStateList.valueOf(color));
            ((MyMsgViewHolder) holder).msg.setText(message.getContent());
            ((MyMsgViewHolder) holder).msg.setOnClickListener(view -> {
                int v = ((MyMsgViewHolder) holder).msgTime.getVisibility();
                if (v == View.GONE) {
                    ((MyMsgViewHolder) holder).msgTime.setVisibility(View.VISIBLE);
                } else
                    ((MyMsgViewHolder) holder).msgTime.setVisibility(View.GONE);
            });
            ((MyMsgViewHolder) holder).msgTime.setText(st);
        } else if (holder instanceof FriendMsgViewHolder) {
            ((FriendMsgViewHolder) holder).msg.setText(message.getContent());
            ((FriendMsgViewHolder) holder).friendImage.setVisibility(View.VISIBLE);
            ((FriendMsgViewHolder) holder).msg.setOnClickListener(view -> {
                int v1 = ((FriendMsgViewHolder) holder).friendMsgTime.getVisibility();
                int v2 = ((FriendMsgViewHolder) holder).friendName.getVisibility();

                if (v1 == View.GONE)
                    ((FriendMsgViewHolder) holder).friendMsgTime.setVisibility(View.VISIBLE);
                else
                    ((FriendMsgViewHolder) holder).friendMsgTime.setVisibility(View.GONE);
                if (v2 == View.GONE)
                    ((FriendMsgViewHolder) holder).friendName.setVisibility(View.VISIBLE);
                else
                    ((FriendMsgViewHolder) holder).friendName.setVisibility(View.GONE);
            });
            ((FriendMsgViewHolder) holder).friendMsgTime.setText(st);
            ((FriendMsgViewHolder) holder).friendName.setText(message.getSenderName());
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (position >= 1 && !message.getSenderId().equals(userId) && message.getSenderId().equals(messages.get(position - 1).getSenderId())) {
                ((FriendMsgViewHolder) holder).friendImage.setVisibility(View.INVISIBLE);
            } else if (message.getSenderAvatar() != null) {
                Picasso.get().load(message.getSenderAvatar()).transform(new RoundTransform())
                        .placeholder(R.drawable.ic_account_placeholder)
                        .into(((FriendMsgViewHolder) holder).friendImage);
            } else
                ((FriendMsgViewHolder) holder).friendImage.setImageResource(R.drawable.ic_account_placeholder);
        } else if (holder instanceof MyImageHolder) {
            Bitmap bitmap = ImageUtil.convertFromBase64(message.getBase64Image());
            ((MyImageHolder) holder).myImage.setImageDrawable(new BitmapDrawable(c.getResources(), bitmap));
            ((MyImageHolder) holder).myImage.setOnClickListener(view -> openImage(message.getBase64Image(), message.getSenderName()));
        } else if (holder instanceof FriendImageHolder) {
            Picasso.get().load(message.getSenderAvatar()).transform(new RoundTransform()).placeholder(R.drawable.ic_account_placeholder).into(((FriendImageHolder) holder).friendImage);
            Bitmap bitmap = ImageUtil.convertFromBase64(message.getBase64Image());
            ((FriendImageHolder) holder).friendMsgImage.setImageDrawable(new BitmapDrawable(c.getResources(), bitmap));
            ((FriendImageHolder) holder).friendMsgImage.setOnClickListener(view -> openImage(message.getBase64Image(), message.getSenderName()));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.size() > 0) {
            Message message = messages.get(position);
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            int t = 0;
            if (message.getContent() != null && message.getSenderId().equals(userId))
                t = Const.MY_TEXT_MESSAGE;
            else if (!message.getSenderId().equals(userId) && message.getContent() != null)
                t = Const.FRIEND_TEXT_MESSAGE;
            else if (message.getContent() == null && message.getSenderId().equals(userId))
                t = Const.MY_IMAGE;
            else if (message.getContent() == null && !message.getSenderId().equals(userId))
                t = Const.FRIEND_IMAGE;
            return t;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void changeColor(int color) {
        this.color = color;
        notifyDataSetChanged();
    }

    private void openImageInGallery(Bitmap bitmap) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File filesDir = c.getFilesDir();
        long ss = System.currentTimeMillis();
        File imageFile = new File(filesDir, ss + ".png");
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            Log.i("imageFile", imageFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
            Log.i("imageFile", imageFile.exists() + "");
            intent.setDataAndType(Uri.fromFile(imageFile), "image/png");
            c.startActivity(intent);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
    }

    private void openImage(String bitmap, String name) {
        Intent intent = new Intent(c, ImageViewer.class);
        intent.putExtra("title", name);
        Single.create((emitter) -> {
            SharedPref sharedPref = new SharedPref(c);
            sharedPref.writeImage(bitmap);
            emitter.onSuccess("");
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        c.startActivity(intent);
    }

    public class MyMsgViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.myMsg)
        TextView msg;
        @BindView(R.id.myMsgTime)
        TextView msgTime;

        public MyMsgViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public class FriendMsgViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.friendMsg)
        TextView msg;
        @BindView(R.id.friendMsgTime)
        TextView friendMsgTime;
        @BindView(R.id.friendMsgImage)
        ImageView friendImage;
        @BindView(R.id.friendName)
        TextView friendName;


        public FriendMsgViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class MyImageHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.myImgMsg)
        ImageView myImage;

        public MyImageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class FriendImageHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.friendImgMsg)
        ImageView friendMsgImage;
        @BindView(R.id.friendMsgImage2)
        ImageView friendImage;

        public FriendImageHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
