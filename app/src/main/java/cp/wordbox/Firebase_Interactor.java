package cp.wordbox;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Chantal on 20.11.2017.
 */

public class Firebase_Interactor {

    private DatabaseReference wordsRef;
    private DatabaseReference topicRef;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    //oder hier rausfinden welche topics?

    public Firebase_Interactor(){
        rootRef = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();
        wordsRef = FirebaseDatabase.getInstance().getReference().child("words").child(current_user_id);
        topicRef = FirebaseDatabase.getInstance().getReference().child("topics").child(current_user_id);
    }

    public void add_word(HashMap word){
        //to wordRef
        String wordId = word.get("id").toString();
        wordsRef.child(wordId).child("learn").setValue("1");
        wordsRef.child(wordId).child("degree").setValue("0");
        wordsRef.child(wordId).child("otherLang").setValue(word.get("otherLang"));
        wordsRef.child(wordId).child("yourLang").setValue(word.get("yourLang"));
        wordsRef.child(wordId).child("sortVersion").setValue(word.get("sortVersion"));
        wordsRef.child(wordId).child("yourLang2").setValue(word.get("yourLang2"));
        wordsRef.child(wordId).child("yourLang3").setValue(word.get("yourLang3"));
        wordsRef.child(wordId).child("otherLang2").setValue(word.get("otherLang2"));
        wordsRef.child(wordId).child("otherLang3").setValue(word.get("otherLang3"));
        for(String topic: (ArrayList<String>) word.get("topics")){
            DatabaseReference topicKey = wordsRef.child(wordId).child("topics").push();
            String topicId = topicKey.getKey();
            wordsRef.child(wordId).child("topics").child(topicId).setValue(topic);
        }

        //to topicRef
        for (String topic: (ArrayList<String>) word.get("topics")){
            topicRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //all topics in Database
                    Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();
                    while (children.hasNext()){
                        
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

//    public void add_topic(){
//
//    }
//
//    public void remove_topic(){
//
//    }
}
