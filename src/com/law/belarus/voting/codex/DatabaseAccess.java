package com.law.belarus.voting.codex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.Html;
import android.text.Spanned;


/**
 * Класс для доступа к базе данных, содержащей данные кодекса
 * 
 * @author Aniterum
 * 
 */
public class DatabaseAccess {

	@SuppressWarnings("unused")
	private static final String LogTag = "DatabaseAccess";
	private static final String SQLITE_ERROR_MSG = "!Ошибка при загрузке!";
	public static SQLiteDatabase base;
	
	private static final String ARTICLE = "Статья ";
	
	private static final String TABLE_NAME_ARTICLES = "Articles";
	private static final String TABLE_NAME_CHAPTERS = "Chapters";
	private static final String TABLE_NAME_DOCINFO = "Docinfo";
	
	private static final String SELECT = "select ";
	private static final String FROM = " from ";
	private static final String WHERE  = " where ";
	private static final String EQUAL  = "=";
	private static final String LIKE  = " like ";
	private static final String AND  = " and ";
	
	
	private static final String SQL_GET_ARTICLES = "select * from " + TABLE_NAME_ARTICLES;
	private static final String COLUMN_ID = "ID";
	private static final String COLUMN_CHAPTER = "CHAPTER";
	private static final String COLUMN_TITLE= "TITLE";
	private static final String ARTICLE_COLUMN_TEXT = "ARTICLE_TEXT";
	private static final String ARTICLE_COLUMN_BOOKMARK = "IN_BOOKMARKS";
	private static final String ARTICLE_COLUMN_OFFSET = "OFFSET";
	private static final String ARTICLE_COLUMN_EXTRA_ID = "EXTRA_ID";
	private static final String DOCINFO_COLUMN_TEXT = "INFO";
	private static final String DOCINFO_COLUMN_VERSION = "VERSION";
	
	private static final int BOOKMARK_OFF = 0;
	private static final int BOOKMARK_ON = 1;
	
	private static int SQL_ARTICLE_ID;
	private static int SQL_ARTICLE_CHAPTER;
	private static int SQL_ARTICLE_TITLE;
	private static int SQL_ARTICLE_TEXT;
	private static int SQL_ARTICLE_BOOKMARK;
	private static int SQL_ARTICLE_OFFSET;
	private static int SQL_ARTICLE_EXTRA_ID;
	
	private static final String SQL_GET_CHAPTERS = "select * from " + TABLE_NAME_CHAPTERS;
	private static int SQL_CHAPTER_ID;
	private static int SQL_CHAPTER_TITLE;
	
	private static final String SQL_GET_DOCINFO = "select * from " + TABLE_NAME_DOCINFO;
	private static int SQL_DOCINFO_TEXT;
	
	public static final int BOOKMARK_ALREADY_EXISTS = -1;
	
	public static int CHAPTERS_COUNT;

	/**
	 * Конструктор, получаем доступ к файлу базы данных или создаём его, если он
	 * отсутствует.
	 * 
	 * @param dbPath - Путь к базе данных
	 * @param res - Ресурсы приложения, для доступа к базе данных
	 */
	public DatabaseAccess(String dbPath, Resources res) {
 
		Boolean db_exists = new File(dbPath).exists();
		//Проверка на существование файла
		if (db_exists) {
			try {
				base = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
				
				Cursor docinfo = base.rawQuery(SQL_GET_DOCINFO, null);
				docinfo.moveToFirst();
				
				//Проверяем наличие столбца с версией базы данных
				int version_column = docinfo.getColumnIndex(DOCINFO_COLUMN_VERSION);
				
				if (version_column != -1){
					/* Для сравнения баз данных старой и обновленной используем версии
					   базы данных и строки с версией в приложении */
					
					String db_version = docinfo.getString(version_column);
					String app_db_version = res.getString(R.string.db_version);
					
					
					if (!db_version.equals(app_db_version)){
						ArrayList<Article> temporary_bookmarks = getBookmarks();
						base.close();
						CopyDatabaseFromResources(dbPath, res);
						//После копирования базы данных открываем её для чтения
						base = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
						
						
						//Копируем закладки в новую базу данных
						if (temporary_bookmarks.size() != 0)
							for (Article article : temporary_bookmarks)
								addBookmark(article.id);
							
					}
						
					
					
					
				} else { 
					//Старая версия базы данных
					ArrayList<Article> temporary_bookmarks = getBookmarks();
					base.close();
					CopyDatabaseFromResources(dbPath, res);
					//После копирования базы данных открываем её для чтения
					base = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
					
					
					//Копируем закладки в новую базу данных
					if (temporary_bookmarks != null)
						if (temporary_bookmarks.size() != 0)
							for (Article article : temporary_bookmarks) 
								addBookmark(article.id);
					
				}
				
			} catch (SQLiteException e) {
				e.printStackTrace();
			} 
			

		} else {
			//База данных не существует. Либо это первый запуск либо чистили данные приложения.

			CopyDatabaseFromResources(dbPath, res);
			base = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);

		}		
		
		//Находим столбцы соответствующих таблиц, для совместимости с перестановками
				
		Cursor articles = base.rawQuery(SQL_GET_ARTICLES, null);
		SQL_ARTICLE_ID 		 = articles.getColumnIndex(COLUMN_ID);
		SQL_ARTICLE_CHAPTER  = articles.getColumnIndex(COLUMN_CHAPTER);
		SQL_ARTICLE_TITLE 	 = articles.getColumnIndex(COLUMN_TITLE);
		SQL_ARTICLE_TEXT	 = articles.getColumnIndex(ARTICLE_COLUMN_TEXT);
		SQL_ARTICLE_BOOKMARK = articles.getColumnIndex(ARTICLE_COLUMN_BOOKMARK);
		SQL_ARTICLE_OFFSET   = articles.getColumnIndex(ARTICLE_COLUMN_OFFSET);
		SQL_ARTICLE_EXTRA_ID   = articles.getColumnIndex(ARTICLE_COLUMN_EXTRA_ID);
		
		Cursor chapters = base.rawQuery(SQL_GET_CHAPTERS, null);
		SQL_CHAPTER_ID    = chapters.getColumnIndex(COLUMN_ID);
		SQL_CHAPTER_TITLE = chapters.getColumnIndex(COLUMN_TITLE);
		
		Cursor docinfo = base.rawQuery(SQL_GET_DOCINFO, null); 
		SQL_DOCINFO_TEXT = docinfo.getColumnIndex(DOCINFO_COLUMN_TEXT);
		
		CHAPTERS_COUNT = getChaptersList().size();

	}

	
	private void CopyDatabaseFromResources(String dbPath, Resources res){
		
		File db_file = new File(dbPath);
		//Открываем исходную базу данных в ресурсах
		InputStream resBase = res.openRawResource(R.raw.codex);

		try {

			FileOutputStream saveBase = new FileOutputStream(db_file);
			final int bufSize = resBase.available(); //Длина файла базы данных в ресурсах

			final byte[] buffer = new byte[bufSize];
			resBase.read(buffer);

			saveBase.write(buffer);

			resBase.close();
			saveBase.close();

		} // try
		  catch (FileNotFoundException e) {
			 e.printStackTrace();
		} catch (IOException e) {
			 e.printStackTrace();
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		
	}
	

	/**
	 * Загрузить все статьи из определённой главы
	 * @param chapter - номер главы
	 * @return
	 */
	public ArrayList<Article> getArticlesByChapter(int chapter) {

		final String SQL_QUERY = SQL_GET_ARTICLES + WHERE + COLUMN_CHAPTER + EQUAL + chapter;

		Cursor cursor = base.rawQuery(SQL_QUERY, null);
		cursor.moveToFirst();

		if (cursor.getCount() == 0)
			return null;

		ArrayList<Article> result = new ArrayList<Article>();
		do {
			result.add(new Article(cursor.getInt(SQL_ARTICLE_ID), cursor.getInt(SQL_ARTICLE_CHAPTER), cursor.getString(SQL_ARTICLE_TITLE), cursor.getString(SQL_ARTICLE_TEXT), cursor.getInt(SQL_ARTICLE_OFFSET), cursor.getString(SQL_ARTICLE_EXTRA_ID)));

		} while (cursor.moveToNext());

		return result;

	}
	
	/**
	 * Загрузить массив названий статей из нужной главы
	 * @param chapter - Номер главы
	 */
	public ArrayList<Spanned> getArticlesTitlesByChapter(int chapter) {

		
		final String SQL_QUERY = SQL_GET_ARTICLES + WHERE + COLUMN_CHAPTER + EQUAL + chapter;

		Cursor cursor = base.rawQuery(SQL_QUERY, null);
		cursor.moveToFirst();

		if (cursor.getCount() == 0)
			return null;
		
		ArrayList<Spanned> result = new ArrayList<Spanned>();
			do {
				if (cursor.getString(SQL_ARTICLE_EXTRA_ID).equals(""))
					result.add(Html.fromHtml(ARTICLE + cursor.getInt(SQL_ARTICLE_ID) + ".<br>" + cursor.getString(SQL_ARTICLE_TITLE))); // Формируем название в стиле "Статья 43.\nНазвание статьи"
				else
					result.add(Html.fromHtml(ARTICLE + cursor.getString(SQL_ARTICLE_EXTRA_ID) + ".<br>" + cursor.getString(SQL_ARTICLE_TITLE)));

			} while (cursor.moveToNext());

		return result;

	}
	
	/**
	 * Получаем список глав с их ID, Названием, номером первой и последней статьи
	 */
	public ArrayList<Chapter> getChaptersList(){

		final String SQL_QUERY_ARTICES  = SELECT + COLUMN_ID + FROM + TABLE_NAME_ARTICLES + WHERE + COLUMN_CHAPTER + EQUAL;
		
		//Загружаем названия всех глав
		Cursor chapters = base.rawQuery(SQL_GET_CHAPTERS, null);
		chapters.moveToFirst();
		
		if (chapters.getCount() == 0)
			return null;
		
		
		ArrayList<Chapter> result = new ArrayList<Chapter>();
		Cursor articles;
		
		int chapterID;
		int firstArticle;
		int lastArticle;
		
		do {
			chapterID = chapters.getInt(SQL_CHAPTER_ID); //Получаем порядковый номер главы
			articles = base.rawQuery(SQL_QUERY_ARTICES + chapterID, null); //Загружаем статьи из этой главы

			articles.moveToFirst();
			firstArticle = articles.getInt(0);	//Узнаём ID первой статьи в этой главе
			
			articles.moveToLast();
			lastArticle = articles.getInt(0);	//Узнаём ID последней статьи в этой главе
			
			result.add(new Chapter(chapterID, chapters.getString(SQL_CHAPTER_TITLE), firstArticle, lastArticle));	
			
		} while (chapters.moveToNext());	//Следующий

	return result;
		
	}
	
	/**
	 * Получаем название главы на номеру
	 * @param id
	 * @return
	 */
	public String getChapterTitleById(int id) {

		final String SQL_QUERY = SELECT + COLUMN_TITLE + FROM + TABLE_NAME_CHAPTERS + WHERE + COLUMN_ID + EQUAL + id;

		Cursor chapters = base.rawQuery(SQL_QUERY, null);

		if (chapters.getCount() == 0)
			return SQLITE_ERROR_MSG;

		chapters.moveToFirst();

		return chapters.getString(0);

	}
	
	/**
	 * Загружаем информацю о документе
	 */
	public String getDocumentInfo() {

		Cursor docInfo = null;
		try {
			docInfo = base.rawQuery(SQL_GET_DOCINFO, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
		}

		if (docInfo != null) {
			docInfo.moveToFirst();

			return docInfo.getString(SQL_DOCINFO_TEXT);
		} else
			return SQLITE_ERROR_MSG;
	}
	
	/**
	 * Добавляем закладку
	 * @param chapterId - номер главы
	 * @param articleId - номер статьи в главе
	 * @return
	 */
	public int addBookmark(int chapterId, int articleOffset) {

		Cursor bookmark = null;

		ArrayList<Article> art = this.getArticlesByChapter(chapterId);
		int id = art.get(articleOffset).id;
		
		final String SQL_QUERY = SQL_GET_ARTICLES + WHERE + "(" + ARTICLE_COLUMN_BOOKMARK + "=1 AND " + COLUMN_ID + EQUAL + id + ")";

		try {

			bookmark = base.rawQuery(SQL_QUERY, null);

			if (bookmark.getCount() != 0)
				return BOOKMARK_ALREADY_EXISTS;

			ContentValues values = new ContentValues();
			values.put(ARTICLE_COLUMN_BOOKMARK, BOOKMARK_ON);

			base.update(TABLE_NAME_ARTICLES, values, COLUMN_ID + EQUAL + id, null);

		} catch (SQLiteException e) {
			e.printStackTrace();
		}

		return id;

	}
	
	/**
	 * Добавляет закладку используя только id статьи
	 */
	public int addBookmark(int articleId) {

		final String SQL_QUERY = SQL_GET_ARTICLES + WHERE + "(" + ARTICLE_COLUMN_BOOKMARK + "=1 AND " + COLUMN_ID + EQUAL + articleId + ")";

		try {

			Cursor bookmark = base.rawQuery(SQL_QUERY, null);

			if (bookmark.getCount() != 0)
				return BOOKMARK_ALREADY_EXISTS;

			ContentValues values = new ContentValues();
			values.put(ARTICLE_COLUMN_BOOKMARK, BOOKMARK_ON);

			base.update(TABLE_NAME_ARTICLES, values, COLUMN_ID + EQUAL + articleId, null);

		} catch (SQLiteException e) {
			e.printStackTrace();
		}

		return articleId;

	}
	
	
	
	/**
	 * Удаляем закладку, а точнее помечаем в базе данных, что флаг IN_BOOKMARKS = 0
	 * @param id
	 */
	public int removeBookmark(int id){
		ContentValues values = new ContentValues();
		values.put(ARTICLE_COLUMN_BOOKMARK, BOOKMARK_OFF);
		return base.update(TABLE_NAME_ARTICLES, values, COLUMN_ID + EQUAL + id, null);
	}
	
	
	
	/**
	 * Загружаем закладки
	 * 
	 */
	public ArrayList<Article> getBookmarks() {

		final String SQL_QUERY = SQL_GET_ARTICLES + WHERE + ARTICLE_COLUMN_BOOKMARK + EQUAL + BOOKMARK_ON;

		Cursor ret = base.rawQuery(SQL_QUERY, null);
		ret.moveToFirst();

		if (ret.getCount() == 0)
			return null;

		ArrayList<Article> result = new ArrayList<Article>();
		do {
			result.add(new Article(ret.getInt(SQL_ARTICLE_ID), ret.getInt(SQL_ARTICLE_CHAPTER), ret.getString(SQL_ARTICLE_TITLE), ret.getString(SQL_ARTICLE_TEXT), ret.getInt(SQL_ARTICLE_OFFSET), ret.getString(SQL_ARTICLE_EXTRA_ID)));

		} while (ret.moveToNext());

		return result;

	}
	

	
//	/**
//	 * Получаем номер статьи в разделе относительно начала, используя колонку OFFSET 
//	 * @param id - Номер статьи
//	 */
//	public int getArticleOffsetInChapter(int id) {
//		
//		final String SQL_QUERY = SELECT + ARTICLE_COLUMN_OFFSET + FROM + TABLE_NAME_ARTICLES + WHERE + COLUMN_ID + EQUAL + id;
//		
//		Cursor article = base.rawQuery(SQL_QUERY,	null);
//		article.moveToFirst();
//		return article.getInt(0);
//		
//	}
	
	/**
	 * Поиск текста в статьях
	 * @param String text - Текст для поиска
	 * @return - id статей, подходящих под поиск
	 */
	public ArrayList<Integer> findInArticles(String text){
		
		if (text == "")
			return null;
		
		final String SQL_QUERY = SELECT + COLUMN_ID + FROM + TABLE_NAME_ARTICLES + WHERE + ARTICLE_COLUMN_TEXT + LIKE + "\"%" + text + "\"%";
		
		Cursor searchText = base.rawQuery(SQL_QUERY, null);

		ArrayList<Integer> result = new ArrayList<Integer>();

		if (searchText.getCount() == 0)
			return null;

		searchText.moveToFirst();
		do {
			result.add(searchText.getInt(0));

		} while (searchText.moveToNext());

		return result;
	}
	
//	public String getArticleTextByChapterAndOffset(int chapter, int offset){
//		
//		final String SQL_QUERY = SQL_GET_ARTICLES + WHERE + "(" + COLUMN_CHAPTER + EQUAL + chapter + AND + ARTICLE_COLUMN_OFFSET + EQUAL + offset + ")";
//		
//		Cursor cursor = base.rawQuery(SQL_QUERY, null);
//		cursor.moveToFirst();
//
//		if (cursor.getCount() == 0)
//			return null;
//		
//		return ARTICLE + cursor.getInt(SQL_ARTICLE_ID) + ". " + cursor.getString(SQL_ARTICLE_TITLE) + ".\n\n" + cursor.getString(SQL_ARTICLE_TEXT);
//
//	}
	
	/**
	 * Возвращает текст статьи
	 * @param articleId
	 * @return
	 */
	public String getArticleTextById(int articleId){
		
		final String SQL_QUERY = SQL_GET_ARTICLES + WHERE + COLUMN_ID + EQUAL + articleId;
		
		Cursor cursor = base.rawQuery(SQL_QUERY, null);
		cursor.moveToFirst();

		if (cursor.getCount() == 0)
			return null;
		
		return ARTICLE + cursor.getInt(SQL_ARTICLE_ID) + ". " + cursor.getString(SQL_ARTICLE_TITLE) + ".\n\n" + cursor.getString(SQL_ARTICLE_TEXT);
		
	}
	
	
	
	
	
}








