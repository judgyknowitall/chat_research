package ivy.learn.chat.chatKit;

import android.os.Parcel;
import android.os.Parcelable;

import com.stfalcon.chatkit.commons.models.IUser;

/**
 * Alternative class for a user/author object
 * Features: Firestore Compatible, parcelable
 */
public class Author implements IUser, Parcelable {

    private String username;
    private String avatar;


    // Needed for Firebase
    public Author(){}

    public Author(String username){
     this.username = username;
    }

    public Author(String username, String avatar) {
        this.username = username;
        this.avatar = avatar;
    }

    /* Getters & Setters
***************************************************************************************************/

    @Override
    public String getId() {
        return username;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


/* Parcelable Methods
***************************************************************************************************/

    protected Author(Parcel in) {
        username = in.readString();
        avatar = in.readString();
    }

    public static final Creator<Author> CREATOR = new Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(avatar);
    }
}
