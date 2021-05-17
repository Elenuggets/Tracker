package com.example.tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.wrappers.PackageManagerWrapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    //Buttons
    private Button button;

    //Location
    LocationManager locMan;

    // variable to remember if we are tracking location or not
    public boolean gpsStart = false;

    // File
    private File currentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Location
        locMan = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // click on the button
        this.button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (gpsStart) // the gps is tracking, so now stop the tracking
                {
                    gpsStart =! gpsStart;
                    button.setText("START");
                    // go to the reportActivity
                    Intent reportActivity = new Intent(MainActivity.this,ReportActivity.class);

                    // start the activity
                    startActivity(reportActivity);
                }
                else // begin the recording of the gps
                {
                    gpsStart =! gpsStart;
                    button.setText("END");
                    createGPXFile();
                    createLocationListener();
                }
            }
        });


    } // end onCreate method


    @Override
    protected void onStart(){
        super.onStart();
    } // end onStart

    @Override
    protected void onStop() {
        super.onStop();
        locMan = null;
    } // end onStop


    // return the latitude and the longitude
    private void createLocationListener() {

        try{
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) { // the location is changed
                    //my value of lat and lon
                    double lat= location.getLatitude();
                    double lon= location.getLongitude();

                    String segments = "";
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    segments += "<trkpt lat=\"" + lat + "\" lon=\"" + lon + "\"><time>" + df.format(new Date()) + "</time></trkpt>\n";
                    String footer = "</trkseg></trk></gpx>"; // todo : à voir

                    try
                    {
                        FileWriter writer = new FileWriter(currentFile, true);
                        writer.append(segments);
                        writer.append(footer);
                        writer.flush();
                        writer.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }//try

        catch(SecurityException sc){
            sc.printStackTrace();
        }


    }


    // create the gpx file
    private void createGPXFile(){

        File MyExternalFile; // my file

        // create my file
        String DirPath = "GPStracks";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        MyExternalFile = new File(getExternalFilesDir(DirPath), sdf.format(new Date()));

        // write in my file
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        String name = "<name>" + sdf.format(new Date()) + "</name><trkseg>\n";

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(MyExternalFile);
            String all = header+name;
            fos.write(all.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentFile = MyExternalFile;
    }

    // decode the GPX file
    private List<Location> decodeGPX(File file)
    {
        //todo : decode the file
        List<Location> list = new ArrayList<Location>();

        return list;

    }
}