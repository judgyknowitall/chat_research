package ivy.learn.chat.chatKit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;
import java.util.List;

import ivy.learn.chat.R;
import ivy.learn.chat.utility.Util;

/**
 * Docs:
 * https://github.com/stfalcon-studio/ChatKit/blob/master/docs/COMPONENT_DIALOGS_LIST.MD
 */
public class DialogListActivity extends AppCompatActivity {
    private static final String TAG = "DialogListActivity";

    private Author this_user;
    private DialogsListAdapter<Dialog> adapter;

    // Firebase
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private List<ListenerRegistration> list_regs = new ArrayList<>();


/* Overridden Methods
***************************************************************************************************/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatkit_dialogslist);

        this_user = new Author("user1");
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
        for (ListenerRegistration list_reg : list_regs)
            list_reg.remove();  // No need to listen if you're not there
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

        adapter = new DialogsListAdapter<>((imageView, url, payload) -> {
            // If you using another library - write here your way to load image
            if (url != null && !url.isEmpty()) Picasso.get().load(url).into(imageView);
        });
        adapter.setOnDialogClickListener(this::onDialogClick);
        adapter.setOnDialogLongClickListener(dialog -> {
            // TODO
        });
        adapter.setDatesFormatter(Util::format);    // Dates format

        DialogsList dialogsListView = findViewById(R.id.dialogsList);
        dialogsListView.setAdapter(adapter);
    }

/* OnClick Methods
***************************************************************************************************/

    public void onDialogClick(Dialog dialog) {
        Intent intent = new Intent(this, MessageListActivity.class);
        intent.putExtra("dialog", dialog);
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, Util.CHATROOM_REQUEST);
    }

/* Firebase related Methods
***************************************************************************************************/

    // TODO
    private void setDialogListener(){
        list_regs.add(
        mFirestore.collection("dialogs")
                .whereArrayContains("userIds", this_user.getId())
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) Log.w(TAG, "Error in attaching listener.", e);
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentChange docChange : queryDocumentSnapshots.getDocumentChanges()) {

                            // Get dialog object from results
                            Dialog dialog = docChange.getDocument().toObject(Dialog.class);
                            dialog.setId(docChange.getDocument().getId());

                            // Update RecyclerView Adapter base on type of change
                            if (docChange.getType() == DocumentChange.Type.ADDED) addNewDialog(dialog);
                            else if (docChange.getType() == DocumentChange.Type.REMOVED) removeDialog(dialog);
                            else updateDialog(dialog); // MODIFIED
                        } Log.d(TAG, queryDocumentSnapshots.size() + " rooms uploaded!");
                    }
                }));
    }

    // Sets a listener to latest message in a dialog so time_stamp gets updated real time
    // Then, it adds the updated dialog to the sorted list
    private void addNewDialog(Dialog dialog) {
        Log.d(TAG, "Document added: " + dialog.getId());
        String address = "dialogs/" + dialog.getId() + "/messages";

        list_regs.add(
        mFirestore.collection(address)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        DocumentChange docChange = queryDocumentSnapshots.getDocumentChanges().get(0);
                        Log.d(TAG, "Last message is: " + docChange.getDocument().getData());
                        dialog.setLastMessage(docChange.getDocument().toObject(Message.class));
                    }
                    adapter.upsertItem(dialog);     // Updates dialog if already exists (inserts otherwise)
                    Log.d(TAG, dialog.getDialogName() + " ADDED DIALOG");
                }));
    }

    //TODO needs more testing
    private void updateDialog(Dialog dialog) {
        Log.d(TAG, "Document modified: " + dialog.getId());
       adapter.updateItemById(dialog);
    }

    // TODO needs testing
    private void removeDialog(Dialog dialog) {
        Log.d(TAG, "Document removed: " + dialog.getId());
        adapter.deleteById(dialog.getId());
    }
}