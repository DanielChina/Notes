package clas.daniel.notes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import clas.daniel.notes.login.SignInActivity;


public class SplashActivity extends BaseActivity implements Handler.Callback{
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler=new Handler(this);
        mHandler.postDelayed(new Runnable(){
            @Override
            public void run() {
                Message msg=new Message();
                boolean state=getUserPreference().isUserLoggedIn();
                msg.arg1=0;
                msg.arg2=0;
                msg.obj=state;
                mHandler.sendMessage(msg);
            }
        },3000l);
    }
    @Override
    public boolean handleMessage(Message msg) {
        if((boolean)msg.obj){
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }else{
            startActivity(new Intent(SplashActivity.this, SignInActivity.class));
        }
        return false;
    }
}
