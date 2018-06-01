package clas.daniel.notes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import clas.daniel.notes.bean.Group;
import clas.daniel.notes.bean.Note;
import clas.daniel.notes.database.GroupDao;
import clas.daniel.notes.database.NoteDao;
import clas.daniel.notes.login.SignInActivity;
import clas.daniel.notes.utils.CommonUtil;
import clas.daniel.notes.utils.StringUtils;
import clas.daniel.notes.view.RichTextView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class DisplayActivity extends AppCompatActivity{
    private TextView tv_note_title;//title
    private RichTextView tv_note_content;//content
    private TextView tv_note_time;//created time
    private TextView tv_note_group;//note category
    private ImageView tv_note_star;
    private Note note;//note
    private String myTitle;
    private String myContent;
    private int myIsStar;
    private String myGroupName;
    private GroupDao groupDao;
    private ProgressDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        iniView();
    }
    void iniView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_note);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        note=new Note();
        groupDao = new GroupDao(this);
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Loading...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
        tv_note_title = (TextView) findViewById(R.id.tv_note_title);//标题
        tv_note_title.setTextIsSelectable(true);
        tv_note_content = (RichTextView) findViewById(R.id.tv_note_content);//内容
        tv_note_time = (TextView) findViewById(R.id.tv_note_time);
        tv_note_group = (TextView) findViewById(R.id.tv_note_group);
        tv_note_star=(ImageView) findViewById(R.id.star_img);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        note = (Note) bundle.getSerializable("note");

        myTitle = note.getTitle();
        // 9/5
        myIsStar=note.getIsStar();
        myContent = note.getContent();

        Group group = groupDao.queryGroupById(note.getGroupId());
        myGroupName = group.getName();

        tv_note_title.setText(myTitle);
        tv_note_content.post(new Runnable() {
            @Override
            public void run() {
                tv_note_content.clearAllLayout();
                showData(myContent);
            }
        });
        tv_note_time.setText(note.getCreateTime());
        tv_note_group.setText(myGroupName);
        if(myIsStar==1)
            tv_note_star.setImageResource(R.drawable.ic_star_on);
        else
            tv_note_star.setImageResource(R.drawable.ic_star_off);
    }
    private void showData(final String html){

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                showEditData(subscriber, html);
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        Toast.makeText(DisplayActivity.this,"Image does not exist",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(String text) {
                        if (text.contains("<img") && text.contains("src=")) {
                            String imagePath = StringUtils.getImgSrc(text);
                            tv_note_content.addImageViewAtIndex(tv_note_content.getLastIndex(), imagePath);
                        } else {
                            tv_note_content.addTextViewAtIndex(tv_note_content.getLastIndex(), text);
                        }
                    }
                });

    }

    /**
     * show data
     * @param html
     */
    private void showEditData(Subscriber<? super String> subscriber, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                subscriber.onNext(text);
            }
            subscriber.onCompleted();
        } catch (Exception e){
            e.printStackTrace();
            subscriber.onError(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_note_edit://Edit Note
                Intent intent = new Intent(DisplayActivity.this, EditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                startActivity(intent);
                finish();
                break;
            case R.id.action_note_share://Share Note
                CommonUtil.shareTextAndImage(this, note.getTitle(), note.getContent(), null);//分享图文
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
