package ivy.learn.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;


import ivy.learn.chat.R;
import ivy.learn.chat.utility.ChatRoom;
import ivy.learn.chat.utility.GroupChat;
import ivy.learn.chat.utility.Util;

public class LobbyAdapter extends RecyclerView.Adapter<LobbyAdapter.LobbyViewHolder> {

    // Attributes
    private String this_username;
    private SortedList<ChatRoom> chatrooms;
    OnChatRoomClickListener selection_listener;

    // Constructor
    public LobbyAdapter(String this_username, OnChatRoomClickListener listener) {
        this.this_username = this_username;
        this.selection_listener = listener;
    }

    public void setChatrooms(SortedList<ChatRoom> chatrooms) {
        this.chatrooms = chatrooms;
    }


/* Overridden Methods
***************************************************************************************************/

    @NonNull
    @Override
    public LobbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_chatroom, parent, false);
        return new LobbyViewHolder(view, selection_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull LobbyViewHolder holder, int position) {
        ChatRoom this_chatroom = chatrooms.get(position);

        // Set Chat Title
        if (this_chatroom.getIs_groupChat())    // group chat
            holder.tv_name.setText(((GroupChat)this_chatroom).getName());
        else {                                  // private messaging
            holder.tv_name.setText(this_chatroom.getMembers().get(0));
            if (this_chatroom.getMembers().size() > 1 && this_username.equals(this_chatroom.getMembers().get(0)))
                holder.tv_name.setText(this_chatroom.getMembers().get(1));
        }

        // Set time_stamp
        if (this_chatroom.getLast_message_timestamp() != null) {
            holder.tv_timeStamp.setText(Util.millisToDateTime(this_chatroom.getLast_message_timestamp()));
            holder.tv_timeStamp.setVisibility(View.VISIBLE);
        } else holder.tv_timeStamp.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }


/* View Holder subclass
***************************************************************************************************/

    static class LobbyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_name;
        TextView tv_timeStamp;
        ConstraintLayout layout;


        public LobbyViewHolder(@NonNull View itemView, final OnChatRoomClickListener listener) {
            super(itemView);

            // Initialize views
            tv_name = itemView.findViewById(R.id.item_chatroom_name);
            tv_timeStamp = itemView.findViewById(R.id.item_chatroom_timestamp);
            layout = itemView.findViewById(R.id.item_chatroom_layout);

            //Set Listeners
            layout.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onShortClick(position);
                }
            });

            layout.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onLongClick(position, v);
                    return true;
                }
                else return false;
            });
        }
    }


/* Item Click Interface (different methods for short and long(click and hold) clicks)
***************************************************************************************************/

    public interface OnChatRoomClickListener {
        void onShortClick(int position);
        void onLongClick(int position, View v);
    }
}

