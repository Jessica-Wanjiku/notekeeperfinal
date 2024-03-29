package com.example.notekeeperfinal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import java.util.List;

public class NoteActivity<savedInstanceState, menuItem> extends AppCompatActivity {
    public static final String NOTE_POSITION = "com.example.notekeeperfinal.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeperfinal.NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.notekeeperfinal.NOTE_TITLE";
    private static final String ORIGINAL_NOTE_TEXT = "com.example.notekeeperfinal.NOTE_TEXT";
    public boolean mIsNewNote;
    public Spinner mSpinnerCourses;
    public EditText mTextNoteTitle;
    public EditText mTextNoteText;
    public boolean mIsCancelling;
    public String TAG;
    public int mNotePosition;
    public String mOriginalCourseId;
    public String mOriginalNoteText;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private NoteInfo mNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSpinnerCourses =findViewById(R.id.spinner_courses);

        List<CourseInfo> courses =DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();

        mTextNoteTitle =findViewById(R.id.text_note_title);
        mTextNoteText =findViewById(R.id.text_note_text);

        if (!mIsNewNote)
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
//        inflate the menu; this adda items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(mNote.getTitle());
        mTextNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNote = intent.getParcelableExtra(NOTE_POSITION);
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = position == POSITION_NOT_SET;

        if (mIsNewNote) {
            createNewNote();
        } else
            mNote = DataManager.getInstance().getNotes().get(position);
        Log.i(TAG, "mNotePosition is" + mNotePosition);
    }

        private void createNewNote (){
            DataManager dm = DataManager.getInstance();
            mNotePosition = dm.createNewNote();
            mNote = dm.getNotes().get(mNotePosition);
        }

    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;
        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }


    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalCourseId=savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle=savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText=savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void saveOriginalStateValue() {
        if(mIsNewNote)
            return;
        mOriginalCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            Log.i(TAG, "Cancelled information at" + mNotePosition);
            if(mIsNewNote) {
                DataManager.getInstance().removeNote(mNotePosition);
            }
            else {
                storePreviousNoteValues();
            }
        }
        else{
            saveNote();
        }
        Log.d(TAG, "onPause");
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);
    }
    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
//        noinspection SimplifiableStatement
            if (id == R.id.action_send_mail) {
                sendEmail();
                return true;
            } else if (id == R.id.action_cancel) {
                mIsCancelling = true;
                finish();
            }
            return super.onOptionsItemSelected(item);
        }

        private void sendEmail () {
            CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
            String subject = mTextNoteTitle.getText().toString();
            String text = "Check out what I learned in the PluralSight course\"" +
                    course.getTitle() + "\"\n" + mTextNoteText.getText().toString();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc2822");
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(intent);
        }
    }


