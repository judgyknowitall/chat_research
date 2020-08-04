package ivy.learn.chat.chatKit;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;


/**
 * Class for a message object
 * Features: Firestore Compatible, parcelable
 */
public class Message implements IMessage, MessageContentType.Image, Parcelable {

    private String id;
    private String text;
    private Author user;
    private String userId;
    private long createdAt;
    private String imageUrl;

    // Firebase
    private static final String TAG = "MessageClass";
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();


    // For Firebase Only!
    public Message(){}

    public Message(String id, String text, Author user){
        this.id = id;
        this.text = text;
        this.user = user;
        this.userId = user.getId();
        this.createdAt = System.currentTimeMillis();
    }


/* Getters and Setters
***************************************************************************************************/

    @Exclude
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Exclude
    @Override
    public IUser getUser() {
        if (user == null) {
            if (userId != null) {
                user = new Author(userId);
                getUserFromFirebase();
            } else user = new Author("GUEST"); // For testing only
        }
        return user;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public Date getCreatedAt() {
        return new Date(createdAt);
    }

    @Nullable
    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUser(Author user) {
        if (user != null) this.user = user;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


/* Parcelable Methods
***************************************************************************************************/

    protected Message(Parcel in) {
        id = in.readString();
        text = in.readString();
        user = in.readParcelable(Author.class.getClassLoader());
        createdAt = in.readLong();
        imageUrl = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(text);
        dest.writeParcelable(user, 0);
        dest.writeLong(createdAt);
        dest.writeString(imageUrl);
    }


/* Firestore Methods
***************************************************************************************************/

    public void getUserFromFirebase(){
        mFirestore.collection("usernames").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Author user = task.getResult().toObject(Author.class);
                        if (user!= null) this.user = user;
                    }
                    else Log.e(TAG, "user object doesn't exist in database!!");
                });
    }
}
