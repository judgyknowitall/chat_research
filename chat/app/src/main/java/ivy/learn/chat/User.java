package ivy.learn.chat;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class for a user object
 * Features: Firestore Compatible, parcelable
 */
public class User implements Parcelable {

    private String username;

    public User(String username){
        this.username = username;
    }

    protected User(Parcel in) {
        username = in.readString();
    }


/* Getters and Setters
***************************************************************************************************/

    public String getUsername() {
        return username;
    }


/* Parcel related Methods
***************************************************************************************************/

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
