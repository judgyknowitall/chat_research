package ivy.learn.chat.chatKit;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import ivy.learn.chat.R;
import ivy.learn.chat.utility.Util;

/**
 * Docs:
 * https://github.com/stfalcon-studio/ChatKit/blob/master/docs/COMPONENT_MESSAGES_LIST.md
 * https://github.com/stfalcon-studio/ChatKit/blob/master/docs/COMPONENT_MESSAGE_INPUT.MD
 */
public class MessageListActivity  extends AppCompatActivity {
    private static final String TAG = "MessageListActivity";

    private Author this_user;
    private Dialog this_dialog;

    private int counter = 0;
    private MessagesListAdapter<Message> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatkit_messageslist);

        getIntentExtras();
        if (this_user != null && this_dialog != null){
            setAdapter();
            setMessageInput();
        }
        else Log.e(TAG, "Intent Extras null! this_user: " + this_user + ", this_dialog: " + this_dialog);

    }

    private void getIntentExtras() {
        if (getIntent() != null){
            this_user = getIntent().getParcelableExtra("this_user");
            this_dialog = getIntent().getParcelableExtra("dialog");
        }
    }

    private void setAdapter(){
        // Set imageLoader if adding profile images
        adapter = new MessagesListAdapter<>(this_user.getId(), (imageView, url, payload) -> {
            Picasso.get().load(url).into(imageView);    // If you using another library - write here your way to load image
        });
        adapter.setDateHeadersFormatter(Util::format);
        MessagesList messagesList = findViewById(R.id.messagesList);
        messagesList.setAdapter(adapter);


        // TODO ON CLICK LISTENERS
        // adapter.setOnMessageClickListener();
        // adapter.setOnMessageLongClickListener();
        // adapter.registerViewClickListener(); onClicks for different views of a message item

        // TEST
        adapter.addToStart(new Message("message_1", "1st Message", this_user), true);
        adapter.addToStart(new Message("message_2", "2nd Message", new Author("author_2", "Author 2")), true);
        adapter.addToStart(new Message("message_3", "a Phone number: 123-456-7890", new Author("author_3", "Author 3")), false);
    }

    /* Adapter class can be extended for custom ViewHolder & functions
     * Current functions:
     * adapter.addToStart(IMessage message, boolean scroll) - add new oncoming messages
     * adapter.addToEnd(List<IMessage> messages, boolean reverse) - add history of messages
     * adapter.onLoadMore - callback that fires when scrolling up -> used for pagination
     * adapter.deleteById(String id)
     * adapter.clear()
     * adapter.update(IMessage message /OR/ adapter.update(String oldId, IMessage message)
     * Selection Mode - can select a number of messages and perform actions (must enable) -> ignores OnLongClickListener after enabled
     */

    /* To Use Custom Layouts:
    * MessagesListAdapter.HoldersConfig holdersConfig = new MessagesListAdapter.HoldersConfig();
    * holdersConfig.setIncomingLayout(R.layout.item_custom_incoming_message);
    * holdersConfig.setOutcomingLayout(R.layout.item_custom_outcoming_message);
    * adapter = new MessagesListAdapter<>(senderId, holdersConfig, imageLoader);
    */

    private void setMessageInput() {
        MessageInput inputView = findViewById(R.id.input);
        inputView.setInputListener(input -> {
            // validate and send message
            adapter.addToStart(new Message("newMessage" + counter, input.toString(), this_user), true);
            return true; // return true if valid message
        });

        inputView.setAttachmentsListener(() -> {
            // select attachments
            Message imageMessage = new Message("newMessage"+counter, null, this_user);
            imageMessage.setImageUrl("https://www.bloomnation.com/blog/wp-content/uploads/2012/07/Rainbow-Rose.jpg");
            adapter.addToStart(imageMessage, true);
        });
    }
}
