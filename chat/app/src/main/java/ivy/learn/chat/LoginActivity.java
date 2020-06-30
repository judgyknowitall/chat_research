package ivy.learn.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Currently user can pick a name. [NO AUTH]
 *
 * maybe: Use Custom Tokens to sign in to Firebase auth
 * See: https://www.youtube.com/watch?v=Fi2dv6NcHWA
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "loginActivity";

    // Views
    private EditText name_input;
    private Button login_button;
    private ProgressBar loading_bar;

    // Firebase
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();


/* Overridden Methods
***************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        //establishKey();     // To get Custom token
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
        String username = name_input.getText().toString().trim();
        name_input.setError(null);

        if (!username.isEmpty()) {
            loading(true);
            findUserInDB(username);    // Check if name already exists in database (create one if no)
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

    private void transitionToMain(User user){
        Intent intent = new Intent(this, ChatLobbyActivity.class);
        intent.putExtra("this_user", user);
        loading(false);
        startActivity(intent);
        finish();
    }


/* Firebase related Methods
***************************************************************************************************/

    // Check if Username exists in DB
    private void findUserInDB(String username){
        String address = "usernames/"+ username;
        mFirestore.document(address).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()){
                        Log.d(TAG, "username already exists!");
                        transitionToMain(task.getResult().toObject(User.class));
                    }
                    else {
                        Log.d(TAG, "Welcome new user!");

                        // Make new user
                        User newUser = new User(username);
                        mFirestore.document(address).set(newUser).addOnCompleteListener(task1 -> {
                           if (task.isSuccessful()) transitionToMain(newUser);
                           else {
                               Log.e(TAG, "Something went wrong!", task.getException());
                               loading(false);
                           }
                        });
                    }
                });
    }









/* Trashcan
 ***************************************************************************************************/


    // Configure Firebase Options
    /*private void establishKey(){


        try {
            FileInputStream serviceAccount = new FileInputStream("./ServiceAccountKey.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(this, options, "ChatResearch2");
            FirebaseApp secondary = FirebaseApp.getInstance("ChatResearch2");
            FirebaseDatabase secondaryDatabase = FirebaseDatabase.getInstance(secondary);

        } catch (IOException e) {
            e.printStackTrace();
        }
    } */


    // Sign in with custom token
    /*private void signInWithCustomToken(String uid){
        String mCustomToken = null;
        try { mCustomToken = FirebaseAuth.getInstance().createCustomToken(uid); }
        catch (FirebaseAuthException e) { e.printStackTrace(); }
        if (mCustomToken == null) return;


        auth.signInWithCustomToken(mCustomToken)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCustomToken:success");
                            FirebaseUser user = auth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }*/

}