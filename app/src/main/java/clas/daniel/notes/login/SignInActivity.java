package clas.daniel.notes.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import clas.daniel.notes.BaseActivity;
import clas.daniel.notes.MainActivity;
import clas.daniel.notes.R;
import clas.daniel.notes.database.UserDatabaseHelper;
import clas.daniel.notes.utils.PasswordValidator;
import clas.daniel.notes.utils.TextUtils;

public class SignInActivity extends BaseActivity {

    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnSignIn;
    private Button btnSignOn;
    private UserDatabaseHelper userDatabaseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initViews();
        setupEvents();
    }

    private void initViews() {
        txtEmail = findViewById(R.id.email);
        txtPassword = findViewById(R.id.password);
        btnSignIn = findViewById(R.id.sign_in);
        btnSignOn = findViewById(R.id.sign_on);
        userDatabaseHelper = new UserDatabaseHelper(this);
    }
    private void setupEvents() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        btnSignOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignOnActivity.class);
                startActivity(intent);
            }
        });
    }

    public void signIn() {
        if (!validate()) {
            return;
        }
        onSignInSuccessCheck();
    }

    public void onSignInSuccessCheck() {
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        if (userDatabaseHelper.checkUser(email, password)) {
            getUserPreference().setUserName(email);
            getUserPreference().setPassword(password);
            getUserPreference().setUserSignInStatus(true);
            txtEmail.setText("");
            txtPassword.setText("");
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            onSignInFailed();
        }
    }

    public void onSignInFailed() {
        Toast.makeText(getBaseContext(), getString(R.string.login_failed), Toast.LENGTH_LONG).show();
    }

    public boolean validate() {
        boolean valid = true;

        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        if (TextUtils.isNullOrEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError(getString(R.string.alert_email_not_valid));
            valid = false;
        } else {
            txtEmail.setError(null);
        }
        if (!PasswordValidator.isPasswordLengthValid(password)) {
            txtPassword.setError(getString(R.string.alert_password_length_error));
            valid = false;
        } else {
            txtPassword.setError(null);
        }
        return valid;
    }
}