package cp.wordbox.textRecognition;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import cp.wordbox.R;

public class textRecActivity extends AppCompatActivity {

    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_rec);

        mToolbar = (Toolbar) findViewById(R.id.text_rec_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Text recognition");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//includes back button
    }
}
