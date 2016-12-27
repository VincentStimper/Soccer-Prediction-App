package com.stimper.vincent.soccerprediction;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.stimper.vincent.soccerprediction.charting.PieChart;

public class MainActivity extends AppCompatActivity {

    /* Attributes for GUI */
    private static final String TAG = MainActivity.class.getSimpleName();
    /* Index selected team .
     * Needed to disable multiple selection of one team. */
    private static int indTeamA = 1;
    private static int indTeamB = 2;
    /* Boolean button already pressed */
    private static boolean buttonPressed = false;
    /* Index selected team when prediction was made */
    private static int indTeamPredA = 0;
    private static int indTeamPredB = 0;
    /* Flag whether to do animation */
    private static boolean doAnimation = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_table);


        /* Build GUI */

        /* Populate spinners with content
         * see: https://developer.android.com/guide/topics/ui/controls/spinner.html */
        /* Team spinners */
        Spinner teamASpinner = (Spinner) findViewById(R.id.teamA);
        Spinner teamBSpinner = (Spinner) findViewById(R.id.teamB);
        Resources res = getResources();
        ArrayAdapter<CharSequence> teamAAdapter =  new ArrayAdapter(this, R.layout.spinner_item, res.getStringArray(R.array.bundesliga_teams)) {
            /* Disable other selected team */
            @Override
            public boolean isEnabled(int position) {
                if (position == indTeamB) {
                    return false;
                } else {
                    indTeamA = position;
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
        ArrayAdapter<CharSequence> teamBAdapter =  new ArrayAdapter(this, R.layout.spinner_item, res.getStringArray(R.array.bundesliga_teams)) {
            /* Disable other selected team */
            @Override
            public boolean isEnabled(int position) {
                if (position == indTeamA) {
                    return false;
                } else {
                    indTeamB = position;
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
        TextView textView= (TextView) findViewById(R.id.instruction);
        textView.setText(R.string.instruction);
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


    /* Prediction */
    private void predict() {
        /* Get coefficients */
        /*      Model */
        Resources res = getResources();
        String[] lambdaAttack = res.getStringArray(R.array.lambda_attack);
        String[] lambdaDefence = res.getStringArray(R.array.lambda_defence);
        Spinner spinner = (Spinner) findViewById(R.id.teamA);
        indTeamPredA = spinner.getSelectedItemPosition();
        spinner = (Spinner) findViewById(R.id.teamB);
        indTeamPredB = spinner.getSelectedItemPosition();
        double lambdaAttackA = Double.parseDouble(lambdaAttack[indTeamPredA]);
        double lambdaAttackB = Double.parseDouble(lambdaAttack[indTeamPredB]);
        double lambdaDefenceA = Double.parseDouble(lambdaDefence[indTeamPredA]);
        double lambdaDefenceB = Double.parseDouble(lambdaDefence[indTeamPredB]);
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
        double won = 0;
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
                    won += p;
                } else if (i < j) {
                    loss += p;
                } else {
                    draw += p;
                }
            }
        }
        int odds_win = (int) Math.round(won / (won + draw + loss) * 100);
        int odds_draw = (int) Math.round(draw / (won + draw + loss) * 100);
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
        pieChart.addItem((String) getText(R.string.win), loss, 0xff4caf50);
        pieChart.addItem((String) getText(R.string.draw), draw, 0xff3f51b5);
        pieChart.addItem((String) getText(R.string.loss), won, 0xfff44336);
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

}
