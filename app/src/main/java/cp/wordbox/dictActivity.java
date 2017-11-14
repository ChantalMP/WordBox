package cp.wordbox;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class dictActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsPagerAdapter myTabsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dict);

        mToolbar = (Toolbar) findViewById(R.id.dict_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Dictionary");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//includes back button

        //Tabs for DictActivity
        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        //set adapter
        myTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsPagerAdapter);
        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }
}
