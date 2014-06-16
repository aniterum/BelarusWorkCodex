# coding: utf-8

codex_file = "codex.txt"

with open(codex_file, "rt") as codex:
    allLines = codex.readlines()

divisor = []

#Разбиваем весь кодекс на части Инфо, Разделы, Статьи по строке "<!>"
for line_idx in range(len(allLines)):
    if allLines[line_idx].startswith("<!>"):
        divisor.append(line_idx)

codex_info = [line for line in allLines[:divisor[0]]] #if (line != "\n") ]
chapters   = [line.strip() for line in allLines[divisor[0]+1:divisor[1]] if (line != "\n")]
articles   = [line.strip() for line in allLines[divisor[1]+1:] if line != line.upper() ]

#===========================================================================================

#Получаем номер строк, в которых есть слово ГЛАВА или РАЗДЕЛ VI. (это заключительные положения)
chapters_idx = []
for line_idx in range(len(chapters)):
    if (chapters[line_idx].startswith("ГЛАВА") or chapters[line_idx].startswith("РАЗДЕЛ VI.")):
        chapters_idx.append(line_idx)

#С помощью генератора получаем список имён всех глав
chapters_names_temp = [chapters[idx][chapters[idx].find(". ")+2:] for idx in chapters_idx]
#Помещаем все имена в словарь, добавляя порядковый номер
chapters_names = [{"id":i, "title":chapters_names_temp[i]} for i in range(len(chapters_names_temp))]


articles_in_chapters_list = []
for idx in chapters_idx:
    try:
        #Разделяем названия статей, относящиеся к разным главам
        articles_index = chapters[idx+1:chapters_idx[chapters_idx.index(idx)+1]]
    except IndexError:
        #Это для последней главы, т.к. за ним нет больше глав
        articles_index = chapters[idx+1:]

    #Создаем list, состоящий из сгруппированных в list названиях статей
    articles_in_chapters_list.append(articles_index)


chapterID = 0
chapters_dict = {}
for articles_ in articles_in_chapters_list:
    offset = 0
    for art in articles_:
        idx = art.split(".")[0].split(" ")[1]
        #Добавляем в dict соотношение {ID статьи : [ID главы, сдвиг в главе]}
        chapters_dict[idx] = [str(chapterID), offset]
        offset += 1

     
    chapterID += 1
    


#===========================================================================================


articles_idx = []
for line_idx in range(len(articles)):
    if articles[line_idx].startswith("Статья"):
        articles_idx.append(line_idx)


articles_list_dict = []
for line_idx in articles_idx:

    dot = articles[line_idx].find(".")
    article_name = articles[line_idx][:dot]
    article_title = articles[line_idx][dot+1:]
    article_id = article_name.split(" ")[1]
    try:
        test_id = int(article_id)
    except:
        print("Error int convertation " + article_id)

 

    try:
        text = "\n".join(articles[line_idx+1 : articles_idx[articles_idx.index(line_idx) + 1]])
    except IndexError:
        #print(line_idx)
        text = "\n".join(articles[line_idx+1:])

    ex = text.find("\nГлава")
    if ex != -1:
        text = text[:ex]

    
    
    articles_list_dict.append({"id":article_id, "chapter":chapters_dict[article_id][0], "title":article_title.strip(), "text":text, "offset":chapters_dict[article_id][1]})

#===========================================================================================

# Начинаем формирование sqlite базы данных
import sqlite3
import os

#Удаляем имеющийся файл

dbFileName = codex_file[:codex_file.find(".")]+".sqlite"

os.system("rm ./" + dbFileName)

db = sqlite3.connect(dbFileName)
cur = db.cursor()

CREATE_TABLE_ARTICLES = "CREATE TABLE Articles (ID UNSIGNED INT PRIMARY KEY, CHAPTER UNSIGNED INT, TITLE TINYTEXT, ARTICLE_TEXT TEXT, IN_BOOKMARKS INT, OFFSET INT)"
CREATE_TABLE_CHAPTERS = "CREATE TABLE Chapters (ID UNSIGNED INT, TITLE TINYTEXT)"
CREATE_TABLE_DOCINFO  = "CREATE TABLE Docinfo  (ID UNSIGNED INT, TITLE TINYTEXT, INFO TEXT, VERSION TINYTEXT)"

INSERT_ARTICLE = "INSERT INTO Articles VALUES ('%(id)s', '%(chapter)s', '%(title)s', '%(text)s', '0', '%(offset)s')"
INSERT_CHAPTER = "INSERT INTO Chapters VALUES ('%(id)s', '%(title)s')"
INSERT_DOCINFO = "INSERT INTO Docinfo  VALUES ('%(zero)s', '%(title)s', '%(text)s', '%(version)s')"


cur.execute(CREATE_TABLE_ARTICLES)
cur.execute(CREATE_TABLE_CHAPTERS)
cur.execute(CREATE_TABLE_DOCINFO)

for article in articles_list_dict:
    cur.execute(INSERT_ARTICLE % article)

for chapter in chapters_names:
    cur.execute(INSERT_CHAPTER % chapter)

import time, datetime, hashlib

cur_time = str(datetime.datetime.toordinal(datetime.datetime.now())) + "_" + str(time.time())
version = hashlib.sha1(cur_time.encode()).hexdigest()

cur.execute(INSERT_DOCINFO % {"zero":"0", "title":"О документе", "text":"".join(codex_info).strip(), "version":version})

db.commit()
db.close()

#Автоматическое обновление файла строк с текущим временем создания базы данных

file = "../res/values/strings.xml"
template = "    <string name=\"db_version\">%s</string>\n"

result = []

with open(file, "rt") as str_file:
    for line in str_file:
        if (line.find("db_version") != -1):
            result.append(template % version)
        else:
            result.append(line)

open(file, "wt").write("".join(result))

os.system("cp -b ./codex.sqlite ../res/raw/codex.db")
