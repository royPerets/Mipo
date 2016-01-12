package com.example.mipo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity {

    private EditText etMessage;
    private ListView lvChat;
    private ArrayList<Message> mMessages;
    private ArrayList<Room> mRoom;
    private ChatListAdapter mAdapter;
    private boolean mFirstLoad;
    private Handler handler = new Handler ();
    static String otherUserId;
    String des;
    String des2;
    boolean isSaved = false;
    static String other_user_name;
    String body;
    int combinedConversationId;
    int conversationId;
    int otherConversationId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        this.requestWindowFeature (Window.FEATURE_NO_TITLE);
        setContentView (R.layout.activity_chat);
        Intent intent = getIntent ();

        otherUserId = intent.getStringExtra ("userId");
        other_user_name = intent.getStringExtra ("userName");

        for (int i = 0; i < MainPageActivity.ud.size (); i++) {
            if (other_user_name.equals (MainPageActivity.ud.get (i).getName ())) {
                otherConversationId = MainPageActivity.ud.get (i).getMessage_roomId ();
            }
        }
        conversationId = MainPageActivity.currentUser.getMessage_roomId ();
        combinedConversationId = (otherConversationId * conversationId);

        etMessage = (EditText) findViewById (R.id.etMessage);
        lvChat = (ListView) findViewById (R.id.lvChat);

        mMessages = new ArrayList<Message> ();
        mRoom = new ArrayList<Room> ();
        // Automatically scroll to the bottom when a data set change notification is received and only if the last item is already visible on screen. Don't scroll to the bottom otherwise.
        lvChat.setTranscriptMode (1);
        des = MainPageActivity.currentUser.getId () + " - " + otherUserId;
        des2 = otherUserId + " - " + MainPageActivity.currentUser.getId ();

        mFirstLoad = true;
        mAdapter = new ChatListAdapter (ChatActivity.this, MainPageActivity.currentUser.getId (), mMessages);
        lvChat.setAdapter (mAdapter);
        handler.postDelayed (runnable, 0);
    }

    public void sendMessage(View view) {
        body = etMessage.getText ().toString ();
        Message message = new Message ();
        message.setUserId (MainPageActivity.currentUser.getId ());
        message.setBody (body);
        message.setDes (des);
        message.setCombinedID (Integer.toString (combinedConversationId));
        try {
            message.save ();
        } catch (ParseException e) {
            Toast.makeText (getApplicationContext (), "error save your msg in parse", Toast.LENGTH_SHORT).show ();
            e.printStackTrace ();
        }
        etMessage.setText ("");
        receiveNoBackGround();
        deleteMessageRoomItem ();
    }


    private void receiveMessage() {
        ParseQuery<Message> query = ParseQuery.getQuery (Message.class);
        query.whereEqualTo ("combinedID", Integer.toString (combinedConversationId));
        query.orderByAscending ("createdAt");
        query.findInBackground (new FindCallback<Message> () {
            public void done(List<Message> messages, ParseException e) {
                if (e == null) {
                    if (messages.size () > mMessages.size ()) {
                        mMessages.clear ();
                        mMessages.addAll (messages);
                        mAdapter.notifyDataSetChanged (); // update adapter
                        // Scroll to the bottom of the list on initial load
                        if (mFirstLoad) {
                            lvChat.setSelection (mAdapter.getCount () - 1);
                            mFirstLoad = false;
                        }
                    }
                } else {
                    e.printStackTrace ();
                }
            }
        });
    }

    private void receiveNoBackGround() {
        ParseQuery<Message> query = ParseQuery.getQuery (Message.class);
        query.whereEqualTo ("combinedID", Integer.toString (combinedConversationId));
        query.orderByAscending ("createdAt");
        List<Message> messages = null;
        try {
            messages = query.find ();
            if (messages.size () > mMessages.size ()) {
                mMessages.clear ();
                mMessages.addAll (messages);
                mAdapter.notifyDataSetChanged (); // update adapter
                // Scroll to the bottom of the list on initial load
                if (mFirstLoad) {
                    lvChat.setSelection (mAdapter.getCount () - 1);
                    mFirstLoad = false;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace ();
        }
    }

    private Runnable runnable = new Runnable () {
        @Override
        public void run() {
            refreshMessages ();
            handler.postDelayed (this, 300);
        }
    };

    private void refreshMessages() {
        receiveMessage ();
    }

    private void saveToMessagesRoom() {
        Room room = new Room ();
        room.setUserId (MainPageActivity.currentUser.getId ());
        room.setConversationId (combinedConversationId);
        room.setDes (body);
        room.saveInBackground (new SaveCallback () {
            @Override
            public void done(ParseException e) {
                isSaved = true;
            }
        });
    }

    public void deleteMessageRoomItem() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery ("Room");
        query.whereEqualTo ("ConversationId", combinedConversationId);
        query.getFirstInBackground (new GetCallback<ParseObject> () {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    try {
                        object.delete ();
                    } catch (ParseException e1) {
                        e1.printStackTrace ();
                    }
                    object.saveInBackground ();
                }
                saveToMessagesRoom ();
            }
        });
    }
}