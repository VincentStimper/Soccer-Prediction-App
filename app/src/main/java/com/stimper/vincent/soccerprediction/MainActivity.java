package com.stimper.vincent.soccerprediction;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
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

import com.stimper.vincent.soccerprediction.charting.PieChart;

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
    /* League label */
    private static String leagueLabel = "ger1";
    /* Resource id model coefficients */
    private static int idAttackHome = 0;
    private static int idAttackAway = 0;
    private static int idDefenceHome = 0;
    private static int idDefenceAway = 0;


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


        /* Set coefficient ids */
        idAttackHome = R.array.lambda_attack_home_ger1;
        idAttackAway = R.array.lambda_attack_away_ger1;
        idDefenceHome = R.array.lambda_defence_home_ger1;
        idDefenceAway = R.array.lambda_defence_away_ger1;


        /* Build GUI */

        /* Populate spinners with content
         * see: https://developer.android.com/guide/topics/ui/controls/spinner.html */
        /* Team spinners */
        setSpinnerTeamAdapters(R.array.team_names_ger1);
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

        if (id == R.id.nav_bel1) {
            if (leagueLabel != "bel1") {
                leagueLabel = "bel1";
                changeLeague();
            }
        } else if (id == R.id.nav_eng1) {
            if (leagueLabel != "eng1") {
                leagueLabel = "eng1";
                changeLeague();
            }
        } else if (id == R.id.nav_eng2) {
            if (leagueLabel != "eng2") {
                leagueLabel = "eng2";
                changeLeague();
            }
        } else if (id == R.id.nav_eng3) {
            if (leagueLabel != "eng3") {
                leagueLabel = "eng3";
                changeLeague();
            }
        } else if (id == R.id.nav_eng4) {
            if (leagueLabel != "eng4") {
                leagueLabel = "eng4";
                changeLeague();
            }
        } else if (id == R.id.nav_fra1) {
            if (leagueLabel != "fra1") {
                leagueLabel = "fra1";
                changeLeague();
            }
        } else if (id == R.id.nav_fra2) {
            if (leagueLabel != "fra2") {
                leagueLabel = "fra2";
                changeLeague();
            }
        } else if (id == R.id.nav_ger1) {
            if (leagueLabel != "ger1") {
                leagueLabel = "ger1";
                changeLeague();
            }
        } else if (id == R.id.nav_ger2) {
            if (leagueLabel != "ger2") {
                leagueLabel = "ger2";
                changeLeague();
            }
        } else if (id == R.id.nav_ita1) {
            if (leagueLabel != "ita1") {
                leagueLabel = "ita1";
                changeLeague();
            }
        } else if (id == R.id.nav_ita2) {
            if (leagueLabel != "ita2") {
                leagueLabel = "ita2";
                changeLeague();
            }
        } else if (id == R.id.nav_net1) {
            if (leagueLabel != "net1") {
                leagueLabel = "net1";
                changeLeague();
            }
        } else if (id == R.id.nav_por1) {
            if (leagueLabel != "por1") {
                leagueLabel = "por1";
                changeLeague();
            }
        } else if (id == R.id.nav_sco1) {
            if (leagueLabel != "sco1") {
                leagueLabel = "sco1";
                changeLeague();
            }
        } else if (id == R.id.nav_sco2) {
            if (leagueLabel != "sco2") {
                leagueLabel = "sco2";
                changeLeague();
            }
        } else if (id == R.id.nav_sco3) {
            if (leagueLabel != "sco3") {
                leagueLabel = "sco3";
                changeLeague();
            }
        } else if (id == R.id.nav_sco4) {
            if (leagueLabel != "sco4") {
                leagueLabel = "sco4";
                changeLeague();
            }
        } else if (id == R.id.nav_spa1) {
            if (leagueLabel != "spa1") {
                leagueLabel = "spa1";
                changeLeague();
            }
        } else if (id == R.id.nav_spa2) {
            if (leagueLabel != "spa2") {
                leagueLabel = "spa2";
                changeLeague();
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
        Resources res = getResources();
        String[] lambdaAttackHome = res.getStringArray(idAttackHome);
        String[] lambdaDefenceHome = res.getStringArray(idDefenceHome);
        String[] lambdaAttackAway = res.getStringArray(idAttackAway);
        String[] lambdaDefenceAway = res.getStringArray(idDefenceAway);
        Spinner spinner = (Spinner) findViewById(R.id.teamA);
        indTeamPredA = spinner.getSelectedItemPosition();
        spinner = (Spinner) findViewById(R.id.teamB);
        indTeamPredB = spinner.getSelectedItemPosition();
        double lambdaAttackA = Double.parseDouble(lambdaAttackHome[indTeamPredA]);
        double lambdaAttackB = Double.parseDouble(lambdaAttackAway[indTeamPredB]);
        double lambdaDefenceA = Double.parseDouble(lambdaDefenceHome[indTeamPredA]);
        double lambdaDefenceB = Double.parseDouble(lambdaDefenceAway[indTeamPredB]);
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
        if (leagueLabel == "bel1") {
            idAttackHome = R.array.lambda_attack_home_bel1;
            idAttackAway = R.array.lambda_attack_away_bel1;
            idDefenceHome = R.array.lambda_defence_home_bel1;
            idDefenceAway = R.array.lambda_defence_away_bel1;
            idLeagueName = R.string.bel1;
            setSpinnerTeamAdapters(R.array.team_names_bel1);
        } else if (leagueLabel == "eng1") {
            idAttackHome = R.array.lambda_attack_home_eng1;
            idAttackAway = R.array.lambda_attack_away_eng1;
            idDefenceHome = R.array.lambda_defence_home_eng1;
            idDefenceAway = R.array.lambda_defence_away_eng1;
            idLeagueName = R.string.eng1;
            setSpinnerTeamAdapters(R.array.team_names_eng1);
        } else if (leagueLabel == "eng2") {
            idAttackHome = R.array.lambda_attack_home_eng2;
            idAttackAway = R.array.lambda_attack_away_eng2;
            idDefenceHome = R.array.lambda_defence_home_eng2;
            idDefenceAway = R.array.lambda_defence_away_eng2;
            idLeagueName = R.string.eng2;
            setSpinnerTeamAdapters(R.array.team_names_eng2);
        } else if (leagueLabel == "eng3") {
            idAttackHome = R.array.lambda_attack_home_eng3;
            idAttackAway = R.array.lambda_attack_away_eng3;
            idDefenceHome = R.array.lambda_defence_home_eng3;
            idDefenceAway = R.array.lambda_defence_away_eng3;
            idLeagueName = R.string.eng3;
            setSpinnerTeamAdapters(R.array.team_names_eng3);
        } else if (leagueLabel == "eng4") {
            idAttackHome = R.array.lambda_attack_home_eng4;
            idAttackAway = R.array.lambda_attack_away_eng4;
            idDefenceHome = R.array.lambda_defence_home_eng4;
            idDefenceAway = R.array.lambda_defence_away_eng4;
            idLeagueName = R.string.eng4;
            setSpinnerTeamAdapters(R.array.team_names_eng4);
        } else if (leagueLabel == "fra1") {
            idAttackHome = R.array.lambda_attack_home_fra1;
            idAttackAway = R.array.lambda_attack_away_fra1;
            idDefenceHome = R.array.lambda_defence_home_fra1;
            idDefenceAway = R.array.lambda_defence_away_fra1;
            idLeagueName = R.string.fra1;
            setSpinnerTeamAdapters(R.array.team_names_fra1);
        } else if (leagueLabel == "fra2") {
            idAttackHome = R.array.lambda_attack_home_fra2;
            idAttackAway = R.array.lambda_attack_away_fra2;
            idDefenceHome = R.array.lambda_defence_home_fra2;
            idDefenceAway = R.array.lambda_defence_away_fra2;
            idLeagueName = R.string.fra2;
            setSpinnerTeamAdapters(R.array.team_names_fra2);
        } else if (leagueLabel == "ger1") {
            idAttackHome = R.array.lambda_attack_home_ger1;
            idAttackAway = R.array.lambda_attack_away_ger1;
            idDefenceHome = R.array.lambda_defence_home_ger1;
            idDefenceAway = R.array.lambda_defence_away_ger1;
            idLeagueName = R.string.ger1;
            setSpinnerTeamAdapters(R.array.team_names_ger1);
        } else if (leagueLabel == "ger2") {
            idAttackHome = R.array.lambda_attack_home_ger2;
            idAttackAway = R.array.lambda_attack_away_ger2;
            idDefenceHome = R.array.lambda_defence_home_ger2;
            idDefenceAway = R.array.lambda_defence_away_ger2;
            idLeagueName = R.string.ger2;
            setSpinnerTeamAdapters(R.array.team_names_ger2);
        } else if (leagueLabel == "ita1") {
            idAttackHome = R.array.lambda_attack_home_ita1;
            idAttackAway = R.array.lambda_attack_away_ita1;
            idDefenceHome = R.array.lambda_defence_home_ita1;
            idDefenceAway = R.array.lambda_defence_away_ita1;
            idLeagueName = R.string.ita1;
            setSpinnerTeamAdapters(R.array.team_names_ita1);
        } else if (leagueLabel == "ita2") {
            idAttackHome = R.array.lambda_attack_home_ita2;
            idAttackAway = R.array.lambda_attack_away_ita2;
            idDefenceHome = R.array.lambda_defence_home_ita2;
            idDefenceAway = R.array.lambda_defence_away_ita2;
            idLeagueName = R.string.ita2;
            setSpinnerTeamAdapters(R.array.team_names_ita2);
        } else if (leagueLabel == "net1") {
            idAttackHome = R.array.lambda_attack_home_net1;
            idAttackAway = R.array.lambda_attack_away_net1;
            idDefenceHome = R.array.lambda_defence_home_net1;
            idDefenceAway = R.array.lambda_defence_away_net1;
            idLeagueName = R.string.net1;
            setSpinnerTeamAdapters(R.array.team_names_net1);
        } else if (leagueLabel == "por1") {
            idAttackHome = R.array.lambda_attack_home_por1;
            idAttackAway = R.array.lambda_attack_away_por1;
            idDefenceHome = R.array.lambda_defence_home_por1;
            idDefenceAway = R.array.lambda_defence_away_por1;
            idLeagueName = R.string.por1;
            setSpinnerTeamAdapters(R.array.team_names_por1);
        } else if (leagueLabel == "sco1") {
            idAttackHome = R.array.lambda_attack_home_sco1;
            idAttackAway = R.array.lambda_attack_away_sco1;
            idDefenceHome = R.array.lambda_defence_home_sco1;
            idDefenceAway = R.array.lambda_defence_away_sco1;
            idLeagueName = R.string.sco1;
            setSpinnerTeamAdapters(R.array.team_names_sco1);
        } else if (leagueLabel == "sco2") {
            idAttackHome = R.array.lambda_attack_home_sco2;
            idAttackAway = R.array.lambda_attack_away_sco2;
            idDefenceHome = R.array.lambda_defence_home_sco2;
            idDefenceAway = R.array.lambda_defence_away_sco2;
            idLeagueName = R.string.sco2;
            setSpinnerTeamAdapters(R.array.team_names_sco2);
        } else if (leagueLabel == "sco3") {
            idAttackHome = R.array.lambda_attack_home_sco3;
            idAttackAway = R.array.lambda_attack_away_sco3;
            idDefenceHome = R.array.lambda_defence_home_sco3;
            idDefenceAway = R.array.lambda_defence_away_sco3;
            idLeagueName = R.string.sco3;
            setSpinnerTeamAdapters(R.array.team_names_sco3);
        } else if (leagueLabel == "sco4") {
            idAttackHome = R.array.lambda_attack_home_sco4;
            idAttackAway = R.array.lambda_attack_away_sco4;
            idDefenceHome = R.array.lambda_defence_home_sco4;
            idDefenceAway = R.array.lambda_defence_away_sco4;
            idLeagueName = R.string.sco4;
            setSpinnerTeamAdapters(R.array.team_names_sco4);
        } else if (leagueLabel == "spa1") {
            idAttackHome = R.array.lambda_attack_home_spa1;
            idAttackAway = R.array.lambda_attack_away_spa1;
            idDefenceHome = R.array.lambda_defence_home_spa1;
            idDefenceAway = R.array.lambda_defence_away_spa1;
            idLeagueName = R.string.spa1;
            setSpinnerTeamAdapters(R.array.team_names_spa1);
        } else if (leagueLabel == "spa2") {
            idAttackHome = R.array.lambda_attack_home_spa2;
            idAttackAway = R.array.lambda_attack_away_spa2;
            idDefenceHome = R.array.lambda_defence_home_spa2;
            idDefenceAway = R.array.lambda_defence_away_spa2;
            idLeagueName = R.string.spa2;
            setSpinnerTeamAdapters(R.array.team_names_spa2);
        }
        TextView textView= (TextView) findViewById(R.id.league);
        textView.setText(idLeagueName);
    }

    private void setSpinnerTeamAdapters(int idTeams) {
        Resources res = getResources();
        Spinner teamASpinner = (Spinner) findViewById(R.id.teamA);
        Spinner teamBSpinner = (Spinner) findViewById(R.id.teamB);
        ArrayAdapter<CharSequence> teamAAdapter =  new ArrayAdapter(this, R.layout.spinner_item, res.getStringArray(idTeams)) {
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
        ArrayAdapter<CharSequence> teamBAdapter =  new ArrayAdapter(this, R.layout.spinner_item, res.getStringArray(idTeams)) {
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
    }

}
