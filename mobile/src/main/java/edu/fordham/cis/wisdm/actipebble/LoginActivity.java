package edu.fordham.cis.wisdm.actipebble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Allows users to set their name and choose an activity to collect data for
 * @author Andrew H. Johnston <a href="mailto:ajohnston9@fordham.edu">ajohnston9@fordham.edu</a>
 * @version 1.0STABLE
 */
public class LoginActivity extends Activity {

    /**
     * The field for setting a user's name
     */
    private EditText mName;

    /**
     * The button that takes a user to the training activity
     */
    private Button mStartTraining;

    /**
     * The spinner for choosing an activity
     */
    private Spinner mSpinner;

    /**
     * The spinner to choose sensor sampling frequency
     */
    private Spinner mSpinnerFrequency;


    /**
     * The map that maps a character to its activity name
     */
    private static HashMap<String,Character> spinnerEntries = new HashMap<String, Character>();

    // List of Berg Balance Scale tasks
    static {
        spinnerEntries.put("B. Standing unsupported", 'B');
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mName = (EditText)findViewById(R.id.name);
        mSpinner = (Spinner)findViewById(R.id.spinner);


        final Object[] activities = spinnerEntries.keySet().toArray();

        //sort() is called so the activities will be ordered by grouping (otherwise they'd be in a random order)
        Arrays.sort(activities);

        ArrayAdapter adapter =
                new ArrayAdapter(this, android.R.layout.simple_spinner_item, activities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        // Started implementing the spinner for frequency.
        // TODO when the frequency is selected update the frequencies
        mSpinnerFrequency = (Spinner)findViewById(R.id.sampling_rate);

        String[] frequencies = new String[]{"20Hz", "50Hz", "100Hz"};

        ArrayAdapter<String> frequency_adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, frequencies);
        mSpinnerFrequency.setAdapter(frequency_adapter);



        mStartTraining = (Button)findViewById(R.id.login);

        mStartTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = mName.getText().toString().toLowerCase().trim().replace(" ", "_");
                char activity = spinnerEntries.get(mSpinner.getSelectedItem());

                Intent i = new Intent(getApplicationContext(), MainActivity.class);

                i.putExtra("ACTIVITY", activity);
                i.putExtra("ACTIVITY_NAME", mSpinner.getSelectedItem().toString());
                i.putExtra("NAME", name);

                startActivity(i);

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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
}
