/*
 * #%L
 * SlidingMenuDemo
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 Paul Grime
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.law.belarus.job.codex;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * Методы для работы с View.
 */


public class ViewUtils {
	
	private static final String CHAPTER_NAME = "ch";
	private static final String CHAPTER_ID = "ch_id";
	private static final String ARTICLES_IN_CHAPTER = "art";
	private static final float PERCENT = 0.85f;
	private static final int PLUS_ONE_FOR_BOOKMARKS = 1;
	
	public static final int DO_NOT_SLIDE = 0;
	public static final int SLIDE_IN     = 1;
	public static final int SLIDE_OUT    = -1;
	
	public static ArticleItemCallback articleCallback = null;
	
	public static Animation Animation_Slide_In;
	public static Animation Animation_Slide_Out;
	public static Animation Animation_Delete;
	private static Context ParentContext;
	
	public static int    openedChapter = -1;
	
    public ViewUtils(ArticleItemCallback articleCallback, Context context) {
    	
    	ViewUtils.articleCallback = articleCallback; //Ссылка на вызов функции из главной активности
    	        
        Animation_Slide_In = AnimationUtils.loadAnimation(context, R.anim.slide_in);
        Animation_Slide_Out = AnimationUtils.loadAnimation(context, R.anim.slide_out);
        Animation_Delete = AnimationUtils.loadAnimation(context, R.anim.delete);
        ParentContext = context;
        
   }

    public static void setViewWidths(View view, View[] views) {
        int w = view.getWidth();
        int h = view.getHeight();
        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            v.layout((i + 1) * w, 0, (i + 2) * w, h);
            //printView("view[" + i + "]", v);
        }
    }

/**
 * Функция инициализирует основное меню выбора глав
 * 
 * @param context
 * @param listView
 * @param layout
 * @param articleListView - список, который будет наполнен статьями из главы
 */
public void initListViewChapters(Context context, final ListView listView, int layout,  final ListView articleListView) {
    	
    	ArrayList<HashMap<String, Object>> chapterInfo = new ArrayList<HashMap<String, Object>>();      
        HashMap<String, Object> hm;                            

        //Наполняем Хеш-карту значениями для списка Глав
        ArrayList<Chapter> chaptersList = MainActivity.db.getChaptersList();
        
        //Добавляем Пункт меню глав "Закладки" в хеш-карту
        hm = new HashMap<String, Object>();
  	   	hm.put(CHAPTER_NAME, "    ЗАКЛАДКИ");                 
  	   	hm.put(ARTICLES_IN_CHAPTER, "     все, что сохранили");         
 	    hm.put(CHAPTER_ID, "");
  	    chapterInfo.add(hm); 
        
        /* Формируем хеш-карты для всех глав кодекса
         * чтобы они выглядели как:
         * 
         *   | Название главы. |
         * 1 |                 | > 
         *   | ст. 1 - 14      |  
         */
        for (Chapter chapter : chaptersList){
     	   hm = new HashMap<String, Object>();
     	   hm.put(CHAPTER_NAME, chapter.title);                 
     	   hm.put(ARTICLES_IN_CHAPTER, "ст." + chapter.firstArticle + "-" + chapter.lastArticle);         
     	   if (chapter.id < 9)
     		   hm.put(CHAPTER_ID, " " + (chapter.id + PLUS_ONE_FOR_BOOKMARKS));
     	   else
     		   hm.put(CHAPTER_ID, Integer.toString(chapter.id + PLUS_ONE_FOR_BOOKMARKS));
      
     	   chapterInfo.add(hm); 
        }
       
        SimpleAdapter adapter = new SimpleAdapter(
        		context, 
        		chapterInfo, 
        		layout, 
        new String[]{ 			 		 // Список хеш-ключей
        		CHAPTER_NAME,         	 // Название главый
        		ARTICLES_IN_CHAPTER,	 // Какие статьи есть в главе
        		CHAPTER_ID				 // Номер Главы
                }, 
        new int[]{				 		 // Значения хеш-ключей
                R.id.chapter_menu_title, // Тут id TextBox'a в list.xml
                R.id.chapter_menu_articles,
                R.id.chapter_menu_id});    
    	
        listView.setAdapter(adapter); 
        
        
        
        
        // Задаём реакцию списка глав на нажатие -----------------------------------------------------------
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                if (position==0){
                	//Нажаты ЗАКЛАДКИ
                	if (MainActivity.db.getBookmarks() == null){
                		 if (articleListView.getVisibility() == ListView.VISIBLE){
                			 articleListView.startAnimation(Animation_Slide_Out);
                     		 articleListView.setVisibility(ListView.INVISIBLE);
                		 }
                		 else
                			 Toast.makeText(ParentContext, R.string.you_have_no_bookmarks, Toast.LENGTH_SHORT).show();
                		
                		return;
                	}
                }
                
              
				if (articleListView.getVisibility() == ListView.INVISIBLE) {
					if (articleListView.getWidth() != (int) listView.getWidth() * PERCENT) {
						ViewGroup.LayoutParams params = articleListView.getLayoutParams();
						params.width = (int) (listView.getWidth() * PERCENT);
						articleListView.setLayoutParams(params);
						articleListView.requestLayout();
					}

					int use_layout;

					switch (position) {
					case 0:
						use_layout = R.layout.bookmark_list_item;
						break;
					default:
						use_layout = R.layout.article_list_item;
						break;
					}

					ViewUtils.initListViewArticles(ParentContext, articleListView, use_layout, position);
					articleListView.setVisibility(ListView.VISIBLE);
					articleListView.startAnimation(Animation_Slide_In);
					openedChapter = position;
					
				} else {
					articleListView.startAnimation(Animation_Slide_Out);
					articleListView.setVisibility(ListView.INVISIBLE);
					openedChapter = position;
				}
            }
        });
    }

	public static void initListViewArticles(Context context, ListView listView, int layout, int chapter) {

		//Поправка на меню закладок
		final int MINUS_ONE_FOR_BOOKMARKS = 1;

		if (chapter != 0) {

			listView.setAdapter(new ArrayAdapter<String>(context, layout, MainActivity.db.getArticlesTitlesByChapter(chapter - MINUS_ONE_FOR_BOOKMARKS)));

			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					articleCallback.onArticleItemClick(openedChapter - MINUS_ONE_FOR_BOOKMARKS, position, ViewUtils.DO_NOT_SLIDE);
				}
			});
		} else {

			// Если нажали вместо главы "Закладки"

			final ArrayList<Article> bookmarks = MainActivity.db.getBookmarks();

			if (bookmarks == null)
				return;

			// Создаём список, содержащий статьи в закладках
			listView.setAdapter(new BookmarksListAdapter(context, R.layout.bookmark_list_item, bookmarks));

		}

	}
	
}

    
    
