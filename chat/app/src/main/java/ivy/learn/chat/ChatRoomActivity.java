package ivy.learn.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ivy.learn.chat.entities.ChatRoom;
import ivy.learn.chat.entities.Message;
import ivy.learn.chat.entities.User;

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

    // Other Values
    private ChatRoom this_chatroom;
    private User this_user;

/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        getIntentExtras();

        if (this_user != null && this_chatroom != null){
            initViews();
            setListeners();
            initRecycler();
        } else Log.e(TAG, "A parcel was null!");
    }


/* Initialization Methods
***************************************************************************************************/

    // Get User object passed from login Activity
    private void getIntentExtras(){
        if (getIntent() != null){
            this_user = getIntent().getParcelableExtra("this_user");
            this_chatroom = getIntent().getParcelableExtra("chatroom");
        }
    }

    private void initViews(){
        tv_room_title = findViewById(R.id.room_title);
        rv_messages = findViewById(R.id.room_recyclerview);
        et_message = findViewById(R.id.room_writeMessage);
        send_button = findViewById(R.id.room_sendButton);

        tv_room_title.setText(this_chatroom.getName());
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
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initRecycler(){

        adapter = new RoomAdapter(messages, this_user.getUsername());
        adapter.setOnUserItemClickListener(this);
        layout_man = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true);
        rv_messages.setLayoutManager(layout_man);
        rv_messages.setAdapter(adapter);

        getMessagesFromDB();

        // Scroll Listener used for pagination
        rv_messages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (message_list_updated) {
                    if (layout_man.findLastCompletelyVisibleItemPosition() > (messages.size() - 1 )){
                        message_list_updated = false;
                        getMessagesFromDB();
                    }
                }
            }
        });
    }


/* OnClick Methods
***************************************************************************************************/

    public void openOptions(View view) {
        //TODO open hamburger menu: delete chat, add member, change pic, change name
        Toast.makeText(this, "Options under construction", Toast.LENGTH_SHORT).show();
    }

    public void returnToLobby(View view) {
        if (isTaskRoot()) {
            Intent intent = new Intent(this, ChatLobbyActivity.class);
            intent.putExtra("this_user", this_user);
            startActivity(intent);
        }
        finish();
    }

    public void sendMessage(View view) {

        messagePending(true);   // Disable sending message again

        // Show in UI first
        Message newMessage = new Message(this_user.getUsername(), et_message.getText().toString().trim());


        // Then add to Database
    }

    @Override
    public void onShortClick(int position) {
        //TODO see timestamp
    }

    @Override
    public void onLongClick(int position) {
        //TODO popup: delete message / forward message / copy
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
        String address = "usernames/" + this_chatroom.getHost() + "/conversations/" + this_chatroom.getName() + "/messages";
        if (address.contains("null")){
            Log.e(TAG, "chatroom address contained null values");
            return;
        }

        // Build Query
        Query query = mFirestore.collection(address)
                .orderBy("time_stamp", Query.Direction.ASCENDING)
                .limit(PAGE_LIMIT);
        if (last_doc != null) query = query.startAfter(last_doc);

        query.get().addOnCompleteListener(task -> {
                   if (task.isSuccessful() && task.getResult() != null) {
                       QuerySnapshot query_doc = task.getResult();
                       int i = 0;
                       for (QueryDocumentSnapshot doc : query_doc){
                           messages.add(doc.toObject(Message.class));
                           adapter.notifyItemInserted(messages.size()-1);
                           i++;
                       }
                       if (!query_doc.isEmpty())
                           last_doc = query_doc.getDocuments().get(query_doc.size()-1);    // Save last doc retrieved
                       Log.d(TAG, i + " messages were uploaded from database!");
                   }
                   else Log.e(TAG, "Couldn't retrieve messages.", task.getException());
                });
    }
}