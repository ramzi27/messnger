package com.ramzi.messanger.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ramzi.messanger.R;
import com.ramzi.messanger.models.User;
import com.ramzi.messanger.utils.RoundTransform;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ramzi on 31-Mar-18.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context c;
    private List<User> users;
    private OnUserClicked onUserClicked;

    public UserAdapter(Context c, List<User> users) {
        this.c = c;
        this.users = users;
    }

    public void setOnUserClicked(OnUserClicked onUserClicked) {
        this.onUserClicked = onUserClicked;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(c).inflate(R.layout.user_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = users.get(position);
        holder.userName.setText(user.getName());
        holder.userMsgNo.setText(user.getMsgNo() + "");
        holder.bindClick(position);
        if (user.isStatus())
            holder.userStatus.setImageResource(R.drawable.ic_online);
        else
            holder.userStatus.setImageResource(R.drawable.ic_offline);
        if (user.getImgUrl() != null)
            Picasso.get().load(user.getImgUrl()).transform(new RoundTransform()).placeholder(R.drawable.ic_account_placeholder).resizeDimen(R.dimen.img_size, R.dimen.img_size).into(holder.userImg);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public interface OnUserClicked {
        void onClick(User user);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.userName)
        TextView userName;
        @BindView(R.id.userMsgNo)
        TextView userMsgNo;
        @BindView(R.id.userImage)
        ImageView userImg;
        @BindView(R.id.userStatus)
        ImageView userStatus;
        private View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            ButterKnife.bind(this, v);
        }

        public void bindClick(int i) {
            if (onUserClicked != null) {
                this.v.setOnClickListener(view -> {
                    onUserClicked.onClick(users.get(i));
                });
            }
        }
    }
}
