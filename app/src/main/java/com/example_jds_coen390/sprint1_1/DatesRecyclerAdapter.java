package com.example_jds_coen390.sprint1_1;

import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DatesRecyclerAdapter extends RecyclerView.Adapter<DatesRecyclerAdapter.ViewHolder>{
    private List<String> ViewList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_textView);
        }
        public TextView getDateView(){
            return date;
        }
    }

    public DatesRecyclerAdapter(List<String> list ){
        this.ViewList = list;
    }

    @NonNull
    @Override
    public DatesRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DatesRecyclerAdapter.ViewHolder holder, int position) {
        holder.getDateView().setText(ViewList.get(position));
        Log.i("Tag2", " " + ViewList.get(position));
    }

    @Override
    public int getItemCount() {
        return ViewList.size();
    }
   /* public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textTemplate);
        }
        public TextView getTextView() {
            return textView;
        }
    }*/
}