package ivy.learn.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Lobby where logged-in user can view active chat rooms
 */
public class ChatLobbyActivity extends AppCompatActivity implements LobbyAdapter.OnChatRoomClickListener{
    private static final String TAG = "ChatLobbyActivity";

    // RecyclerView
    private RecyclerView rv_chat_rooms;
    private LobbyAdapter adapter;

    // Firebase
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    // Other Values
    private User this_user;
    private List<ChatRoom> chatrooms = new ArrayList<>();


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
    }

/* Initialization Methods
***************************************************************************************************/

    // Get User object passed from login Activity
    private void getIntentExtras(){
        if (getIntent() != null){
            this_user = getIntent().getParcelableExtra("this_user");
            if (this_user == null) Log.e(TAG, "User parcel was null!");
        }
    }

    private void initViews(){
        // Views
        TextView tv_title = findViewById(R.id.lobby_username);
        rv_chat_rooms = findViewById(R.id.lobby_recyclerview);
        tv_title.setText(this_user.getUsername());
    }

    private void initRecycler(){
        adapter = new LobbyAdapter(chatrooms);
        adapter.setOnUserItemClickListener(this);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        rv_chat_rooms.setLayoutManager(manager);
        rv_chat_rooms.setAdapter(adapter);

        getChatRoomsFromDB();
    }


/* OnClick Methods
***************************************************************************************************/

    public void logoutUser(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void newChatRoom(View view) {
        // TODO construct address
        // TODO add members
        Toast.makeText(this, "New chat room WIP", Toast.LENGTH_SHORT).show();
    }

    // Go to chatroom
    @Override
    public void onShortClick(int position) {
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("chatroom", chatrooms.get(position));
        intent.putExtra("this_user", this_user);
        startActivity(intent);
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

    private void getChatRoomsFromDB(){
        for (String chatroom_address : this_user.getChatroom_addresses()){
            mFirestore.document(chatroom_address).get().addOnCompleteListener(task ->{
               if (task.isSuccessful() && task.getResult() != null) {
                   ChatRoom chatRoom = task.getResult().toObject(ChatRoom.class);
                   if (chatRoom != null){
                       chatrooms.add(chatRoom);
                       adapter.notifyItemInserted(chatrooms.size()-1);
                   }
                   else Log.e(TAG, "Chatroom was null! " + chatroom_address);
               }
               else Log.e(TAG, "Couldn't retrieve Chatroom: " + chatroom_address);
            });
        }
    }
}