package ivy.learn.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import ivy.learn.chat.utility.GroupChat;
import ivy.learn.chat.utility.User;
import ivy.learn.chat.utility.Util;

/**
 * Container activity for main messaging fragment
 * Used for group Messaging only
 * Contains group chatroom actions in navigation drawer
 */
public class GroupChatActivity extends AppCompatActivity {
    private static final String TAG = "GroupChatActivity";

    // Views
    TextView tv_room_title;
    private DrawerLayout drawer;

    // Firebase
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    // Other Values
    private GroupChat this_chatroom;
    private User this_user;

/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);
        getIntentExtras();

        if (this_user != null && this_chatroom != null){
            initViews();
            initNavDrawer();
            setFragment();
        } else Log.e(TAG, "A parcel was null!");
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END))
            drawer.closeDrawer(GravityCompat.END);
        else super.onBackPressed();
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
        // Views
        tv_room_title = findViewById(R.id.room_title);
        drawer = findViewById(R.id.room_drawerLayout);

        // Set Room Title
        if (this_chatroom.getIs_groupChat())    // group chat
            tv_room_title.setText((this_chatroom).getName());
        else {                                  // private messaging
            tv_room_title.setText(this_chatroom.getMembers().get(0));
            if (this_chatroom.getMembers().size() > 1 && this_user.getUsername().equals(this_chatroom.getMembers().get(0)))
                tv_room_title.setText(this_chatroom.getMembers().get(1));
        }
    }

    // Initialize Navigation Drawer: set colours, listener, and hide some items if necessary
    private void initNavDrawer(){
        NavigationView nav = findViewById(R.id.groupChat_navView);

        // Navigation Drawer:
        Menu nav_Menu = nav.getMenu();
        if (!this_user.getUsername().equals(this_chatroom.getHost())) { // hide host-only actions
            nav_Menu.findItem(R.id.roomNavOptions_changeTitle).setVisible(false);   // only host can change room title
            nav_Menu.findItem(R.id.roomNavOptions_delete).setVisible(false);        // only host can delete chatroom
        }
        // Set colours
        nav.setItemIconTintList(null);
        Util.colorMenuItem(nav_Menu.findItem(R.id.roomNavOptions_delete), getColor(R.color.red));
        Util.colorMenuItem(nav_Menu.findItem(R.id.roomNavOptions_leave), getColor(R.color.red));

        // Drawer item selection
        nav.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    // Set main messaging fragment
    private void setFragment(){
        Fragment frag = new ChatRoomFragment(this_chatroom, this_user);
        getSupportFragmentManager().beginTransaction().replace(R.id.room_frameLayout, frag).commit();
    }


/* OnClick Methods
***************************************************************************************************/

    public void openOptions(View view) {
        if (drawer.isDrawerOpen(GravityCompat.END))
            drawer.closeDrawer(GravityCompat.END);
        else drawer.openDrawer(GravityCompat.END);

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

    // OnClick listener for drawer navigation items
    private boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.roomNavOptions_members:
                viewMembers();
                break;

            case R.id.roomNavOptions_addMembers:
                addMembers();
                break;

            case R.id.roomNavOptions_changeTitle:
                changeRoomTitle();
                break;

            case R.id.roomNavOptions_leave:
                leaveChatRoom();
                break;

            case R.id.roomNavOptions_delete:
                deleteChatRoom();
                break;
        }
        drawer.closeDrawer(GravityCompat.END);
        return true;
    }


/* Chatroom Navigation Options
***************************************************************************************************/


    // Delete Chatroom completely and return to Lobby
    private void deleteChatRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.deleteRoom_title))
                .setMessage(getString(R.string.deleteRoom_message))
                .setPositiveButton("Confirm", (dialog, which) ->
                        mFirestore.collection("conversations").document(this_chatroom.getId())
                                .delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(this, getString(R.string.deleteRoom), Toast.LENGTH_SHORT).show();
                                returnToLobby(drawer);
                            } else {
                                Toast.makeText(this, getString(R.string.error_deleteRoom), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, getString(R.string.error_deleteRoom), task.getException());
                            }
                        }))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Leave Chatroom and return to Lobby
    private void leaveChatRoom() {
        if (this_chatroom.getMembers().size() < 2){ // Not enough people in a chatroom! (there will be no members...)
            deleteChatRoom();
            return;
        }

        // Change host ?
        Task<Void> leave_task;
        DocumentReference docRef = mFirestore.collection("conversations").document(this_chatroom.getId());
        if (this_chatroom.getHost().equals(this_user.getUsername()))
            leave_task = docRef.update("members", FieldValue.arrayRemove(this_user.getUsername()),
                    "host", this_chatroom.getMembers().get(1));  // Host is 1st member
        else leave_task = docRef.update("members", FieldValue.arrayRemove(this_user.getUsername()));

        // Add On Complete Listener
        leave_task.addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(this, getString(R.string.leaveRoom), Toast.LENGTH_SHORT).show();
                returnToLobby(drawer);
            } else {
                Toast.makeText(this, getString(R.string.error_leaveRoom), Toast.LENGTH_SHORT).show();
                Log.e(TAG, getString(R.string.error_leaveRoom), task.getException());
            }
        });
    }

    // Change Chatroom Title (hosts only)
    private void changeRoomTitle() {
        EditTextDialog dialog = new EditTextDialog(this::editChatroomText, this_chatroom.getId(), this_chatroom.getName());
        dialog.show(getSupportFragmentManager(), "Edit Room Title Dialog");
        tv_room_title.setText(this_chatroom.getName());
    }

    // Updates chatroom title in firebase and in UI
    private void editChatroomText(String new_title, String chatroom_id) {
        mFirestore.collection("conversations").document(chatroom_id)
                .update("name", new_title).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(this, getString(R.string.change_chatroom_title), Toast.LENGTH_SHORT).show();
                tv_room_title.setText(new_title);
            } else {
                Toast.makeText(this, getString(R.string.error_change_chatroom_title), Toast.LENGTH_SHORT).show();
                Log.e(TAG, getString(R.string.error_change_chatroom_title), task.getException());
            }
        });
    }

    // Add members to chatroom
    private void addMembers() {
        Toast.makeText(this, "add members TODO", Toast.LENGTH_SHORT).show();
        //TODO
    }

    private void viewMembers() {
        Intent intent = new Intent(this, SeeAllMembersActivity.class);
        intent.putExtra("this_user", this_user);
        intent.putExtra("chatroom", this_chatroom);
        startActivity(intent);
    }
}
