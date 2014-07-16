package com.law.belarus.job.codex;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ZoomControls;

/*
 * Выводит диалог изменения размера текста
 */
public class SetTextSizeDialog extends Activity {
	
	private static Context appContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		appContext = this.getApplicationContext();
		
		setContentView(R.layout.dialog_settextsize);
		
		ZoomControls controls = (ZoomControls)findViewById(R.id.zoomControls);
		
		controls.setOnZoomInClickListener(new OnClickListener(){

			public void onClick(View v) {
				MainActivity.ChangeTextSize(MainActivity.TEXT_SIZE_OFFSET);		
			}
			
		});
		
		controls.setOnZoomOutClickListener(new OnClickListener(){

			public void onClick(View v) {
				MainActivity.ChangeTextSize( - MainActivity.TEXT_SIZE_OFFSET);
			}
			
		});
		
	}
	
	public static Context getContext(){
		return appContext;
	}

	@Override
	protected void onPause() {
		Toast.makeText(getContext(), "onPause", Toast.LENGTH_SHORT).show();
		MainActivity.UpdateTextViews();
		super.onPause();
	}



}
