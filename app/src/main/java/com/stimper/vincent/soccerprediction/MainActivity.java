package com.stimper.vincent.soccerprediction;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.opencsv.CSVReader;
import com.stimper.vincent.soccerprediction.charting.PieChart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /* Attributes for GUI */
    private static final String TAG = MainActivity.class.getSimpleName();
    /* Index selected team .
     * Needed to disable multiple selection of one team. */
    private static int indTeamA = 0;
    private static int indTeamB = 1;
    /* Boolean button already pressed */
    private static boolean buttonPressed = false;
    /* Index selected team when prediction was made */
    private static int indTeamPredA = 0;
    private static int indTeamPredB = 0;
    /* Flag whether to do animation */
    private static boolean doAnimation = true;
    /* Team identifiers to access resources, coefficients, ... */
    private static String[] leagueLabels = new String[19];
    /* League label */
    private static String currentLeagueLabel = "ger1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* Set leagueLabels */
        Resources resources = getResources();
        leagueLabels = resources.getStringArray(R.array.league_labels);



        /* Get team names and model coefficients */

        /* Get data from assets if necessary */
        getDataFromAssets();

        /* Download team names and coefficients */
        String urlBase = "https://vincent.sumpi.org/SoccerPredictionData/";
        for (String leagueLabel: leagueLabels) {
            downloadFile(urlBase + "ModelCoefficients/" + leagueLabel + ".csv", "modelCoefficients/" + leagueLabel + ".csv");
            downloadFile(urlBase + "TeamNames/" + leagueLabel + ".csv", "teamNames/" + leagueLabel + ".csv");
        }



        /* Build GUI */

        /* Populate spinners with content
         * see: https://developer.android.com/guide/topics/ui/controls/spinner.html */
        /* Team spinners */
        setSpinnerTeamAdapters();
        /* Goal spinners */
        Spinner goalASpinner = (Spinner) findViewById(R.id.goalA);
        Spinner goalBSpinner = (Spinner) findViewById(R.id.goalB);
        int goalMax = 9;
        Integer[] goals = new Integer[goalMax + 1];
        for (int i = 0; i <= goalMax; i++) {
            goals[i] = i;
        }
        ArrayAdapter<Integer> goalsAdapter = new ArrayAdapter<Integer>(this, R.layout.spinner_item, goals);
        goalsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalASpinner.setAdapter(goalsAdapter);
        goalBSpinner.setAdapter(goalsAdapter);
        /* Certainty spinners */
        Spinner certaintySpinner = (Spinner) findViewById(R.id.certainty);
        String[] certainties = new String[11];
        for (int i = 0; i < 11; i++) {
            certainties[i] = Integer.toString(i * 10) + "%";
        }
        ArrayAdapter<CharSequence> certaintyAdapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_item, certainties);
        certaintyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        certaintySpinner.setAdapter(certaintyAdapter);

        /* Place text */
        TextView textView= (TextView) findViewById(R.id.league);
        textView.setText(R.string.ger1);
        textView = (TextView) findViewById(R.id.certainty_text);
        textView.setText(R.string.certainty_text);
        textView = (TextView) findViewById(R.id.predict);
        textView.setText(R.string.predict_text);
        textView = (TextView) findViewById(R.id.result_text);
        textView.setText(R.string.result_text);

        if (buttonPressed) {
            /* Redo prediction */
            predict();
        } else {
            /* Hide prediction group */
            RelativeLayout predictionGroup = (RelativeLayout) findViewById(R.id.prediction_group);
            predictionGroup.setVisibility(LinearLayout.INVISIBLE);
            Log.i(TAG, "invisible");
        }

        /* Event when clicking on the button */
        Button button = (Button) findViewById(R.id.predict);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                predict();
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Resources resources = getResources();
        String packageName = getPackageName();
        for(String leagueLabel: leagueLabels) {
            if (id == resources.getIdentifier("nav_" + leagueLabel, "id", packageName)) {
                if (currentLeagueLabel != leagueLabel) {
                    currentLeagueLabel = new String(leagueLabel);
                    changeLeague();
                }
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /* Prediction */
    private void predict() {
        /* Get coefficients */
        /*      Model */
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader(getApplicationContext().getFilesDir() + "/modelCoefficients/" + currentLeagueLabel + ".csv"));
        } catch (IOException e) {
            Log.e("error", "Could not read modelCoefficients/" + currentLeagueLabel + ".csv.");
            return;
        }
        String[] lambdaAttackHome = null;
        String[] lambdaDefenceHome = null;
        String[] lambdaAttackAway = null;
        String[] lambdaDefenceAway = null;
        try {
            lambdaAttackHome = csvReader.readNext();
            lambdaAttackAway = csvReader.readNext();
            lambdaDefenceHome = csvReader.readNext();
            lambdaDefenceAway = csvReader.readNext();
        } catch (IOException e) {
            Log.e("error", "Could not parse modelCoefficients/" + currentLeagueLabel + ".csv.");
            return;
        }
        Spinner spinner = (Spinner) findViewById(R.id.teamA);
        indTeamPredA = spinner.getSelectedItemPosition();
        spinner = (Spinner) findViewById(R.id.teamB);
        indTeamPredB = spinner.getSelectedItemPosition();
        double lambdaAttackA = 0;
        double lambdaAttackB = 0;
        double lambdaDefenceA = 0;
        double lambdaDefenceB = 0;
        try {
            lambdaAttackA = Double.parseDouble(lambdaAttackHome[indTeamPredA]);
            lambdaAttackB = Double.parseDouble(lambdaAttackAway[indTeamPredB]);
            lambdaDefenceA = Double.parseDouble(lambdaDefenceHome[indTeamPredA]);
            lambdaDefenceB = Double.parseDouble(lambdaDefenceAway[indTeamPredB]);
        } catch (Exception e) {
            Log.e("error", "Could not access team spinner.");
            lambdaAttackA = Double.parseDouble(lambdaAttackHome[0]);
            lambdaAttackB = Double.parseDouble(lambdaAttackAway[0]);
            lambdaDefenceA = Double.parseDouble(lambdaDefenceHome[0]);
            lambdaDefenceB = Double.parseDouble(lambdaDefenceAway[0]);
        }
        /*      Guess */
        spinner = (Spinner) findViewById(R.id.certainty);
        double certainty = ((double) spinner.getSelectedItemPosition()) / 10.0; // Probability of user guess
        spinner = (Spinner) findViewById(R.id.goalA);
        int goalAUser = (int) spinner.getSelectedItemPosition();
        spinner = (Spinner) findViewById(R.id.goalB);
        int goalBUser = (int) spinner.getSelectedItemPosition();

        /* Calculate probabilities */
        double resultA = 0;
        double resultB = 0;
        double pA = 0;
        double pB = 0;
        double win = 0;
        double loss = 0;
        double draw = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                double pA1 = Math.exp(- lambdaAttackA * lambdaDefenceB) * Math.pow(lambdaAttackA * lambdaDefenceB, (double) i) / ((double) factorial(i));
                double pB1 = Math.exp(- lambdaAttackB * lambdaDefenceA) * Math.pow(lambdaAttackB * lambdaDefenceA, (double) j) / ((double) factorial(j));
                double pA2 = 0;
                double pB2 = 0;
                if (i == goalAUser) {
                    pA2 = 1;
                }
                if (j == goalBUser) {
                    pB2 = 1;
                }
                double p = pA1 * pB1 * (1 - certainty) + pA2 * pB2 * certainty;
                resultA += (pA1 * (1 - certainty) + pA2 * certainty) * ((double) i);
                resultB += (pB1 * (1 - certainty) + pB2 * certainty) * ((double) j);
                pA += pA1 * (1 - certainty) + pA2 * certainty;
                pB += pB1 * (1 - certainty) + pB2 * certainty;
                if (i > j) {
                    win += p;
                } else if (i < j) {
                    loss += p;
                } else {
                    draw += p;
                }
            }
        }
        int odds_win = (int) Math.round(win / (win + draw + loss) * 100);
        int odds_draw = (int) Math.round(draw / (win + draw + loss) * 100);
        int odds_loss = 100 - odds_draw - odds_win;
        if (odds_loss < 0) {
            odds_loss = 0;
            if (odds_draw > 0) {
                odds_draw -= 1;
            } else {
                odds_win -= 1;
            }
        }

        /* Show results */
        TextView textView = (TextView) findViewById(R.id.result);
        textView.setText(Integer.toString((int) Math.round(resultA / pA)) + ":" + Integer.toString((int) Math.round(resultB / pB)));
        textView = (TextView) findViewById(R.id.odds_win);
        textView.setText(Integer.toString(odds_win) + "%");
        textView = (TextView) findViewById(R.id.odds_draw);
        textView.setText(Integer.toString(odds_draw) + "%");
        textView = (TextView) findViewById(R.id.odds_loss);
        textView.setText(Integer.toString(odds_loss) + "%");

        /* Draw pie chart */
        PieChart pieChart = (PieChart) findViewById(R.id.pie_chart);
        pieChart.removeAllItems();
        pieChart.addItem((String) getText(R.string.loss), loss, 0xfff44336);
        pieChart.addItem((String) getText(R.string.draw), draw, 0xff3f51b5);
        pieChart.addItem((String) getText(R.string.win), win, 0xff4caf50);
        pieChart.invalidate();

        if (doAnimation) {
            /* Animation */
            ObjectAnimator pieAnimation = ObjectAnimator.ofFloat(pieChart, "paddingShare", 1.0f, .24f);
            pieAnimation.setDuration(400);
            pieAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            ObjectAnimator labelAnimation = ObjectAnimator.ofFloat(pieChart, "labelOpacity", 0.0f, 9.0f / 16.0f);
            labelAnimation.setDuration(200);
            labelAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            AnimatorSet pieChartAnimation = new AnimatorSet();
            pieChartAnimation.play(pieAnimation).before(labelAnimation);
            pieChart.setLabelOpacity(0f);
            pieChartAnimation.start();
        } else {
            pieChart.setPaddingShare(.24f);
            pieChart.setLabelOpacity(9.0f / 16.0f);
        }

        RelativeLayout predictionGroup = (RelativeLayout) findViewById(R.id.prediction_group);
        predictionGroup.setVisibility(LinearLayout.VISIBLE);
        buttonPressed = true;
    }

    private int factorial(int n) {
        if (n == 0) {
            return 1;
        } else {
            return factorial(n - 1) * n;
        }
    }

    private void changeLeague() {
        /* Reset Spinners */
        indTeamA = 0;
        indTeamB = 1;
        Spinner goalSpinner = (Spinner) findViewById(R.id.goalA);
        goalSpinner.setSelection(0);
        goalSpinner = (Spinner) findViewById(R.id.goalB);
        goalSpinner.setSelection(0);
        Spinner certaintySpinner = (Spinner) findViewById(R.id.certainty);
        certaintySpinner.setSelection(0);
        /* Hide predict group */
        RelativeLayout predictionGroup = (RelativeLayout) findViewById(R.id.prediction_group);
        predictionGroup.setVisibility(LinearLayout.INVISIBLE);
        int idLeagueName = 0;
        /* Change teams */
        Resources resources = getResources();
        String packageName = getPackageName();
        for(String leagueLabel: leagueLabels) {
            if (currentLeagueLabel.equals(leagueLabel)) {
                idLeagueName = resources.getIdentifier(leagueLabel, "string", packageName);
            }
        }
        setSpinnerTeamAdapters();
        TextView textView= (TextView) findViewById(R.id.league);
        textView.setText(idLeagueName);
    }

    private void setSpinnerTeamAdapters() {
        /* Sets spinner team adapter to league with currentLeagueLabel */
        Resources resources = getResources();
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader(getApplicationContext().getFilesDir() + "/teamNames/" + currentLeagueLabel + ".csv"));
        } catch (IOException e) {
            Log.e("error", "Could not read file teamNames/" + currentLeagueLabel + ".csv.");
            return;
        }
        String[] teamNames = null;
        try {
            teamNames = csvReader.readNext();
        } catch (IOException e) {
            Log.e("error", "Could not read team names of " + currentLeagueLabel + ".csv.");
            return;
        }
        try {
            Spinner teamASpinner = (Spinner) findViewById(R.id.teamA);
            Spinner teamBSpinner = (Spinner) findViewById(R.id.teamB);
            ArrayAdapter<CharSequence> teamAAdapter = new ArrayAdapter(this, R.layout.spinner_item, teamNames) {
                /* Disable other selected team */
                @Override
                public boolean isEnabled(int position) {
                    if (position == indTeamB) {
                        return false;
                    } else {
                        return true;
                    }
                }

                /* Change color of disabled field */
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View mView = super.getDropDownView(position, convertView, parent);
                    TextView mTextView = (TextView) mView;
                    if (position == indTeamB) {
                        mTextView.setTextColor(Color.GRAY);
                    } else {
                        mTextView.setTextColor(Color.BLACK);
                    }
                    return mView;
                }
            };
            ArrayAdapter<CharSequence> teamBAdapter = new ArrayAdapter(this, R.layout.spinner_item, teamNames) {
                /* Disable other selected team */
                @Override
                public boolean isEnabled(int position) {
                    if (position == indTeamA) {
                        return false;
                    } else {
                        return true;
                    }
                }

                /* Change color of disabled field */
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View mView = super.getDropDownView(position, convertView, parent);
                    TextView mTextView = (TextView) mView;
                    if (position == indTeamA) {
                        mTextView.setTextColor(Color.GRAY);
                    } else {
                        mTextView.setTextColor(Color.BLACK);
                    }
                    return mView;
                }
            };
            teamAAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            teamBAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            teamASpinner.setAdapter(teamAAdapter);
            teamBSpinner.setAdapter(teamBAdapter);
            teamASpinner.setSelection(indTeamA);
            teamBSpinner.setSelection(indTeamB);
        /*      On item selected listener to disable selected item in the other spinner */
            teamASpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    indTeamA = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            teamBSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    indTeamB = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } catch (Exception e) {
            Log.e("error", "Team adapter setup failed.");
        }
    }

    /* Update team names and model coefficients */
    private void downloadFile(final String urlName, final String fileName) {
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(urlName);

                    // Create the new connection
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.connect();

                    // Create  new file
                    File file = new File(getApplicationContext().getFilesDir(), fileName);

                    // Output Stream to download file
                    FileOutputStream fileOutput = new FileOutputStream(file);

                    // Input stream to read data
                    InputStream inputStream = urlConnection.getInputStream();

                    // Store total downloaded bytes
                    int downloadedSize = 0;

                    // Create buffer
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0; //used to store a temporary size of the buffer

                    // Read through the input buffer and write contents to the file
                    while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                        // Add the data in the buffer to the file in the file output stream
                        fileOutput.write(buffer, 0, bufferLength);
                        // Add up the size so we know how much is downloaded
                        downloadedSize += bufferLength;

                    }
                    // Close the output stream when done
                    fileOutput.close();

                    // Catch some possible errors
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void getDataFromAssets() {
        File appDir = getApplicationContext().getFilesDir();
        File modelCoefficients = new File(appDir, "modelCoefficients");
        File teamNames = new File(appDir, "teamNames");
        if (!teamNames.exists() || !modelCoefficients.exists()) {
            AssetManager assetManager = getApplicationContext().getAssets();
            if (!teamNames.exists()) {
                String[] teamNameFiles = null;
                try {
                    teamNameFiles = assetManager.list("teamNames");
                } catch (IOException e) {
                    Log.e("error", "Failed to get asset file list.", e);
                }
                File teamNamesDir = new File(appDir, "teamNames");
                teamNamesDir.mkdirs();
                for (String filename : teamNameFiles) {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = assetManager.open("teamNames/" + filename);
                        File outFile = new File(teamNamesDir, filename);
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    } catch (IOException e) {
                        Log.e("error", "Failed to copy asset file: " + filename, e);
                    }
                }
            }
            if (!modelCoefficients.exists()) {
                String[] modelCoefficientsFiles = null;
                try {
                    modelCoefficientsFiles = assetManager.list("modelCoefficients");
                } catch (IOException e) {
                    Log.e("error", "Failed to get asset file list.", e);
                }
                File modelCoefficientsDir = new File(appDir, "modelCoefficients");
                modelCoefficientsDir.mkdirs();
                for (String filename : modelCoefficientsFiles) {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = assetManager.open("modelCoefficients/" + filename);
                        File outFile = new File(modelCoefficientsDir, filename);
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    } catch (IOException e) {
                        Log.e("error", "Failed to copy asset file: " + filename, e);
                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    /* For debugging */
    private void logFile(String fileName) {
        try {
            // String name = getApplicationContext().getFilesDir() + "/" + fileName;
            FileInputStream instream = new FileInputStream(new File(getApplicationContext().getFilesDir(), fileName));
            if (instream != null)
            {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line,line1 = "";
                try
                {
                    while ((line = buffreader.readLine()) != null) {
                        line1+=line;
                        Log.d("debug", line);
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
    }
    private void logFolderConent(String folderName) {
        File appDir = getApplicationContext().getFilesDir();
        File folder = new File(appDir, folderName);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                Log.d("debug", "File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                Log.d("debug", "Directory " + listOfFiles[i].getName());
            }
        }
    }

}
