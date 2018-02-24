package mobile.labs.acw;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Menu");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.gc();
    }

    public void puzzleDownload_Button_OnClick(View pView){
        Intent intent = new Intent(pView.getContext(), PuzzleDownloadActivity.class);
        startActivity(intent);
    }
    public void puzzleSolve_Button_OnClick(View pView) {
        Intent intent = new Intent(pView.getContext(), PuzzleSolvingActivity.class);
        startActivity(intent);
    }
    public void highscore_Button_OnClick(View pView) {
        //TODO: add intent code
    }
}

