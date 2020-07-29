package ivy.learn.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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

    // Firebase
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();


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
        String memberToKick = this_chatroom.getMembers().get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.kickMember_title))
                .setMessage(getString(R.string.kickMember_message) + memberToKick + "?")
                .setPositiveButton("Confirm", (dialog, which) ->
                        mFirestore.collection("conversations").document(this_chatroom.getId())
                                .update("members", FieldValue.arrayRemove(memberToKick)).addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                this_chatroom.removeMember(memberToKick);
                                Toast.makeText(this, memberToKick + getString(R.string.kickMember), Toast.LENGTH_SHORT).show();
                                goBackToChatroom(null);
                            } else {
                                Toast.makeText(this, getString(R.string.error_kickMember), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.error_kickMember), task.getException());
                            }
                        }))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
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
        intent.putExtra("updated_chatroom", this_chatroom);
        finish();
    }
}
