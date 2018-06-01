package clas.daniel.notes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.view.KeyEvent;
import android.widget.SearchView;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import clas.daniel.notes.bean.Note;
import clas.daniel.notes.login.SignInActivity;
import clas.daniel.notes.utils.DeviceUtils;
import clas.daniel.notes.utils.SDCardUtil;
import clas.daniel.notes.view.RichTextEditor;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ListFragment.OnListFragmentInteractionListener,
        EditFragment.OnFragmentInteractionListener,
        RichTextEditor.OnDeleteImageListener{
    private View bottomLayout;
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private boolean isFromSplash = true;
    private NavigationView navigationView;
    CoordinatorLayout layout;
    private boolean updateScreenSign=false;
    private int currentType=Constant.TYPE_NOTE_LIST;
    private Fragment currentFragmentInstance = null;
    private boolean isQuit=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniView();
    }
    private void iniView() {
        //this is to set the background of main activity based on different selection
        bottomLayout=findViewById(R.id.drawer_layout);
        verifyStoragePermissions(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        layout = (CoordinatorLayout) findViewById(R.id.app_bar);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        TextView log_name=(navigationView.getHeaderView(0)).findViewById(R.id.log_name);
        log_name.setText(getUserPreference().getUserName());
        navigationView.setNavigationItemSelectedListener(this);
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        };
        //set bootom navigation selected color and normal color
        int[] colors = new int[]{getResources().getColor(R.color.grey_600),
                getResources().getColor(R.color.colorBlack)
        };
        ColorStateList csl = new ColorStateList(states, colors);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setItemTextColor(csl);
        navigation.setItemIconTintList(csl);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        showFragment(true,Constant.TYPE_NOTE_LIST);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
          DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
          if(drawer.isDrawerOpen(GravityCompat.START)){
              drawer.closeDrawer(GravityCompat.START);
          }
          if (isQuit == false) {
              isQuit = true;
              Toast.makeText(getBaseContext(), "Quite Now? Click again",
                      Toast.LENGTH_SHORT).show();
              TimerTask task=new TimerTask() {
                  public void run() {
                      isQuit = false;
                  }
              };
              Timer timer=new Timer();
              timer.schedule(task, 2000);
          } else {
              if(currentType==Constant.TYPE_NEW_NOTE){
                  ((EditFragment)currentFragmentInstance).saveNoteData();
              }
              finish();
              System.exit(0);
          }
      }
      return false;
  }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //before change, it is necessary to clear previous menu
        menu.clear();
        if(currentType != Constant.TYPE_NEW_NOTE){
            getMenuInflater().inflate(R.menu.search_menu, menu);
            MenuItem item = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) item.getActionView();
            searchView.setInputType(EditorInfo.TYPE_CLASS_TEXT);
            // 设置回车键表示查询操作
            searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
//                    final String q=query;
////                    MainActivity.this.runOnUiThread(new Runnable() {
////                        @Override
////                        public void run() {
////                            Toast.makeText(MainActivity.this,"type1:"+q,Toast.LENGTH_SHORT).show();
////                        }
////                    });
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    final String q=newText;
//                    MainActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this,"type2"+q,Toast.LENGTH_SHORT).show();
//                        }
//                    });
                    if(newText.equals("")){
                        ((ListFragment)currentFragmentInstance).updateScreen();
                    } else
                        ((ListFragment)currentFragmentInstance).setSearchContent(newText);
                    return true;
                }
            });

        } else {
            getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if(currentType == Constant.TYPE_NEW_NOTE){
            switch (item.getItemId()) {
                case R.id.action_insert_image:
                    DeviceUtils.callGallery(this);
                    break;
                case R.id.action_new_save:
                    //if return true,change to note list
                    if(((EditFragment) currentFragmentInstance).saveNoteData()) {
                        //switch to note list fragment
                        actionOfCurrentNavigation(true,Constant.TYPE_NOTE_LIST);
                        View view = findViewById(R.id.note_list);
                        view.performClick();
                    }
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_sign_out){
            getUserPreference().logout();
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int type;
            switch (item.getItemId()) {
                case R.id.note_list:
                    type = Constant.TYPE_NOTE_LIST;
                    break;
                case R.id.star_list:
                    type = Constant.TYPE_STAR_LIST;
                    break;
                default:
                    type = Constant.TYPE_NEW_NOTE;
                    break;
            }
            actionOfCurrentNavigation(false,type);
            return true;
        }
    };
    public void actionOfCurrentNavigation(boolean haveSaved,int type) {
        if(currentType != type){
            showFragment(haveSaved,type);
            invalidateOptionsMenu();
        }
    }
    public void showFragment(boolean haveSaved,int type) {
        Fragment fg;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fmTransaction = fm.beginTransaction();
        if(currentType==Constant.TYPE_NEW_NOTE && !haveSaved){
            if(((EditFragment) currentFragmentInstance).newNoteShouldSaved()){
                ((EditFragment) currentFragmentInstance).createSaveConfirmationDialog();
            }
        }
        if(type == Constant.TYPE_NEW_NOTE){
            Boolean editFlag=false;
            fg = EditFragment.newInstance(editFlag.toString(), null);
            bottomLayout.setBackgroundColor(Color.WHITE);
        } else {
            fg = ListFragment.newInstance(type);
            bottomLayout.setBackgroundColor(Color.DKGRAY);
        }
        currentType = type;
        fmTransaction.replace(R.id.fragment, fg);
        fmTransaction.commit();
        currentFragmentInstance = fg;
    }
    @Override
    public void onListFragmentInteraction(Note note) {
        Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("note", note);
        intent.putExtra("data", bundle);
        startActivity(intent);
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    public static void verifyStoragePermissions(Activity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if(permission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
            permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.READ_EXTERNAL_STORAGE");
            if(permission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onFragmentInteraction(Uri uri) {
        if(currentType!=Constant.TYPE_NEW_NOTE){
            ((ListFragment) currentFragmentInstance).updateScreen();
        } else{
            updateScreenSign=true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(updateScreenSign){
            ((ListFragment) currentFragmentInstance).updateScreen();
            updateScreenSign = false;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null && requestCode == Constant.REQUEST_CODE_CHOOSE){
            ((EditFragment) currentFragmentInstance).insertImagesASync(data);
        }
    }
    @Override
    public void onDeleteImage(String imagePath) {
        boolean isOK = SDCardUtil.deleteFile(imagePath);
        if(isOK){
            Toast.makeText(this,"Success to delete：" + imagePath,Toast.LENGTH_SHORT).show();
        }
    }
}