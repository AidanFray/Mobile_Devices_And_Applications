package mobile.labs.acw;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Menu");
    }

    //Button_OnClick
    public void puzzleDownload_Button_OnClick(View pView){
        Log.i("MainActivity_LOG", "puzzleDownload_Button_OnClick() Clicked!");

        Intent intent = new Intent(pView.getContext(), PuzzleDownload.class);
        startActivity(intent);
    }

    public void highscore_Button_OnClick(View pView) {
        Log.i("MainActivity_LOG", "highscore_Button_OnClick() Clicked!");

        //TODO: Add intent code
    }
}

