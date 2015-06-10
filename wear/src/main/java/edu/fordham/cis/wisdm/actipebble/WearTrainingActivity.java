package edu.fordham.cis.wisdm.actipebble;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.common.collect.Lists;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Handles the collection of data and the transmission of the data back to the phone in batches.
 * @author Andrew H. Johnston <a href="mailto:ajohnston9@fordham.edu">ajohnston9@fordham.edu</a>
 * @version 1.0STABLE
 */
public class WearTrainingActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private TextView mPrompt;
    private TextView mProgress;

    private GoogleApiClient googleApiClient;

    private ArrayList<AccelerationRecord> mAccelerationRecords = new ArrayList<AccelerationRecord>();
    private ArrayList<GyroscopeRecord> mGyroscopeRecords = new ArrayList<GyroscopeRecord>();

    /**
     * Debugging tag 
     */
    private static final String TAG = "WearTrainingActivity";

    /**
     * Flag that signals the end of data transmission to the phone
     */
    private static final String DATA_COLLECTION_DONE = "/COMPLETE";

    /**
     * Maximum number of records we can send to the phone in one transmission.
     */
    private static final int MAX_RECORDS_SENT_AT_ONCE = 3500;

    /**
     * One of the strategies to keep the watch screen on
     */
    private PowerManager.WakeLock wakeLock;

    /**
     * Timer used for the sensor recording
     */
    private Timer timer;

    /**
     * Timer task used for the sensor recording
     */
    private TimerTask timerTask;

    /**
     * If true, record data from sensors.
     */
    private boolean shouldCollect = false;

    /**
     * Sample rate, expressed as number of microseconds between samplings
     */
    private static final int SAMPLE_RATE = SensorManager.SENSOR_DELAY_FASTEST;

    /**
     * Number of seconds to collect data.
     */
    private static final int SECONDS_TO_COLLECT = 120;

    /**
     * Total time for data collection in milliseconds.
     */
    private static final int COLLECTION_TIME = SECONDS_TO_COLLECT*1000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        //Easy way to keep watch from sleeping on me
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mProgress = (TextView) findViewById(R.id.txtProgress);
        mPrompt = (TextView) findViewById(R.id.txtPrompt);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mProgress = (TextView) findViewById(R.id.txtProgress);
                mPrompt = (TextView) findViewById(R.id.txtPrompt);
            }
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "Connected to phone.");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "Connection to phone suspended. Code: " + i);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "Fuck! Connection failed: " + connectionResult);
                    }
                })
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope     = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG);
        wakeLock.acquire();
    }


    @Override
    protected void onResume() {
        super.onResume();

        timer = new Timer();

        timerTask = new TimerTask() {
            // Set the collection variable to false. This will stop the collection of data.
            @Override
            public void run() {
                shouldCollect = false;
                new Thread(new SendDataToPhoneTask()).start();
            }
        };

        // Set collection variable to true
        shouldCollect = true;

        //Register sensors at SensorManager.SENSOR_DELAY_FASTEST
        mSensorManager.registerListener(this, mAccelerometer, SAMPLE_RATE);
        mSensorManager.registerListener(this, mGyroscope, SAMPLE_RATE);
        Log.i(TAG, "Registered sensors at " + SAMPLE_RATE + " rate.");

        timer.schedule(timerTask, COLLECTION_TIME);
        Log.i(TAG, "Recording for " + COLLECTION_TIME / 1000 + " seconds.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Set collection variable to false
        shouldCollect = false;

        // If the timer has been set, cancel all events.
        if(timer != null) {
            timer.cancel();
        }

        // Release the wake lock
        if(wakeLock.isHeld()) {
            wakeLock.release();
        }

        // Unregister the sensors.
        mSensorManager.unregisterListener(this);
    }

    /**
     * Handler for incoming sensor records.
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (shouldCollect) {

            long timestamp = System.currentTimeMillis();
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            switch(event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    mAccelerationRecords.add(new AccelerationRecord(x,y,z,timestamp));
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    mGyroscopeRecords.add(new GyroscopeRecord(x,y,z,timestamp));
                    break;
            }
        }
    }

    /**
     * Not used but must be overridden.
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    /**
     * Class that implements runnable. Used to send data from the watch to the phone when collection
     * is complete.
     *
     */
    class SendDataToPhoneTask implements Runnable {

        @Override
        public void run() {
            Log.i(TAG, "Preparing to send data.");

            try {

                // Partition the lists of records in smaller lists for transfer purposes.
                List<List<AccelerationRecord>> accelLists =
                        Lists.partition(mAccelerationRecords, MAX_RECORDS_SENT_AT_ONCE);
                List<List<GyroscopeRecord>> gyroLists =
                        Lists.partition(mGyroscopeRecords, MAX_RECORDS_SENT_AT_ONCE);

                // Process and send acceleration lists
                for (List<AccelerationRecord> list : accelLists) {
                    Log.i(TAG, "Sending list of acceleration records...");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    ArrayList<AccelerationRecord> tmp = new ArrayList<AccelerationRecord>(list);

                    Log.i(TAG, "List is of size: " + tmp.size());

                    oos.writeObject(tmp);
                    oos.flush();
                    oos.close();

                    byte[] data = baos.toByteArray();
                    PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/accel-data");
                    dataMapRequest.getDataMap().putByteArray("/accel", data);
                    PutDataRequest request = dataMapRequest.asPutDataRequest();

                    PendingResult<DataApi.DataItemResult> pendingResult =
                            Wearable.DataApi.putDataItem(googleApiClient, request);
                }

                // Process and send gyroscope lists followed by the completion message.
                for (int i = 0; i < gyroLists.size(); i++) {
                    Log.d(TAG, "Sending list of gyroscope records...");

                    List<GyroscopeRecord> list = gyroLists.get(i);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oosG = new ObjectOutputStream(baos);
                    ArrayList<GyroscopeRecord> tmp = new ArrayList<GyroscopeRecord>(list);

                    Log.i(TAG, "List is of size: " + tmp.size());

                    oosG.writeObject(tmp);
                    oosG.flush();
                    oosG.close();

                    byte[] data = baos.toByteArray();
                    PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/gyro-data");
                    dataMapRequest.getDataMap().putByteArray("/gyro", data);

                    if ((i+1) == gyroLists.size()) {
                        dataMapRequest.getDataMap().putString("/done", DATA_COLLECTION_DONE);
                    } else {
                        dataMapRequest.getDataMap().putString("/done", "/not-done");
                    }
                    PutDataRequest request = dataMapRequest.asPutDataRequest();
                    PendingResult<DataApi.DataItemResult> pendingResult =
                            Wearable.DataApi.putDataItem(googleApiClient, request);
                }


                // Vibrate and tell the user to check their phone
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(500L); //Vibrate for half a second
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPrompt.setText("Please finish the training by opening your phone.");
                        mProgress.setText("");
                    }
                });

            } catch (IOException e) {
                //TODO Implement some sort of data saving in this case.
                Log.e(TAG, "Could not send data. Please see following message:\n\n" + e.getMessage());

            } finally {
                //Release wake lock.
                wakeLock.release();
            }

        }
    }
}
