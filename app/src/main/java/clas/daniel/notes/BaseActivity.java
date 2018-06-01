package clas.daniel.notes;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import clas.daniel.notes.utils.DeviceUtils;


public class BaseActivity extends AppCompatActivity {

    private UserPreference mPreference;

    public UserPreference getUserPreference() {
        if (mPreference == null) {
            mPreference = new UserPreference(this);
        }
        return mPreference;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

    private void init() {
        mPreference = new UserPreference(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void finish() {
        DeviceUtils.hideSoftKeyboard(this);
        super.finish();
    }

    @Override
    public void finishAffinity() {
        DeviceUtils.hideSoftKeyboard(this);
        super.finishAffinity();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
