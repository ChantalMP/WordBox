package cp.wordbox;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WordBox");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_button){
            mAuth.signOut(); //logout from firebase
            Intent startPageIntent = new Intent(MainActivity.this, LoginActivity.class);
            //not allow to go back to main act with pressing back button
            startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(startPageIntent);
        }

        return true;
    }

    public void dictBtn(View view){
        Intent i = new Intent(MainActivity.this, dictActivity.class);
        startActivity(i);
    }

    public void grammarBtn(View view){
        Intent i = new Intent(MainActivity.this, grammarActivity.class);
        startActivity(i);
    }

    public void learnBtn(View view){
        Intent i = new Intent(MainActivity.this, learnActivity.class);
        startActivity(i);
    }

    public void textRecBtn(View view){
        Intent i = new Intent(MainActivity.this, textRecActivity.class);
        startActivity(i);
    }
}
