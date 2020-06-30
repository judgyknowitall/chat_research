package ivy.learn.chat;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class for a message object
 * Features: Firestore Compatible, parcelable
 */
public class Message implements Parcelable {

    private String author;
    private String text;
    private long time_stamp;

    // Needed fore Firebase
    public Message(){}

    public Message(String author, String text){
        this.author = author;
        this.text = text;
        this.time_stamp = System.currentTimeMillis();
    }


/* Getters and Setters
***************************************************************************************************/

    public String getAuthor() {
        return author;
    }

    public long getTime_stamp() {
        return time_stamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


/* Parcel related Methods
***************************************************************************************************/

    protected Message(Parcel in) {
        author = in.readString();
        text = in.readString();
        time_stamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(text);
        dest.writeLong(time_stamp);
    }

    @Override
    public int describeContents() {
        return 0;
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
}