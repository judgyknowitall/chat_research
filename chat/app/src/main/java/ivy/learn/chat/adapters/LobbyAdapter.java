package ivy.learn.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import ivy.learn.chat.R;
import ivy.learn.chat.utility.ChatRoom;
import ivy.learn.chat.utility.Util;

public class LobbyAdapter extends RecyclerView.Adapter<LobbyAdapter.LobbyViewHolder> {
    private static final String TAG = "LobbyAdapter";

    // Attributes
    private String this_username;
    private List<ChatRoom> chatrooms;
    OnChatRoomClickListener selection_listener;

    // Firebase
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private List<ListenerRegistration> lastMessage_listeners = new ArrayList<>();


    // Constructor
    public LobbyAdapter(String this_username, OnChatRoomClickListener listener, List<ChatRoom> chatrooms) {
        this.this_username = this_username;
        this.selection_listener = listener;
        this.chatrooms = chatrooms;
    }

    public void cleanUp(){
        for (ListenerRegistration listener : lastMessage_listeners)
            listener.remove();
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

        if (this_chatroom.getName().equals(this_username))
            holder.tv_name.setText(this_chatroom.getHost());    // private messaging
        else
            holder.tv_name.setText(this_chatroom.getName());    // group chat

        setLastMessageListener(holder, position);   // Set a listener for last message
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }

    public void updateChatroom(int position, ChatRoom chatRoom){
        chatrooms.set(position, chatRoom);
        notifyItemChanged(position);
    }

    // TODO order chatrooms based on latest message timestamp
    public void addChatroom(int position, ChatRoom chatRoom){
        if (position > 0 && position < chatrooms.size()-1)
            chatrooms.add(position, chatRoom);
        else {
            chatrooms.add(chatRoom);
            position = chatrooms.size()-1;
        }
        notifyItemInserted(position);
    }

    public void removeChatroom(int position){
        chatrooms.remove(position);
        lastMessage_listeners.get(position).remove();   // Remove listener
        lastMessage_listeners.remove(position);
        notifyDataSetChanged();
    }


/* Firebase Related Methods
***************************************************************************************************/

    // private void
    private void setLastMessageListener(LobbyViewHolder holder, int position){
        String address = "conversations/" + chatrooms.get(position).getId() + "/messages";

        lastMessage_listeners.add(position,
        mFirestore.collection(address)
                .orderBy("time_stamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        DocumentChange docChange = queryDocumentSnapshots.getDocumentChanges().get(0);
                        Long time_stamp = (Long) docChange.getDocument().get("time_stamp");

                        // Change time_stamp
                        if (time_stamp != null) {
                            holder.tv_timeStamp.setText(Util.millisToDateTime(time_stamp));
                            holder.tv_timeStamp.setVisibility(View.VISIBLE);
                        } else holder.tv_timeStamp.setVisibility(View.INVISIBLE);
                    } else holder.tv_timeStamp.setVisibility(View.INVISIBLE);
                }));
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

