package com.example_jds_coen390.sprint1_1;

import static java.lang.Boolean.TRUE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import java.util.Date;
import com.firebase.ui.auth.AuthUI;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.os.CountDownTimer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    public static final int BLUETOOTH_REQ_CODE = 1;
    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    static final byte SOS = (byte) 's';
    static final byte EOS = (byte) 'e';
    static final byte SOO = (byte) 't';
    static final byte EOO = (byte) 'o';
    Button settings;
    ToggleButton toggle;
    Button Data_button;
    DatabaseHelper data;
    String bpm;
    BluetoothAdapter bluetoothAdapter;
    String spO2;
    CountDownTimer countDownTimer;
    //boolean isCounterRunning = false;
    private ImageView profilePic;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    String path;
    String latest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Initialize interface, database instance, etc...
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Date date = new Date(System.currentTimeMillis());
        Long timestamp = dateToTimestamp(date);
        path = Long.toString(timestamp);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        profilePic = findViewById(R.id.profilePic);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        // calling this activity's function to
        // use ActionBar utility methods
        ActionBar actionBar = getSupportActionBar();

        // providing title for the ActionBar
        actionBar.setTitle("   CardiO2!");

        // providing subtitle for the ActionBar
        actionBar.setSubtitle("   Your Top Health App!");

        // adding icon in the ActionBar
        actionBar.setIcon(R.drawable.app_logo);

        // methods to display the icon in the ActionBar
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        //This initializes the toggle button that controls the starts and ends for each measurement session.
        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        Data_button = (Button) findViewById(R.id.Data_act_button);
        data = new DatabaseHelper();        //timer();
        assert getSupportActionBar() != null;
        TextView HR_textView = findViewById(R.id.HR_textView);
        TextView SPO_textView = findViewById(R.id.SPO_textView);
        // Connects to the bluetooth chip connected to the arduino
        Runnable HRrunnable = () -> {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter.isEnabled() == TRUE){
            System.out.println(btAdapter.getBondedDevices());

            BluetoothDevice hc05 = btAdapter.getRemoteDevice("00:18:91:D7:24:BD");   //find what the mac address of the connected BT module is on your android device
            System.out.println(hc05.getName());
            BluetoothSocket btSocket = null;
            int counter = 0;

                do {
                    try {
                        btSocket = hc05.createRfcommSocketToServiceRecord(mUUID);
                        System.out.println(btSocket);
                        btSocket.connect();
                        System.out.println(btSocket.isConnected());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    counter++;
                } while (!btSocket.isConnected() && counter < 3); //make sure connection made with bluetooth

            // When connected to the device, this reads the incoming information from the arduino
            InputStream inputStream = null;
            bpm = "";
            spO2 = "";

            //flag indicates whether bpm or sp02 is being transferred
            boolean Flag = false;
            int i = 0;
            //double j = 0;
            try {
                inputStream = btSocket.getInputStream();

                while (i != -1) {
                    i = inputStream.read();
                    //  j = inputStream.read();
                    if (i == 0) continue;

                    if (i == SOS) { // received start of bpm character
                        bpm = "";
                        Flag = false;
                        continue;
                    }

                    if (i == EOS) { // received end of bpm character
                        System.out.println(bpm);
                        HR_textView.setText(bpm);

                        continue;
                    }

                    if (i == SOO) { // received start of sp02 character
                        spO2 = "";
                        Flag = true;
                        continue;
                    }

                    if (i == EOO) { //received end of sp02 character
                        System.out.println(spO2);
                        SPO_textView.setText(spO2);
                        continue;
                    }
                    if(Flag == false){ // if bpm mode, add incoming characters to bpm
                        bpm += (char) i;
                    }
                    if(Flag == true){ // if sp02 mode, add characters to sp02
                        spO2 += (char) i;
                    }
                }
                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                btSocket.close();
                System.out.println(btSocket.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }};
        Thread HRthread = new Thread(HRrunnable);
        HRthread.start();



    }


    @Override
    protected void onStart(){
        super.onStart();



        // initialize button to bring user to DataDisplay activity
        Data_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DataDisplay.class);
                intent.putExtra("passPath", latest);
                Log.i("Tag", latest + "");
                startActivity(intent);
            }
        });

        // when a measurement session is active, show some graphics to the user, and initialize the timer
        toggle.setOnClickListener(view -> {
            if (toggle.isChecked()) {
                countDownTimer = createTimer();
                countDownTimer.start();
                ImageView img=(ImageView)findViewById(R.id.heartgif);
                img.setVisibility(View.VISIBLE);
                ImageView img2=(ImageView)findViewById(R.id.spo2gif);
                img2.setVisibility(View.VISIBLE);
                return;
            }

            if (countDownTimer != null) { // if timer already exists, finish what it was doing, then reset.
                countDownTimer.onFinish();
                countDownTimer.cancel();
                ImageView img=(ImageView)findViewById(R.id.heartgif);
                img.setVisibility(View.INVISIBLE);
                ImageView img2=(ImageView)findViewById(R.id.spo2gif);
                img2.setVisibility(View.INVISIBLE);
            }
        });
        FirebaseAuth.getInstance().addAuthStateListener(this);

    }

    //timer that controls measurement sessions. In parallel to bluetooth communication,
    // save bpm and sp02 measurements every second for a minute
    public CountDownTimer createTimer() {
        return new CountDownTimer(60000, 1000) {
            final String id = UUID.randomUUID().toString();
            final ArrayList<Double> heartrates = new ArrayList<>();
            final ArrayList<Double> bloodoxygen = new ArrayList<>();

            @Override
            public void onTick(long millisUntilFinished) {
                if (!bpm.equals("")) {
                    double heart = Double.parseDouble(bpm);
                    heartrates.add(heart);

                }
                if (!spO2.equals("")) {
                    double oxy = Double.parseDouble(spO2);
                    bloodoxygen.add(oxy);
                }
            }

            //Save measurement session to database when finishing
            @Override
            public void onFinish() {
                data.addSession(id, System.currentTimeMillis(), heartrates, bloodoxygen);
                toggle.setChecked(false);
            }
        };
    }

    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    // Control's in-app access to phone's bluetooth adapter
    @Override
    public boolean onOptionsItemSelected( @NonNull MenuItem item ) {
        bluetoothAdapter = bluetoothAdapter.getDefaultAdapter();

        switch (item.getItemId()) {
           case R.id.bluetoothsettingsactionbar:
                if(!bluetoothAdapter.isEnabled()){
                    Intent blueToothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(blueToothIntent, BLUETOOTH_REQ_CODE);
                }else{
                    bluetoothAdapter.disable();
                }
                return true;
            case R.id.logout_btn:
                AuthUI.getInstance().signOut(this);
                return true;
        }
        return super.onOptionsItemSelected(item);


    }

    // Allows user to select a profile picture
    private void choosePicture(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);

    }

    // Sets up Profile Picture for App
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            profilePic.setImageURI((imageUri));
            uploadPicture();
        }
    }

    //saves user profile picture
    private void uploadPicture() {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image...");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference mountainsRef = storageReference.child("images/" + randomKey);

        mountainsRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), "Image Uploaded.", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(),"Failed To Upload",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progressPercent= (100.00 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        pd.setMessage("Progress: "+(int) progressPercent+"%");
                    }
                });

    }


    //Necessary for Firebase authentication (Profile picture, data, login, etc)
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
    //Necessary for Action Bar menu elements
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

}


