package cp.wordbox;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Chantal on 20.11.2017.
 */

public class Firebase_Interactor {

    private DatabaseReference wordsRef;
    private DatabaseReference topicRef;
    private FirebaseAuth mAuth;
    ArrayList<String> topicsUpdate;

    public Firebase_Interactor(){
        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();
        wordsRef = FirebaseDatabase.getInstance().getReference().child("words").child(current_user_id);
        topicRef = FirebaseDatabase.getInstance().getReference().child("topics").child(current_user_id);
    }

    public void add_word(final HashMap word){
        //to wordRef
        final String wordId = word.get("id").toString();
        wordsRef.child(wordId).child("learn").setValue("1");
        wordsRef.child(wordId).child("degree").setValue("0");
        wordsRef.child(wordId).child("otherLang").setValue(word.get("otherLang"));
        wordsRef.child(wordId).child("yourLang").setValue(word.get("yourLang"));
        wordsRef.child(wordId).child("sortVersion").setValue(word.get("sortVersion"));
        wordsRef.child(wordId).child("yourLang2").setValue(word.get("yourLang2"));
        wordsRef.child(wordId).child("yourLang3").setValue(word.get("yourLang3"));
        wordsRef.child(wordId).child("otherLang2").setValue(word.get("otherLang2"));
        wordsRef.child(wordId).child("otherLang3").setValue(word.get("otherLang3"));

        for(String topic: (ArrayList<String>) word.get("topicsNew")){
            DatabaseReference topicKey = wordsRef.child(wordId).child("topics").push();
            String topicId = topicKey.getKey();
            wordsRef.child(wordId).child("topics").child(topicId).setValue(topic);
        }

        //to topicRef
        final ArrayList<String> allTopics = (ArrayList<String>) word.get("topicsAll");
        final ArrayList<String> newTopics = (ArrayList<String>) word.get("topicsNew");
        Log.i("test", "interact all " + allTopics.toString());
        Log.i("test", "interact new " + newTopics.toString());

        for (final String topic: allTopics){
            topicRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //all topics in Database
                    Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();
                    while (children.hasNext()){
                        //topic in topiclist of word
                        DataSnapshot topicchild = children.next();

                        //remove topic in topiclist in word in topic
                        DatabaseReference remRev = topicchild.child("words").child(wordId).child("topics").getRef();
                        remRev.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();
                                while (children.hasNext()) {
                                    DataSnapshot childSel = children.next();
                                    String topic = childSel.getValue().toString();
                                    if(!allTopics.contains(topic)){//topic was removed
                                        childSel.getRef().removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });






                        //word doesn't already exists in topic -> needs to be made new instead of updated ->all topics
                        if (newTopics.contains(topicchild.child("name").getValue().toString()))
                            topicsUpdate = allTopics;
                        else
                            topicsUpdate = newTopics;

                        if(topicchild.child("name").getValue().toString().equals(topic)){
                            topicchild.child("words").child(wordId).child("learn").getRef().setValue("1");
                            topicchild.child("words").child(wordId).child("degree").getRef().setValue("0");
                            topicchild.child("words").child(wordId).child("otherLang").getRef().setValue(word.get("otherLang"));
                            topicchild.child("words").child(wordId).child("yourLang").getRef().setValue(word.get("yourLang"));
                            topicchild.child("words").child(wordId).child("sortVersion").getRef().setValue(word.get("sortVersion"));
                            topicchild.child("words").child(wordId).child("yourLang2").getRef().setValue(word.get("yourLang2"));
                            topicchild.child("words").child(wordId).child("yourLang3").getRef().setValue(word.get("yourLang3"));
                            topicchild.child("words").child(wordId).child("otherLang2").getRef().setValue(word.get("otherLang2"));
                            topicchild.child("words").child(wordId).child("otherLang3").getRef().setValue(word.get("otherLang3"));

                            for(String topic: topicsUpdate){
                                DatabaseReference topicKey = topicchild.child("words").child(wordId).child("topics").getRef().push();
                                String topicId = topicKey.getKey();
                                topicchild.child("words").child(wordId).child("topics").child(topicId).getRef().setValue(topic);
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void remove_word(HashMap word){

    }
}