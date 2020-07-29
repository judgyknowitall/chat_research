package ivy.learn.chat.chatKit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ivy.learn.chat.ChatRoomActivity;
import ivy.learn.chat.R;
import ivy.learn.chat.utility.ChatRoom;
import ivy.learn.chat.utility.Util;

/**
 * Docs:
 * https://github.com/stfalcon-studio/ChatKit/blob/master/docs/COMPONENT_DIALOGS_LIST.MD
 */
public class DialogListActivity extends AppCompatActivity {

    private Author this_user;

    // Firebase
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private List<ListenerRegistration> listeners = new ArrayList<>();


/* Overridden Methods
***************************************************************************************************/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatkit_dialogslist);

        this_user = new Author("author_1", "Author1");
        setAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setDialogListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (ListenerRegistration listener : listeners)
            listener.remove();
    }


/* Initialization Methods
***************************************************************************************************/

    /* Adapter class can be extended for custom ViewHolder & functions
     * Current functions:
     * adapter.setItems(List items) - replaces existing list with a new dialog list;
     * adapter.addItems(List items) - adds a new dialog list to the end of the list;
     * adapter.addItem(DIALOG dialog) - adds one dialog to the end of the list
     * adapter.addItem(int position, DIALOG dialog) - adds a new dialog to the specified position.
     * adapter.upsertItem(DIALOG dialog) - adds one dialog to the end of the list if not exists, otherwise updates the existing dialog.
     * adapter.updateItem(int position, DIALOG item)
     * adapter.updateItemById(DIALOG item)
     * adapter.updateDialogWithMessage(String dialogId, IMessage message) - returns false if not found
     * adapter.deleteById(String id)
     * adapter.clear() - deletes all dialogs
     */
    private void setAdapter(){

        DialogsListAdapter<DefaultDialog> dialogsListAdapter = new DialogsListAdapter<>((imageView, url, payload) -> {
            Picasso.get().load(url).into(imageView);    // If you using another library - write here your way to load image
        });
        DialogsList dialogsListView = findViewById(R.id.dialogsList);
        dialogsListView.setAdapter(dialogsListAdapter);

        dialogsListAdapter.setOnDialogClickListener(this::onDialogClick);

        dialogsListAdapter.setOnDialogLongClickListener(dialog -> {
            // TODO
        });

        // Dates format
        dialogsListAdapter.setDatesFormatter(Util::format);

        // For TESTING
        DefaultDialog d1 = new DefaultDialog("diag_1", null, "Dialog 1");
        d1.setLastMessage(new Message("message_1", "last message for diag_1", this_user));
        DefaultDialog d2 = new DefaultDialog("diag_2", null, "Dialog 2");
        d2.setLastMessage(new Message("message_2", "last message for diag_2", this_user));

        dialogsListAdapter.addItem(d1);
        dialogsListAdapter.addItem(d2);

    }

/* OnClick Methods
***************************************************************************************************/




    public void onDialogClick(DefaultDialog dialog) {
        Intent intent = new Intent(this, MessageListActivity.class);
        intent.putExtra("dialog", dialog);
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, Util.CHATROOM_REQUEST);
    }

/* Firebase related Methods
***************************************************************************************************/

    private void setDialogListener(){
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
}