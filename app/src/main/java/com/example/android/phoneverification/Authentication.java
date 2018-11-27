package com.example.android.phoneverification;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.shuhart.stepview.StepView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


public class Authentication extends AppCompatActivity {

	 private int currentStep = 0;
	 LinearLayout layout1, layout2;
	 ConstraintLayout layout3;
	 StepView stepView;
	 AlertDialog dialog_verifying, profile_dialog;
	 String sendCode;

	 private static final String TAG = "FirebasePhoneNumAuth";

	 private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
	 private FirebaseAuth firebaseAuth;

	 private Button sendCodeButton;
	 private Button verifyCodeButton;
	 private Button button3;

	 private EditText phoneNum;
	 private PinView verifyCodeET;
	 private TextView phonenumberText, reloadSend;

	 private ProgressBar progressBar;
	 private String mVerificationId;
	 private PhoneAuthProvider.ForceResendingToken mResendToken;

	 CountryCodePicker ccp;
	 private FirebaseAuth mAuth;

	 @SuppressLint("SetTextI18n")
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  StatusBar();
		  setContentView(R.layout.activity_authentication);

		  mAuth = FirebaseAuth.getInstance();
		  mAuth.setLanguageCode("ar");
		  layout1 = findViewById(R.id.layout1);
		  layout2 = findViewById(R.id.layout2);
		  layout3 = findViewById(R.id.layout3);
		  sendCodeButton = findViewById(R.id.submit1);
		  verifyCodeButton = findViewById(R.id.submit2);
		  button3 = findViewById(R.id.submit3);
		  firebaseAuth = FirebaseAuth.getInstance();
		  phoneNum = findViewById(R.id.phonenumber);
		  verifyCodeET = findViewById(R.id.pinView);
		  phonenumberText = findViewById(R.id.phonenumberText);
		  reloadSend = findViewById(R.id.reloadSend);

		  ccp = findViewById(R.id.ccp);
		  stepView = findViewById(R.id.step_view);
		  stepView.setStepsNumber(3);
		  stepView.go(0 , true);
		  progressBar = findViewById(R.id.progressBar);
		  layout1.setVisibility(View.VISIBLE);

		  sendCodeButton.setOnClickListener(new View.OnClickListener() {
			   @Override
			   public void onClick(View view) {

					sendCode();

			   }
		  });

		  reloadSend.setOnClickListener(new View.OnClickListener() {
			   @Override
			   public void onClick(View v) {

					progressBar.setVisibility(View.VISIBLE);
					sendAthCode();

			   }
		  });

		  mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
			   @Override
			   public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
					LayoutInflater inflater = getLayoutInflater();
					View alertLayout = inflater.inflate(R.layout.processing_dialog , null);
					AlertDialog.Builder show = new AlertDialog.Builder(Authentication.this);

					show.setView(alertLayout);
					show.setCancelable(false);
					dialog_verifying = show.create();
					dialog_verifying.show();
					signInWithPhoneAuthCredential(phoneAuthCredential);
					progressBar.setVisibility(View.INVISIBLE);
			   }

			   @Override
			   public void onVerificationFailed(FirebaseException e) {
					String err = e.getMessage();

					if ( err.contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.") ) {

						 showNetworkMethod();

					} else {

						 progressBar.setVisibility(View.INVISIBLE);
						 Toast.makeText(Authentication.this , "Error  \n" + err , Toast.LENGTH_SHORT).show();
					}
			   }

			   @Override
			   public void onCodeSent(String verificationId , PhoneAuthProvider.ForceResendingToken token) {

					// Save verification ID and resending token so we can use them later
					mVerificationId = verificationId;
					mResendToken = token;

					// ...
			   }
		  };


		  verifyCodeButton.setOnClickListener(new View.OnClickListener() {
			   @Override
			   public void onClick(View view) {


					String verificationCode = verifyCodeET.getText().toString();
					if ( verificationCode.isEmpty() ) {
						 Toast.makeText(Authentication.this , "Enter verification code" , Toast.LENGTH_SHORT).show();

					} else if ( verifyCodeET.length() < 6 ) {
						 Toast.makeText(Authentication.this , "please complete verification code" , Toast.LENGTH_SHORT).show();

					} else {

						 LayoutInflater inflater = getLayoutInflater();
						 View alertLayout = inflater.inflate(R.layout.processing_dialog , null);
						 AlertDialog.Builder show = new AlertDialog.Builder(Authentication.this);

						 show.setView(alertLayout);
						 show.setCancelable(false);
						 dialog_verifying = show.create();
						 dialog_verifying.show();

						 PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId , verificationCode);
						 signInWithPhoneAuthCredential(credential);


					}
			   }
		  });

		  button3.setOnClickListener(new View.OnClickListener() {
			   @Override
			   public void onClick(View view) {

					if ( currentStep < stepView.getStepCount() - 1 ) {
						 currentStep++;
						 stepView.go(currentStep , true);
					} else {
						 stepView.done(true);
					}
					LayoutInflater inflater = getLayoutInflater();
					View alertLayout = inflater.inflate(R.layout.profile_create_dialog , null);
					AlertDialog.Builder show = new AlertDialog.Builder(Authentication.this);
					show.setView(alertLayout);
					show.setCancelable(false);
					profile_dialog = show.create();
					profile_dialog.show();
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						 @Override
						 public void run() {
							  profile_dialog.dismiss();
							  startActivity(new Intent(Authentication.this , ProfileActivity.class));
							  finish();
						 }
					} , 4000);
			   }
		  });

	 }

	 private void sendCode() {
		  ccp.registerCarrierNumberEditText(phoneNum);

		  ccp.setDefaultCountryUsingNameCode("+20");
		  String SelectedCountry = ccp.getSelectedCountryCodeWithPlus();

		  String phoneNumber = phoneNum.getText().toString();

		  SelectedCountry += phoneNumber;

		  sendCode = SelectedCountry;

		  phonenumberText.setText(sendCode);

		  if ( TextUtils.isEmpty(sendCode) ) {
			   phoneNum.setError("Enter a Phone Number");
			   phoneNum.requestFocus();
		  } else if ( sendCode.length() < 10 ) {
			   phoneNum.setError("Please enter a valid phone");
			   phoneNum.requestFocus();
		  } else {

			   progressBar.setVisibility(View.VISIBLE);

			   if ( currentStep < stepView.getStepCount() - 1 ) {
					currentStep++;
					stepView.go(currentStep , true);
			   } else {
					stepView.done(true);
			   }
			   sendAthCode();
		  }
	 }

	 private void sendAthCode() {
		  layout1.setVisibility(View.GONE);
		  layout2.setVisibility(View.VISIBLE);
		  PhoneAuthProvider.getInstance().verifyPhoneNumber(
				  sendCode ,        // Phone number to verify
				  60 ,                 // Timeout duration
				  TimeUnit.SECONDS ,   // Unit of timeout
				  Authentication.this ,               // Activity (for callback binding)
				  mCallbacks);        // OnVerificationStateChangedCallbacks

	 }

	 private void showNetworkMethod() {

		  progressBar.setVisibility(View.INVISIBLE);

		  final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

		  LayoutInflater layoutInflater = LayoutInflater.from(this);
		  View view = layoutInflater.inflate(R.layout.network_error , null);
		  bottomSheetDialog.setContentView(view);

		  Button wifi = view.findViewById(R.id.wifi);
		  Button data = view.findViewById(R.id.data);
		  ImageView dismiss = view.findViewById(R.id.dismiss);

		  wifi.setOnClickListener(new View.OnClickListener() {
			   @Override
			   public void onClick(View v) {
					WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
					wifi.setWifiEnabled(true);
					bottomSheetDialog.dismiss();

			   }
		  });

		  data.setOnClickListener(new View.OnClickListener() {
			   @Override
			   public void onClick(View v) {
					DataOnOff(true , Authentication.this);
					bottomSheetDialog.dismiss();

			   }
		  });

		  dismiss.setOnClickListener(new View.OnClickListener() {
			   @Override
			   public void onClick(View v) {
					bottomSheetDialog.dismiss();
			   }
		  });


		  bottomSheetDialog.show();


	 }

	 private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
		  mAuth.signInWithCredential(credential)
				  .addOnCompleteListener(this , new OnCompleteListener<AuthResult>() {
					   @Override
					   public void onComplete(@NonNull Task<AuthResult> task) {
							if ( task.isSuccessful() ) {
								 // Sign in success, update UI with the signed-in user's information
								 Log.d(TAG , "signInWithCredential:success");
								 dialog_verifying.dismiss();
								 if ( currentStep < stepView.getStepCount() - 1 ) {
									  currentStep++;
									  stepView.go(currentStep , true);
								 } else {
									  stepView.done(true);
								 }
								 layout1.setVisibility(View.GONE);
								 layout2.setVisibility(View.GONE);
								 layout3.setVisibility(View.VISIBLE);
								 // ...
							} else {

								 progressBar.setVisibility(View.INVISIBLE);
								 dialog_verifying.dismiss();
								 Toast.makeText(Authentication.this , "Something wrong" , Toast.LENGTH_SHORT).show();
								 Log.w(TAG , "signInWithCredential:failure" , task.getException());
								 if ( task.getException() instanceof FirebaseAuthInvalidCredentialsException ) {

								 }
							}
					   }
				  });
	 }

	 private void setMobileDataEnabled(Context context , boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		  final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		  final Class conmanClass = Class.forName(conman.getClass().getName());
		  final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
		  connectivityManagerField.setAccessible(true);
		  final Object connectivityManager = connectivityManagerField.get(conman);
		  final Class connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
		  final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled" , Boolean.TYPE);
		  setMobileDataEnabledMethod.setAccessible(true);

		  setMobileDataEnabledMethod.invoke(connectivityManager , enabled);
	 }

	 public boolean toggleMobileDataConnection(boolean ON) {
		  try {
			   // create instance of connectivity manager and get system service

			   final ConnectivityManager conman = (ConnectivityManager) this
					   .getSystemService(Context.CONNECTIVITY_SERVICE);
			   // define instance of class and get name of connectivity manager
			   // system service class
			   final Class conmanClass = Class
					   .forName(conman.getClass().getName());
			   // create instance of field and get mService Declared field
			   final Field iConnectivityManagerField = conmanClass
					   .getDeclaredField("mService");
			   // Attempt to set the value of the accessible flag to true
			   iConnectivityManagerField.setAccessible(true);
			   // create instance of object and get the value of field conman
			   final Object iConnectivityManager = iConnectivityManagerField
					   .get(conman);
			   // create instance of class and get the name of iConnectivityManager
			   // field
			   final Class iConnectivityManagerClass = Class
					   .forName(iConnectivityManager.getClass().getName());
			   // create instance of method and get declared method and type
			   final Method setMobileDataEnabledMethod = iConnectivityManagerClass
					   .getDeclaredMethod("setMobileDataEnabled" , Boolean.TYPE);
			   // Attempt to set the value of the accessible flag to true
			   setMobileDataEnabledMethod.setAccessible(true);
			   // dynamically invoke the iConnectivityManager object according to
			   // your need (true/false)
			   setMobileDataEnabledMethod.invoke(iConnectivityManager , ON);
		  } catch (Exception e) {
		  }
		  return true;
	 }

	 boolean DataOnOff(boolean status , Context context) {
		  int bv = 0;
		  try {
			   if ( bv == Build.VERSION_CODES.FROYO ) {
					//android 2.2 versiyonu için
					Method dataConnSwitchmethod;
					Class<?> telephonyManagerClass;
					Object ITelephonyStub;
					Class<?> ITelephonyClass;

					TelephonyManager telephonyManager = (TelephonyManager) context
							.getSystemService(Context.TELEPHONY_SERVICE);

					telephonyManagerClass = Class.forName(telephonyManager
							.getClass().getName());
					Method getITelephonyMethod = telephonyManagerClass
							.getDeclaredMethod("getITelephony");
					getITelephonyMethod.setAccessible(true);
					ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
					ITelephonyClass = Class.forName(ITelephonyStub.getClass()
							.getName());

					if ( status ) {
						 dataConnSwitchmethod = ITelephonyClass
								 .getDeclaredMethod("enableDataConnectivity");
					} else {
						 dataConnSwitchmethod = ITelephonyClass
								 .getDeclaredMethod("disableDataConnectivity");
					}
					dataConnSwitchmethod.setAccessible(true);
					dataConnSwitchmethod.invoke(ITelephonyStub);

			   } else {
					// android 2.2 üstü versiyonlar için
					final ConnectivityManager conman = (ConnectivityManager) context
							.getSystemService(Context.CONNECTIVITY_SERVICE);
					final Class<?> conmanClass = Class.forName(conman.getClass()
							.getName());
					final Field iConnectivityManagerField = conmanClass
							.getDeclaredField("mService");
					iConnectivityManagerField.setAccessible(true);
					final Object iConnectivityManager = iConnectivityManagerField
							.get(conman);
					final Class<?> iConnectivityManagerClass = Class
							.forName(iConnectivityManager.getClass().getName());
					final Method setMobileDataEnabledMethod = iConnectivityManagerClass
							.getDeclaredMethod("setMobileDataEnabled" , Boolean.TYPE);
					setMobileDataEnabledMethod.setAccessible(true);
					setMobileDataEnabledMethod.invoke(iConnectivityManager , status);
			   }

			   return true;

		  } catch (Exception e) {
			   Log.e("Mobile Data" , "error turning on/off data");

			   return false;
		  }

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