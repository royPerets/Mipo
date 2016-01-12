package com.example.mipo;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class MainActivity extends Activity {

	//for debug mode
	boolean alreadyLogIn = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		connectToParse ();
	}

	public void connectToParse() {
		Parse.enableLocalDatastore(this);
		Parse.initialize(this);
		ParseObject.registerSubclass(com.example.mipo.Message.class);
		ParseObject.registerSubclass(com.example.mipo.Room.class);
		ParseUser.enableAutomaticUser();
		ParseACL defaultAcl = new ParseACL();
		defaultAcl.setPublicReadAccess (true);
		ParseACL.setDefaultACL (defaultAcl, true);
		if(alreadyLogIn){
			login();
		}
		else if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())){
			Intent intent = new Intent(MainActivity.this,LoginActivity.class);
			startActivity(intent);
			finish();
		}
		else {
			ParseUser currentUser = ParseUser.getCurrentUser();
			if(currentUser != null && true == false) {
				Toast.makeText(getApplicationContext(), "Successfully Signed in", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(MainActivity.this,MainPageActivity.class);
				startActivity(intent);
				finish();
			}
			else {
				Intent intent = new Intent(MainActivity.this,LoginActivity.class);
				startActivity(intent);
				finish();
			}
		}
	}

	public void login()
	{
		String username = "1";
		String password = "1";
		ParseUser.logInInBackground(username, password, new LogInCallback () {
			@Override
			public void done(ParseUser user, ParseException e) {
				if (user != null) {
					Toast.makeText (getApplicationContext (), "Successfully Loged in", Toast.LENGTH_SHORT).show ();
					Intent intent = new Intent(MainActivity.this,MainPageActivity.class);
					startActivity (intent);
					finish();
				} else
					Toast.makeText (getApplicationContext (), "this user does not exist", Toast.LENGTH_SHORT).show ();
			}
		});
	}
}