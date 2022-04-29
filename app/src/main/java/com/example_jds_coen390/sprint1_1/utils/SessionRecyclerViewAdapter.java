package com.example_jds_coen390.sprint1_1.utils;

import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example_jds_coen390.sprint1_1.R;
import com.example_jds_coen390.sprint1_1.Session;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SessionRecyclerViewAdapter extends RecyclerView.Adapter<SessionRecyclerViewAdapter.ViewHolder>{
    private final List<Session> sessions;

    public SessionRecyclerViewAdapter(List<Session> sessions ){
        this.sessions = sessions;
    }
    //Returns the session associated with a particular position.
    public Session getSession(int position) {
        return sessions.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout, parent, false);
        return new ViewHolder(view);
    }

    // sets the correct date_time formatting for the recorded sessions in the DataDisplay activity
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull SessionRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.getDateView().setText(sessions.get(position).getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    //Returns the number of sessions in the list.
    @Override
    public int getItemCount() {
        return sessions.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_textView);
        }
        public TextView getDateView(){
            return date;
        }
    }
}