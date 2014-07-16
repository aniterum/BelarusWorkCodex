package com.law.belarus.job.codex;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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
				MainActivity.MakeTextBigger();			
				
			}
			
		});
		
		controls.setOnZoomOutClickListener(new OnClickListener(){

			public void onClick(View v) {
				MainActivity.MakeTextSmaller();
			}
			
		});
		
	}
	
	public static Context getContext(){
		return appContext;
	}

}
