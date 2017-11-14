package cp.wordbox;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;


public class AllWordsFragment extends Fragment {

    private FirebaseAuth mAuth;

    private RecyclerView wordsList;
    private DatabaseReference wordsRef;

    private String wordId = "";
    DatabaseReference word_key;

    private View myMainView;

    String dialogInputYourLang = "";
    String dialogInputOtherLang = "";
    String field2Your = "";
    String field3Your = "";
    String field2Other = "";
    String field3Other = "";

    private ArrayList<String> selected;
    private ArrayList<String> selectedOld;


    public AllWordsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView =  inflater.inflate(R.layout.fragment_all_words, container, false);

        selected = new ArrayList<String>();
        selectedOld = new ArrayList<String>();

        Button addWordBtn = (Button) myMainView.findViewById(R.id.addWordBtn);
        addWordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create unique random key for word
                word_key = wordsRef.push();
                //get key
                wordId = word_key.getKey();
                showWordDialog(false, null, wordId); //edit, model (not edit -> no  model), id on witch word will be createda
            }
        });

        mAuth = FirebaseAuth.getInstance();

        String current_user_id = mAuth.getCurrentUser().getUid();
        wordsRef = FirebaseDatabase.getInstance().getReference().child("words").child(current_user_id);

        wordsList = (RecyclerView) myMainView.findViewById(R.id.allWords_list);
        wordsList.setHasFixedSize(true);
        wordsList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        return myMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        //get Data from Firebase with Firebase Recycler Apdapter

        //to order values after word in your language - not sorted if you just pass the reference to your adapter
        Query query = wordsRef.orderByChild("alphabet");

        //model class, ViewHolder Class
        FirebaseRecyclerAdapter<Word, TopicViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Word, TopicViewHolder>(
                Word.class,
                R.layout.words_item,
                TopicViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(TopicViewHolder viewHolder, final Word model, final int position) {
                //firebase id of clicked field
                final String id = getRef(position).getKey();
                //used to set values for Recycler View -> displaying data
                viewHolder.setYourLang(model.getYourLang());
                viewHolder.setOtherLang(model.getOtherLang());
                viewHolder.setDegree(model.getDegree());

                ImageButton editBtn = (ImageButton) viewHolder.mView.findViewById(R.id.editWordBtn);
                editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showWordDialog(true, model, id);//id
                    }
                });

                ImageButton deleteBtn = (ImageButton) viewHolder.mView.findViewById(R.id.deleteWordBtn);
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder removeDialogBuilder = new AlertDialog.Builder(getContext());
                        removeDialogBuilder
                                .setMessage("Do you want to remove this word?")
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
                                        wordsRef.child(id).removeValue();//id
                                        //delete words, which are in topic - (only if only in this topic, else: only delete topic)
                                    }
                                });
                        AlertDialog removeDialog = removeDialogBuilder.create();
                        removeDialog.setTitle("Remove '" + model.getYourLang() + "'");
                        removeDialog.show();

                    }
                });
            }
        };

        wordsList.setAdapter(firebaseRecyclerAdapter);
//       EXTREMELY IMPORTANT
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public TopicViewHolder(View itemView) {
            super(itemView);
            mView = itemView; //this will be used by Firebase Adapter
        }

        //same Methodnames as in allUsersClass
        public void setYourLang(String yourLang){
            //get Name from Database and set

            TextView yourL = (TextView) mView.findViewById(R.id.yourLangText);
            yourL.setText(yourLang);
        }

        public void setOtherLang(String otherLang){
            //get Name from Database and set

            TextView otherL = (TextView) mView.findViewById(R.id.otherLangText);
            otherL.setText(otherLang);
        }

        public void setDegree(String degree){
            //get Name from Database and set

            TextView degreeF = (TextView) mView.findViewById(R.id.word_degree_field);
            degreeF.setText(degree);
        }
    }

    private void showWordDialog(final boolean edit, Word model, final String id){
        final Dialog addDialog = new Dialog(getContext());
        addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addDialog.setContentView(R.layout.add_word_dialog);

        //Textfields
        final EditText yourLang2 = (EditText) addDialog.findViewById(R.id.add_word_dialog_your_lang2);
        final EditText yourLang3 = (EditText) addDialog.findViewById(R.id.add_word_dialog_your_lang3);
        final EditText otherLang2 = (EditText) addDialog.findViewById(R.id.add_word_dialog_other_lang2);
        final EditText otherLang3 = (EditText) addDialog.findViewById(R.id.add_word_dialog_other_lang3);
        final EditText yourLang = (EditText) addDialog.findViewById(R.id.add_word_dialog_your_lang);
        final EditText otherLang = (EditText) addDialog.findViewById(R.id.add_word_dialog_other_lang);

        //set Dialog width according to device size
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(addDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        addDialog.getWindow().setAttributes(lp);

        if(edit){
            TextView title = (TextView) addDialog.findViewById(R.id.add_word_dialog_title);
            title.setText("Edit word:");
            EditText[] fields = {yourLang, yourLang2, yourLang3, otherLang, otherLang2, otherLang3};
            String your1 = model.getYourLang();
            String your2 = model.getYourLang2();
            String your3 = model.getYourLang3();
            String other1 = model.getOtherLang();
            String other2 = model.getOtherLang2();
            String other3 = model.getOtherLang3();
            String[] meanings = {your1, your2, your3, other1, other2, other3};

            for(int i = 0; i < fields.length; i++){
                fields[i].setText(meanings[i]);
                if(!meanings[i].equals(""))
                    fields[i].setVisibility(View.VISIBLE);
            }
        }

        ImageButton editTopics = (ImageButton) addDialog.findViewById(R.id.word_edit_topics_button);
        editTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create unique random key for word
                wordId = id;
                Intent editTopicsIntent = new Intent(getContext(), AddWordEditTopic.class);
                editTopicsIntent.putExtra("wordId", wordId);
                startActivity(editTopicsIntent);
                //showTopicDialog();
            }
        });


        Button addWord = (Button) addDialog.findViewById(R.id.add_word_dialog_btn);
        if(edit)
            addWord.setText("save");

        addWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogInputYourLang = yourLang.getText().toString();
                dialogInputOtherLang = otherLang.getText().toString();

                if(dialogInputOtherLang.equals("")||dialogInputOtherLang.equals("")){
                    Toast.makeText(getContext(), "Please write at least one meaning.", Toast.LENGTH_SHORT).show();
                    return;
                }

                field2Your = yourLang2.getText().toString();
                field3Your = yourLang3.getText().toString();
                field2Other = otherLang2.getText().toString();
                field3Other = otherLang3.getText().toString();

                addDialog.cancel();

                wordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(edit){
                            wordId = id;
                        }
                        //single String values
                        wordsRef.child(wordId).child("learn").setValue("1");
                        wordsRef.child(wordId).child("degree").setValue("0");
                        wordsRef.child(wordId).child("otherLang").setValue(dialogInputOtherLang);
                        wordsRef.child(wordId).child("yourLang").setValue(dialogInputYourLang);
                        wordsRef.child(wordId).child("alphabet").setValue(dialogInputYourLang.substring(0,1).toLowerCase());
                        wordsRef.child(wordId).child("yourLang2").setValue(field2Your);
                        wordsRef.child(wordId).child("yourLang3").setValue(field3Your);
                        wordsRef.child(wordId).child("otherLang2").setValue(field2Other);
                        wordsRef.child(wordId).child("otherLang3").setValue(field3Other);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        ImageButton moreWordsYourLang = (ImageButton) addDialog.findViewById(R.id.addDialogMoreWordsYourLang);
        moreWordsYourLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addInputField(yourLang2, yourLang3);
            }
        });

        ImageButton moreWordsOtherLang = (ImageButton) addDialog.findViewById(R.id.addDialogMoreWordsOtherLang);
        moreWordsOtherLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addInputField(otherLang2,otherLang3);
            }
        });



        addDialog.show();
    }

//    private void showTopicDialog() {
//        final Dialog addTopicsDialog = new Dialog(getContext());
//        addTopicsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        addTopicsDialog.setContentView(R.layout.add_word_edit_topic);
//
//        //set Dialog width according to device size
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(addTopicsDialog.getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        addTopicsDialog.getWindow().setAttributes(lp);
//
//        Button addTopicBtn = (Button) addTopicsDialog.findViewById(R.id.addTopicBtn);
//        Button saveButton = (Button) addTopicsDialog.findViewById(R.id.add_word_edit_topic_save_btn);
//
//        addTopicBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //addTopicDialog("Add new Topic");
//                Log.i("test", "noch ist es nicht möglich hier Themen hinzuzufügen");
//            }
//        });
//
//        saveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //populate checked boxes to Firebase
//                for (final String topic : selected) {
//                    if (!selectedOld.contains(topic)) {
//                        //add topic to word->topics
//                        DatabaseReference topicKey = wordsRef.child(wordId).child("topics").push();
//                        String topicId = topicKey.getKey();
//                        wordsRef.child(wordId).child("topics").child(topicId).setValue(topic);
//
//                        //add word to topic->words
////                        for(final String id : topicIdsTopics){
////                            DatabaseReference nameRef = topicRef.child(id).child("name");
////                            nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
////                                @Override
////                                public void onDataChange(DataSnapshot dataSnapshot) {
////                                    String name = dataSnapshot.getValue().toString();
////                                    //Log.i("test", "test " + name);
////                                    if (topic.equals(name)){
////                                        //Log.i("test", "match " + name);
////                                        DatabaseReference wordKey = topicRef.child(id).child("words").push();
////                                        String topicWordId = wordKey.getKey();
////                                        //set value at reference to word
////                                        topicRef.child(id).child("words").child(topicWordId).setValue(wordId);
////                                    }
////                                }
////
////                                @Override
////                                public void onCancelled(DatabaseError databaseError) {
////
////                                }
////                            });
////                        }
//                    }
//                }
//            }
//        });
//
//
//
//    }

    private void addInputField(EditText field2, EditText field3) {
        if(field2.getVisibility() == View.GONE)
            field2.setVisibility(View.VISIBLE);
        else if(field3.getVisibility() == View.GONE)
            field3.setVisibility(View.VISIBLE);
        else
            Toast.makeText(getContext(), "You can only add three meanings.", Toast.LENGTH_SHORT).show();
    }
}
