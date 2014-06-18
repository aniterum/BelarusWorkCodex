package com.law.belarus.work.codex;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.ClipboardManager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.law.belarus.work.codex.MyHorizontalScrollView.SizeCallback;

public class MainActivity extends Activity implements ArticleItemCallback {
	private MyHorizontalScrollView scrollView;
	private View menu;
	private View app;
	private ImageView btnSlide;
	public static ListView articlesListView;
	public static ListView chapterListView;
	private ViewPager swipePageView;	//Контейнер статей главы
	private LinearLayout container;
	private TextView chapterCaption;

	private static ImageView menuButton = null;
	private static ScrollView docInfo = null;

	int btnWidth;
	// public static DataContainer data = null;
	private static ViewUtils viewUtils;

	public static int NOTHING_OPENED = -1;
	public static int OPENED_DOC_INFO = -2;

	private static int openedChapter = NOTHING_OPENED;
	private static int openedArticleInChapter = NOTHING_OPENED;

	public static LayoutInflater inflater = null;

	public static int scrollToViewIdx = 1;

	private static boolean isMenuVisible = false;

	private static String TAG = "t";

	private static final String DB_NAME = "codex.db";
	public static String FILES_DIR;
	public static DatabaseAccess db;

	private static final String VOTE_URL = "https://docs.google.com/spreadsheet/viewform?formkey=dFFtU3RwT2FIUjNqOHZDWVhSc09NSWc6MQ#gid=0";

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

		menuButton = (ImageView) app.findViewWithTag(TAG);
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

		container = (LinearLayout) app.findViewById(R.id.pagesContainer);
		chapterCaption = (TextView) app.findViewById(R.id.caption_text);

		viewUtils = new ViewUtils(this);

		chapterListView = (ListView) menu.findViewById(R.id.list);

		viewUtils.initListViewChapters(this, chapterListView, R.layout.simple_list_item_with_chapter_num, articlesListView);

		if (openedChapter == OPENED_DOC_INFO)
			loadDocInfo();
		else if (openedChapter != NOTHING_OPENED) {
			chapterCaption.setText(db.getChapterTitleById(openedChapter));
			loadPages(openedChapter, openedArticleInChapter);
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
			if ((openedArticleInChapter == NOTHING_OPENED)	| (openedArticleInChapter == OPENED_DOC_INFO))
				Toast.makeText(this, R.string.why_do_you_want_this,	Toast.LENGTH_LONG).show();
			else {
				int id = db.addBookmark(openedChapter, swipePageView.getCurrentItem());
				if (id == DatabaseAccess.BOOKMARK_ALREADY_EXISTS)
					Toast.makeText(this, R.string.bookmark_already_exists,Toast.LENGTH_LONG).show();
				else
					Toast.makeText(this, R.string.bookmark_added,Toast.LENGTH_LONG).show();
			}
			break;
		}

		// == Меню - Отправить сообщение разработчику ==
		case R.id.menu_email: {
			Intent intent = new Intent(this, EmailActivity.class);
			startActivityForResult(intent, 0);
			break;
		}

		// == Меню - Голосование ==
		case R.id.menu_vote: {
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(VOTE_URL));
			startActivity(i);
			break;
		}

		// == Меню - О Документе ==
		case R.id.menu_info: {
			this.loadDocInfo();
			break;
		}

		// == Меню - Копировать текст статьи ==
		case R.id.menu_text_copy: {
			try {
				if ((openedArticleInChapter == NOTHING_OPENED)	| (openedArticleInChapter == OPENED_DOC_INFO))
					Toast.makeText(this, R.string.can_not_copy_this, Toast.LENGTH_LONG).show();
				else{
					
					String textToCopy = db.getArticleTextByChapterAndOffset(openedChapter, swipePageView.getCurrentItem());
					
					if (textToCopy != null){
						ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						clipboard.setText(textToCopy);
						Toast.makeText(this, R.string.text_copy_msg,Toast.LENGTH_LONG).show();
					}
					else 
						Toast.makeText(this, R.string.text_copy_msg_err,Toast.LENGTH_LONG).show();
					
					
				}
				
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
					articlesListView.startAnimation(AnimationUtils
							.loadAnimation(this, R.anim.slide_out));
					articlesListView.setVisibility(ListView.INVISIBLE);
				} else
					btnSlide.performClick();
			} else {

				super.onKeyDown(keyCode, event);
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
	 * Наполняем массив для передачи его конструктору слайдера
	 * 
	 * @param pages - массив, в который нужно добавлять текст статей
	 * @param inflater
	 * @param layot
	 * @param view
	 * @param chapter - нужная глава
	 */
	public void makePages(List<View> pages, LayoutInflater inflater, int layot, int view, int chapter) {

		final String ARTICLE_START = "<b><i>Статья ";
		final String ARTICLE_FIN = ". ";
		final String ARTICLE_FIN2 = ".</i></b><br><br>";

		ArrayList<Article> articlesInChapter = db.getArticlesByChapter(chapter);

		for (Article article : articlesInChapter) {
			
			View page = inflater.inflate(layot, null);
			TextView textView = (TextView) page.findViewById(view);
			textView.setText(Html.fromHtml(ARTICLE_START + article.id    + ARTICLE_FIN
												         + article.title + ARTICLE_FIN2
												         + article.text.replace("\n", "<br><br>")));

			pages.add(page);
		}

		articlesInChapter.clear();

	}

	/**
	 * Создание слайдера со статьями из главы
	 * 
	 * @param chapter - Глава, которую необходимо загрузить
	 * @param articleIndex - Номер статьи внутри главы
	 */
	public void loadPages(int chapter, int articleIndex) {

		List<View> pages = new ArrayList<View>();
		makePages(pages, inflater, R.layout.article_page, R.id.articleTextPageView, chapter);

		SamplePagerAdapter pagerAdapter = new SamplePagerAdapter(pages);

		swipePageView = new ViewPager(this);
		swipePageView.setAdapter(pagerAdapter);
		swipePageView.setCurrentItem(articleIndex);

		container.removeAllViews();

		container.addView(swipePageView);
	}

	
	
	public void loadDocInfo() {

		openedChapter = OPENED_DOC_INFO;
		openedArticleInChapter = NOTHING_OPENED;

		docInfo = (ScrollView) inflater.inflate(R.layout.article_page, null);
		
		((TextView) docInfo.getChildAt(0)).setText(Html.fromHtml(db.getDocumentInfo().replace("\n", "<br>")));

		container.removeAllViews();
		container.addView(docInfo);
		chapterCaption.setText(getResources().getString(R.string.menu_info));
	}
	
	

	/**
	 * Загружает главный экран с иконкой приложения
	 */
	public void loadStartScreen() {

		LayoutInflater docInfo = LayoutInflater.from(this);
		LinearLayout l = (LinearLayout) docInfo.inflate(R.layout.start_screen, null);

		container.removeAllViews();
		container.addView(l);
	}

	
	/**
	 * Реакция на выбор статьи в списке
	 */
	public void onArticleItemClick(int chapter, int article) {
		// Изменяем заголовок окна
		chapterCaption.setText(db.getChapterTitleById(chapter));

		if (chapter == openedChapter) {
			// Переход необходим внутри текущей главы
			swipePageView.setCurrentItem(article);
		} else {
			// Переход в другую статью
			openedChapter = chapter;
			loadPages(chapter, article);
			openedArticleInChapter = article;
		}

		// Закрываем списко глав и статей
		btnSlide.performClick();
	}

	
	public void openMenu() {
		this.openOptionsMenu();
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
