package cp.wordbox;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

/**
 * Created by Chantal on 20.10.2017.
 */

//Adapter for Tabs View Pager
class TabsPagerAdapter extends FragmentPagerAdapter{
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    //get position when someone clicks on tabs
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                AllWordsFragment allWordsFragment = new AllWordsFragment();
                return allWordsFragment;
            case 1:
                TopicWordFragment topicWordFragment = new TopicWordFragment();
                return topicWordFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;//we have two fragments
    }

    //Titles for the Tabs
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "ALL";
            case 1:
                return "TOPICS";
            default:
                return null;
        }
    }
}
