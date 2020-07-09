package ivy.learn.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ivy.learn.chat.adapters.RoomAdapter;
import ivy.learn.chat.utility.ChatRoom;
import ivy.learn.chat.utility.Message;
import ivy.learn.chat.utility.User;

public class ChatRoomActivity extends AppCompatActivity implements RoomAdapter.OnMessageClickListener {
    private static final String TAG = "ChatRoomActivity";

    // Views
    private TextView tv_room_title;
    private RecyclerView rv_messages;
    private EditText et_message;
    private ImageButton send_button;

    // RecyclerView
    RoomAdapter adapter;
    List<Message> messages = new ArrayList<>();
    private LinearLayoutManager layout_man;
    private static final int PAGE_LIMIT = 10;
    private DocumentSnapshot last_doc;                  // Snapshot of last message loaded
    private boolean message_list_updated;

    // Firebase
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    List<ListenerRegistration> listeners = new ArrayList<>();

    // Other Values
    private String chatroom_messages_address;
    private ChatRoom this_chatroom;
    private User this_user;

/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        getIntentExtras();

        if (this_user != null && this_chatroom != null && !chatroom_messages_address.contains("null")){
            initViews();
            setListeners();
            initRecycler();
        } else Log.e(TAG, "A parcel was null!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Detach Listeners
        for (ListenerRegistration listener : listeners){
            listener.remove();
        }
    }

/* Initialization Methods
***************************************************************************************************/

    // Get User object passed from login Activity
    private void getIntentExtras(){
        if (getIntent() != null){
            this_user = getIntent().getParcelableExtra("this_user");
            this_chatroom = getIntent().getParcelableExtra("chatroom");
            if (this_chatroom != null)
                chatroom_messages_address = "conversations/" + this_chatroom.getName() + "/messages";
        }
    }

    private void initViews(){
        tv_room_title = findViewById(R.id.room_title);
        rv_messages = findViewById(R.id.room_recyclerview);
        et_message = findViewById(R.id.room_writeMessage);
        send_button = findViewById(R.id.room_sendButton);

        if (this_chatroom.getName().equals(this_user.getUsername()))
            tv_room_title.setText(this_chatroom.getHost());     // private messaging
        else
            tv_room_title.setText(this_chatroom.getName());     // group chat
    }

    private void setListeners(){
        // Send Message if pressed [ENTER]
        et_message.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.room_sendButton || id == EditorInfo.IME_NULL) {
                sendMessage(textView);
                return true;
            }
            else return false;
        });

        // Disable send button if no message to send
        et_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                send_button.setClickable(!et_message.getText().toString().trim().isEmpty());
                if (send_button.isClickable()) send_button.setColorFilter(getColor(R.color.colorPrimary));
                else send_button.setColorFilter(getColor(R.color.grey));

                // Scroll to bottom while editing text ? TODO only when keyboard comes up?
                if (layout_man.findFirstCompletelyVisibleItemPosition() != 0) rv_messages.smoothScrollToPosition(0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initRecycler(){

        adapter = new RoomAdapter(messages, this_user.getUsername());
        adapter.setOnUserItemClickListener(this);
        layout_man = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true);
        layout_man.setStackFromEnd(true);   // Always show bottom of recycler
        rv_messages.setLayoutManager(layout_man);
        rv_messages.setAdapter(adapter);

        getMessagesFromDB();

        // Scroll Listener used for pagination
        rv_messages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (message_list_updated) {
                    if (layout_man.findLastCompletelyVisibleItemPosition() > (messages.size() - 2 )){
                        message_list_updated = false;
                        Log.d(TAG, "Update Messages!!!");
                        getMessagesFromDB();
                    }
                }
            }
        });
    }


/* OnClick Methods
***************************************************************************************************/

    public void openOptions(View view) {
        //TODO open hamburger menu: delete chat, add member, change name
        Toast.makeText(this, "Options under construction", Toast.LENGTH_SHORT).show();
    }

    public void returnToLobby(View view) {
        if (isTaskRoot()) { // Got here from a notification
            Intent intent = new Intent(this, ChatLobbyActivity.class);
            intent.putExtra("this_user", this_user);
            startActivity(intent);
        }
        else { // Got here from lobby
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    public void sendMessage(View view) {
        String text = et_message.getText().toString().trim();
        if (text.isEmpty()) return;

        messagePending(true);   // Disable sending message again
        Message newMessage = new Message(this_user.getUsername(), text);
        et_message.setText(null);
        sendMessageToDB(newMessage);
    }

    @Override
    public void onLongClick(int position, View v) {
        Message clicked_message = messages.get(position);

        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.messageOptions_copy:
                    copyTextToClipboard(clicked_message.getText());
                    return true;

                case R.id.messageOptions_edit:
                    editMessage(clicked_message.getId());
                    return true;

                case R.id.messageOptions_delete:
                    deleteMessage(clicked_message.getId());
                    return true;
            }
            return false;
        });
        popup.inflate(R.menu.message_options);

        // Hide certain items if not logged in
        if (!messages.get(position).getAuthor().equals(this_user.getUsername()) ){
            Menu menu = popup.getMenu();
            menu.findItem(R.id.messageOptions_delete).setVisible(false);
            menu.findItem(R.id.messageOptions_edit).setVisible(false);
        } else popup.setGravity(Gravity.END);

        popup.show();
    }

    private void copyTextToClipboard(String text){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(TAG, text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "copied to clipboard", Toast.LENGTH_SHORT).show();
    }


/* Transition Methods
***************************************************************************************************/

// TODO: some loading mechanism?


    private void messagePending(boolean load){
        ProgressBar send_pending = findViewById(R.id.room_sendLoading);

        // Start loading
        if (load){
            send_button.setVisibility(View.INVISIBLE);
            send_button.setClickable(false);
            send_pending.setVisibility(View.VISIBLE);
        }
        // Stop loading
        else {
            send_button.setVisibility(View.VISIBLE);
            send_button.setClickable(true);
            send_pending.setVisibility(View.GONE);
        }
    }


/* Firebase related Methods
***************************************************************************************************/

    private void getMessagesFromDB() {
        // Build Query
        Query query = mFirestore.collection(chatroom_messages_address)
                .orderBy("time_stamp", Query.Direction.DESCENDING)
                .limit(PAGE_LIMIT);
        if (last_doc != null) query = query.startAfter(last_doc);

        Query finalQuery = query;
        query.get().addOnCompleteListener(task -> {
           if (task.isSuccessful() && task.getResult() != null) {

               // Extract useful data from result
               QuerySnapshot query_doc = task.getResult();
               DocumentSnapshot first = null, last = null;
               if (!query_doc.isEmpty()) {
                   first = query_doc.getDocuments().get(0);
                   last = query_doc.getDocuments().get(query_doc.size()-1);
               }

               // Get messages and add to recycler
               for (QueryDocumentSnapshot doc : query_doc){
                   Message message = doc.toObject(Message.class);
                   message.setId(doc.getId());
                   messages.add(message);
                   adapter.notifyItemInserted(messages.size()-1);
               }

               // Update pagination and add snapshot listener
               if (first != null && last != null) {

                   // Add listener for loaded data
                   // Also listen to new upcoming messages if query != older messages
                   // Added to a list so they can be removed later
                   if (last_doc == null) { // This is the first get Request
                       listeners.add(finalQuery.endBefore(first).addSnapshotListener(getListener(true)));
                       rv_messages.scrollToPosition(0); // Scroll to bottom on first get
                   }
                   listeners.add(finalQuery.startAt(first).endAt(last).addSnapshotListener(getListener(false)));

                   // Update pagination data
                   last_doc = last;             // Save last doc retrieved
                   message_list_updated = true;
               }
               Log.d(TAG, query_doc.size() + " messages were uploaded from database!");
           }
           else Log.e(TAG, "Couldn't retrieve messages.", task.getException());
        });
    }

    // Get an event listener depending on if we want to add new messages
    private EventListener<QuerySnapshot> getListener(boolean addNewMessages) {
        return (queryDocumentSnapshots, e) -> {
            if (e != null) Log.w(TAG, "Error in attaching listener.", e);
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {

                    // Get Message
                    Message message = docChange.getDocument().toObject(Message.class);
                    message.setId(docChange.getDocument().getId());

                    // Update RecyclerView Adapter base on type of change
                    if (docChange.getType() == DocumentChange.Type.ADDED && addNewMessages){
                        Log.d(TAG, "Document added.");
                        messages.add(0, message);
                        adapter.notifyItemInserted(0);
                        rv_messages.scrollToPosition(0);
                    }
                    else {
                        // Get Existing Message position
                        int position = messages.indexOf(message);
                        if (position < 0) {
                            Log.e(TAG, "Message not found in list! " + message.getId());
                            break;
                        }
                        // REMOVED ?
                        if (docChange.getType() == DocumentChange.Type.REMOVED){
                            Log.d(TAG, "Document removed.");
                            adapter.removeMessage(position);
                        }
                        else { // MODIFIED
                            Log.d(TAG, "Document modified.");
                            messages.get(position).setText(message.getText());
                            adapter.notifyItemChanged(position);
                        }
                    }
                }
            }
        };
    }


/* Message Option Methods
***************************************************************************************************/

    // Send a single message to database
    private void sendMessageToDB(Message newMessage) {
        mFirestore.collection(chatroom_messages_address).add(newMessage).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) Log.e(TAG, "Couldn't send message!", task.getException());
            messagePending(false);
        });
    }

    // Pop up a dialog to edit message
    private void editMessage(String message_id){
        EditTextDialog dialog = new EditTextDialog(this::editText, message_id);
        dialog.show(getSupportFragmentManager(), "Edit Text Dialog");
    }

    // Update in Firebase (listener will update UI)
    private void editText(String new_text, String message_id){
        mFirestore.collection(chatroom_messages_address).document(message_id)
                .update("text", new_text);
    }

    // Pop up dialog to confirm delete
    private void deleteMessage(String message_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete message?")
                .setPositiveButton("Confirm", (dialog, which) ->
                        mFirestore.collection(chatroom_messages_address).document(message_id)
                        .delete())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}