package ivy.learn.chat.utility;

import android.os.Parcel;

import java.util.ArrayList;


/**
 * Class for a group chatRoom object
 * Features: Firestore Compatible, parcelable
 */
public class GroupChat extends ChatRoom{

    String host;
    String name = "New Chatroom";

    // Needed For Firebase
    public GroupChat(){ super(true); }

    public GroupChat(String host){
        members = new ArrayList<>();
        members.add(host);
        is_groupChat = true;
    }


/* Getters and Setters
***************************************************************************************************/

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

/* Parcelable Override Methods
***************************************************************************************************/

    protected GroupChat(Parcel in) {
        super(in);
        name = in.readString();
        host = in.readString();
    }

    public static final Creator<GroupChat> CREATOR = new Creator<GroupChat>() {
        @Override
        public GroupChat createFromParcel(Parcel in) {
            return new GroupChat(in);
        }

        @Override
        public GroupChat[] newArray(int size) {
            return new GroupChat[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(name);
        dest.writeString(host);
    }
}
