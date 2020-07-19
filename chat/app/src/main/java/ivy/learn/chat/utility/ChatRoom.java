package ivy.learn.chat.utility;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for a 1on1 chatRoom object
 * Features: Firestore Compatible, parcelable
 */
public class ChatRoom implements Parcelable {

    protected String id;
    protected Long last_message_timestamp;
    protected boolean is_groupChat = false;
    protected List<String> members = new ArrayList<>();

    // Needed for Firebase
    public ChatRoom(){}

    // Used by children's constructor
    public ChatRoom(boolean is_groupChat){
        this.is_groupChat = is_groupChat;
    }

    public ChatRoom(String user1){
        members.add(user1);
        last_message_timestamp = System.currentTimeMillis();    // Creation time
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return id.equals(chatRoom.id);
    }

/* Getters and Setters
***************************************************************************************************/

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public Long getLast_message_timestamp() {
        if (last_message_timestamp == null) last_message_timestamp = 0L;
        return last_message_timestamp;
    }

    public void setLast_message_timestamp(Long last_message_timestamp) {
        this.last_message_timestamp = last_message_timestamp;
    }

    public boolean getIs_groupChat() {
        return is_groupChat;
    }

    public List<String> getMembers() {
        if (members == null) members = new ArrayList<>();
        return new ArrayList<>(members);
    }

    public void addMember(String newMember){
        if (newMember != null) members.add(newMember);
        else Log.w("ChatRoom", "newMember was null!");
    }

    public void removeMember(String member){
        members.remove(member);
    }

/* Parcel related Methods
***************************************************************************************************/

    protected ChatRoom(Parcel in) {
        id = in.readString();
        is_groupChat = in.readByte() != 0;
        members = in.createStringArrayList();
    }

    public static final Creator<ChatRoom> CREATOR = new Creator<ChatRoom>() {
        @Override
        public ChatRoom createFromParcel(Parcel in) {
            return new ChatRoom(in);
        }

        @Override
        public ChatRoom[] newArray(int size) {
            return new ChatRoom[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeByte((byte) (is_groupChat ? 1 : 0));
        dest.writeStringList(members);
    }
}
