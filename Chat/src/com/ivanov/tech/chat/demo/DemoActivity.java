package com.ivanov.tech.chat.demo;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.ivanov.tech.chat.Chat;
import com.ivanov.tech.chat.R;
import com.ivanov.tech.chat.reciever.MessageReciever;
import com.ivanov.tech.chat.ui.FragmentSplashScreen;
import com.ivanov.tech.connection.Connection;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.ui.MainActivity;
import com.ivanov.tech.session.Session;

/**
 * Created by Igor on 15.01.15.
 */
public class DemoActivity extends SherlockFragmentActivity {

	private static final String TAG = DemoActivity.class
            .getSimpleName();  
	
	private boolean ApiKeyActual=false;
	private boolean TimerFinished=false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Session.Initialize(getApplicationContext());
        
        setContentView(R.layout.activity_main);
        
        getSupportActionBar().hide();
        
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setIcon( new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        
        showSplashScreen();
        
                
      //Api-Key checking protocol (through connection protocol) 
        Session.checkApiKey(this, getSupportFragmentManager(), R.id.main_container, new Connection.ProtocolListener(){
        	@Override
			public void isCompleted() {
        		Log.d(TAG, "onCreate checkApiKey isCompleted"); 
        		ApiKeyActual=true;
        		
        		Chat.startChatService(DemoActivity.this);
        		
        		tryToShowRecentList();
			}
        	
        	@Override
			public void onCanceled() {
				finish();
				Log.d(TAG, "onCreate checkApiKey onCanceled");
			}

        });
        
        //Splash-screen timer
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {}
            
            public void onFinish() {
            	TimerFinished=true;
            	tryToShowRecentList();
            }
            
         }.start();
        
    }
        
    @Override
    protected void onNewIntent(Intent intent)   {
        super.onNewIntent(intent);
        
        Log.d(TAG, "onNewIntent intent.hasType="+intent.hasExtra(MessageReciever.EXTRA_TYPE));
        doIncomingMessageIntent(intent);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	   
		int id = item.getItemId();
	    
		if(id==android.R.id.home){
			getSupportFragmentManager().popBackStack();
			return true;
		}
		
		return false;
	}
    
    //If it has incoming message intent then show appropriate conversation activity
    private boolean doIncomingMessageIntent(Intent intent){
    	if(intent.hasExtra(MessageReciever.EXTRA_TYPE)){
    	
    		
        	if(intent.getIntExtra(MessageReciever.EXTRA_TYPE, 0)==MessageReciever.TYPE_PRIVATE){
        		Chat.showConversationPrivate(intent.getIntExtra(MessageReciever.EXTRA_USERID, 0), this, getSupportFragmentManager(), R.id.main_container);
        		return true;
        	}else{
        		Chat.showConversationGroup(intent.getIntExtra(MessageReciever.EXTRA_GROUPID, 0), this, getSupportFragmentManager(), R.id.main_container);
        		return true;
        	}
        }
    	
    	return false;
    }
        
    private void tryToShowRecentList(){
    	if(ApiKeyActual&&TimerFinished){    
    		
    		//Do incoming message process, otherwise show Recent Activity
    		
    		if( ! doIncomingMessageIntent(getIntent()) ){
    			Chat.showRecentList(DemoActivity.this, getSupportFragmentManager(), R.id.main_container,false);
    		}
    	}
    }
    
    private void showSplashScreen(){
    	FragmentSplashScreen fragment=FragmentSplashScreen.newInstance();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment, "SplashScreen");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }
}