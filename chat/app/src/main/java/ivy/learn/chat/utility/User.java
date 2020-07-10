package ivy.learn.chat.utility;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for a user object
 * Features: Firestore Compatible, parcelable
 */
public class User implements Parcelable {
    public static final String universal_chat_address = "usernames/user1/conversations/universalConvo";

    private String username;
    //TODO last visit time_stamp per conversation?

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

/* Parcel related Methods
***************************************************************************************************/

    protected User(Parcel in) {
        username = in.readString();
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
    }
}
