package cp.wordbox.learning;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import cp.wordbox.R;

public class learnActivity extends AppCompatActivity {

    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        mToolbar = (Toolbar) findViewById(R.id.learn_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Learn your words");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//includes back button
    }
}