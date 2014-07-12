package com.law.belarus.work.codex;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


/**
 * Класс, управляющий списком закладок
 */
public class BookmarksListAdapter extends ArrayAdapter<Object> {
	
	private ArrayList<Article> bookmarks;
	private static final String BOOKMARK_TEXT_TAG = "BOOKMARK_TEXT";
	private static final String DELETE_TAG = "DELETE_TAG";
	private static final String TITLE_LAYOUT_TAG = "TITLE_LAYOUT";
	private static final String ARTICLE_START = "Статья ";
	
/**
 * Конструктор класс, нужен только для того, чтобы загрузить закладки
 *
 * @param bookmarks - Закладки, которые нужно изобразить
 */
	public BookmarksListAdapter(Context context, int resource, ArrayList<Article> bookmarks) {
		super(context, resource);
		this.bookmarks = bookmarks;
	}


/**
 * Функция возвращает Представление по номеру в списке
 */
	@Override
	public View getView(final int position, View layout, ViewGroup parent) {

		View bookmarkView = MainActivity.inflater.inflate(R.layout.bookmark_list_item, parent, false);
		
		LinearLayout bookmarkLayout = (LinearLayout)bookmarkView.findViewWithTag(TITLE_LAYOUT_TAG);					//Получаем объект-контейнер текста и кнопки "Удалить"
		TextView     bookmarkText   = (TextView)bookmarkLayout.findViewWithTag(BOOKMARK_TEXT_TAG);					//Получаем ссылку на текстовое поле для названия статьи
		TextView     deleteText     = (TextView)bookmarkView.findViewWithTag(DELETE_TAG);							//Получаем ссылку на кнопку "Удалить"
		bookmarkText.setText(ARTICLE_START + bookmarks.get(position).id + ".\n" + bookmarks.get(position).title);	//Задаём текст названия статьи

		
		//Задаём реакцию на нажатие по закладке, как на обычную статью в списке		
		bookmarkLayout.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				
				int chapter = bookmarks.get(position).chapter;
				int offset  = bookmarks.get(position).offset;
				
				ViewUtils.articleCallback.onArticleItemClick(chapter, 
															 offset,
															 ViewUtils.DO_NOT_SLIDE);
			}
		});
		
		//Задаём реакцию на нажатие по кнопку "Удалить"
		deleteText.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				MainActivity.db.removeBookmark(bookmarks.get(position).id);
				bookmarks.remove(position);
				
				try {
					// Скрываем и так невидимый список статей, для того, чтобы не было одного лишнего нажатия
					if (bookmarks.size() == 0)
						MainActivity.articlesListView.setVisibility(ListView.INVISIBLE);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//Извещаем, что количество строк изменилось (для обновления списка)
				notifyDataSetChanged();
				
			}
		});
		
		return bookmarkView;
	}


	/**
	 * Обязательно должен быть, для того, чтобы знать сколько раз вызывать getView
	 */
	@Override
	public int getCount() {
		return this.bookmarks.size();
	}


	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	
	

}
