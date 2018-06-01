package clas.daniel.notes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhihu.matisse.Matisse;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import clas.daniel.notes.bean.Group;
import clas.daniel.notes.bean.Note;
import clas.daniel.notes.database.GroupDao;
import clas.daniel.notes.database.NoteDao;
import clas.daniel.notes.utils.CommonUtil;
import clas.daniel.notes.utils.ImageUtils;
import clas.daniel.notes.utils.SDCardUtil;
import clas.daniel.notes.utils.StringUtils;
import clas.daniel.notes.view.RichTextEditor;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Activity currentActivity;
    private EditText et_new_title;
    private RichTextEditor et_new_content;
    private TextView tv_new_time;

    private GroupDao groupDao;
    private NoteDao noteDao;
    private Note note;//this note is to save record
    private String myTitle;
    private int myIsStar;
    private int originalIsStar;
    private String myContent;
    private String myGroupName;
    private String myNoteTime;
    private Boolean editFlag;//this flag is to distinguish edit or new,new=0,edit=1
    private ImageView star_image;
    private static final int cutTitleLength = 20;//get the title of first 20
    private ProgressDialog loadingDialog;
    private ProgressDialog insertDialog;
    public EditFragment() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditFragment newInstance(String param1, String param2) {
        EditFragment fragment = new EditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mParam1 = getArguments().getString(ARG_PARAM1);
            editFlag=Boolean.valueOf(mParam1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView=inflater.inflate(R.layout.fragment_edit, container, false);
        iniView(fragmentView);
        return fragmentView;
    }
    private void iniView(View view){
        currentActivity=getActivity();
        tv_new_time =view.findViewById(R.id.tv_new_time);
        groupDao = new GroupDao(currentActivity);
        noteDao = new NoteDao(currentActivity);
        note = new Note();
        star_image=view.findViewById(R.id.star_img);

        insertDialog = new ProgressDialog(currentActivity);
        insertDialog.setMessage("Loading...");
        insertDialog.setCanceledOnTouchOutside(false);

        et_new_title = (EditText) view.findViewById(R.id.et_new_title);
        et_new_content = (RichTextEditor) view.findViewById(R.id.et_new_content);
        tv_new_time = (TextView) view.findViewById(R.id.tv_new_time);
        if (editFlag){ //edit state
            note=((EditActivity)currentActivity).getNoteData();
            myTitle = note.getTitle();
            originalIsStar=note.getIsStar();
            myIsStar=originalIsStar;
            setStarImg();
            myContent = note.getContent();
            myNoteTime = note.getCreateTime();
            Group group = groupDao.queryGroupById(note.getGroupId());
            myGroupName = group.getName();

            loadingDialog = new ProgressDialog(currentActivity);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();
            tv_new_time.setText(note.getCreateTime());
            et_new_title.setText(note.getTitle());
            et_new_content.post(new Runnable() {
                @Override
                public void run() {
                    et_new_content.clearAllLayout();
                    showData(note.getContent());
                }
            });
        } else { //new note state, star sign is clear
            myIsStar=0;
            originalIsStar=0;
            if (myGroupName == null) {
                myGroupName = "default";
            }
            myNoteTime = CommonUtil.date2string(new Date());
            tv_new_time.setText(myNoteTime);
        }
        star_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStarImg();
            }
        });

    }
    public void changeStarImg(){
        if(myIsStar==1){
            star_image.setImageResource(R.drawable.ic_star_off);
            myIsStar = 0;
        } else{
            star_image.setImageResource(R.drawable.ic_star_on);
            myIsStar= 1;
        }
    }
    public void setStarImg(){
        if(myIsStar==1){
            star_image.setImageResource(R.drawable.ic_star_on);
        } else{
            star_image.setImageResource(R.drawable.ic_star_off);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener){
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    /**
     * show data on html format
     * @param html
     */
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
                        //After insertion, a new editText is created to insert text conveniently.
                        et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null){
                            loadingDialog.dismiss();
                        }
                        ((MainActivity)currentActivity).showToast("Picture is destroyed or unavailable");
                    }

                    @Override
                    public void onNext(String text) {
                        if (text.contains("<img") && text.contains("src=")) {
                            //imagePath can be either local path or web path
                            String imagePath = StringUtils.getImgSrc(text);
                            //a new EditText is created to insert text conveniently
                            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                            et_new_content.addImageViewAtIndex(et_new_content.getLastIndex(), imagePath);
                        } else {
                            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
                        }
                    }
                });
    }
    //show what have stored
    protected void showEditData(Subscriber<? super String> subscriber, String html) {
        try{
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                subscriber.onNext(text);
            }
            subscriber.onCompleted();
        }catch (Exception e){
            e.printStackTrace();
            subscriber.onError(e);
        }
    }

    /**
     * get edited data
     */
    private String getEditData() {
        List<RichTextEditor.EditData> editList = et_new_content.buildEditData();
        StringBuffer content = new StringBuffer();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
                content.append(itemData.inputStr);
            } else if (itemData.imagePath != null) {
                content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
            }
        }
        return content.toString();
    }

    /*
     * for new note, the next step is to jump to list after saving,
     * while for edited note, the next step is to finish current activity
     */
    public boolean saveNoteData() {
        String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        String groupName = "default";
        String noteTime = tv_new_time.getText().toString();
        //data has no change, reture true;
        if(noteTitle.equals(note.getContent()) && note.getIsStar()==myIsStar
                && noteContent.equals(note.getContent()) )
            return false;
        Group group = groupDao.queryGroupByName(myGroupName);
        {
            if (noteTitle.length() == 0 ){//If title is empty, a bit content is filled
                noteTitle="New Note";
            }
            int groupId = 1;
            note.setTitle(noteTitle);
            note.setContent(noteContent);
            note.setIsStar(myIsStar);
            note.setGroupId(groupId);
            note.setGroupName(groupName);
            note.setType(2);
            note.setBgColor("#FFFFFF");
            note.setIsEncrypt(0);
            note.setCreateTime(CommonUtil.date2string(new Date()));
            if (!editFlag) {//new note
                if (noteTitle.length() == 0 && noteContent.length() == 0 && myIsStar==0) {
                        Toast.makeText( currentActivity, "Empty note fails to save", Toast.LENGTH_LONG).show();
                        return false;
                } else {
                    //update database
                    long noteId = noteDao.insertNote(note);
                    note.setId((int) noteId);
                }
            }
            else {//edit note
                if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                        || !noteTime.equals(myNoteTime)|| originalIsStar!=myIsStar) {
                    noteDao.updateNote(note);
                }
            }
        }
        return true;
    }
    public void insertImagesASync(final Intent data){
        insertDialog.show();
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try{
                    et_new_content.measure(0, 0);
                    int width = CommonUtil.getScreenWidth(getContext());
                    int height = CommonUtil.getScreenHeight(getContext());
                    ArrayList<Uri> photos  =(ArrayList<Uri>) Matisse.obtainResult(data);
                    //insert certain photos once
                    for (Uri imageUri : photos) {
                        Bitmap bitmap=getBitmapFromUri(imageUri);
                        //temporary file to store pic
                        File file=getFile(bitmap);
                        String imagePath=file.getPath();
                        bitmap = ImageUtils.getSmallBitmap(imagePath, width, height);//压缩图片
                        //delete temporary file
                        file.delete();
                        imagePath = SDCardUtil.saveToSdCard(bitmap);
                        subscriber.onNext(imagePath);
                    }
                    subscriber.onCompleted();
                }catch (Exception e){
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//production event is in io
                .observeOn(AndroidSchedulers.mainThread())//consumtion event is in UI
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        insertDialog.dismiss();
                        et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), " ");
                        Toast.makeText(currentActivity,"Insert Photo Successfully",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        insertDialog.dismiss();
                        Toast.makeText(currentActivity,"Fail to Insert Photo:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(String imagePath) {
                        et_new_content.insertImage(imagePath, et_new_content.getMeasuredWidth());
                    }
                });
    }
    public void createSaveConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setTitle("Confirmation");
        builder.setMessage("Save this new note？");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveNoteData();
                ((MainActivity)currentActivity).onFragmentInteraction(null);
            }
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }
    public boolean newNoteShouldSaved(){
        String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        //data has no change, reture true;
        if(noteTitle.length()!=0 || myIsStar==1
                ||noteContent.length()!=0){
            return true;
        } else
            return false;
    }
    private Bitmap getBitmapFromUri(Uri uri) {
        Uri imageUri =uri;
        InputStream inputStream;
        Bitmap bitmap = null;
        try {
            inputStream = currentActivity.getContentResolver().openInputStream(imageUri);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(currentActivity,"get image failed",Toast.LENGTH_SHORT);
        }
       return bitmap;
    }
    private File getFile(Bitmap bitmap){
        String pictureDir=null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ByteArrayOutputStream baos = null;
        File file = null;
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            String saveDir = Environment.getExternalStorageDirectory()
                    + "/notes";
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdir();
            }
            file = new File(saveDir, Calendar.getInstance().getTime()+".jpg");
            file.delete();
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(byteArray);
            pictureDir = file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

}
