package ivy.learn.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import ivy.learn.chat.adapters.LobbyAdapter;
import ivy.learn.chat.utility.ChatRoom;
import ivy.learn.chat.utility.GroupChat;
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
    private SortedList<ChatRoom> chatrooms;

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
        setContentView(R.layout.activity_chatlobby);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Coming back from ChatRoomActivity
        if (requestCode == Util.CHATROOM_REQUEST && resultCode == RESULT_OK){
            selected_chatroom_index = -1;
            /*// Reorder Chatroom lists
            Util.reorderItem(chatrooms, selected_chatroom_index, 0);
            selected_chatroom_index = -1;

            // Update Chatroom and adapter
            adapter.notifyDataSetChanged();*/
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
        adapter = new LobbyAdapter(this_user.getUsername(), this);
        chatrooms = getChatroomSortedList(adapter);
        adapter.setChatrooms(chatrooms);
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
        Intent intent = new Intent(this, NewChatroomActivity.class);
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, Util.NEWCHATROOM_REQUEST);
    }

    // Go to chatroom
    @Override
    public void onShortClick(int position) {
        selected_chatroom_index = position;
        Intent intent;
        if (chatrooms.get(position).getIs_groupChat())
            intent = new Intent(this, GroupChatActivity.class);
        else intent = new Intent(this, ChatRoomActivity.class);

        intent.putExtra("chatroom", chatrooms.get(position));
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, Util.CHATROOM_REQUEST);
    }


    // TODO: use popup window instead?
    //  Remove altogether? Or change options...
    @Override
    public void onLongClick(int position, View v) {
        /*
        if (!(chatrooms.get(position).getIs_groupChat())) return;
        GroupChat selected_room = (GroupChat) chatrooms.get(position);

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

        popup.show();*/
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

                            ChatRoom chatroom;
                            if (docChange.getDocument().getData().get("is_groupChat") == null){
                                Log.e(TAG, "null field... " + docChange.getDocument().getData().toString());
                                break;
                            }
                            if ((boolean)docChange.getDocument().get("is_groupChat"))
                                chatroom = docChange.getDocument().toObject(GroupChat.class);
                            else chatroom = docChange.getDocument().toObject(ChatRoom.class);
                            chatroom.setId(docChange.getDocument().getId());

                            // Update RecyclerView Adapter base on type of change
                            if (docChange.getType() == DocumentChange.Type.ADDED){ // ADDED?
                                Log.d(TAG, "Document added: " + chatroom.getId());
                                adapter.addChatroom(chatroom);

                                // Came back from creating new chatroom?
                                if (selected_chatroom_index == chatrooms.size()-1)
                                    onShortClick(selected_chatroom_index); // Go to chatroom
                            }
                            else if (docChange.getType() == DocumentChange.Type.REMOVED){ // REMOVED ?
                                    Log.d(TAG, "Document removed: " + chatroom.getId());
                                    adapter.removeChatroom(chatroom);
                                }
                            else { // MODIFIED
                                // Get Existing Message position
                                int position = chatrooms.indexOf(chatroom);
                                if (position < 0) {
                                    Log.e(TAG, "Message not found in list! " + chatroom.getId());
                                    break;
                                }
                                Log.d(TAG, "Document modified: " + chatroom.getId());
                                adapter.updateChatroom(position, chatroom);

                            }
                        } Log.d(TAG, queryDocumentSnapshots.size() + " rooms uploaded!");
                    }
                });
    }




/* Chatroom Option Methods (also used in ChatRoomActivity)
***************************************************************************************************/

    private void changeRoomTitle(GroupChat chatroom){
        EditTextDialog dialog = new EditTextDialog(this::editText, chatroom.getId(), chatroom.getName());
        dialog.show(getSupportFragmentManager(), "Edit Room Title Dialog");
    }

    // Update in Firebase (listener will update UI)
    private void editText(String new_title, String chatroom_id){
        mFirestore.collection("conversations").document(chatroom_id)
                .update("name", new_title).addOnCompleteListener(Util.getSimpleOnCompleteListener
                (this,getString(R.string.change_chatroom_title), getString(R.string.error_change_chatroom_title)));
    }

    // Delete room entirely
    private void deleteChatRoom(String chatroom_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.deleteRoom_message))
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
    private void leaveChatRoom(GroupChat chatRoom){
        if (chatRoom.getMembers().size() < 2){ // Not enough people in a chatroom! (there will be no members...)
            deleteChatRoom(chatRoom.getId());
            return;
        }

        // Change host ?
        Task<Void> leave_task;
        DocumentReference docRef = mFirestore.collection("conversations").document(chatRoom.getId());
        if (chatRoom.getHost().equals(this_user.getUsername()))
            leave_task = docRef.update("members", FieldValue.arrayRemove(this_user.getUsername()),
                    "host", chatRoom.getMembers().get(1));  // Host is 1st member
        else leave_task = docRef.update("members", FieldValue.arrayRemove(this_user.getUsername()));

        // Add On Complete Listener
        leave_task.addOnCompleteListener(Util.getSimpleOnCompleteListener(this,
                getString(R.string.leaveRoom),
                getString(R.string.error_leaveRoom)));
    }


/* Utility Methods
***************************************************************************************************/

    public SortedList<ChatRoom> getChatroomSortedList(LobbyAdapter adapter){
        return new SortedList<>(ChatRoom.class, new SortedList.Callback<ChatRoom>() {
            @Override
            public void onInserted(int position, int count) {
                adapter.notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                adapter.notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                adapter.notifyItemMoved(fromPosition, toPosition);
            }

            // TODO: something wrong here!!!
            @Override
            public int compare(ChatRoom o1, ChatRoom o2) {
                int result = -1;
                if (areItemsTheSame(o1, o2)) result = 0;
                else if (o1.getLast_message_timestamp() != null && o2.getLast_message_timestamp() != null)
                    result = o1.getLast_message_timestamp().compareTo(o2.getLast_message_timestamp());
                Log.d(TAG, "o1: " + o1.getId() + ", o2: " + o2.getId());
                Log.d(TAG, "o1 time: " + o1.getLast_message_timestamp() + ", o2 time: " + o2.getLast_message_timestamp());
                Log.d(TAG, "result was: " + result);
                return result;
            }

            @Override
            public void onChanged(int position, int count) {
                adapter.notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(ChatRoom oldItem, ChatRoom newItem) {
                boolean same_timestamp = true;
                if (newItem.getLast_message_timestamp() != null)
                    same_timestamp = newItem.getLast_message_timestamp().equals(oldItem.getLast_message_timestamp());

                boolean same_title = true;
                if (oldItem instanceof GroupChat && newItem instanceof GroupChat)
                    same_title = ((GroupChat) oldItem).getName().equals(((GroupChat) newItem).getName());

                return same_timestamp && same_title;
            }

            @Override
            public boolean areItemsTheSame(ChatRoom item1, ChatRoom item2) {
                if (item1 == null && item2 == null) return true;
                else if (item1 == null || item2 == null) return false;
                else return item1.getId().equals(item2.getId());
            }
        });
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