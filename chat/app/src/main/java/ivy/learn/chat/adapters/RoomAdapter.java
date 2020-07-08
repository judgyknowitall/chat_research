package ivy.learn.chat.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ivy.learn.chat.R;
import ivy.learn.chat.utility.Message;
import ivy.learn.chat.utility.Util;


public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.MessageViewHolder> {
    private static final String TAG = "RoomAdapter";

    // Attributes
    private String this_user_id;
    private List<Message> messages;
    private Context context;
    OnMessageClickListener selection_listener;



    public RoomAdapter(List<Message> messages, String user_id) {
        this.messages = messages;
        this_user_id = user_id;
    }

    public void setOnUserItemClickListener (OnMessageClickListener listener){
        this.selection_listener = listener;
    }


/* Overridden Methods
***************************************************************************************************/

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view, selection_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message this_message = messages.get(position);
        holder.tv_author.setText(this_message.getAuthor());
        holder.tv_message.setText(this_message.getText());
        holder.tv_timestamp.setText(Util.millisToDateTime(this_message.getTime_stamp()));
        setChatRowAppearance(this_message.getAuthor().equals(this_user_id), holder);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


/* Other Methods
***************************************************************************************************/

    private void setChatRowAppearance(boolean isMe, MessageViewHolder holder){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.tv_author.getLayoutParams();

        if (isMe){
            params.gravity = Gravity.END;
            holder.tv_author.setTextColor(context.getColor(R.color.colorPrimary));
            holder.tv_message.setBackground(context.getDrawable(R.drawable.bubble1));
        }
        else {
            params.gravity = Gravity.START;
            holder.tv_author.setTextColor(context.getColor(R.color.grey));
            holder.tv_message.setBackground(context.getDrawable(R.drawable.bubble2));
        }

        holder.tv_author.setLayoutParams(params);
        holder.tv_message.setLayoutParams(params);
        holder.tv_timestamp.setLayoutParams(params);
    }

    public void removeMessage(int position){
        messages.remove(position);
        notifyItemRemoved(position);
    }



/* View Holder subclass
***************************************************************************************************/

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView tv_author;
        TextView tv_message;
        TextView tv_timestamp;
        LinearLayout layout;


        public MessageViewHolder(@NonNull View itemView, final OnMessageClickListener listener) {
            super(itemView);

            // Initialize views
            tv_author = itemView.findViewById(R.id.item_chatmessage_username);
            tv_message = itemView.findViewById(R.id.item_chatmessage_text);
            tv_timestamp = itemView.findViewById(R.id.item_chatmessage_timestamp);
            layout = itemView.findViewById(R.id.item_chatmessage_layout);

            //Set Listeners
            layout.setOnClickListener(v -> {
                if (tv_timestamp.getVisibility() == View.GONE) tv_timestamp.setVisibility(View.VISIBLE);
                else tv_timestamp.setVisibility(View.GONE);
            });

            layout.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onLongClick(position, v);
                        return true;
                    }
                }
                return false;
            });
        }
    }


/* Item Click Interface (different methods for short and long(click and hold) clicks)
***************************************************************************************************/

    public interface OnMessageClickListener {
        void onLongClick(int position, View v);
    }
}