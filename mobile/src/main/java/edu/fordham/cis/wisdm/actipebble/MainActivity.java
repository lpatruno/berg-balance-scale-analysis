package edu.fordham.cis.wisdm.actipebble;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


/**
 * This activity is the one that displays the user's activity and allows them to begin the training
 * @author Andrew H. Johnston <a href="mailto:ajohnston9@fordham.edu">ajohnston9@fordham.edu</a>
 * @version 1.0STABLE
 */
public class MainActivity extends Activity {

    /**
     * String for logging purposes
     */
    private static final String TAG = "MainActivity";

    /**
     * String for Luigi's logging.
     */
    private static final String LTAG = "Luigi's New Code";

    /**
     * Buttons to trigger events
     */
    private Button mStartButton, mStopButton;

    /**
     * Text views to display pebble accelerometer info
     */
    private TextView mActivity, mUsername;

    /**
     * Flag to send to watch to trigger the start of training
     */
    private static final String START_TRAINING = "/start-training";

    /**
     * The label for the activity being done
     */
    private char label;

    /**
     * The user's name
     */
    private String name;

    /**
     * The name of the activity to be collected
     */
    private String actname;

    /**
     * Flag for determining if data collection is occurring
     */
    private boolean isRunning = false;

    /**
     * Enables the activity to track the screen being locked and trigger appropriate events
     */
    private ScreenLockReceiver screenLockReceiver;

    /**
     * Flag to tell if Receiver is registered
     */
    private boolean isReceiverRegistered = false;

    /**
     * Enables communication between the watch and the phone
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Holds the data collection dataManagementService (global so it can be stopped by the cancel button)
     */
    private Intent dataManagementService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();

        if (i != null) {
            label = i.getCharExtra("ACTIVITY", 'A');
            name  = i.getStringExtra("NAME");
            actname = i.getStringExtra("ACTIVITY_NAME");
        } else {
            Toast.makeText(this, "Started without name or activity label", Toast.LENGTH_LONG).show();
        }

        mActivity = (TextView)findViewById(R.id.activity_label);
        mUsername = (TextView)findViewById(R.id.username);

        mUsername.setText(name);
        mActivity.setText(actname);

        mStartButton = (Button)findViewById(R.id.start_button);
        mStopButton = (Button)findViewById(R.id.stop_button);


        /**
         * We actually need to make this button do what it's supposed to do. These things are
         *      1. Start the DataManagementService.
         *      2. Contact the Wear device and start the dataManagementService on the watch as well.
         *      3. After these things are done, the button should be disabled in some way.
         */
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startSampling();
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    /*
                    try {
                        unregisterReceiver(screenLockReceiver);
                    } catch (IllegalArgumentException e) {
                        Log.i(TAG, "Unregistered receiver was asked to unregister. Ignoring.");
                    }
                    */

                    //Avoid some NullPointerExceptions
                    if (isRunning) {
                        isRunning = false;
                        stopService(dataManagementService);
                    }
                    finish();
            }
        });


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "Connected to wearable.");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "Connection to wearable suspended. Code: " + i);
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
        mGoogleApiClient.connect();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenLockReceiver = new ScreenLockReceiver();
        //registerReceiver(screenLockReceiver, intentFilter);
        isReceiverRegistered = true;
    }

    /**
     * This function is called when the start button is clicked.
     * It starts sampling the phone's accelerometer and gyroscope and also contacts the wearable
     * to do the same. The isRunning boolean is set to prevent the service from restarting.
     */
    private void startSampling(){

        if (!isRunning){
            // Only start if the service isn't already running.
            Log.d(LTAG, "Service is now starting.");

            isRunning = true;
            // Now start the dataManagementService and do everything else here

            // Start the DataManagementService which samples the sensors
            dataManagementService = new Intent(getApplicationContext(), DataManagementService.class);
            dataManagementService.putExtra("NAME", name);
            dataManagementService.putExtra("ACTIVITY", label);
            startService(dataManagementService);

            // Contact the Wearable to begin sampling its sensors
            new Thread(new Worker()).start();


            Toast.makeText(getApplicationContext(), "Please lock phone and place in pocket.",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Display error message if service already running
            Log.d(LTAG, "Service is already started. Do nothing.");
            Toast.makeText(getApplicationContext(),
                    "Service already running. Please hit the stop button.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {

        /*
                I am commenting all of this out because it is being changed.
                The dataManagementService will start/stop with the start/stop buttons
                I will also get rid of this screen shutting off thing

        // The ScreenLockReceiver boolean is true only when the screen had been shut off
        // Hence, the contents of this block only occur when onPause is called since the screen
        // has been shut off.
        if(ScreenLockReceiver.wasScreenOn && isRunning) {

            // Start the DataManagementService which samples the sensors
            dataManagementService = new Intent(this, DataManagementService.class);
            dataManagementService.putExtra("NAME", name);
            dataManagementService.putExtra("ACTIVITY", label);
            startService(dataManagementService);

            // Contact the Wearable to begin sampling its sensors
            new Thread(new Worker()).start();

            // Unregister the screenLockReceiver.
            // Not sure why Andrew does this.
            try {
                unregisterReceiver(screenLockReceiver);
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "Unregistered receiver was asked to unregister. Ignoring.");
            }

            // Set to false so this block isn't triggered again if we turn the screen on and off,
            // I imagine
            isRunning = false;
        }
        */
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sends the "start training" message to the wearable. Needs to be a separate thread
     * because you can't call .await() from the UI thread
     */
    class Worker implements Runnable {

        @Override
        public void run() {
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

            for (Node node : nodes.getNodes()) {

                MessageApi.SendMessageResult result;

                Log.d(TAG, "Started message sending process.");

                result = Wearable.MessageApi.sendMessage(
                        mGoogleApiClient, node.getId(), START_TRAINING, null).await();

                Log.d(TAG, "Sent to node: " + node.getId() + " with display name: " + node.getDisplayName());

                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "ERROR: failed to send Message: " + result.getStatus());
                } else {
                    Log.d(TAG, "Message Successfully sent.");
                }
            }
        }
    }
}

