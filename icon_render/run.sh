#!/bin/bash

# Создание иконок, т.к. вручную операция довольно рутинная

inkscape --export-png=../res/drawable-ldpi/ic_launcher.png --export-width=36 --export-height=36 ./codex_icon.svg
inkscape --export-png=../res/drawable-mdpi/ic_launcher.png --export-width=48 --export-height=48 ./codex_icon.svg
inkscape --export-png=../res/drawable-hdpi/ic_launcher.png --export-width=72 --export-height=72 ./codex_icon.svg
inkscape --export-png=../res/drawable-xhdpi/ic_launcher.png --export-width=96 --export-height=96 ./codex_icon.svg

inkscape --export-png=../ic_launcher-web.png --export-width=512 --export-height=512 ./codex_icon.svg

# Создание изображения для стартовой страницы

inkscape --export-png=../res/drawable-hdpi/icon_book.png --export-width=320 --export-height=320 ./codex_book.svg
