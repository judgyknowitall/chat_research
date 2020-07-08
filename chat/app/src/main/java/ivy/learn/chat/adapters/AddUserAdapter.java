package ivy.learn.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ivy.learn.chat.R;
import ivy.learn.chat.utility.User;

public class AddUserAdapter extends RecyclerView.Adapter<AddUserAdapter.AddUserViewHolder> {

    // Attributes
    private List<User> users;
    OnAddUserClickListener listener;

    public AddUserAdapter(List<User> users, OnAddUserClickListener listener) {
        this.users = users;
        this.listener =  listener;
    }


/* Overridden Methods
***************************************************************************************************/

    @NonNull
    @Override
    public AddUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_choose_user, parent, false);
        return new AddUserViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AddUserViewHolder holder, int position) {
        holder.ctv_user.setText(users.get(position).getUsername());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


/* View Holder subclass
***************************************************************************************************/

    static class AddUserViewHolder extends RecyclerView.ViewHolder {

        CheckedTextView ctv_user;

        public AddUserViewHolder(@NonNull View itemView, OnAddUserClickListener listener) {
            super(itemView);
            ctv_user = itemView.findViewById(R.id.item_chooseUser_check);
            ctv_user.setOnClickListener(v -> {
                ctv_user.setChecked(!ctv_user.isChecked());
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION)
                        listener.onSelected(position, ctv_user.isChecked());
                }
            });
        }
    }


/* Item Click Interface
***************************************************************************************************/

    public interface OnAddUserClickListener {
        void onSelected(int position, boolean checked);
    }
}