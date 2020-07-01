package ivy.learn.chat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ivy.learn.chat.entities.ChatRoom;

public class LobbyAdapter extends RecyclerView.Adapter<LobbyAdapter.LobbyViewHolder> {
    private static final String TAG = "LobbyAdapter";

    // Attributes
    private List<ChatRoom> chat_rooms;
    private Context context;
    OnChatRoomClickListener selection_listener;

    private StorageReference firebase_storage = FirebaseStorage.getInstance().getReference();


    public LobbyAdapter(List<ChatRoom> chat_rooms) {
        this.chat_rooms = chat_rooms;
    }

    public void setOnUserItemClickListener (OnChatRoomClickListener listener){
        this.selection_listener = listener;
    }


    /* Overridden Methods
     ***************************************************************************************************/

    @NonNull
    @Override
    public LobbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_room, parent, false);
        return new LobbyViewHolder(view, selection_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull LobbyViewHolder holder, int position) {
        holder.tv_name.setText(chat_rooms.get(position).getName());
        loadImage(holder, chat_rooms.get(position));
    }

    @Override
    public int getItemCount() {
        return chat_rooms.size();
    }

    public void removeChatroom(int position){
        chat_rooms.remove(position);
        notifyDataSetChanged();
    }


/* Firebase Methods
***************************************************************************************************/

    private void loadImage(LobbyViewHolder holder, ChatRoom chatroom) {
        // Load a placeholder first in case something goes wrong
        holder.circle_img.setImageDrawable(context.getDrawable(R.drawable.ic_chat));

        // Find address of possible image
        String address = "chatrooms/"+chatroom.getName()+"/chatImage.jpg";
        if (address.contains("null")){
            Log.e(TAG, "Address contained null! Chatroom: " + chatroom.getName());
            return;
        }

        firebase_storage.child(address).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null)
                        Picasso.get().load(task.getResult()).into(holder.circle_img);
                    else Log.w(TAG, "A chatroom image doesn't exist! Chatroom: " + chatroom.getName());
                });
    }


/* View Holder subclass
***************************************************************************************************/

    static class LobbyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circle_img;
        TextView tv_name;
        ConstraintLayout layout;


        public LobbyViewHolder(@NonNull View itemView, final OnChatRoomClickListener listener) {
            super(itemView);

            // Initialize views
            circle_img = itemView.findViewById(R.id.item_chatroom_image);
            tv_name = itemView.findViewById(R.id.item_chatroom_name);
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
                        listener.onLongClick(position);
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
        void onLongClick(int position);
    }
}

