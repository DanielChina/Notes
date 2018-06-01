package clas.daniel.notes;

import android.content.ContentProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


import clas.daniel.notes.adapter.MyNoteListAdapter;
import clas.daniel.notes.bean.Note;
import clas.daniel.notes.database.NoteDao;
import clas.daniel.notes.utils.StringUtils;
import clas.daniel.notes.view.SpacesItemDecoration;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ListFragment extends Fragment{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private static int type;  //TYPE__ALL_LIST or TYPE_STAR_LIST
    private static final String TAG = "MainActivity";
    private RecyclerView rv_list_main;
    private MyNoteListAdapter adapter;
    private NoteDao noteDao;
    private int groupId;//ID
    private MainActivity activity;
    private View view;
    private String searchContent;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListFragment() {
    }
    // TODO: Customize parameter initialization
    public static ListFragment newInstance(int type) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, 1);
        fragment.setArguments(args);
        ListFragment.type=type;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list, container, false);
        iniView();
        return view;
    }
    private void iniView(){
        activity=(MainActivity) getActivity();

        rv_list_main = (RecyclerView) view.findViewById(R.id.rv_list_main);
        rv_list_main.addItemDecoration(new SpacesItemDecoration(0));
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
     //   layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_list_main.setLayoutManager(layoutManager);
        adapter = new MyNoteListAdapter();
        setAdapterData();
        rv_list_main.setAdapter(adapter);
        adapter.setOnItemClickListener(new MyNoteListAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Note note) {
                activity.onListFragmentInteraction(note);
            }
        });
        adapter.setOnItemLongClickListener(new MyNoteListAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final Note note) {
                createDeleteConfirmationDialog(note);
            }
        });
    }
    public void createDeleteConfirmationDialog(Note deletedNote){
        final Note note=deletedNote;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Confirmation");
        builder.setMessage("Delete this note？");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int ret = noteDao.deleteNote(note.getId());
                if (ret > 0){
                    updateScreen();
                }
            }
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnListFragmentInteractionListener){
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Note note);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateScreen();
        rv_list_main.scrollToPosition(0);

    }
    public List<Note> reverseListSequence(List<Note> list){
        List<Note> newList=new ArrayList<>();
        int len=list.size();
        for(int i=0;i<len;i++){
            newList.add(list.get(len-1-i));
        }
        return newList;
    }
    public void setAdapterData(){
        List<Note> list;
        noteDao = new NoteDao(activity);
        if(ListFragment.type== Constant.TYPE_STAR_LIST){
            list=refreshStarList();
        } else
            list=refreshNoteList();
        adapter.setmNotes(list);
    }
    private List<Note> setFilter(String searchContent, List<Note> sourceList){
        //store new List
        List<Note> newNoteList=new ArrayList<>();
        List<String> searchableList=getSearchedSourceList(sourceList);
        int count= searchableList.size();
        for(int i=0;i<count;i++){
            if(searchableList.get(i).contains(searchContent)){
                newNoteList.add(sourceList.get(i));
            }
        }
        return newNoteList;
    }
    public void setSearchContent(String searchContent) {
        String text = searchContent.toLowerCase();
        if(ListFragment.type==Constant.TYPE_NOTE_LIST)
            adapter.setmNotes(setFilter(text,refreshNoteList()));
        else
            adapter.setmNotes(setFilter(text,refreshStarList()));
        adapter.notifyDataSetChanged();
    }
    //set the searchable key words
    public List<String> getSearchedSourceList(List<Note> list){
        List<String> resultList=null;
        int len=list.size();
        if(len!=0){
            resultList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                String storedData = list.get(i).getTitle() + list.get(i).getContent();
                storedData = StringUtils.getTextFromStringData(storedData);
                resultList.add(storedData.toLowerCase());
            }
        }
        return resultList;
    }
    public List<Note> refreshNoteList(){
        List<Note> noteList=noteDao.queryNotesAll(groupId);
        return reverseListSequence(noteList);
    }
    public List<Note> refreshStarList(){
        List<Note> noteList=noteDao.queryNotesAll(groupId);
        noteList=reverseListSequence(noteList);
        List<Note> list=new ArrayList<>();
        for (int i = 0; i < noteList.size(); i++) {
            if(noteList.get(i).getIsStar()==1){
                list.add(noteList.get(i));
            }
        }
        return list;
    }
    public void updateScreen(){
        setAdapterData();
        adapter.notifyDataSetChanged();
    }
}
