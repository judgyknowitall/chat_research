package ivy.learn.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "loginActivity";

    // Views
    private EditText name_input;
    private Button login_button;
    private ProgressBar loading_bar;

    // Firebase
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();



/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
    }


/* Initialization Methods
***************************************************************************************************/

    private void initViews(){
        name_input = findViewById(R.id.login_input);
        login_button = findViewById(R.id.login_button);
        loading_bar = findViewById(R.id.login_progressBar);
    }


/* OnClick Methods
***************************************************************************************************/

    // login button OnClick
    public void loginUser(View v){
        String input = name_input.getText().toString().trim();
        name_input.setError(null);

        if (!input.isEmpty()) {
            loading(true);
            // TODO Check if name already exists in database
            // TODO Login anonymously
        }
        else name_input.setError("Please input a nonempty username.");

    }



/* Transition Methods
***************************************************************************************************/

    private void loading(boolean load){
        // Start loading
        if (load){
            login_button.setVisibility(View.GONE);
            loading_bar.setVisibility(View.VISIBLE);

            // Disable interaction
            hideKeyboard();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        // Stop loading
        else {
            login_button.setVisibility(View.VISIBLE);
            loading_bar.setVisibility(View.GONE);

            // Enable Interaction
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null && inputManager != null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            inputManager.hideSoftInputFromInputMethod(getCurrentFocus().getWindowToken(), 0);
        }
    }


/* Firebase related Methods
***************************************************************************************************/

    // Check to see if user exists

}