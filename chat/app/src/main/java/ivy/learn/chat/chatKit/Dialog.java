package ivy.learn.chat.chatKit;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for any dialog/conversation object
 * Features: Firestore Compatible, parcelable
 */
public class Dialog implements IDialog<Message>, Parcelable {

    private String id;
    private String dialogPhoto;
    private String dialogName;
    private List<Author> users = new ArrayList<>();
    private List<String> userIds = new ArrayList<>();
    private Message lastMessage;
    private int unreadCount = 0;


    // Needed for Firebase
    public Dialog(){}

    public Dialog(String id, String dialogPhoto, String dialogName){
        this.id = id;
        this.dialogPhoto = dialogPhoto;
        this.dialogName =  dialogName;
    }

/* Getters & Setters
***************************************************************************************************/

    @Override
    @Exclude
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

    @Exclude
    @Override
    public List<? extends IUser> getUsers() {
        if (users.isEmpty())
            for (String id : userIds) users.add(new Author(id));

        return users;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    @Override
    @Exclude
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
        userIds.add(user.getId());
    }

    public void removeUser(Author user){
        users.remove(user);
        userIds.remove(user.getId());
    }



/* Parcelable Methods
***************************************************************************************************/

    protected Dialog(Parcel in) {
        id = in.readString();
        dialogPhoto = in.readString();
        dialogName = in.readString();
        users = in.createTypedArrayList(Author.CREATOR);
        userIds = in.createStringArrayList();
        lastMessage = in.readParcelable(getClass().getClassLoader());
        unreadCount = in.readInt();
    }

    public static final Creator<Dialog> CREATOR = new Creator<Dialog>() {
        @Override
        public Dialog createFromParcel(Parcel in) {
            return new Dialog(in);
        }

        @Override
        public Dialog[] newArray(int size) {
            return new Dialog[size];
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
        dest.writeStringList(userIds);
        dest.writeParcelable(lastMessage,0);
        dest.writeInt(unreadCount);
    }
}
