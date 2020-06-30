package ivy.learn.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ChatRoomActivity extends AppCompatActivity {
    private static final String TAG = "ChatRoomActivity";

    // Views
    private TextView tv_room_title;
    private RecyclerView rv_messages;
    private EditText et_message;

    // RecyclerView


    // Firebase

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

        tv_room_title.setText(this_chatroom.getName());
    }

    private void initRecycler(){
        //TODO
        /*adapter = new LobbyAdapter(chatrooms);
        adapter.setOnUserItemClickListener(this);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        rv_chat_rooms.setLayoutManager(manager);
        rv_chat_rooms.setAdapter(adapter);

        getChatRoomsFromDB();*/
    }




/* OnClick Methods
***************************************************************************************************/

    public void openOptions(View view) {
        //TODO open hamburger menu: delete chat, add member, change pic, change name
    }

    public void returnToLobby(View view) {
        finish(); //TODO???
    }

    public void sendMessage(View view) {
        //TODO
    }
}