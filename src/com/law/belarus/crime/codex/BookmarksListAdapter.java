package com.law.belarus.crime.codex;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
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
	private static final String NEW_LINE = ".\n";
//	private static final int ANIMATION_DURATION = 250;
	
	private static final int BOOKMARK_MAX_LENGTH = 45;
	private static final int BOOKMARK_CUT_LENGTH = 40;
	private static final String BOOKMARK_CUT_END = "...";
	
	
/**
 * Конструктор класс, нужен только для того, чтобы загрузить закладки
 *
 * @param bookmarks - Закладки, которые нужно изобразить
 */
	public BookmarksListAdapter(Context context, int resource, ArrayList<Article> bookmarks) {
		super(context, resource);
		this.bookmarks = bookmarks;
	}
	
	private static class ViewHolder{
		
		LinearLayout bookmarkLayout;
		TextView     bookmarkText;
		TextView     deleteText;
		
	}

	
	/**
	 * Функция возвращает Представление по номеру в списке
	 */
		public View getView(final int position,  View convertView, ViewGroup parent) {
			
			final ViewHolder holder;
			

			if (convertView == null){
				convertView = MainActivity.inflater.inflate(R.layout.bookmark_list_item, parent, false);
			
				holder = new ViewHolder();
				
				holder.bookmarkLayout = (LinearLayout)convertView.findViewWithTag(TITLE_LAYOUT_TAG);					//Получаем объект-контейнер текста и кнопки "Удалить"
				holder.bookmarkText   = (TextView)holder.bookmarkLayout.findViewWithTag(BOOKMARK_TEXT_TAG);					//Получаем ссылку на текстовое поле для названия статьи
				holder.deleteText     = (TextView)convertView.findViewWithTag(DELETE_TAG);							//Получаем ссылку на кнопку "Удалить"

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			String bookmarkShortText = bookmarks.get(position).title;
			if (bookmarkShortText.length() > BOOKMARK_MAX_LENGTH)
				bookmarkShortText = bookmarkShortText.substring(0, BOOKMARK_CUT_LENGTH) + BOOKMARK_CUT_END;
			
			int bookmarkArticleId = bookmarks.get(position).id;
			if ((bookmarkArticleId > DatabaseAccess.MIN_NOTE_ID) & (bookmarkArticleId < DatabaseAccess.MAX_NOTE_ID))
				//Задаём текст названия статьи с поправкой на Примечания
				holder.bookmarkText.setText(bookmarkShortText + " главы " + (bookmarks.get(position).chapter + 1));	
			else
				holder.bookmarkText.setText(ARTICLE_START + bookmarks.get(position).id + NEW_LINE + bookmarkShortText);	//Задаём текст названия статьи

			
			//Задаём реакцию на нажатие по закладке, как на обычную статью в списке		
			holder.bookmarkLayout.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					
					int chapter = bookmarks.get(position).chapter;
					int offset  = bookmarks.get(position).offset;
					
					ViewUtils.articleCallback.onArticleItemClick(chapter, offset, ViewUtils.DO_NOT_SLIDE);
				}
			});
			
			final View row = convertView;
			
			//Задаём реакцию на нажатие по кнопку "Удалить"
			holder.deleteText.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {

					deleteCell(row, position);
					
				}
			});
			
			return convertView;
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
	
	
	private void deleteCell(final View v, final int index) {
		AnimationListener al = new AnimationListener() {
			public void onAnimationEnd(Animation arg0) {
				//Удаление произойдёт после проигрыша анимации
				MainActivity.db.removeBookmark(bookmarks.get(index).id);
				bookmarks.remove(index);
				
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
			
			public void onAnimationRepeat(Animation animation) {}

			public void onAnimationStart(Animation animation) {}

		};

		collapse(v, al);
		
	}
	
	/*Использован пример из репозитория
	 * https://github.com/paraches/ListViewCellDeleteAnimation/blob/master/src/com/example/myanimtest/MainActivity.java
	 */
	private void collapse(final View v, AnimationListener al) {
//		final int initialHeight = v.getMeasuredHeight();
//
//		Animation anim = new Animation() {
//			@Override
//			protected void applyTransformation(float interpolatedTime, Transformation t) {
//				if (interpolatedTime == 1) {
//					v.setVisibility(View.GONE);
//				} else {
//					v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
//					v.requestLayout();
//				}
//			} 
//
//			@Override
//			public boolean willChangeBounds() {
//				return true;
//			}
//		};
		
		Animation anim = ViewUtils.Animation_Delete;

		if (al != null) {
			anim.setAnimationListener(al);
		}
		//anim.setDuration(ANIMATION_DURATION);
		v.startAnimation(anim);
	}

}
