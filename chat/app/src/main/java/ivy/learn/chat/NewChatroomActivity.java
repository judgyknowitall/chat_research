package ivy.learn.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ivy.learn.chat.adapters.AddUserAdapter;
import ivy.learn.chat.utility.ChatRoom;
import ivy.learn.chat.utility.GroupChat;
import ivy.learn.chat.utility.User;

public class NewChatroomActivity extends AppCompatActivity {
    private static final String TAG = "NewChatroomActivity";

    // Views
    ImageButton button_confirm;
    EditText et_searchUser;
    ImageButton button_search;
    RecyclerView rv_users;

    // RecyclerView
    AddUserAdapter adapter;
    List<User> users = new ArrayList<>();
    private LinearLayoutManager layout_man;
    private static final int PAGE_LIMIT = 10;
    private DocumentSnapshot last_doc;                  // Snapshot of last user loaded
    private boolean user_list_updated;

    // Firebase
    FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    // Other Values
    private User this_user;
    private ChatRoom chatroom;
    private Set<Integer> selected_positions = new HashSet<>();


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosepeople);
        getIntentExtras();

        if (this_user != null) {
         initViews();
         setListeners();
         initRecycler();
        } else Log.e(TAG, "A parcel was null!");
    }


/* Initialization Methods
***************************************************************************************************/

// Get User object passed from Lobby Activity
    private void getIntentExtras(){
        if (getIntent() != null)
            this_user = getIntent().getParcelableExtra("this_user");
    }

    private void initViews(){
        button_confirm = findViewById(R.id.newChat_okButton);
        et_searchUser = findViewById(R.id.newChat_searchUser);
        button_search = findViewById(R.id.newChat_searchButton);
        rv_users = findViewById(R.id.newChat_recyclerview);
    }

    private void setListeners(){
        // TODO Search user edit text -> show users based on search
    }

    private void initRecycler(){

        adapter = new AddUserAdapter(users, this::onSelected);
        layout_man = new LinearLayoutManager(this);
        rv_users.setLayoutManager(layout_man);
        rv_users.setAdapter(adapter);

        getUsersFromDB();

        // Scroll Listener used for pagination
        rv_users.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (user_list_updated) {
                    if (layout_man.findLastCompletelyVisibleItemPosition() > (users.size() - 3 )){
                        user_list_updated = false;
                        Log.d(TAG, "Update Users!!!");
                        getUsersFromDB();
                    }
                }
            }
        });
    }


/* OnClick Methods
***************************************************************************************************/

    // OnClick for search image button
    public void searchUser(View view) {
        //TODO
    }

    // OnClick for confirm chatroom creation
    public void createNewChatroom(View view) {
        if (selected_positions.size() == 0){
            Toast.makeText(this, "Must select members.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Group chat or private chat?
        if (selected_positions.size() > 1)
            chatroom = new GroupChat(this_user.getUsername());
        else chatroom = new ChatRoom(this_user.getUsername());

        for (Integer i : selected_positions)    // Add member usernames to chatroom object
            chatroom.addMember(users.get(i).getUsername());

        addChatroomInDB();      // Add chatroom to DB
    }

    // OnClick for RecyclerView Items
    public void onSelected(int position, boolean checked){
        if (checked) selected_positions.add(position);
        else selected_positions.remove(position);
    }


/* Transition Methods
***************************************************************************************************/

    public void returnToLobby(View view) {
        Intent returnIntent = new Intent();
        if (view == button_confirm && chatroom != null)
            setResult(Activity.RESULT_OK, returnIntent);
        else setResult(RESULT_CANCELED);
        finish();
    }


/* Firebase related Methods
***************************************************************************************************/

    private void getUsersFromDB(){
        // Build Query
        Query query = mFirestore.collection("usernames").limit(PAGE_LIMIT);
        if (last_doc != null) query = query.startAfter(last_doc);

        // Make a get request from Firebase
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot query_doc = task.getResult();

                // Get Users and add to recycler
                for (QueryDocumentSnapshot doc : query_doc) {
                    users.add(doc.toObject(User.class));
                    adapter.notifyItemInserted(users.size()-1);
                }

                // Update pagination data
                last_doc = query_doc.getDocuments().get(query_doc.size()-1); // Save last doc retrieved
                user_list_updated = true;

                Log.d(TAG, query_doc.size() + " users were uploaded from database!");

            }
            else Log.e(TAG, "Couldn't retrieve users.", task.getException());
        });
    }

    // Create a new chatroom in Firebase
    private void addChatroomInDB(){
        mFirestore.collection("conversations").add(chatroom).addOnCompleteListener(task -> {
            if (task.isSuccessful()){

                // Add chatroom address to all members' list of conversations
                //this_user.addChatroom_address(0, chatroom_addr);
                //addChatroomAddrToMembers(chatroom_addr);

                Log.d(TAG, "Added new Chatroom to database");
                returnToLobby(button_confirm);
            }
            else Log.e(TAG, "Wasn't able to add chatroom", task.getException());
        });
    }


    // TODO: instead, add conversation to "last-read map"
    // Add new chatroom address to all members' conversation lists
    private void addChatroomAddrToMembers(String chatroom_address){
        for (String member : chatroom.getMembers()){
            String user_address = "usernames/" + member;
            mFirestore.document(user_address).update("chatroom_addresses", FieldValue.arrayUnion(chatroom_address))
                    .addOnCompleteListener( task -> {
                        if (task.isSuccessful()){

                            if (member.equals(this_user.getUsername())) {
                                returnToLobby(button_confirm);
                            }

                            Log.d(TAG, "Updated conversation list for user: " + member);
                        }
                        else Log.e(TAG, "Couldn't update conversation list for user: " + member, task.getException());
                    });
        }
    }

}