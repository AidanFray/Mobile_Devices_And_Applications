package mobile.labs.acw;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mobile.labs.acw.JSON.JSON;
import mobile.labs.acw.Utility.Screen;

/**
 * Activity that contains the grid of images and where the game will actually be played.
 * It will handle loading images into the grid, score calculation and the code required to play
 * the game
 */
public class PuzzleSolvingActivity extends FragmentActivity
        implements PuzzleGridFragment.OnFragmentInteractionListener {

    private final String FRAGMENT_TAG = getClass().getSimpleName();

    //Layout's views
    private Spinner mPuzzleSpinner;
    private PuzzleGridFragment mGridFragment;
    private TextView mTimeTextView;
    private TextView mScoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_solving);

        mTimeTextView = (TextView) findViewById(R.id.time_TextView);
        mScoreTextView = (TextView) findViewById(R.id.score_TextView);

        mPuzzleSpinner = (Spinner) findViewById(R.id.puzzleSpinner);
        mPuzzleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onPuzzleSelection(adapterView, view, i, l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        if (savedInstanceState == null) {
            mGridFragment = new PuzzleGridFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.gridLayout, mGridFragment);
            transaction.commit();

        } else {
            mGridFragment = (PuzzleGridFragment) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_TAG);
        }

        loadDownloadedPuzzles();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, FRAGMENT_TAG, mGridFragment);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGridFragment != null && mGridFragment.mTimeStarted) {
            mGridFragment.pauseTimeUpdate();
            mGridFragment.mShowResumeMenu = true;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (mGridFragment.mShowResumeMenu) {
            showResumePopup();
        }
    }

    /**
     * Run when the selected index on the puzzle spinner is changed
     *
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    private void onPuzzleSelection(AdapterView<?> adapterView, View view, int i, long l) {

        if (view != null) {
            mGridFragment.onPuzzleSelection(view);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Loads the names of the downloaded puzzles that are currently present on the device
     */
    private void loadDownloadedPuzzles() {

        String puzzleIndexDir
                = getDir(getString(R.string.puzzleIndexDir), MODE_PRIVATE).getAbsolutePath();
        File indexFile
                = new File(puzzleIndexDir + "/" + getString(R.string.puzzleIndexLocalName));

        //If there is not index file
        if (!indexFile.exists()) {
            showNeedToDownloadPuzzleToast();
            return;
        }

        //Grabs the values from the json
        JSONObject index = JSON.ReadFromFile(indexFile.getAbsolutePath());
        JSONArray indexValues = JSON.GetJSONArray(index, getString(R.string.jsonArrayIndexName));

        //Checks if puzzles are downloaded
        SharedPreferences sharedPreferences
                = getSharedPreferences(getString(R.string.puzzleSharedPreferencesID),
                MODE_PRIVATE);

        List<String> downloadedPuzzles = new ArrayList<>();

        //Adds the nothing selected value
        downloadedPuzzles.add(getString(R.string.NoPuzzleSelected));

        for (int i = 0; i < indexValues.length(); i++) {
            String puzzleName = (String) JSON.GetIndex(indexValues, i);

            //Chops off the file name
            puzzleName = puzzleName.split(".json")[0];

            if (sharedPreferences.getBoolean(puzzleName, false)) {
                downloadedPuzzles.add(puzzleName);
            }
        }

        //If there are no puzzles downloaded
        if (downloadedPuzzles.size() == 0) {
            showNeedToDownloadPuzzleToast();
            return;
        }

        //Adds the values to the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_textview, downloadedPuzzles) {

            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                return v;
            }

            //TODO: Issues with API??
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                v.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                v.setBackgroundColor(Color.BLACK);
                ((TextView) v).setTextColor(Color.WHITE);
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mPuzzleSpinner.setAdapter(adapter);
    }

    /**
     * Toast used to inform the user that they need to download a puzzle
     */
    private void showNeedToDownloadPuzzleToast() {
        Toast.makeText(
                this,
                "There are not puzzles to play, please download a puzzle",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Resets the entire Activity to the initial state
     */
    private void resetActivity() {
        //Resets the grid fragment
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        overridePendingTransition(0, 0);
        finish();
        startActivity(intent);
        finish();
    }

    /**
     * This method shows the menu containing a single button that resumes the time counter
     * and score incrementation
     */
    private void showResumePopup() {
        new ResumeDialog(this).show();
    }

    // ## Fragment methods
    @Override
    public void ResetPuzzle() {
        resetActivity();
    }

    @Override
    public void UpdateTime(double pTimeValue) {
        String value = String.format(getString(R.string.scoring_time), pTimeValue);
        mTimeTextView.setText(value);
    }

    @Override
    public void UpdateScore(double pScoreValue) {
        mScoreTextView.setText(
                String.format(getString(R.string.scoring_score), pScoreValue));
    }

    @Override
    public void ResetTimeAndScore() {
        mTimeTextView.setText(String.format(getString(R.string.scoring_time), 0.0));
        mScoreTextView.setText(String.format(getString(R.string.scoring_score), 0.0));
    }

    class ResumeDialog extends AppCompatDialog {

        Context mContext;
        Button mResumeButton;

        public ResumeDialog(Context context) {
            super(context);

            mContext = context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setTitle("RESUME");

            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            setContentView(layoutInflater.inflate(R.layout.resume_popup, null));

            mResumeButton = (Button) this.findViewById(R.id.resume_button);

            mResumeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mGridFragment != null) {
                        mGridFragment.startScoreUpdate();
                    }
                    cancel();
                }
            });

        }

        @Override
        public void show() {
            super.show();

            Window window = getWindow();
            if (window == null) return;

            float h = Screen.getHeight(PuzzleSolvingActivity.this);
            float w = Screen.getHeight(PuzzleSolvingActivity.this);

            window.setLayout((int) w / 2, (int) h / 2);
        }

    }
}
