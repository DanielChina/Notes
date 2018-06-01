package clas.daniel.notes.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import clas.daniel.notes.R;
import clas.daniel.notes.bean.Note;
import clas.daniel.notes.utils.CommonUtil;
import clas.daniel.notes.utils.ImageUtils;
import clas.daniel.notes.utils.StringUtils;

public class MyNoteListAdapter extends RecyclerView.Adapter<MyNoteListAdapter.ViewHolder>
        implements View.OnClickListener, View.OnLongClickListener {
    private Context mContext;
    private List<Note> mNotes;
    private OnRecyclerViewItemClickListener mOnItemClickListener ;
    private OnRecyclerViewItemLongClickListener mOnItemLongClickListener ;

    public MyNoteListAdapter() {
        mNotes = new ArrayList<>();
    }
    public void setmNotes(List<Note> notes) {
        this.mNotes = notes;
    }
    public int imgWidth,imgHeight;
    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //get data by getTag()
            mOnItemClickListener.onItemClick(v,(Note)v.getTag());
        }
    }
    @Override
    public boolean onLongClick(View v) {
        if (mOnItemLongClickListener != null) {
            mOnItemLongClickListener.onItemLongClick(v,(Note)v.getTag());
        }
        return true;
    }
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Note note);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view, Note note);
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        imgWidth= CommonUtil.getScreenWidth(mContext);
        imgHeight=200;
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_item,parent,false);
        //set click response
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Log.i(TAG, "###onBindViewHolder: ");
        final Note note = mNotes.get(position);
        //put data into the tag
        holder.itemView.setTag(note);
        holder.tv_list_title.setText(note.getTitle());
        holder.tv_list_time.setText(note.getCreateTime());
        String content=note.getContent();
        int index=content.indexOf("<img src=");
        if(index>=0){
            holder.tv_list_summary.setText(content.substring(0,index));
        }else{
            holder.tv_list_summary.setText(content);
        }
        String path=StringUtils.getFirstImgPathFromStringData(content);
        Glide.with(mContext).load(path)
                .crossFade().centerCrop()
                .placeholder(R.drawable.small_img_load_fail)
                .error(R.drawable.small_img_load_fail)
                .into(holder.tv_first_note_img);
        holder.tv_first_note_img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if(note.getIsStar()==1)
            holder.tv_star_img.setImageResource(R.drawable.ic_star_on);
        else
            holder.tv_star_img.setImageResource(R.drawable.ic_star_off);
    }
    @Override
    public int getItemCount() {
        if (mNotes != null && mNotes.size()>0){
            return mNotes.size();
        }
        return 0;
    }
    //set each element of recyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_list_title;
        public TextView tv_list_summary;
        public TextView tv_list_time;//created time
        public ImageView tv_star_img;//star sign
        public ImageView tv_first_note_img;
        public CardView card_view_note;

        public ViewHolder(View view){
            super(view);
            card_view_note = (CardView) view.findViewById(R.id.card_view_note);
            tv_list_title = (TextView) view.findViewById(R.id.tv_list_title);
            tv_list_summary = (TextView) view.findViewById(R.id.tv_list_summary);
            tv_list_time = (TextView) view.findViewById(R.id.tv_list_time);
            tv_star_img = (ImageView) view.findViewById(R.id.star_img);
            tv_first_note_img=(ImageView) view.findViewById(R.id.first_note_img);
            //glide need a initial step before show correct size image
            Glide.with(mContext).load((Bitmap) null)
                    .crossFade().centerCrop()
                    .placeholder(R.drawable.small_img_load_fail)
                    .error(R.drawable.small_img_load_fail)
                    .into(tv_first_note_img);
        }
    }
}
