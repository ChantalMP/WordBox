package cp.wordbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "SignIn";

    //Add Auth members
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText emailF, passwordF;
    private ProgressDialog mProgressDialog;

    private Toolbar mToolbar;

    private DatabaseReference storeUserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(i);
        }

        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WordBox");

        emailF = (EditText) findViewById(R.id.emailF);
        passwordF = (EditText) findViewById(R.id.passwordF);

        //get a Reference to the Firebase auth object
        mAuth = FirebaseAuth.getInstance();

        //waiting screen
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("loading");
        mProgressDialog.setMessage("please wait");
        mProgressDialog.setIndeterminate(true);

        //attach a new AuthListener to detect sign in and out
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };
    }

    @Override
    public void onStart(){
        super.onStart();
        //Connect AuthListener
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        //remove AuthListener
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void login(View view){
        signUserIn();
    }

    public void signup(View view){
        if (!checkFormField())
            return;

        mProgressDialog.setTitle("creating account");
        mProgressDialog.show();

        String email = emailF.getText().toString();
        String password = passwordF.getText().toString();

        //Create user account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    //to check if task was successfull
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //falls user speichern nötig ist für offline Nutzung
//                            String device_token = FirebaseInstanceId.getInstance().getToken();
//                            String current_user_id = mAuth.getCurrentUser().getUid();
//                            storeUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(current_user_id);
//                            storeUserRef.child("user_device_token").setValue(device_token);
                            Toast.makeText(LoginActivity.this, "Account was created.", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            finish();
                            startActivity(i);
                        }
                        else{
                            mProgressDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(LoginActivity.this, "Email already in use", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Account creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUserIn() {
        if (!checkFormField())
            return;

        final String email = emailF.getText().toString();
        String password = passwordF.getText().toString();

        mProgressDialog.setTitle("logging in");
        mProgressDialog.show();

        //sign in
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    //to check if task was successfull
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Signed In.", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            mProgressDialog.dismiss();
                            finish();
                            startActivity(i);
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Signing In failed", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginActivity.this, "Invalid Password", Toast.LENGTH_SHORT);
                        }
                        else if (e instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(LoginActivity.this, "No account with this email", Toast.LENGTH_SHORT);
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT);
                        }
                    }
                });
    }

    private boolean checkFormField() {
        String email, password;

        email = emailF.getText().toString();
        password = passwordF.getText().toString();

        if (email.isEmpty()) {
            emailF.setError("Email Required");
            return false;
        }
        if (password.isEmpty()){
            passwordF.setError("Password Required");
            return false;
        }

        return true;
    }
}
