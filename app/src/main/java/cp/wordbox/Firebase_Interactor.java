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
                                    if(!allTopics.contains(topic)){//topic was unselected
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

    public void remove_word(final String wordId){
        //remove word from wordlist
        wordsRef.child(wordId).removeValue();

        //remove word from wordlists in topics
        topicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> alltopics = dataSnapshot.getChildren().iterator();
                while (alltopics.hasNext()) {
                    DataSnapshot topicSel = alltopics.next();
                    DatabaseReference wordsintopicsRef = topicSel.child("words").getRef();
                    wordsintopicsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterator<DataSnapshot> wordsintopics = dataSnapshot.getChildren().iterator();
                            while (wordsintopics.hasNext()) {
                                DataSnapshot wordintopicSel = wordsintopics.next();
                                if(wordintopicSel.getKey().toString().equals(wordId)){
                                    wordintopicSel.getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void remove_topic(final String topicId, final String topicName, boolean removeAll){

        //which words are in topic
        final ArrayList<String> wordIds = new ArrayList<String>();
        final DatabaseReference wordInTopicRef = topicRef.child(topicId).child("words");
        wordInTopicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();
                while (children.hasNext()) {
                    DataSnapshot childSel = children.next();
                    wordIds.add(childSel.getKey().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        Log.i("test", wordIds.toString());

        if(removeAll){//remove included words

            //remove included words from wordList
            wordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> allWords = dataSnapshot.getChildren().iterator();
                    while (allWords.hasNext()) {
                        DataSnapshot wordSel = allWords.next();
                        if(wordIds.contains(wordSel.getKey().toString())){
                            wordSel.getRef().removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            //remove included words in all (other) topics
            topicRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> allTopics = dataSnapshot.getChildren().iterator();
                    while (allTopics.hasNext()) {
                        DataSnapshot topicSel = allTopics.next();
                        DatabaseReference wordsInTopic = topicSel.child("words").getRef();
                        wordsInTopic.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterator<DataSnapshot> allWordsInTopic = dataSnapshot.getChildren().iterator();
                                while (allWordsInTopic.hasNext()) {
                                    DataSnapshot wordSel = allWordsInTopic.next();
                                    if (wordIds.contains(wordSel.getKey().toString()))
                                        wordSel.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        //remove deleted topic from topiclist
        topicRef.child(topicId).removeValue();

        //remove topicRef from all topiclists in words
        wordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> allWords = dataSnapshot.getChildren().iterator();
                while (allWords.hasNext()) {
                    DataSnapshot wordSel = allWords.next();
                    if(wordIds.contains(wordSel.getKey().toString())){
                        wordSel.child("topics").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterator<DataSnapshot> allTopicsInWord = dataSnapshot.getChildren().iterator();
                                while (allTopicsInWord.hasNext()) {
                                    DataSnapshot topicSel = allTopicsInWord.next();
                                    String name = topicSel.getValue().toString();
                                    if(name.equals(topicName)){
                                        topicSel.getRef().removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //remove topicRef from all words in other topics
        topicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> allTopics = dataSnapshot.getChildren().iterator();
                while (allTopics.hasNext()) {
                    DataSnapshot topicSel = allTopics.next();
                    //all words in topicSel
                    DatabaseReference wordsInTopicRef = topicSel.child("words").getRef();
                    wordsInTopicRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterator<DataSnapshot> allWordsInTopic = dataSnapshot.getChildren().iterator();
                            while (allWordsInTopic.hasNext()) {
                                DataSnapshot wordInTopicSel = allWordsInTopic.next();
                                if(wordIds.contains(wordInTopicSel.getKey().toString())){
                                    wordInTopicSel.child("topics").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Iterator<DataSnapshot> allTopicsInWord = dataSnapshot.getChildren().iterator();
                                            while (allTopicsInWord.hasNext()) {
                                                DataSnapshot topicSel = allTopicsInWord.next();
                                                String name = topicSel.getValue().toString();
                                                if(name.equals(topicName)){
                                                    topicSel.getRef().removeValue();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}