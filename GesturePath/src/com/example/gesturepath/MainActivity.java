package com.example.gesturepath;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.example.gesturepath.GesturePathView.OnGestureFinishListener;

public class MainActivity extends Activity {

	GesturePathView view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		view = (GesturePathView) findViewById(R.id.path);
		view.setOnGestureFinishListener(new OnGestureFinishListener() {

			@Override
			public void OnGestureFinish(boolean success) {
				if (success) {
					Toast.makeText(MainActivity.this, "成功!!!!", 0).show();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
