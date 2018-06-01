package clas.daniel.notes;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import clas.daniel.notes.bean.Note;
import clas.daniel.notes.utils.DeviceUtils;
import clas.daniel.notes.utils.SDCardUtil;
import clas.daniel.notes.view.RichTextEditor;

public class EditActivity extends AppCompatActivity implements
        EditFragment.OnFragmentInteractionListener,
        RichTextEditor.OnDeleteImageListener{
    private Note note;
    private EditFragment editFragmentInstance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        iniView();
    }
    private void iniView(){
        showFragment();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editFragmentInstance.saveNoteData();
                finish();
            }
        });
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        return super.onPrepareOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.action_insert_image:
                DeviceUtils.callGallery(this);
                break;
            case R.id.action_new_save:
                editFragmentInstance.saveNoteData();
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        editFragmentInstance.saveNoteData();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editFragmentInstance.saveNoteData();
    }

    public void showFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fmTransaction = fm.beginTransaction();
        Boolean editFlag=true;
        Bundle bundle = getIntent().getBundleExtra("data");
        note = (Note) bundle.getSerializable("note");
        editFragmentInstance = EditFragment.newInstance(editFlag.toString(), null);
        fmTransaction.replace(R.id.fragment, editFragmentInstance);
        fmTransaction.commit();
    }
    public Note getNoteData(){
        return note;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onDeleteImage(String imagePath) {
        boolean isOK = SDCardUtil.deleteFile(imagePath);
        if(isOK){
            Toast.makeText(this,"Success to delete：" + imagePath,Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null && requestCode == Constant.REQUEST_CODE_CHOOSE){
            editFragmentInstance.insertImagesASync(data);
        }
    }
}
