package ivy.learn.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ivy.learn.chat.adapters.MemberAdapter;
import ivy.learn.chat.utility.ChatRoom;
import ivy.learn.chat.utility.GroupChat;
import ivy.learn.chat.utility.User;


public class SeeAllMembersActivity extends AppCompatActivity implements MemberAdapter.OnUserItemClickListener {

    // Constants
    private final static String TAG = "SeeAllUsersActivity";

    // Variables passed by intent
    private User this_user;             // Current user
    private GroupChat this_chatroom;


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seemembers);

        // Initialization
        getIntentExtras();          // Get array for recycler
        setRecycler();
    }


/* Initialization Methods
***************************************************************************************************/

    // Get a list of user ids to display
    private void getIntentExtras(){
        if (getIntent() != null){
            this_user = getIntent().getParcelableExtra("this_user");        // currently logged in user
            this_chatroom = getIntent().getParcelableExtra("chatroom");     // chatroom whose members we want to show

            if (this_chatroom == null || this_user == null) {
                Log.e(TAG, "Must Provide a list of user ids and uni domain.");
                finish();
            }
        }
    }

    // Initialize recycler values and load first batch of items
    private void setRecycler(){
        // Views
        RecyclerView recycler_view = findViewById(R.id.seeMembers_recycler);

        // Set LayoutManager and Adapter
        MemberAdapter adapter = new MemberAdapter(this_chatroom.getMembers(), this_user, this_chatroom);
        adapter.setOnUserItemClickListener(this);   // Check onNameClick and onOptionClick methods
        LinearLayoutManager layout_man = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(layout_man);
        recycler_view.setAdapter(adapter);
    }


/* OnClick Methods
***************************************************************************************************/

    // OnLongClick for username: popup wih options
    @Override
    public void onUserClick(int position, View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.userOptions_message) {
                createChatWithUser(this_chatroom.getMembers().get(position));
                return true;
            } else return false;
        });
        popup.inflate(R.menu.user_options);
        popup.show();
    }


    @Override
    public void onKickClick(int position) {
        // TODO kick member
    }

    private void createChatWithUser(String username) {
        // TODO create a private chat with user
    }


/* Transition Methods
***************************************************************************************************/

    // Handle Up Button
    public void goBackToChatroom(View v){
        Log.d(TAG, "Returning to parent");
        Intent intent;

        // Try to go back to activity that called startActivityForResult()
        if (getCallingActivity() != null)
            intent = new Intent(this, getCallingActivity().getClass());
        else intent = new Intent(this, ChatRoomActivity.class); // Go to room as default

        setResult(RESULT_OK, intent);
        finish();
    }
}
