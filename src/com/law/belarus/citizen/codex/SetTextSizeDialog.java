package com.law.belarus.citizen.codex;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ZoomControls;

/*
 * Выводит диалог изменения размера текста
 */
public class SetTextSizeDialog extends Activity {
	
	private static Context appContext;
	private static TextView lastUsedView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		appContext = this.getApplicationContext();
		
		setContentView(R.layout.dialog_settextsize);
		
		ZoomControls controls = (ZoomControls)findViewById(R.id.zoomControls);
		
		controls.setOnZoomInClickListener(new OnClickListener(){

			public void onClick(View v) {
				lastUsedView = MainActivity.ChangeTextSize(MainActivity.TEXT_SIZE_OFFSET, lastUsedView);		
			}
			
		});
		
		controls.setOnZoomOutClickListener(new OnClickListener(){

			public void onClick(View v) {
				lastUsedView = MainActivity.ChangeTextSize( - MainActivity.TEXT_SIZE_OFFSET, lastUsedView);		
			}
			
		});
		
	}
	
	public static Context getContext(){
		return appContext;
	}

	/**
	 * Сработает при нажатии мимо диалога 
	 */
	@Override
	protected void onPause() {
		MainActivity.UpdateTextViews();
		lastUsedView = null;
		super.onPause();
	}



}
