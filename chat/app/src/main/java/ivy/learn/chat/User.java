package ivy.learn.chat;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for a user object
 * Features: Firestore Compatible, parcelable
 */
public class User implements Parcelable {

    private String username;
    private List<String> chatroom_addresses;

    // Needed for Firebase
    public User(){}

    public User(String username){
        this.username = username;
    }


/* Getters and Setters
***************************************************************************************************/

    public String getUsername() {
        return username;
    }

    public ArrayList<String> getChatroom_addresses() {
        if (chatroom_addresses == null) chatroom_addresses = new ArrayList<>();
        return new ArrayList<>(chatroom_addresses);
    }

    public void addChatroom_address(String addr){
        chatroom_addresses.add(addr);
    }

    public void removeChatroom_address(String addr){
        chatroom_addresses.remove(addr);
    }

/* Parcel related Methods
***************************************************************************************************/

    protected User(Parcel in) {
        username = in.readString();
        chatroom_addresses = in.createStringArrayList();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeStringList(chatroom_addresses);
    }
}
