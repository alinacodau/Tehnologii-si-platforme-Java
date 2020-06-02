package com.example.raindrops;

import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.john.waveview.WaveView;

import static com.example.raindrops.GameView.INITIAL_WATER_LEVEL;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;
    private ImageView deleteIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);

        // inflate mainXML
        View mainView = getLayoutInflater().inflate(R.layout.activity_game, null);
        // find container
        LinearLayout container = (LinearLayout) mainView.findViewById(R.id.container);
        // initialize your custom view
        gameView = new GameView(this, point.x, point.y);
        // add your custom view to container
        container.addView(gameView);

        setContentView(mainView);

        EditText editText = findViewById(R.id.editText);
        MyKeyboard keyboard = findViewById(R.id.keyboard);

        InputConnection ic = editText.onCreateInputConnection(new EditorInfo());
        keyboard.setInputConnection(ic);
        deleteIcon = findViewById(R.id.delete_icon);
        setOnClick(deleteIcon, ic);

        Button enterButton = findViewById(R.id.button_enter);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            EditText resultEditText = findViewById(R.id.editText);
            gameView.insertedValue =  Integer.parseInt(resultEditText.getText().toString());
            resultEditText.setText("");
            }
        });

        WaveView waveView = findViewById(R.id.wave_view);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) waveView.getLayoutParams();
        params.height = INITIAL_WATER_LEVEL;
        waveView.setLayoutParams(params);
        gameView.waveView = waveView;
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    private void setOnClick(final ImageView imageView, final  InputConnection inputConnection){
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputConnection == null)
                    return;

                CharSequence selectedText = inputConnection.getSelectedText(0);

                if (TextUtils.isEmpty(selectedText)) {
                    inputConnection.deleteSurroundingText(1, 0);
                } else {
                    inputConnection.commitText("", 1);
                }
            }
        });
    }
}
