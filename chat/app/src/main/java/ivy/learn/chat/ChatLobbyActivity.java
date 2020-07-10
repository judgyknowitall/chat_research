package ivy.learn.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.firestore.FieldValue;
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

    // Other Values
    private User this_user;
    private int selected_chatroom_index = -1;


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
        adapter.cleanUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
        // Save user info
        String user_address = "usernames/"+ this_user.getUsername();
        mFirestore.document(user_address).update("chatroom_addresses", this_user.getChatroom_addresses())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) Log.d(TAG, "User updated in database.");
                    else Log.e(TAG, "Could not update user in database.", task.getException());
                });
         */
    }


    // TODO make chatroom listener
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Coming back from ChatRoomActivity
        if (requestCode == Util.CHATROOM_REQUEST && resultCode == RESULT_OK){

            // Reorder Chatroom lists
            Util.reorderItem(chatrooms, selected_chatroom_index, 0);
            selected_chatroom_index = -1;

            // Update Chatroom and adapter
            adapter.notifyDataSetChanged();
        }

        // Go to chatroom after it is ADDED by listener
        if (requestCode == Util.NEWCHATROOM_REQUEST && resultCode == RESULT_OK)
            selected_chatroom_index = chatrooms.size();
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
        selected_chatroom_index = chatrooms.size();
        Intent intent = new Intent(this, NewChatroomActivity.class);
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, Util.NEWCHATROOM_REQUEST);
    }

    // Go to chatroom
    @Override
    public void onShortClick(int position) {
        selected_chatroom_index = position;
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("chatroom", chatrooms.get(position));
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, Util.CHATROOM_REQUEST);
    }


    // TODO: use popup window instead?
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
                    deleteChatRoom(selected_room.getId());
                    return true;

                case R.id.roomOptions_leave:
                    leaveChatRoom(selected_room);
                    return true;
            }
            return false;
        });
        popup.inflate(R.menu.chatroom_options);
        popup.setGravity(Gravity.END);

        // Adjust view displays
        Menu menu = popup.getMenu();
        Util.colorMenuItem(menu.findItem(R.id.roomOptions_delete), getColor(R.color.red));

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
                .whereArrayContains("members", this_user.getUsername())
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) Log.w(TAG, "Error in attaching listener.", e);
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {

                            ChatRoom chatroom = docChange.getDocument().toObject(ChatRoom.class);
                            chatroom.setId(docChange.getDocument().getId());

                            // Update RecyclerView Adapter base on type of change
                            if (docChange.getType() == DocumentChange.Type.ADDED && !chatrooms.contains(chatroom)){ // ADDED?
                                Log.d(TAG, "Document added: " + chatroom.getName());
                                adapter.addChatroom(-1, chatroom);

                                // Came back from creating new chatroom?
                                if (selected_chatroom_index == chatrooms.size()-1)
                                    onShortClick(selected_chatroom_index); // Go to chatroom
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
                                    Log.d(TAG, "Document removed: " + chatroom.getName());
                                    adapter.removeChatroom(position);
                                }
                                else { // MODIFIED
                                    Log.d(TAG, "Document modified: " + chatroom.getName());
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
        EditTextDialog dialog = new EditTextDialog(this::editText, chatroom.getId(), chatroom.getName());
        dialog.show(getSupportFragmentManager(), "Edit Room Title Dialog");
    }

    // Update in Firebase (listener will update UI)
    private void editText(String new_title, String chatroom_id){
        mFirestore.collection("conversations").document(chatroom_id)
                .update("name", new_title);
    }

    // Delete room entirely
    void deleteChatRoom(String chatroom_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete Conversation for ever?")
                .setPositiveButton("Confirm", (dialog, which) ->
                        mFirestore.collection("conversations").document(chatroom_id)
                        .delete().addOnCompleteListener(Util.getSimpleOnCompleteListener(
                                this,
                                getString(R.string.deleteRoom),
                                getString(R.string.error_deleteRoom))))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Only remove current user from list of members
    void leaveChatRoom(ChatRoom chatRoom){
        if (chatRoom.getMembers().size() < 2){ // Not enough people in a chatroom! (there will be no members...)
            deleteChatRoom(chatRoom.getId());
            return;
        }

        // Change host ?
        if (chatRoom.getHost().equals(this_user.getUsername()))
            mFirestore.collection("conversations").document(chatRoom.getId())
                    .update("members", FieldValue.arrayRemove(this_user.getUsername()),
                            "host", chatRoom.getMembers().get(1))  // Host is 1st member
                    .addOnCompleteListener(Util.getSimpleOnCompleteListener(this,
                            getString(R.string.leaveRoom),
                            getString(R.string.error_leaveRoom)));
        else mFirestore.collection("conversations").document(chatRoom.getId())
                    .update("members", FieldValue.arrayRemove(this_user.getUsername()))
                    .addOnCompleteListener(Util.getSimpleOnCompleteListener(this,
                            getString(R.string.leaveRoom),
                            getString(R.string.error_leaveRoom)));
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