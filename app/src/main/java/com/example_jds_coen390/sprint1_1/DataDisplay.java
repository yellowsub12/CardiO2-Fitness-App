package com.example_jds_coen390.sprint1_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example_jds_coen390.sprint1_1.utils.RecyclerItemClickListener;
import com.example_jds_coen390.sprint1_1.utils.SessionRecyclerViewAdapter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

//DataDisplay  is the activity that contains the graphing capabilities for blood-ox and heart-rate

public class DataDisplay extends AppCompatActivity {
    DatabaseHelper database;
    RecyclerView list;

    protected SessionRecyclerViewAdapter datesAdapter;

    //The onCreate  collects a list of all sessions in the database to put them in a list by date.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        database = new DatabaseHelper();
        database.withAllSessions(this::SetupListView);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle("  CardiO2 - Graph of Data");
    }

    //This method initialized the list of sessions
    private void SetupListView(List<Session> sessions) {
        list = findViewById(R.id.recycle_view);
        list.setLayoutManager(new LinearLayoutManager(list.getContext()));
        list.setAdapter(new SessionRecyclerViewAdapter(sessions));


        //This listener call the graph generation when a particular session is selected
        list.addOnItemTouchListener(
                new RecyclerItemClickListener(list.getContext(), list, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        SessionRecyclerViewAdapter adapter = (SessionRecyclerViewAdapter) list.getAdapter();

                        database.withSession(adapter.getSession(position).getId(), session -> {
                            SetupGraphView(session);
                        });
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                    }
                })
        );
    }

    //This method creates the chart seen in DataDisplay when called by the listener in SetupListView
    //First by clearing te current data,and then plotting the selected session
    private void SetupGraphView(Session session) {
        double x = 0.0;
        GraphView graph = (GraphView) findViewById(R.id.heartrate_graph);
        graph.removeAllSeries();
        LineGraphSeries<DataPoint> heartRateSeries = new LineGraphSeries<>();
        heartRateSeries.setColor(Color.RED);

        for (double heartrate: session.getHeartrate()) {
            heartRateSeries.appendData(new DataPoint(x, heartrate), true, 10000);
            x += 60;
        }
        graph.addSeries(heartRateSeries);

        x = 0.0;
        LineGraphSeries<DataPoint> oxygenLevelSeries = new LineGraphSeries<>();
        oxygenLevelSeries.setColor(Color.GREEN);

        for (double oxygen: session.getBloodOxygen()) {
            oxygenLevelSeries.appendData(new DataPoint(x, oxygen), true, 10000);
            x += 60;
        }

        graph.addSeries(oxygenLevelSeries);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}