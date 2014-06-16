package com.law.belarus.work.codex;

import java.net.URLEncoder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EmailActivity extends Activity {
	
	private final static String URL_FOR_SEND = "https://docs.google.com/spreadsheet/formResponse?formkey=dFN5cEN0UzdiSDZTel9fWDNSNzJSQWc6MQ&entry.0.single=%s&entry.1.single=%s&entry.2.single=%d&entry.3.single=%s&submit=Submit";
	
	private EditText messageBox;
	private EditText signBox;
	private static String appVersion;
	private static String EmailPrefix;
	
	private static final String SPACEBAR_HEX = "%20";	
	private static final String PLUS = "+";
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_message_dialog);
        
        messageBox = (EditText)findViewById(R.id.editText_email_dialog_What_Do_You_Want);
        signBox    = (EditText)findViewById(R.id.editText_email_dialog_Who_Are_You);
        
        EmailPrefix = getResources().getString(R.string.email_prefix);
        
        try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			appVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
			appVersion = "N/A";
	  }
        
    }
    
public void sendMessage(View v){
	
	String msg = messageBox.getText().toString();
	
	if (msg.length() == 0){
		Toast youForgot = Toast.makeText(this, R.string.msg_you_forgot, Toast.LENGTH_LONG);
		youForgot.setGravity(Gravity.CENTER, 0, 0);
		youForgot.show();
		return;
	}
	
	String sign = signBox.getText().toString();
	
	//Добавлено, т.к. ProgressDialog не поддерживает подгрузку текста из ресурсов
	CharSequence message = this.getResources().getText(R.string.msg_send_in_progress);
	
	ProgressDialog dialog = ProgressDialog.show(EmailActivity.this, null, message, true);
	
	SendMessageThread longTask = new SendMessageThread(this, makeURL(msg, sign), dialog);
	longTask.execute();
	
	 }

/**
 * Создаёт URL для отправки запроса в Google Docs
 * @param text - Текст сообщения
 * @param sign - Подпись отправляющего
 * @return отформатированный текст в виде %D0%20%F1 - URL
 */
public static String makeURL(String text, String sign){
	
	String encodedText = URLEncoder.encode(text).replace(PLUS, SPACEBAR_HEX);
	String encodedSign = URLEncoder.encode(sign).replace(PLUS, SPACEBAR_HEX);
	
	
	String result = String.format(URL_FOR_SEND, encodedText, encodedSign, Build.VERSION.SDK_INT, appVersion + SPACEBAR_HEX + EmailPrefix);
	
	return result;
	
}





}




