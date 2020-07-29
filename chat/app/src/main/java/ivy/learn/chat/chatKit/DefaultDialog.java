package ivy.learn.chat.chatKit;

import android.os.Parcel;
import android.os.Parcelable;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.List;

public class DefaultDialog implements IDialog<Message>, Parcelable {

    private String id;
    private String dialogPhoto;
    private String dialogName;
    private List<Author> users;
    private Message lastMessage;
    private int unreadCount;


    public DefaultDialog(String id, String dialogPhoto, String dialogName){
        this.id = id;
        this.dialogPhoto = dialogPhoto;
        this.dialogName =  dialogName;
        users = new ArrayList<>();
        unreadCount = 0;
    }

/* Getters & Setters
***************************************************************************************************/

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public List<? extends IUser> getUsers() {
        return users;
    }

    @Override
    public Message getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDialogPhoto(String dialogPhoto) {
        this.dialogPhoto = dialogPhoto;
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void addUser(Author user){
        users.add(user);
    }

    public void removeUser(Author user){
        users.remove(user);
    }



/* Parcelable Methods
***************************************************************************************************/

    protected DefaultDialog(Parcel in) {
        id = in.readString();
        dialogPhoto = in.readString();
        dialogName = in.readString();
        users = in.createTypedArrayList(Author.CREATOR);
        unreadCount = in.readInt();
    }

    public static final Creator<DefaultDialog> CREATOR = new Creator<DefaultDialog>() {
        @Override
        public DefaultDialog createFromParcel(Parcel in) {
            return new DefaultDialog(in);
        }

        @Override
        public DefaultDialog[] newArray(int size) {
            return new DefaultDialog[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(dialogPhoto);
        dest.writeString(dialogName);
        dest.writeTypedList(users);
        dest.writeInt(unreadCount);
    }
}