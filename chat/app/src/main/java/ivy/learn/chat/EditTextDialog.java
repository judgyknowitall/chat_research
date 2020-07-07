package ivy.learn.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class EditTextDialog extends AppCompatDialogFragment {

    private Activity this_activity;
    private View root_view;
    private EditText edit_text;
    private ImageButton edit_button;
    private EditTextDialogListener listener;
    private String message_id;


    public EditTextDialog(EditTextDialogListener listener, String this_message_id){
        this.listener = listener;
        this.message_id = this_message_id;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getActivity() == null) return super.onCreateDialog(savedInstanceState);

        setViews();         // Set Views
        setListeners();     // Set Listeners

        // Set Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(root_view);

        return builder.create();
    }

/* Other Methods
***************************************************************************************************/

    @SuppressLint("InflateParams")
    private void setViews(){
        if (getActivity() == null) return;
        this_activity = getActivity();
        root_view = this_activity.getLayoutInflater().inflate(R.layout.dialog_edittext, null);
        edit_text = root_view.findViewById(R.id.dialog_editMessage);
        edit_button = root_view.findViewById(R.id.dialog_editButton);
    }

    private void setListeners() {
        // Send Message if pressed [ENTER]
        edit_text.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.room_sendButton || id == EditorInfo.IME_NULL) {
                listener.editText(edit_text.getText().toString().trim(), message_id);
                dismiss();
                return true;
            }
            else return false;
        });

        // Disable send button if no message to send
        edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edit_button.setClickable(!edit_text.getText().toString().trim().isEmpty());
                if (edit_button.isClickable()) edit_button.setColorFilter(this_activity.getColor(R.color.colorPrimary));
                else edit_button.setColorFilter(this_activity.getColor(R.color.grey));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Edit Button listener
        edit_button.setOnClickListener(v -> {
            listener.editText(edit_text.getText().toString().trim(), message_id);
            dismiss();
        });
    }

/* Interface
***************************************************************************************************/

    public interface EditTextDialogListener {
        void editText(String text, String message_id);
    }
}
