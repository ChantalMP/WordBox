package cp.wordbox;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class grammarActivity extends AppCompatActivity {

    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar);

        mToolbar = (Toolbar) findViewById(R.id.grammar_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Grammar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//includes back button
    }
}
