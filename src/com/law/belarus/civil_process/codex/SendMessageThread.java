package com.law.belarus.civil_process.codex;

import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Класс потока, отправляющего сообщения
 * 
 * @author user
 * 
 */
public class SendMessageThread extends AsyncTask<Void, Void, String> {

	private final String url_string;
	private final EmailActivity parent;
	private final ProgressDialog dialog;
	private boolean isOK = false;

	/**
	 * Конструктор класса
	 * 
	 * @param parent - Родитель потока (нужен для отображения сообщений)
	 * @param url    - URL, на который нужно сделать запрос
	 * @param dialog - Диалог, который нужно отображать во время отправки
	 */
	SendMessageThread(EmailActivity parent, String url, ProgressDialog dialog) {
		this.url_string = url;
		this.parent = parent;
		this.dialog = dialog;
	}

	/**
	 * Функция, которая будет работать в фоне
	 */
	@Override
	protected String doInBackground(Void... noargs) {

		HttpURLConnection urlConnection = null;
		boolean noErrors = true;

		try {
			URL url = new URL(this.url_string);
			
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true);
			urlConnection.setChunkedStreamingMode(0);

			urlConnection.getInputStream();

		} catch (Exception e) {
			noErrors = false;
		} finally {
			urlConnection.disconnect();
			isOK = noErrors;
		}
		return "Fine";
	}
/**
 * Функция исполняется по завершении процесса отправки
 */
	@Override
	protected void onPostExecute(String result) {

		//Прячем диалог
		dialog.hide();

		if (isOK) {
			//Сообщение отправлено без ошибок
			Toast connectError = Toast.makeText(parent,	R.string.msg_everything_fine, Toast.LENGTH_LONG);
			connectError.setGravity(Gravity.CENTER, 0, 0);
			connectError.show();
			parent.finish();

		} else {
			//Имелись ошибки при отправке, скорее всего отключен интернет
			Toast connectError = Toast.makeText(parent, R.string.msg_send_error, Toast.LENGTH_LONG);
			connectError.setGravity(Gravity.CENTER, 0, 0);
			connectError.show();

		}

	}
}