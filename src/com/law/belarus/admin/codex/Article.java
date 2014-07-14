package com.law.belarus.admin.codex;

/**
 * Класс - хранилище данных о статьях
 *
 */
public class Article {
	public final int id;
	public final int chapter;
	public final String title;
	public final String text;
	public final int offset;
	
/**
 * Конструктор класса
 * @param id - Номер главы
 * @param chapter - Глава, в которой эта статья находится
 * @param title - Название статьи
 * @param text - Текст статьи
 */
	Article(int id, int chapter, String title, String text, int offset){
		
		this.id = id;
		this.chapter = chapter;
		this.title = title;
		this.text = text;
		this.offset = offset;
		
		
	}
	
}
