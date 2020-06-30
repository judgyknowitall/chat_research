package ivy.learn.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

/**
 * Lobby where logged-in user can view active chat rooms
 */
public class ChatLobbyActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_lobby);
    }

    public void logoutUser(View view) {
        //TODO
    }

    public void newChatRoom(View view) {
        //TODO
    }
}