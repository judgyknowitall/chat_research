package ivy.learn.chat.chatKit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.Date;

import ivy.learn.chat.ChatRoomActivity;
import ivy.learn.chat.R;
import ivy.learn.chat.utility.Util;

/**
 * Docs:
 * https://github.com/stfalcon-studio/ChatKit/blob/master/docs/COMPONENT_DIALOGS_LIST.MD
 */
public class DialogListActivity extends AppCompatActivity {

    private Author this_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatkit_dialogslist);

        this_user = new Author("author_1", "Author1");
        setAdapter();
    }


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
        dialogsListAdapter.setDatesFormatter(this::format);

        // For TESTING
        DefaultDialog d1 = new DefaultDialog("diag_1", null, "Dialog 1");
        d1.setLastMessage(new Message("message_1", "last message for diag_1", this_user));
        DefaultDialog d2 = new DefaultDialog("diag_2", null, "Dialog 2");
        d2.setLastMessage(new Message("message_2", "last message for diag_2", this_user));

        dialogsListAdapter.addItem(d1);
        dialogsListAdapter.addItem(d2);

    }

    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return DateFormatter.format(date, DateFormatter.Template.TIME);
        } else if (DateFormatter.isYesterday(date)) {
            return getString(R.string.date_header_yesterday);
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }

    public void onDialogClick(DefaultDialog dialog) {
        Intent intent = new Intent(this, MessageListActivity.class);
        intent.putExtra("dialog", dialog);
        intent.putExtra("this_user", this_user);
        startActivityForResult(intent, Util.CHATROOM_REQUEST);
    }
}