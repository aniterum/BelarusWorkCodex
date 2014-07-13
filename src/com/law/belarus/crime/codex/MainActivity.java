package com.law.belarus.crime.codex;

import java.util.ArrayList;
import java.util.List;
 
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.law.belarus.crime.codex.MyHorizontalScrollView.SizeCallback;

public class MainActivity extends Activity implements ArticleItemCallback {
	private static MyHorizontalScrollView scrollView;
	private static View menu;
	private static View app;
	private static ImageView btnSlide;
	public  static ListView  articlesListView;
	public  static ListView  chapterListView;
	private static ViewPager swipePageView;	//Контейнер статей главы
	private static TextView  chapterCaption;
	private static LinearLayout pagesContainer;
	private static ImageView menuButton = null;
	private static ScrollView docInfo = null;

//	private int btnWidth;
	private static ViewUtils viewUtils;

	public static final int NOTHING_OPENED = -1;
	public static final int OPENED_DOC_INFO = -2;

	private static int openedChapter = NOTHING_OPENED;
	private static int openedArticleInChapter = NOTHING_OPENED;

	public static LayoutInflater inflater = null;

	public static int scrollToViewIdx = 1;

	private static boolean isMenuVisible = false;

	

	private static final String DB_NAME = "codex.db";
	public static String FILES_DIR;
	public static DatabaseAccess db;
	

	//private static final String VOTE_URL = "https://docs.google.com/spreadsheet/viewform?formkey=dFFtU3RwT2FIUjNqOHZDWVhSc09NSWc6MQ#gid=0";
	private static final String MENU_BUTTON_TAG = "MENU_BUTTON_TAG";
	
	private static final Time time = new Time();
	private static long lastBackButtonPress = 0;
	public static final int BACK_PRESS_TIME = 2000;
	
	public static final int BLACK_TEXT_ON_WHITE = 1;
	public static final int WHITE_TEXT_ON_BLACK = 2;
	
	public static int COLOR_SCHEME = 2;
	 
	public final String BR = "<br>";
	
	public static final String BI_START = "<b><i>";
	public static final String BI_END = "</i></b>";
	public final String ARTICLE_START = BI_START + "Статья ";
	public final String ARTICLE_FIN = ". ";
	public final String ARTICLE_FIN2 = ARTICLE_FIN + BI_END + BR + BR;
	
	public final String TEXT_ITEM_TAG = "textPageView";
	public final String BUTTON_NEXT_TAG = "go_next_chapter_button";
	public final String CAPTION_NEXT_TAG = "go_next_chapter_caption";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FILES_DIR = getBaseContext().getFilesDir().getPath() + "/" + DB_NAME;

		if (db == null)
			db = new DatabaseAccess(FILES_DIR, this.getResources());

		if (inflater == null)
			inflater = LayoutInflater.from(this);


		scrollView = (MyHorizontalScrollView) inflater.inflate(R.layout.horz_scroll_with_list_menu, null);

		menu = inflater.inflate(R.layout.horz_scroll_menu, null);
		app = inflater.inflate(R.layout.horz_scroll_app_with_articles, null);

		menuButton = (ImageView) app.findViewWithTag(MENU_BUTTON_TAG);
		menuButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				openMenu();
			}

		});

		ViewGroup tabBar = (ViewGroup) app.findViewById(R.id.tabBar);

		articlesListView = (ListView) menu.findViewById(R.id.articles_list);
		articlesListView.setVisibility(ListView.INVISIBLE);

		btnSlide = (ImageView) tabBar.findViewById(R.id.BtnSlide);
		btnSlide.setOnClickListener(new ClickListenerForScrolling(scrollView, menu, articlesListView));

		final View[] children = new View[] { menu, app };

		// Scroll to app (view[1]) when layout finished.
		scrollView.initViews(children, scrollToViewIdx, new SizeCallbackForMenu(btnSlide));

		// ===========================================================================================//

		setContentView(scrollView);

		pagesContainer = (LinearLayout) app.findViewById(R.id.pagesContainer);
		chapterCaption = (TextView) app.findViewById(R.id.caption_text);

		viewUtils = new ViewUtils(this, this);

		chapterListView = (ListView) menu.findViewById(R.id.list);

		viewUtils.initListViewChapters(this, chapterListView, R.layout.simple_list_item_with_chapter_num, articlesListView);

		if (openedChapter == OPENED_DOC_INFO)
			loadDocInfo();
		else if (openedChapter != NOTHING_OPENED) {
			chapterCaption.setText(db.getChapterTitleById(openedChapter));
			loadPages(openedChapter, openedArticleInChapter, ViewUtils.DO_NOT_SLIDE);
		}

	}

	/**
	 * Helper for examples with a HSV that should be scrolled by a menu View's
	 * width.
	 */
	static class ClickListenerForScrolling implements OnClickListener {
		final HorizontalScrollView scrollView;
		final View menu;
		final ListView listViewArticles;
		/**
		 * Menu must NOT be out/shown to start with.
		 */
		boolean menuOut = false;

		public ClickListenerForScrolling(HorizontalScrollView scrollView,
				View menu, ListView listViewArticles) {
			super();
			this.scrollView = scrollView;
			this.menu = menu;
			this.listViewArticles = listViewArticles;
		}

		public void onClick(View v) {

			int menuWidth = menu.getMeasuredWidth();

			// Ensure menu is visible
			menu.setVisibility(View.VISIBLE);

			if (!menuOut) {
				// Scroll to 0 to reveal menu
				int left = 0;
				scrollView.smoothScrollTo(left, 0);
			} else {
				// Scroll to menuWidth so menu isn't on screen.
				int left = menuWidth;
				scrollView.smoothScrollTo(left, 0);
			}
			menuOut = !menuOut;

			isMenuVisible = menuOut;


			if (menuOut)
				listViewArticles.setVisibility(ListView.INVISIBLE);

		}
	}

	/**
	 * Helper that remembers the width of the 'slide' button, so that the
	 * 'slide' button remains in view, even when the menu is showing.
	 */
	static class SizeCallbackForMenu implements SizeCallback {
		int btnWidth;
		View btnSlide;

		public SizeCallbackForMenu(View btnSlide) {
			super();
			this.btnSlide = btnSlide;
		}

		public void onGlobalLayout() {
			btnWidth = btnSlide.getMeasuredWidth();
			// System.out.println("btnWidth=" + btnWidth);
		}

		public void getViewSize(int idx, int w, int h, int[] dims) {
			dims[0] = w;
			dims[1] = h;
			final int menuIdx = 0;
			if (idx == menuIdx) 
				dims[0] = w - btnWidth;
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity_menu, menu);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * Реакция на нажатие какого-либо пункта меню
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		switch (item.getItemId()) {

		// == Меню - Добавить закладку ==
		case R.id.menu_add_bookmark: {
			
				if (swipePageView == null){
					makeToast(R.string.why_do_you_want_this);
					return true;
				}

				SamplePagerAdapter adapter = (SamplePagerAdapter) swipePageView.getAdapter();
				Object tag = adapter.pages.get(swipePageView.getCurrentItem()).getTag();
				int articleId = Integer.valueOf(tag.toString());
				
				if (articleId != NOTHING_OPENED){
					
					if (db.addBookmark(articleId) == DatabaseAccess.BOOKMARK_ALREADY_EXISTS)
						makeToast(R.string.bookmark_already_exists);
					else
						makeToast(R.string.bookmark_added);
					
				}
				else {
					makeToast(R.string.why_do_you_want_this);
			}
			break;
		}

		// == Меню - Отправить сообщение разработчику ==
		case R.id.menu_email: {
			Intent intent = new Intent(this, EmailActivity.class);
			startActivityForResult(intent, 0);
			break;
		}
		
		// == Меню - Изменить цветовую схему ==
		case R.id.menu_color_toggle: {

			ColorToggle();

			break;
		}

//		// == Меню - Голосование ==
//		case R.id.menu_vote: {
//			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(VOTE_URL));
//			startActivity(i);
//			break;
//		}

		// == Меню - О Документе ==
		case R.id.menu_info: {
			this.loadDocInfo();
			break;
		}

		// == Меню - Копировать текст статьи ==
		case R.id.menu_text_copy: {
			try {
				
				if (swipePageView == null){
					makeToast(R.string.can_not_copy_this);
					return true;
				}
					
					SamplePagerAdapter adapter = (SamplePagerAdapter) swipePageView.getAdapter();
					Object tag = adapter.pages.get(swipePageView.getCurrentItem()).getTag();
					int articleId = Integer.valueOf(tag.toString());

					if (articleId != NOTHING_OPENED) {

						String textToCopy = db.getArticleTextById(articleId);

						if (textToCopy != null) {
							ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
							clipboard.setText(textToCopy);
							makeToast(R.string.text_copy_msg);
						} else
							makeToast(R.string.text_copy_msg_err);

					}
					else 
						makeToast(R.string.can_not_copy_this);
					
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}

		}

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// Нажата кнопка НАЗАД
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (isMenuVisible) {
				if (articlesListView.getVisibility() == ListView.VISIBLE) {
					articlesListView.startAnimation(ViewUtils.Animation_Slide_Out);
					articlesListView.setVisibility(ListView.INVISIBLE);
				} else {

					btnSlide.performClick();

				}
					
			} else {

				//Нажата кнопка НАЗАД при свернутом меню статей
				time.setToNow();

				if ((time.toMillis(true) - lastBackButtonPress) < BACK_PRESS_TIME) {
					super.onKeyDown(keyCode, event);
				} else {
					lastBackButtonPress = time.toMillis(true);
					makeToast(R.string.press_back_for_exit);
				}

			}

		}

		// Нажата кнопка МЕНЮ
		if (keyCode == KeyEvent.KEYCODE_MENU) {

			// Если открыт список, то меню открывать не будем
			if (!isMenuVisible)
				this.openOptionsMenu();
		}

		return true;
	}

	/**
	 * Создает OnClickListener для кнопки перехода
	 * @param currentChapter
	 * @param goForward - true-для перехода вперед, false-дназад
	 * @return Обработчик нажатия на кнопку перехода к другой главе из текущей
	 */
	private OnClickListener nextChapterOnClick(final int currentChapter, final boolean goForward) {

		final int goToChapter;
		final int goToArticle;

		if (goForward) {
			goToChapter = currentChapter + 1;
			goToArticle = 0;
		} else {
			goToChapter = currentChapter - 1;
			goToArticle = -1;
		}

		return new OnClickListener() {

			public void onClick(View v) {
				if (goForward)
					onArticleItemClick(goToChapter, goToArticle, ViewUtils.SLIDE_IN);
				else
					onArticleItemClick(goToChapter, goToArticle, ViewUtils.SLIDE_OUT);

			}

		};
	}
	
	/**
	 * Наполняем массив для передачи его конструктору слайдера
	 * 
	 * @param pages - массив, в который нужно добавлять текст статей
	 * @param inflater
	 * @param layot
	 * @param view
	 * @param chapter - нужная глава
	 */
	public void makePages(List<View> pages, LayoutInflater inflater, int pageLayout, int nextChapterLayout, int chapter) {
		
		int backGroundColor = Color.WHITE;
		int textColor = Color.BLACK;
		
		switch (COLOR_SCHEME){
		case BLACK_TEXT_ON_WHITE : 
			backGroundColor = Color.WHITE;
			textColor = Color.BLACK;
			break;
		case WHITE_TEXT_ON_BLACK :
			backGroundColor = Color.BLACK;
			textColor = Color.WHITE;
			break;
		}
		
		ArrayList<Article> articlesInChapter = db.getArticlesByChapter(chapter);
	
		//Добавляем переход на предыдущую главу, только если глава не первая
		if (chapter != 0){
			View goPrevChapterItem = inflater.inflate(nextChapterLayout, null);
			goPrevChapterItem.setTag(NOTHING_OPENED);
			ImageButton prevChapter = (ImageButton) goPrevChapterItem.findViewWithTag(BUTTON_NEXT_TAG);
			
			TextView caption = (TextView) goPrevChapterItem.findViewWithTag(CAPTION_NEXT_TAG);
			
			caption.setText(R.string.go_prev_chapter);
			
			prevChapter.setOnClickListener(nextChapterOnClick(chapter, false));
			
			//Цвет фона и букв
			goPrevChapterItem.setBackgroundColor(backGroundColor);
			caption.setTextColor(textColor);
			
			pages.add(goPrevChapterItem);
		}

		for (Article article : articlesInChapter) {
			
			View page = inflater.inflate(pageLayout, null);
			TextView textView = (TextView) page.findViewWithTag(TEXT_ITEM_TAG);
			
			//Цвет фона и текста
			textView.setTextColor(textColor);
			page.setBackgroundColor(backGroundColor);

			if ((article.id > DatabaseAccess.MIN_NOTE_ID) & (article.id < DatabaseAccess.MAX_NOTE_ID))
				textView.setText(Html.fromHtml(BI_START + article.title + ARTICLE_FIN2
				         								+ article.text.replace("\n", BR + BR)));
			else
				textView.setText(Html.fromHtml(ARTICLE_START + article.id    + ARTICLE_FIN
												             + article.title + ARTICLE_FIN2
												             + article.text.replace("\n", BR + BR)));
			
			//Для создания закладок и копирования текста
			page.setTag(article.id);

			pages.add(page);
		}
		
		//Добавляем переход на следующую главу, если только эта глава не последняя
		if (chapter + 1 != DatabaseAccess.CHAPTERS_COUNT) {
			View goNextChapterItem = inflater.inflate(nextChapterLayout, null);
			goNextChapterItem.setTag(NOTHING_OPENED);
			ImageButton nextChapter = (ImageButton) goNextChapterItem.findViewWithTag(BUTTON_NEXT_TAG);
			nextChapter.setImageResource(R.drawable.ic_menu_forward);
			
			
			TextView caption = (TextView) goNextChapterItem.findViewWithTag(CAPTION_NEXT_TAG);
			
			caption.setText(R.string.go_next_chapter);
			
			nextChapter.setOnClickListener(nextChapterOnClick(chapter, true));
			
			//Цвет фона и букв
			goNextChapterItem.setBackgroundColor(backGroundColor);
			caption.setTextColor(textColor);
			
			pages.add(goNextChapterItem);
		}
		

		articlesInChapter.clear();

	}

	/**
	 * Создание слайдера со статьями из главы
	 * 
	 * @param chapter - Глава, которую необходимо загрузить
	 * @param articleIndex - Номер статьи внутри главы
	 */
	public void loadPages(int chapter, int articleIndexOrMinusOneForLastItem, int animation) {
		
		final int ONE_BECAUSE_WE_HAVE_ADDITIONAL_TRANSFER_PAGES = 1;

		List<View> pages = new ArrayList<View>();
		makePages(pages, inflater, R.layout.article_page, R.layout.go_next_chapter_swipe_item, chapter);

		SamplePagerAdapter pagerAdapter = new SamplePagerAdapter(pages);

		swipePageView = new ViewPager(this);
		swipePageView.setAdapter(pagerAdapter);
	
		/*В первой главе не добавляется переход на предыдущую,
		  поэтому и нет необходимости добавлять единицу к индексу  */
		
		switch (articleIndexOrMinusOneForLastItem) {
		case -1:
			if (chapter != DatabaseAccess.CHAPTERS_COUNT - 1)
				swipePageView.setCurrentItem(pages.size() - ONE_BECAUSE_WE_HAVE_ADDITIONAL_TRANSFER_PAGES * 2);
			else
				swipePageView.setCurrentItem(pages.size());
			break;
			
		default:
			if (chapter != 0)
				swipePageView.setCurrentItem(articleIndexOrMinusOneForLastItem + ONE_BECAUSE_WE_HAVE_ADDITIONAL_TRANSFER_PAGES);
			else
				swipePageView.setCurrentItem(articleIndexOrMinusOneForLastItem);
			break;
		}		

		pagesContainer.removeAllViews();

		pagesContainer.addView(swipePageView);
		
//		articlesListView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
		switch (animation){
		case -1: swipePageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.chapter_slide_out)); break;
		case  1: swipePageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.chapter_slide_in)); break;
		}
		
		
	}

	
	/*
	 * Выводим на экран текст изменений закона
	 */
	public void loadDocInfo() {

		openedChapter = OPENED_DOC_INFO;
		openedArticleInChapter = NOTHING_OPENED;
		
		swipePageView = null;

		docInfo = (ScrollView) inflater.inflate(R.layout.article_page, null);
		
		TextView text = (TextView)docInfo.findViewWithTag(TEXT_ITEM_TAG);
		text.setText(Html.fromHtml(db.getDocumentInfo().replace("\n", "<br>")));
		
		switch (COLOR_SCHEME){
		case BLACK_TEXT_ON_WHITE : 
			text.setTextColor(Color.BLACK);
			docInfo.setBackgroundColor(Color.WHITE);
			break;
		case WHITE_TEXT_ON_BLACK :
			
			break;
		}

		pagesContainer.removeAllViews();
		pagesContainer.addView(docInfo);
		chapterCaption.setText(getResources().getString(R.string.menu_info));
	}
	
	

	/**
	 * Загружает главный экран с иконкой приложения
	 */
	public void loadStartScreen() {

		LayoutInflater docInfo = LayoutInflater.from(this);
		LinearLayout l = (LinearLayout) docInfo.inflate(R.layout.start_screen, null);

		pagesContainer.removeAllViews();
		pagesContainer.addView(l);
	}

	
	/**
	 * Реакция на выбор статьи в списке
	 */
	public void onArticleItemClick(int chapter, int article, int useSlide) {

		if (chapter == openedChapter) {
			// Переход необходим внутри текущей главы
			
			switch (chapter){
			case 0: swipePageView.setCurrentItem(article); break;
			// Поправка на страницы перехода в другие главы
			default: swipePageView.setCurrentItem(article + 1); break;
			}

		} else {
			// Переход в другую статью
			openedChapter = chapter;
			// Изменяем заголовок окна
			chapterCaption.setText(db.getChapterTitleById(chapter));
			loadPages(chapter, article, useSlide);
			openedArticleInChapter = article;
		}

		// Закрываем список глав и статей, если он виден
		if (isMenuVisible)
			btnSlide.performClick();
	}

	
	public void openMenu() {
		this.openOptionsMenu();
	}
	
	/**
	 * Выводит сообщение на экран пользователя
	 * @param resId - идентификатор ресурса в R
	 */
	public void makeToast(int resId){
		Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
	}

/**
 * Переключает цветовую схему и меняет цвета всех страниц
 */
	public void ColorToggle(){
		
		if (swipePageView == null)
			return;
		
		//Переключаем цветовую схему
		if (COLOR_SCHEME == BLACK_TEXT_ON_WHITE)
			COLOR_SCHEME = WHITE_TEXT_ON_BLACK;
		else
			COLOR_SCHEME = BLACK_TEXT_ON_WHITE;
		
		int backGroundColor = Color.WHITE;
		int textColor = Color.BLACK;
		
		switch (COLOR_SCHEME){
		case BLACK_TEXT_ON_WHITE : 
			backGroundColor = Color.WHITE;
			textColor = Color.BLACK;
			break;
		case WHITE_TEXT_ON_BLACK :
			backGroundColor = Color.BLACK;
			textColor = Color.WHITE;
			break;
		}
		
		swipePageView.setBackgroundColor(backGroundColor);
		pagesContainer.setBackgroundColor(backGroundColor);
		SamplePagerAdapter adapter = (SamplePagerAdapter) swipePageView.getAdapter();

		for (View v : adapter.pages) {
			TextView textView = (TextView) v.findViewWithTag(TEXT_ITEM_TAG);

			if (textView != null) {
				// Цвет фона и текста

				textView.setTextColor(textColor);
				v.setBackgroundColor(backGroundColor);

			} else {
				TextView caption = (TextView) v.findViewWithTag(CAPTION_NEXT_TAG);
				if (caption != null) {
					v.setBackgroundColor(backGroundColor);
					caption.setTextColor(textColor);
				}
			}
		}
		
		adapter.notifyDataSetChanged();
	}
	
	// @Override
	// protected void onPause() {
	// Log.i("OnPause", "Уход на задний план");
	// DatabaseAccess.base.close();
	// db = null;
	//
	// super.onPause();
	// }

	// @Override
	// protected void onResume() {
	//
	// if (db == null){
	// Log.i("OnPause", "Перезагрузка базы данных");
	// db = new DatabaseAccess(FILES_DIR, this.getResources());
	// }
	// super.onResume();
	// }

}
