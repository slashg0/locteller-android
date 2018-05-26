package xyz.slashg.locteller;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{

	protected static final float DEFAULT_ACCURACY_THRESHOLD_M = 5f;
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int PERMISSION_REQCODE = 370;
	private static final int APP_STATE_INIT = 32;
	private static final int APP_STATE_GETTING = 33;
	private static final int APP_STATE_OUTPUT = 34;
	protected LocationListener myLocationListener;
	//	EditText accuracyInput;
	protected float accuracyThresholdInM = 5f;
	protected int appState = APP_STATE_INIT;
	TextView latitudeText;
	TextView longitudeText;
	TextView altitudeText;
	TextView accuracyText;
	TextView loadingText;
	ProgressBar accuracyProgressBar;
	EditText desiredAccuracyInput;
	FloatingActionButton floatingActionButton;
	CoordinatorLayout parent;
	LocationManager locationManager;

	/**
	 * Method to try and parse text of accuracyText and see if it contains a valid
	 * input for accuracy (float > 0f). Returns default if failed.
	 *
	 * @return The desired accuracy value entered by the user (if successfully parsed). Falls back to DEFAULT_ACCURACY_THRESHOLD_M.
	 * @author SlashG
	 * @since <nextVersion/>
	 */
	protected float getDesiredAccuracy()
	{
		try
		{
			float accuracy = Float.parseFloat(desiredAccuracyInput.getText().toString());
			Log.d(TAG, "Input accuracy = " + accuracy);
			return accuracy;
		} catch (NumberFormatException nFE)
		{
			Log.d(TAG, "input doesn't match");
			return DEFAULT_ACCURACY_THRESHOLD_M;
		} catch (Exception e)
		{
			Log.e(TAG, "getDesiredAccuracy: ", e);
			return DEFAULT_ACCURACY_THRESHOLD_M;
		}
	}

	/**
	 * Method to init {@link LocationListener} if it is null.
	 *
	 * @author SlashG
	 * @since <nextVersion/>
	 */
	protected void initLocationListenerIfNeeded()
	{
		if (myLocationListener == null)
		{

			myLocationListener = new LocationListener()
			{
				@Override
				public void onLocationChanged(Location location)
				{
					Log.d(TAG, "onLocationChanged: Location = " + location);
					displayLocation(location);
					if (location.getAccuracy() <= getDesiredAccuracy())
					{
						Log.d(TAG, "location is accurate enough");
						onDesiredAccuracyAchieved(location);
					}
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras)
				{
					Log.d(TAG, "onStatusChanged: ");
				}

				@Override
				public void onProviderEnabled(String provider)
				{
					Log.d(TAG, "onProviderEnabled: ");

				}

				@Override
				public void onProviderDisabled(String provider)
				{
					Log.d(TAG, "onProviderDisabled: ");
				}
			};
		}
	}

	/**
	 * Method to call when an output has been displayed. This method sets the appState to APP_STATE_INIT.
	 *
	 * @author SlashG
	 * @since <nextVersion/>
	 */
	protected void resetAppState()
	{

		appState = APP_STATE_INIT;
		displayLocation(null);
		onAppStateChanged();
	}

	/**
	 * Method to set values in the output fields of the app.
	 *
	 * @param location The location to set the Latitude, Longitude, Altitude and Accuracy in the repective fields. Pass 'null' to clear all fields.
	 * @author SlashG
	 * @since <nextVersion/>
	 */
	protected void displayLocation(@Nullable Location location)
	{
		if (location == null)
		{

			latitudeText.setText("");
			longitudeText.setText("");
			altitudeText.setText("");
			accuracyText.setText("");

			accuracyProgressBar.setProgress(0);

			Snackbar.make(parent, "Reset state", Snackbar.LENGTH_SHORT).show();
		}
		else
		{
			latitudeText.setText(String.valueOf(location.getLatitude()));
			longitudeText.setText(String.valueOf(location.getLongitude()));
			altitudeText.setText(String.valueOf(location.getAltitude()));
			accuracyText.setText(String.valueOf(location.getAccuracy()));

			accuracyProgressBar.setProgress((int) (accuracyProgressBar.getMax() - Math.min(location.getAccuracy(), 100)));
		}
	}

	/**
	 * Method called from the {@link LocationListener} callback when a location passes the threshold accuracy check.
	 * <p>
	 * Thie method changes appState to APP_STATE_OUTPUT
	 *
	 * @author SlashG
	 * @since <nextVersion/>
	 */
	protected void onDesiredAccuracyAchieved(@NonNull Location location)
	{

		abortLocationUpdates();

		Snackbar.make(parent, "Desired accuracy achieved", Snackbar.LENGTH_SHORT).show();
		appState = APP_STATE_OUTPUT;
		onAppStateChanged();
	}

	/**
	 * Method (that should be) called everytime the app state is changed.
	 *
	 * @author SlashG
	 * @since <nextVersion/>
	 */
	protected void onAppStateChanged()
	{
		Log.d(TAG, "onAppStateChanged: ");
		updateActivityView();
	}

	/**
	 * Overridden to force stop getting location updates when the user exits the app (even on minimizing).
	 * This is to ensure that the get location process doesn't run in the background as that is a potential battery drainer.
	 *
	 * @author SlashG
	 * @since <nextVersion/>
	 */
	@Override
	protected void onStop()
	{
		super.onStop();
		abortLocationUpdates();
	}

	/**
	  * Method that performs the state based updates in the UI.
	  *
	  * @since <nextVersion/>
	  * @author SlashG
	  */
	protected void updateActivityView()
	{
		if (appState == APP_STATE_GETTING)
		{
			loadingText.setVisibility(View.VISIBLE);
			AlphaAnimation animation = new AlphaAnimation(1, 0);
			animation.setDuration(500);
			animation.setRepeatMode(Animation.REVERSE);
			animation.setRepeatCount(Animation.INFINITE);
			loadingText.startAnimation(animation);
//			loadingText.animate().alpha(0f).setDuration(500).setInterpolator(new BounceInterpolator()).start();
			desiredAccuracyInput.setEnabled(false);
		}
		else
		{
			loadingText.clearAnimation();
			loadingText.setVisibility(View.GONE);
			desiredAccuracyInput.setEnabled(true);

		}

		@DrawableRes int buttonGraphicRes;

		switch (appState)
		{
			case APP_STATE_OUTPUT:
				buttonGraphicRes = R.drawable.baseline_repeat_white_24;
				break;
			case APP_STATE_GETTING:
				buttonGraphicRes = R.drawable.baseline_location_off_white_24;
				break;
			case APP_STATE_INIT:
			default:
				buttonGraphicRes = R.drawable.baseline_location_on_white_24;
		}

		floatingActionButton.setImageResource(buttonGraphicRes);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		parent = findViewById(R.id.parent);
		accuracyText = findViewById(R.id.accuracy_tv);
		latitudeText = findViewById(R.id.latitude);
		longitudeText = findViewById(R.id.longitude);
		altitudeText = findViewById(R.id.altitude);
		loadingText = findViewById(R.id.loading_text);
		accuracyProgressBar = findViewById(R.id.accuracy_pb);
		desiredAccuracyInput = findViewById(R.id.accuracy_input);
		floatingActionButton = findViewById(R.id.fab);
//		accuracyInput= findViewById(R.id.;
		setSupportActionBar(toolbar);
		floatingActionButton.setOnClickListener(this::onFabClick);

		desiredAccuracyInput.setOnEditorActionListener((v, actionId, event) ->
		{
			if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND)
			{
				floatingActionButton.performClick();
			}
			return false;
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Log.d(TAG, "onRequestPermissionsResult: permission callback");
		Log.d(TAG, "onRequestPermissionsResult: requestCode = " + requestCode);
		if (requestCode == PERMISSION_REQCODE)
		{
			for (int i = 0; i < grantResults.length && i < permissions.length; i++)
			{
				Log.d(TAG, "onRequestPermissionsResult: permission '" + permissions[i] + "' status; " + grantResults[i]);
			}

			if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
			{
				Log.d(TAG, "onRequestPermissionsResult: Got permission");
				getLocation();

			}
		}
	}

	/**
	 * Method called after {@link LocationManager} and {@link LocationListener} have been init and permissions have been taken care of.
	 * The {@link LocationManager} is used to get quick location updates from the GPS.
	 * The method changes the app state to getting.
	 *
	 * @author SlashG
	 * @since <nextVersion/>
	 */
	protected void getLocation()
	{
		try
		{
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			initLocationListenerIfNeeded();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);

			appState = APP_STATE_GETTING;
			onAppStateChanged();
		} catch (SecurityException se)
		{
			Log.e(TAG, "getLocation: permission revoked, probably", se);
			Snackbar.make(parent, "Please ensure you have provided GPS permission", Snackbar.LENGTH_LONG).show();
		} catch (Exception e)
		{
			Log.e(TAG, "getLocation: some other error", e);
		}
	}

	/**
	 * Method to check for permissions (Marshmallow and above).
	 *
	 * @return true, for Marshmallow (and above) devices where both coarse and fine location permissions have been allowed; false, if either or both are denied. true, always for devices running Android Lollipop and below.
	 * @author SlashG
	 * @since <nextVersion/>
	 */
	protected boolean hasPermission()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			boolean hasFinePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
			boolean hasCoarsePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
			return (hasFinePermission && hasCoarsePermission);
		}
		else
		{
			return true;
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	protected void onFabClick(View view)
	{
		if (appState == APP_STATE_INIT)
		{
			if (!hasPermission())
			{
				requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQCODE);
			}
			else
			{
				getLocation();
			}
		}
		else if (appState == APP_STATE_OUTPUT)
		{
			resetAppState();
		}
		else if (appState == APP_STATE_GETTING)
		{
			abortLocationUpdates();
		}
	}

	/**
	 * Method to stop getting updates from the {@link LocationManager}
	 * The method changes the app state to APP_STATE_OUTPUT.
	 *
	 * @author SlashG
	 * @since <nextVersion/>
	 */
	protected void abortLocationUpdates()
	{
		Log.d(TAG, "abortLocationUpdates: ");
		if (locationManager != null)
		{
			initLocationListenerIfNeeded();
			locationManager.removeUpdates(myLocationListener);
		}
		appState = APP_STATE_OUTPUT;
		onAppStateChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
//		if (id == R.id.action_settings)
//		{
//			return true;
//		}

		return super.onOptionsItemSelected(item);
	}
}
