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
package com.law.belarus.work.codex;

import java.util.ArrayList;
import java.util.HashMap;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * Utility methods for Views.
 */


public class ViewUtils {
	
	public static final String CHAPTER_NAME = "ch";
	public static final String CHAPTER_ID = "ch_id";
	public static final String ARTICLES_IN_CHAPTER = "art";
	public static int    openedChapter = -1;
	public static final float PERCENT = 0.85f;
	
	public static ArticleItemCallback articleCallback = null;
	
    public ViewUtils(ArticleItemCallback articleCallback) {
    	
    	ViewUtils.articleCallback = articleCallback; //Ссылка на вызов функции из главной активности

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
        
        //Добавляем Закладки в хеш-карту
        hm = new HashMap<String, Object>();
  	   	hm.put(CHAPTER_NAME, "    ЗАКЛАДКИ");                 
  	   	hm.put(ARTICLES_IN_CHAPTER, "     все, что сохранили");         
 	    hm.put(CHAPTER_ID, "");
  	    chapterInfo.add(hm); 
        
        
        for (Chapter chapter : chaptersList){
     	   hm = new HashMap<String, Object>();
     	   hm.put(CHAPTER_NAME, chapter.title);                 
     	   hm.put(ARTICLES_IN_CHAPTER, "ст." + chapter.firstArticle + "-" + chapter.lastArticle);         
     	   if (chapter.id < 9)
     		   hm.put(CHAPTER_ID, " " + (chapter.id+1));
     	   else
     		   hm.put(CHAPTER_ID, Integer.toString(chapter.id+1));
      
     	   chapterInfo.add(hm); 
        }
       
        SimpleAdapter adapter = new SimpleAdapter(
        		context, 
        		chapterInfo, 
        		layout, 
        		new String[]{ 			// Список хеш-ключей
        		CHAPTER_NAME,         	//Название главый
        		ARTICLES_IN_CHAPTER,	//Какие статьи есть в главе
        		CHAPTER_ID				//Номер Главы
                }, 
                new int[]{				//Значения хеш-ключей
                R.id.ch_text1,			//Тут id TextBox'a в list.xml
                R.id.ch_text2,
                R.id.ch_text3});    
    	
        listView.setAdapter(adapter); 
        
        //listView.setOnTouchListener()
        
        
        
        // Задаём реакцию списка глав на нажатие -----------------------------------------------------------
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = view.getContext();
                
                if (position==0){
                	//Нажаты ЗАКЛАДКИ
                	if (MainActivity.db.getBookmarks() == null){
                		 if (articleListView.getVisibility() == ListView.VISIBLE){
                			articleListView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out));
                     		articleListView.setVisibility(ListView.INVISIBLE);
                		 }
                		 else
                			 Toast.makeText(context, R.string.you_have_no_bookmarks, Toast.LENGTH_SHORT).show();
                		
                		return;
                	}
                }
                
              
                if (articleListView.getVisibility() == ListView.INVISIBLE){
                	if (articleListView.getWidth() != (int)listView.getWidth() * 0.85){
                		ViewGroup.LayoutParams params = articleListView.getLayoutParams();
                        params.width = (int) (listView.getWidth() * 0.85);
                        articleListView.setLayoutParams(params);
                        articleListView.requestLayout();
                	}
                	
                	int use_layout = R.layout.article_list_item;
                	
                	if (position == 0)
                		use_layout = R.layout.bookmark_list_item;

                	ViewUtils.initListViewArticles(context, articleListView, use_layout, position);
                	articleListView.setVisibility(ListView.VISIBLE);
                	articleListView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in));
                	openedChapter = position; 
                }
                else{

                		articleListView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out));
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
					articleCallback.onArticleItemClick(openedChapter - MINUS_ONE_FOR_BOOKMARKS, position);
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

    
    
