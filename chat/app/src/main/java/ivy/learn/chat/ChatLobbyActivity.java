package ivy.learn.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

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
    private ListenerRegistration list_reg;

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
    protected void onStart() {
        super.onStart();
        setChatroomListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        list_reg.remove();  // No need to listen if you're not there
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
        }

        // Update this_user and go to newly created chatroom
        // else if (requestCode == NEWROOM_REQUEST && resultCode == RESULT_OK)
            //getChatroomFromDB(0,false);     // Add new Chatroom

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
    public void onLongClick(int position, View v) {
        ChatRoom selected_room = chatrooms.get(position);

        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.roomOptions_changeTitle:
                    changeRoomTitle(selected_room);
                    return true;

                case R.id.roomOptions_delete:
                    deleteChatRoom(selected_room);
                    return true;

                case R.id.roomOptions_leave:
                    leaveChatRoom(selected_room);
                    return true;
            }
            return false;
        });
        popup.inflate(R.menu.chatroom_options);
        popup.setGravity(Gravity.END);

        // Hide certain items if not logged in
        Menu menu = popup.getMenu();
        if (!this_user.getUsername().equals(selected_room.getHost()))   // Only host can delete room
            menu.findItem(R.id.roomOptions_delete).setVisible(false);

        popup.show();
    }


/* Transition Methods
***************************************************************************************************/

    // TODO: some loading mechanism?



/* Firebase related Methods
***************************************************************************************************/

    private void setChatroomListener(){
        list_reg =
        mFirestore.collection("conversations")
                .whereArrayContains("members",this_user.getUsername())
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) Log.w(TAG, "Error in attaching listener.", e);
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {

                            ChatRoom chatroom = docChange.getDocument().toObject(ChatRoom.class);

                            // Update RecyclerView Adapter base on type of change
                            if (docChange.getType() == DocumentChange.Type.ADDED && !chatrooms.contains(chatroom)){ // ADDED?
                                Log.d(TAG, "Document added.");
                                adapter.addChatroom(-1, chatroom);
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
                                    adapter.updateChatroom(position, chatroom);
                                }
                            }
                        } Log.d(TAG, queryDocumentSnapshots.size() + " rooms uploaded!");
                    }
                });
    }













/* Chatroom Option Methods (also used in ChatRoomActivity)
***************************************************************************************************/

    void changeRoomTitle(ChatRoom chatroom){
        //TODO
    }

    void deleteChatRoom(ChatRoom chatroom){
        //TODO
    }

    void leaveChatRoom(ChatRoom chatroom){
        //TODO
    }




/* Trash
***************************************************************************************************/

    /*

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

     */


}