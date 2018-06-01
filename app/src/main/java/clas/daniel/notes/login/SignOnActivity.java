package clas.daniel.notes.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import clas.daniel.notes.BaseActivity;
import clas.daniel.notes.MainActivity;
import clas.daniel.notes.Model.User;
import clas.daniel.notes.R;
import clas.daniel.notes.database.UserDatabaseHelper;
import clas.daniel.notes.utils.PasswordValidator;
import clas.daniel.notes.utils.TextUtils;


public class SignOnActivity extends BaseActivity {

    private EditText txtName;
    private EditText txtEmail;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private Button btnSignIn, btnSignOn;
    private UserDatabaseHelper userDatabaseHelper;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_on);
        initViews();
        setupEvents();
    }

    private void initViews() {
        txtName = findViewById(R.id.name);
        txtEmail = findViewById(R.id.email);
        txtPassword =  findViewById(R.id.password);
        txtConfirmPassword = findViewById(R.id.confirm_password);
        btnSignIn = findViewById(R.id.sign_in);
        btnSignOn = findViewById(R.id.sign_on);
        userDatabaseHelper = new UserDatabaseHelper(this);
        user = new User();
    }

    private void setupEvents() {
        btnSignOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOn();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void signOn() {
        if (!validate()) {
            onSignOnFailed();
            return;
        }

        btnSignOn.setEnabled(false);

        onSignOnSuccess();
    }


    public void onSignOnSuccess() {
        btnSignOn.setEnabled(true);

        String name = txtName.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String reEnterPassword = txtConfirmPassword.getText().toString();
        if (!userDatabaseHelper.checkUser(email)) {
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            userDatabaseHelper.addUser(user);
            getUserPreference().setUserName(email);
            getUserPreference().setPassword(password);
            getUserPreference().setUserSignInStatus(true);
            startActivity(new Intent(SignOnActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
        }
    }
    public void onSignOnFailed() {
        btnSignOn.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = txtName.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String reEnterPassword = txtConfirmPassword.getText().toString();

        if (TextUtils.isNullOrEmpty(name)) {
            txtName.setError(getString(R.string.alert_enter_name));
            valid = false;
        } else {
            txtName.setError(null);
        }

        if (TextUtils.isNullOrEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError(getString(R.string.alert_email_not_valid));
            valid = false;
        } else {
            txtEmail.setError(null);
        }

        if (!PasswordValidator.isPasswordLengthValid(password)) {
            txtPassword.setError(getString(R.string.alert_password_error));
            valid = false;
        } else {
            txtPassword.setError(null);
        }

        if (TextUtils.isNullOrEmpty(reEnterPassword) || !(reEnterPassword.equals(password))) {
            txtConfirmPassword.setError(getString(R.string.alert_password_mismatch));
            valid = false;
        } else {
            txtConfirmPassword.setError(null);
        }
        return valid;
    }
}