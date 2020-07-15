package ivy.learn.chat.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ivy.learn.chat.R;
import ivy.learn.chat.utility.ChatRoom;
import ivy.learn.chat.utility.User;


public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.UserViewHolder> {

    // Attributes
    private List<String> usernames;
    private User this_user;
    private ChatRoom chatRoom;
    private Context context;
    OnUserItemClickListener selection_listener;


    public MemberAdapter(List<String> usernames, User this_user, ChatRoom chatRoom) {
        this.usernames = usernames;
        this.this_user = this_user;
        this.chatRoom = chatRoom;
    }

    public void setOnUserItemClickListener (OnUserItemClickListener listener){
        this.selection_listener = listener;
    }


/* Overridden Methods
***************************************************************************************************/

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_seeall_members, parent, false);
        return new UserViewHolder(view, selection_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.tv_name.setText(usernames.get(position));

        // Show a star next to host!
        if (usernames.get(position).equals(chatRoom.getHost()))
            holder.iv_star.setVisibility(View.VISIBLE);

        // Show kick button if host
        if (this_user.getUsername().equals(chatRoom.getHost()))
            holder.button_kick.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return usernames.size();
    }

    public void removeUser(int position){
        usernames.remove(position);
        notifyDataSetChanged();
    }


/* View Holder subclass
***************************************************************************************************/

    static class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_star;
        TextView tv_name;
        ImageButton button_kick;


        public UserViewHolder(@NonNull View itemView, final OnUserItemClickListener listener) {
            super(itemView);

            // Initialize views
            iv_star = itemView.findViewById(R.id.item_member_star);
            tv_name = itemView.findViewById(R.id.item_member_user);
            button_kick = itemView.findViewById(R.id.item_member_kick);

            tv_name.setOnLongClickListener(v -> {
                if (listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        listener.onUserClick(position, v);
                        return true;
                    }
                } return false;
            });

            button_kick.setOnClickListener(v -> {
                if (listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION)
                        listener.onKickClick(position);
                }
            });
        }
    }


/* Item Click Interface (specifically for user items)
***************************************************************************************************/

    public interface OnUserItemClickListener {
        void onUserClick(int position, View v);
        void onKickClick(int position);
    }
}
