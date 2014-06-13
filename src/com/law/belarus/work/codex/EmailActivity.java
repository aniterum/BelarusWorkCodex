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
	private final static String MSG_YOU_FORGOT = "Вы забыли написать сообщение :)";
	public final static String MSG_ERROR	   = "При передаче данных произошла ошибка.\nПроверьте интернет-соединение и повторите";
	public final static String MSG_EVERYTHING_FINE = "Сообщение отправлено.\nЗаглядываю я в них не часто,\nтак что если у вас что-то важное, то лучше свяжитесь со мной по eMail";
	
	
	private EditText messageBox;
	private EditText signBox;
	private static String appVersion;
//	private ProgressDialog dialog;
	private static String EmailPrefix;
	
	
 
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
		Toast youForgot = Toast.makeText(this, MSG_YOU_FORGOT, Toast.LENGTH_LONG);
		youForgot.setGravity(Gravity.CENTER, 0, 0);
		youForgot.show();
		return;
	}
	
	String sign = signBox.getText().toString();
	
	ProgressDialog dialog = ProgressDialog.show(EmailActivity.this, "", "Отправка сообщения...", true);
	
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
	
	String encodedText = URLEncoder.encode(text).replace("+", "%20");
	String encodedSign = URLEncoder.encode(sign).replace("+", "%20");
	
	
	String result = String.format(URL_FOR_SEND, encodedText, encodedSign, Build.VERSION.SDK_INT, appVersion+"%20"+EmailPrefix);
	
	return result;
	
}





}




