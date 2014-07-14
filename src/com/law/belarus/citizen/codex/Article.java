package com.law.belarus.citizen.codex;

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
	public final String extra_id;
	
/**
 * Конструктор класса
 * @param id - Номер главы
 * @param chapter - Глава, в которой эта статья находится
 * @param title - Название статьи
 * @param text - Текст статьи
 */
	Article(int id, int chapter, String title, String text, int offset, String extra_id){
		
		this.id = id;
		this.chapter = chapter;
		this.title = title;
		this.text = text;
		this.offset = offset;
		this.extra_id = extra_id;
		
		
	}
	
}
