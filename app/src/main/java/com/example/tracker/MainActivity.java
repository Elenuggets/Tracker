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
import java.lang.reflect.Array;
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

    // Hours
    Date startDate;
    Date endDate;

    // Location Table
    public static ArrayList locationTbl = new ArrayList<Location>();

    // Location Speed and Distance
    public static ArrayList locationDate = new ArrayList<Date>();
    public static ArrayList locationTime = new ArrayList<Double>();
    public static ArrayList locationDist = new ArrayList<Double>();
    public static ArrayList<Float> locationSpeed = new ArrayList<>();

    //Altitude max and min
    double minAlt;
    double maxAlt;

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
                    gpsStart =! gpsStart; // change the boolean
                    button.setText("START"); // set the button to START now

                    // save the end time
                    endDate = new Date();
                    String curTime = endDate.getHours() + ":" + endDate.getMinutes() + ":" + endDate.getSeconds();
                    System.out.println("L'HEURE END : "+curTime);

                    // the time
                    double time = Time(startDate,endDate)[3];

                    // the distance
                    double d = Distance();

                    // the speed
                    double speed = d/time;

                    // the min/max altitude
                    MinMaxAlt();

                    // go to the reportActivity
                    Intent reportActivity = new Intent(MainActivity.this,ReportActivity.class);

                    // push the data
                    reportActivity.putExtra("timeDay",Time(startDate,endDate)[0]);
                    reportActivity.putExtra("timeHour",Time(startDate,endDate)[1]);
                    reportActivity.putExtra("timeMinute",Time(startDate,endDate)[2]);
                    reportActivity.putExtra("timeSecond",Time(startDate,endDate)[3]);
                    reportActivity.putExtra("distance",d);
                    reportActivity.putExtra("speed",speed);
                    reportActivity.putExtra("minalt",minAlt);
                    reportActivity.putExtra("maxalt",maxAlt);

                    // start the activity
                    startActivity(reportActivity);
                }
                else // begin the recording of the gps
                {
                    gpsStart =! gpsStart; // change the boolean
                    button.setText("END"); // set the button to END now
                    createGPXFile(); // create the gpx file
                    createLocationListener(); // save the location each 5s

                    // save the start time
                    startDate = new Date();
                    String curTime = startDate.getHours() + ":" + startDate.getMinutes() + ":" + startDate.getSeconds();
                    Log.d("Debug","L'HEURE START : "+curTime);
                }
            }
        });

    } // end onCreate method


    @Override
    protected void onStart(){
        super.onStart(); } // end onStart

    @Override
    protected void onStop() {
        super.onStop();
        locMan = null;
    } // end onStop


    // create the location
    private void createLocationListener() {

        try{
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) { // the location is changed

                    //my value of lat and lon
                    double lat= location.getLatitude();
                    double lon= location.getLongitude();
                    double alt= location.getAltitude();

                    // create the Point
                    Location Point =new Location("Point");
                    Point.setLongitude(lon);
                    Point.setLatitude(lat);
                    Point.setAltitude(alt);
                    locationTbl.add(Point); // add the Point to my List


                    // for the time (get the time difference between each point)
                    Date currentDate;
                    if (locationDate.size()==0) // the first localisation
                    {
                        // save the time in the table
                        currentDate = new Date();
                        locationDate.add(currentDate);
                        locationTime.add(0);
                    }

                    else
                    {
                        currentDate = new Date();
                        // get the difference of the two time in seconds
                        double secondsdiff = Time((Date) locationDate.get(locationDate.size() - 1),currentDate)[3];
                        locationDate.add(currentDate);
                        locationTime.add(secondsdiff);
                    }

                    Log.d("Debug","LE TIME DANS LOCATION "+locationTime);

                    // for the distance
                    if (locationDist.size()==0)
                    {
                        locationDist.add(0);
                    }

                    else
                    {
                        DistanceTbl((Location) locationTbl.get(locationTbl.size()-2),Point); // for the distance
                    }


                    Log.d("Debug","LA DISTANCE DANS LOCATION "+locationDist);

                    // for the speed
                    if (locationSpeed.size() == 0)
                    {
                        locationSpeed.add((float)0);
                    }

                    else
                    {
                        Speed();
                    }

                    Log.d("Debug","LE SPEED DANS LOCATION "+locationSpeed);

                    // write in my gpx file
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
    } // end createLocationListener()


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
    } // end createGPXFile()

    // calculate the total distance between Locations
    private double Distance(){
        int size = locationTbl.size(); // number of locations
        double dist = 0; // the counter of distance
        for (int i = 0; i<size-1;i++) // calculate the distance between each locations
        {
            float[] results = new float[1];
            Location startLocation = (Location) locationTbl.get(i); // location start
            Location endLocation = (Location) locationTbl.get(i+1); // location end

            // calculate the distance
            Location.distanceBetween(startLocation.getLatitude(), startLocation.getLongitude(), endLocation.getLatitude(), endLocation.getLongitude(), results);
            dist+=results[0]; // add to the distance count
        }
        return dist;
    } // end Distance()

    // calculate the total distance between each Points
    private void DistanceTbl(Location A, Location B){
        float[] results = new float[1];

        // calculate the distance
        Location.distanceBetween(A.getLatitude(), A.getLongitude(), B.getLatitude(), B.getLongitude(), results);
        locationDist.add(results[0]); // add to the distance count

    } // end Distance()

    // calculate the difference of time
    private long[] Time(Date startDate, Date endDate){
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();
        long[] diffDate = new long[5]; // table to save my data

        // time in milli
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli; // calculate the elapsedDay
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli; // calculate the elapsedHours
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli; // calculate the elapsedMinutes
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli; // calculate the elapsedSeconds

        diffDate[0] = elapsedDays; // number of date
        diffDate[1] = elapsedHours; // number of hours
        diffDate[2] = elapsedMinutes; // number of minutes
        diffDate[3] = elapsedSeconds; // number of seconds
        diffDate[4] = different; // number of miliseconds

        System.out.println("LA DATE DIFF EST : "+diffDate[0]+" "+diffDate[1]+" "+diffDate[2]+" "+diffDate[3]+" "+diffDate[4]);
        return diffDate;
    } // end Time()


    private void Speed(){
        float d = (float) locationDist.get(locationDist.size()-1);
        double t = (double) locationTime.get(locationTime.size()-1);
        double s = (double) ((d/t)*3.6); // convert to km/h
        locationSpeed.add((float)s);

    }

    private void MinMaxAlt(){
        int size = locationTbl.size();
        Location Loc = (Location) locationTbl.get(0);
        double max = Loc.getAltitude();
        double min = max;
        for (int i = 1; i < size;i++){
            Location currentLoc = (Location) locationTbl.get(i);
            double current = currentLoc.getAltitude();
            if (current < min) min = current;
            if (current > max) max = current;
        }
        minAlt = min;
        maxAlt = max;
    }
}