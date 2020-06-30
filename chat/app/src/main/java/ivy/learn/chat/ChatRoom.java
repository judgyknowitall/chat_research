package ivy.learn.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for a chatRoom object
 * Features: Firestore Compatible, parcelable
 */
public class ChatRoom implements Parcelable {

    private String name;    // same as id
    private String host;    // Host user
    private List<String> members;

    // Needed for Firebase
    public ChatRoom(){}

    public ChatRoom(String name, String host){
        this.name = name;
        this.host = host;
        members = new ArrayList<>();
        members.add(host);
    }

/* Getters and Setters
***************************************************************************************************/

    public String getName() {
        return name;
    }

    public List<String> getMembers() {
        if (members == null) members = new ArrayList<>();
        return new ArrayList<>(members);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addMember(String newMember){
        members.add(newMember);
    }

    public void removeMember(String member){
        members.remove(member);
    }


/* Parcel related Methods
***************************************************************************************************/

    protected ChatRoom(Parcel in) {
        name = in.readString();
        host = in.readString();
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
        dest.writeString(name);
        dest.writeString(host);
        dest.writeStringList(members);
    }
}