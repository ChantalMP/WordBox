package cp.wordbox;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RootFragment extends Fragment {

    private static final String TAG = "RootFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.activity_root_fragment, container, false);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
		/*
		 * When this container fragment is created, we fill it with our first
		 * "real" fragment
		 */
        transaction.replace(R.id.root_frame, new TopicWordFragment());

        transaction.commit();

        return view;
    }
}
