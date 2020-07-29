package ivy.learn.chat.chatKit;

import android.os.Parcel;
import android.os.Parcelable;

import com.stfalcon.chatkit.commons.models.IUser;

public class Author implements IUser, Parcelable {

    private String id;
    private String name;
    private String avatar;


    public Author(String id, String name){
     this.id = id;
     this.name = name;
    }

/* Getters & Setters
***************************************************************************************************/

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


/* Parcelable Methods
***************************************************************************************************/

    protected Author(Parcel in) {
        id = in.readString();
        name = in.readString();
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatar);
    }
}
