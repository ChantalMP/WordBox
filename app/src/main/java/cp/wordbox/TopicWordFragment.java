package cp.wordbox;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import cp.wordbox.recyclerView_models.Topic;

public class TopicWordFragment extends Fragment {

    private FirebaseAuth mAuth;

    private RecyclerView topicList;
    private DatabaseReference topicRef;

    private View myMainView;

    public TopicWordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView =  inflater.inflate(R.layout.fragment_topics, container, false);

        mAuth = FirebaseAuth.getInstance();

        String current_user_id = mAuth.getCurrentUser().getUid();
        topicRef = FirebaseDatabase.getInstance().getReference().child("topics").child(current_user_id);

        topicList = (RecyclerView) myMainView.findViewById(R.id.topics_list);
        topicList.setHasFixedSize(true);
        topicList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        Button addTopicBtn = (Button) myMainView.findViewById(R.id.addTopicBtn);
        addTopicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTopicDialog("Add new Topic");
            }
        });

        return myMainView;
    }

    private void addTopicDialog(String s) {
        final Dialog addDialog = new Dialog(getContext());
        addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addDialog.setContentView(R.layout.add_topic_dialog);

        final EditText topicNameInput = (EditText) addDialog.findViewById(R.id.add_topic_dialog_input);
        Button addTopicBtn = (Button) addDialog.findViewById(R.id.add_topic_dialog_btn);

        //set Dialog width according to device size
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(addDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        addDialog.getWindow().setAttributes(lp);

        addTopicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String topicName = topicNameInput.getText().toString();
                if(topicName.equals("")){
                    Toast.makeText(getContext(), "Please write a topic name.", Toast.LENGTH_SHORT).show();
                    return;
                }

                addDialog.cancel();

                topicRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //create unique random key for word
                        DatabaseReference topic_key = topicRef.push();
                        //get key
                        String topicId = topic_key.getKey();
                        //set values
                        topicRef.child(topicId).child("name").setValue(topicName);
                        topicRef.child(topicId).child("sortVersion").setValue(topicName.toLowerCase());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        addDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        //get Data from Firebase with Firebase Recycler Apdapter
        //model class, ViewHolder Class
        Query topicquery = topicRef.orderByChild("sortVersion");
        FirebaseRecyclerAdapter<Topic, TopicViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Topic, TopicViewHolder>(
                Topic.class,
                R.layout.topic_item,
                TopicViewHolder.class,
                topicquery
        ) {
            @Override
            protected void populateViewHolder(TopicViewHolder viewHolder, final Topic model, final int position) {
                //firebase id of clicked field
                final String id = getRef(position).getKey();
                //used to set values for Recycler View -> displaying data
                viewHolder.setName(model.getName());

                //retrieve Data from user
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("test", "click");
//
//                        FragmentPagerAdapter myAdapter = new FragmentPagerAdapter(getActivity().getSupportFragmentManager()) {
//                            @Override
//                            public Fragment getItem(int position) {
//                                return null;
//                            }
//
//                            @Override
//                            public int getCount() {
//                                return 0;
//                            }
//                        };



                        //new Intent -> Topic -> looks like "All" word list, but only with words in topic
                        //i have saved word ids in topic
                        //Intent to AllWordsFragment -> add word is similar, edit also
                        //just RecyclerAdapter only shows words with those Ids
//                        Intent showWordsIntent = new Intent(getContext(), AllWordsFragment.class);
//                        final ArrayList<String> wordList = new ArrayList<String>();
//                        DatabaseReference topicWordRef = topicRef.child("words");
//                        topicWordRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();
//                                while (children.hasNext()){
//                                    DataSnapshot child = children.next();
//                                    String topicWordId = child.getValue().toString();
//                                    wordList.add(topicWordId);
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                        showWordsIntent.putExtra("wordList", wordList);
//                        startActivity(showWordsIntent);
                    }
                });

                ImageButton delteBtn = (ImageButton) viewHolder.mView.findViewById(R.id.deleteTopicBtn);
                delteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder removeDialogBuilder = new AlertDialog.Builder(getContext());
                        removeDialogBuilder
                                .setMessage("Do you want to remove this topic?")
                                .setCancelable(false)
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final AlertDialog.Builder removeDialogBuilder2 = new AlertDialog.Builder(getContext());
                                        removeDialogBuilder2
                                                .setMessage("Do you also want to remove all words in this topic?")
                                                .setCancelable(false)
                                                .setNegativeButton("Only remove topic", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        topicRef.child(id).removeValue();
                                                    }
                                                })
                                                .setPositiveButton("Remove all words", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        topicRef.child(id).removeValue();
                                                        //delete words, which are in topic - (only if only in this topic, else: only delete topic)
                                                    }
                                                })
                                                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                });
                                        AlertDialog removeDialog2 = removeDialogBuilder2.create();
                                        removeDialog2.setTitle("Remove '" + model.getName() + "'");
                                        removeDialog2.show();

                                        //delete words, which are in topic - (only if only in this topic, else: only delete topic)
                                    }
                                });
                        AlertDialog removeDialog = removeDialogBuilder.create();
                        removeDialog.setTitle("Remove '" + model.getName() + "'");
                        removeDialog.show();
                    }
                });
            }
        };

        topicList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public TopicViewHolder(View itemView) {
            super(itemView);
            mView = itemView; //this will be used by Firebase Adapter
        }

        //same Methodnames as in allUsersClass
        public void setName(String name){
            //get Name from Database and set

            TextView nameF = (TextView) mView.findViewById(R.id.topic_name);
            nameF.setText(name);
        }
    }
}