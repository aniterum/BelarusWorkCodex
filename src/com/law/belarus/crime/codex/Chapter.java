package com.law.belarus.crime.codex;

/**
 * Класс - контейнер информации о конкретной главе
 * 
 */
public class Chapter {
	public final int id;
	public final String title;
	public final int firstArticle;
	public final int lastArticle;

/**
 * Конструктор класса
 * @param id - Порядковый номер главы
 * @param title - Название главы
 * @param firstArticle - Номер первой статьи в главе
 * @param lastArticle - Номер последней статьи в главе
 */
	Chapter(int id, String title, int firstArticle, int lastArticle){
		
		this.id = id;
		this.title = title;
		this.firstArticle = firstArticle;
		this.lastArticle = lastArticle;
		
	}
}
