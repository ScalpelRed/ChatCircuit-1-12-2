call gradlew build

chcp 65001

set source=D:\nonascicase\mcmods\stringprocessor\1-12-2\build\libs\chatcircuit-1.0-1.12.2.jar
set destination=D:\Федор_файлы\игры\MultiMC\instances\Для моддинга 1.12.2\.minecraft\mods\

copy "%source%" "%destination%"

set message="Копирование завершено, игру нужно запустить самому."
msg /W * %message%