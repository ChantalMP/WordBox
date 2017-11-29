package cp.wordbox;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cp.wordbox.recyclerView_models.Word;


public class AllWordsFragment extends Fragment {

    private static final String TAG = "AllWordsFragment";

    private FirebaseAuth mAuth;

    private RecyclerView wordsList;
    private DatabaseReference wordsRef;
    private DatabaseReference topicRef;

    private String wordId = "";
    DatabaseReference word_key;

    private View myMainView;

    String dialogInputYourLang = "";
    String dialogInputOtherLang = "";
    String field2Your = "";
    String field3Your = "";
    String field2Other = "";
    String field3Other = "";

    String topicId = "";
    String topicName = "";

    private ArrayList<String> topicsSelectedAll;
    private ArrayList<String> topicsSelectedNew;


    Firebase_Interactor firebase_interactor;

    boolean topicsChanged = false;


    public AllWordsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
            topicId = bundle.getString("topic");
            topicName = bundle.getString("topicName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView =  inflater.inflate(R.layout.fragment_all_words, container, false);

        topicsSelectedAll = new ArrayList<String>();
        topicsSelectedNew = new ArrayList<String>();

        firebase_interactor = new Firebase_Interactor();

        Button addWordBtn = (Button) myMainView.findViewById(R.id.addWordBtn);
        addWordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create unique random key for word
                word_key = wordsRef.push();
                //get key
                wordId = word_key.getKey();
                showWordDialog(false, null, wordId); //edit, model (not edit -> no  model), id on witch word will be created
            }
        });

        mAuth = FirebaseAuth.getInstance();

        String current_user_id = mAuth.getCurrentUser().getUid();
        wordsRef = FirebaseDatabase.getInstance().getReference().child("words").child(current_user_id);
        topicRef = FirebaseDatabase.getInstance().getReference().child("topics").child(current_user_id);

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
        Query query = wordsRef.orderByChild("sortVersion");
        if(!topicId.equals("")){
            //show topicName
            TextView topicname = (TextView) myMainView.findViewById(R.id.topic_name_all_words);
            topicname.setText(topicName);
            topicname.setVisibility(View.VISIBLE);

            //called from topic activity -> query with only words in topic
            query = topicRef.child(topicId).child("words").orderByChild("sortVersion");
        }

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
                                        firebase_interactor.remove_word(id);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            topicsChanged = true;
            if (data.hasExtra("allTopics")) {
                topicsSelectedAll = data.getStringArrayListExtra("allTopics");
            }
            if (data.hasExtra("newTopics")) {
                topicsSelectedNew = data.getStringArrayListExtra("newTopics");
            }
        }
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

        final ImageButton editTopics = (ImageButton) addDialog.findViewById(R.id.word_edit_topics_button);
        editTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create unique random key for word
                wordId = id;
                Intent editTopicsIntent = new Intent(getContext(), AddWordEditTopic.class);
                editTopicsIntent.putExtra("wordId", wordId);
                editTopicsIntent.putExtra("topic", topicId);
                startActivityForResult(editTopicsIntent, 1);//1 ist requestCode
            }
        });

        //update topiclists/ generate topic list if not edited
        if (!topicsChanged){
            wordId = id;
            DatabaseReference WordTopicsRef = wordsRef.child(wordId).child("topics").getRef();
            WordTopicsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();
                        while (children.hasNext()){
                            String topic = children.next().getValue().toString();
                            if(!topicsSelectedAll.contains(topic))
                                topicsSelectedAll.add(topic);
                        }
                    }

                    if(!topicName.equals("") && !topicsSelectedAll.contains(topicName)){//we come from topicactivity and its no editing
                        topicsSelectedAll.add(topicName);
                        topicsSelectedNew.add(topicName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }



        Button addWord = (Button) addDialog.findViewById(R.id.add_word_dialog_btn);//or edit
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


                if(edit){
                    wordId = id;
                }

                HashMap wordInfos = new HashMap();
                wordInfos.put("id", wordId);
                wordInfos.put("learn", "1");
                wordInfos.put("degree", "0");
                wordInfos.put("otherLang", dialogInputOtherLang);
                wordInfos.put("yourLang", dialogInputYourLang);
                wordInfos.put("sortVersion", dialogInputYourLang.toLowerCase());
                wordInfos.put("yourLang2", field2Your);
                wordInfos.put("yourLang3", field3Your);
                wordInfos.put("otherLang2", field2Other);
                wordInfos.put("otherLang3", field3Other);
                wordInfos.put("topicsAll", topicsSelectedAll);
                wordInfos.put("topicsNew", topicsSelectedNew);
                //make empty for next word
                firebase_interactor.add_word(wordInfos);

                topicsSelectedAll = new ArrayList<String>();
                topicsSelectedNew = new ArrayList<String>();
                topicsChanged = false;

                addDialog.cancel();
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


    private void addInputField(EditText field2, EditText field3) {
        if(field2.getVisibility() == View.GONE)
            field2.setVisibility(View.VISIBLE);
        else if(field3.getVisibility() == View.GONE)
            field3.setVisibility(View.VISIBLE);
        else
            Toast.makeText(getContext(), "You can only add three meanings.", Toast.LENGTH_SHORT).show();
    }
}