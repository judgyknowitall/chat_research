package ivy.learn.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import ivy.learn.chat.adapters.LobbyAdapter;
import ivy.learn.chat.utility.ChatRoom;
import ivy.learn.chat.utility.User;
import ivy.learn.chat.utility.Util;

/**
 * Lobby where logged-in user can view active chat rooms
 */
public class ChatLobbyActivity extends AppCompatActivity implements LobbyAdapter.OnChatRoomClickListener{
    private static final String TAG = "ChatLobbyActivity";

    // RecyclerView
    private RecyclerView rv_chat_rooms;
    private LobbyAdapter adapter;
    private ArrayList<ChatRoom> chatrooms = new ArrayList<>();

    // Firebase
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    // Request Codes
    private static final int CHATROOM_REQUEST = 1;
    private static final int NEWROOM_REQUEST = 2;

    // Other Values
    private User this_user;
    private int selected_chatroom_index = 0;


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_lobby);
        getIntentExtras();

        if (this_user != null) {
            initViews();
            initRecycler();
        }
        else Log.e(TAG, "User parcel was null!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Save user info
        String user_address = "usernames/"+ this_user.getUsername();
        mFirestore.document(user_address).update("chatroom_addresses", this_user.getChatroom_addresses())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) Log.d(TAG, "User updated in database.");
                    else Log.e(TAG, "Could not update user in database.", task.getException());
                });
    }


    // TODO make chatroom listener
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Coming back from ChatRoomActivity
        if (requestCode == CHATROOM_REQUEST && resultCode == RESULT_OK){

            // Reorder Chatroom lists
            this_user.reorderChatroom_address( selected_chatroom_index, 0);
            Util.reorderItem(chatrooms, selected_chatroom_index, 0);
            selected_chatroom_index = 0;

            // Update Chatroom and adapter
            adapter.notifyDataSetChanged();
            getChatroomFromDB(0,true);
        }

        // Update this_user and go to newly created chatroom
        else if (requestCode == NEWROOM_REQUEST && resultCode == RESULT_OK)
            getChatroomFromDB(0,false);     // Add new Chatroom

    }

/* Initialization Methods
***************************************************************************************************/

    // Get User object passed from login Activity
    private void getIntentExtras(){
        if (getIntent() != null){
            this_user = getIntent().getParcelableExtra("this_user");
        }
    }

    private void initViews(){
        // Views
        TextView tv_title = findViewById(R.id.lobby_username);
        rv_chat_rooms = findViewById(R.id.lobby_recyclerview);
        tv_title.setText(this_user.getUsername());
    }

    private void initRecycler() {
        adapter = new LobbyAdapter(this_user.getUsername(), this, chatrooms);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        rv_chat_rooms.setLayoutManager(manager);
        rv_chat_rooms.setAdapter(adapter);

        // Populate chatrooms list and update adapter accordingly
        for (int i = 0; i < this_user.getChatroom_addresses().size(); i++){
            chatrooms.add(new ChatRoom());
            getChatroomFromDB(i, true);
        }
    }


/* OnClick Methods
***************************************************************************************************/

    public void logoutUser(View view) {
        // Empty preferences
        SharedPreferences prefs = getSharedPreferences("ChatResearch", 0);
        prefs.edit().remove("username").apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void newChatRoom(View view) {
        selected_chatroom_index = 0;
        Intent intent = new Intent(this, NewChatroomActivity.class);
        intent.putExtra("this_user", this_user);
        startActivity(intent);
    }

    // Go to chatroom
    @Override
    public void onShortClick(int position) {
        selected_chatroom_index = position;
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("chatroom", chatrooms.get(position));
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, CHATROOM_REQUEST);
    }

    @Override
    public void onLongClick(int position) {
        Toast.makeText(this, "Delete? " + chatrooms.get(position), Toast.LENGTH_SHORT).show();
        // TODO delete chatroom option
    }


/* Transition Methods
***************************************************************************************************/

    // TODO: some loading mechanism?



/* Firebase related Methods
***************************************************************************************************/

    // Chatrooms list must already have more than [index] items!
    private void getChatroomFromDB(int index, boolean toUpdate){
        if (index < 0 || index > this_user.getChatroom_addresses().size()-1) {
            Log.e(TAG, "Index out of bounds!");
            return;
        }

        mFirestore.document(this_user.getChatroom_addresses().get(index)).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {

                if (toUpdate) // Updated
                    adapter.updateChatroom(index, task.getResult().toObject(ChatRoom.class));
                else  // Newly added
                    adapter.addChatroom(index, task.getResult().toObject(ChatRoom.class));

                Log.d(TAG, "Chatroom successfully added/updated: " + chatrooms.get(index).getName());
            } else Log.e(TAG, "Wasn't able to get chatroom: " + this_user.getChatroom_addresses().get(index), task.getException());
        });
    }


/*
    // Get an event listener depending on if we want to add new rooms
    private EventListener<QuerySnapshot> getListener(boolean addNewRooms) {
        return (queryDocumentSnapshots, e) -> {
            if (e != null) Log.w(TAG, "Error in attaching listener.", e);
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {

                    ChatRoom chatroom = docChange.getDocument().toObject(ChatRoom.class);

                    // Update RecyclerView Adapter base on type of change
                    if (docChange.getType() == DocumentChange.Type.ADDED && addNewRooms){ // ADDED?
                        Log.d(TAG, "Document added.");
                        chatrooms.add(0, chatroom);
                        adapter.notifyItemInserted(0);
                        rv_chat_rooms.scrollToPosition(0);
                    }
                    else {
                        // Get Existing Message position
                        int position = chatrooms.indexOf(chatroom);
                        if (position < 0) {
                            Log.e(TAG, "Message not found in list! " + chatroom.getName());
                            break;
                        }
                        // REMOVED ?
                        if (docChange.getType() == DocumentChange.Type.REMOVED){
                            Log.d(TAG, "Document removed.");
                            adapter.removeChatroom(position);
                        }
                        else { // MODIFIED
                            Log.d(TAG, "Document modified.");
                            chatrooms.get(position).setName(chatroom.getName());
                            chatrooms.get(position).setTime_stamp(chatroom.getTime_stamp());
                            adapter.notifyItemChanged(position);
                        }
                    }
                }
            }
        };
    }
    */
}