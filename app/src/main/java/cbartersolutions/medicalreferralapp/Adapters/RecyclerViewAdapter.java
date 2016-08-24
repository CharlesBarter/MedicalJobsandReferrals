package cbartersolutions.medicalreferralapp.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.ArrayList;

import cbartersolutions.medicalreferralapp.Fragments.RecyclerViewFragment;
import cbartersolutions.medicalreferralapp.Others.Note;
import cbartersolutions.medicalreferralapp.R;

/**
 * Created by Charles on 24/08/2016.
 */
public class RecyclerViewAdapter extends RecyclerSwipeAdapter<RecyclerViewAdapter.SimpleViewHolder> {

    private Context mContext;
    private ArrayList<Note> mNotes;
    private RecyclerViewFragment fragment_using_adapter;

    private static OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder{
        SwipeLayout swipeLayout;
        TextView listPatientName, listPatientNHI, listPatientAge_Sex, listPatientLocation,
                listDetails;

        public SimpleViewHolder(final View itemView) {
            super(itemView);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.list_swipe);
            listPatientName = (TextView) itemView.findViewById(R.id.listItemPatientName);
            listPatientNHI = (TextView) itemView.findViewById(R.id.listItemPatientNHI);
            listPatientAge_Sex = (TextView) itemView.findViewById(R.id.list_age_sex);
            listPatientLocation = (TextView) itemView.findViewById(R.id.listItemLocation);
            listDetails = (TextView) itemView.findViewById(R.id.listItemDetails);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        //triggers click upwards to fragment
                        listener.onItemClick(itemView, getLayoutPosition());
                    }
                }
            });
        }
    }

    public RecyclerViewAdapter (Context context, ArrayList<Note> notes, RecyclerViewFragment fragment){
        mContext = context;
        mNotes = notes;
        fragment_using_adapter = fragment;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_swipe_layout,
                parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder viewHolder, int position) {
        Note note = mNotes.get(position);

    }

    @Override
    public int getItemCount(){
        return mNotes.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.list_swipe;
    }
}