package com.example.android.phoneverification;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends AppCompatActivity {

	 private FirebaseAuth mAuth;

	 @Override
	 public void onStart() {
		  super.onStart();
		  // Check if user is signed in (non-null) and update UI accordingly.
		  FirebaseUser currentUser = mAuth.getCurrentUser();
		  if (currentUser != null) {
			   startActivity(new Intent(MainActivity.this, ProfileActivity.class));
			   finish();
		  }
	 }

	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  StatusBar();
		  setContentView(R.layout.activity_main);
		  mAuth = FirebaseAuth.getInstance();

		  Button click =   findViewById(R.id.click);
		  click.setOnClickListener(new View.OnClickListener() {
			   @Override
			   public void onClick(View view) {
					startActivity(new Intent(MainActivity.this,Authentication.class));
			   }
		  });
	 }



	 private void StatusBar() {
		  Window window = getWindow();

		  if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ) {
			   window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			   SystemBarTintManager tintManager = new SystemBarTintManager(this);
			   tintManager.setStatusBarTintEnabled(true);
			   tintManager.setTintColor(ContextCompat.getColor(this , R.color.colorPrimaryDark));
		  }
		  if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
			   window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			   window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
		  }

	 }
}
