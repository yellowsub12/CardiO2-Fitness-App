package com.example_jds_coen390.sprint1_1;

import androidx.annotation.NonNull;

import com.example_jds_coen390.sprint1_1.handlers.SessionHandler;
import com.example_jds_coen390.sprint1_1.handlers.SessionListHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String KEY_SESSION = "session";

    FirebaseDatabase database;

    public DatabaseHelper() {
        this.database = FirebaseDatabase.getInstance();
    }

    //To add a measurement session to the database.
    public void addSession(String id, long timestamp, List<Double> heartRate, List<Double> bloodOxygen) {
        Session session = new Session(id, timestamp, heartRate, bloodOxygen);

        database.getReference()
                .child(KEY_SESSION)
                .child(id)
                .setValue(session);
    }

    //When passed a session ID, this will fetch the corresponding session from the database.
    public void withSession(String id, SessionHandler handler) {
        database.getReference()
                .child(KEY_SESSION)
                .child(id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Session session = snapshot.getValue(Session.class);
                        handler.handle(session);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //When passed a handler, this retrieves all sessions associated with it from the database.
    public void withAllSessions(SessionListHandler handler)
    {
        database.getReference()
                .child(KEY_SESSION)
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Session> sessions = new ArrayList<>();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            sessions.add(child.getValue(Session.class));
                        }

                        handler.handle(sessions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}