package cp.wordbox;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import cp.wordbox.recyclerView_models.Topic;

public class AddWordEditTopic extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private RecyclerView topicList;
    private DatabaseReference topicRef;
    private DatabaseReference wordsRef;

    private String wordId;
    private ArrayList<String> topicIdsTopics;

    private ArrayList<String> selected;
    private ArrayList<String> selectedOld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_word_edit_topic);

        selected = new ArrayList<String>();
        selectedOld = new ArrayList<String>();
        topicIdsTopics = new ArrayList<String>();

        mAuth = FirebaseAuth.getInstance();

        String current_user_id = mAuth.getCurrentUser().getUid();
        topicRef = FirebaseDatabase.getInstance().getReference().child("topics").child(current_user_id);
        wordsRef = FirebaseDatabase.getInstance().getReference().child("words").child(current_user_id);

        topicList = (RecyclerView) findViewById(R.id.topics_list);
        topicList.setHasFixedSize(true);
        topicList.setLayoutManager(new LinearLayoutManager(AddWordEditTopic.this));

        Button addTopicBtn = (Button) findViewById(R.id.addTopicBtn);
        Button saveButton = (Button) findViewById(R.id.add_word_edit_topic_save_btn);

        addTopicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //addTopicDialog("Add new Topic");
                Log.i("test", "noch ist es nicht möglich hier Themen hinzuzufügen");
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected.removeAll(selectedOld);
                Intent dataIntent = new Intent();
                dataIntent.putExtra("result", selected);
                setResult(RESULT_OK, dataIntent);
                finish();
                //populate checked boxes to Firebase
//                for (final String topic : selected) {
//                    if (!selectedOld.contains(topic)) {
//
//                        //add word to topic->words
//                        for(final String id : topicIdsTopics){
//                            DatabaseReference nameRef = topicRef.child(id).child("name");
//                            nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    String name = dataSnapshot.getValue().toString();
//                                    if (topic.equals(name)){
//                                        DatabaseReference wordKey = topicRef.child(id).child("words").push();
//                                        String topicWordId = wordKey.getKey();
//                                        //set value at reference to word
//                                        topicRef.child(id).child("words").child(topicWordId).setValue(wordId);
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });
//                        }
//                    }
//                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //get word Id from extras
        wordId = getIntent().getExtras().getString("wordId");
        topicsChecked();

        //get Data from Firebase with Firebase Recycler Apdapter
        //model class, ViewHolder Class
        Query topicquery = topicRef.orderByChild("sortVersion");
        FirebaseRecyclerAdapter<Topic, AddWordEditTopic.TopicViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Topic, AddWordEditTopic.TopicViewHolder>(
                Topic.class,
                R.layout.add_word_list_item_topic,
                AddWordEditTopic.TopicViewHolder.class,
                topicquery
        ) {
            @Override
            protected void populateViewHolder(AddWordEditTopic.TopicViewHolder viewHolder, final Topic model, final int position) {
                //firebase id of clicked field
                topicIdsTopics.add(getRef(position).getKey());
                //used to set values for Recycler View -> displaying data
                viewHolder.setName(model.getName());

                final CheckBox topicCheck = (CheckBox) viewHolder.mView.findViewById(R.id.checkBox_topic);
                topicCheck.setClickable(false);
                topicCheck.setChecked(false);

                if(selectedOld.contains(topicCheck.getText().toString())){
                    topicCheck.setChecked(true);
                }

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!topicCheck.isChecked()){
                            topicCheck.setChecked(true);
                            selected.add(topicCheck.getText().toString());
                        }
                        else{
                            topicCheck.setChecked(false);
                            selected.remove(topicCheck.getText().toString());
                            selectedOld.remove(topicCheck.getText().toString());
                            DatabaseReference removeRef = wordsRef.child(wordId).child("topics");
                            removeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();
                                    while (children.hasNext()){
                                        DataSnapshot childSel = children.next();
                                        String topic = childSel.getValue().toString();
                                        if(topic.equals(topicCheck.getText().toString())){
                                            //child is the one witch was unselected
                                            childSel.getRef().removeValue();
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                });

            }
        };

        topicList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
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

            CheckBox checkBox = (CheckBox) mView.findViewById(R.id.checkBox_topic);
            checkBox.setText(name);
        }
    }

    public void topicsChecked(){
            selectedOld.clear();
            selected.clear();

        //TODO: Prüfen ob topic überhaupt noch existiert
            DatabaseReference WordTopicsRef = wordsRef.child(wordId).child("topics").getRef();
            WordTopicsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();
                        while (children.hasNext()){
                            String topic = children.next().getValue().toString();
                                selected.add(topic);
                                selectedOld.add(topic);
                        }
                    }
                };

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
    }
}
