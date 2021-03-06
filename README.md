# testCSV2

Немного отревьюированная версия. 
Имена переменных не менял, у функций - косметические mapResultFillUp на  mapResultFillIn (так все же правильнее fill in - "заполнить") и 
makerFinalList на makeFinalList. 

Изменения - порядок замены в очереди цен и результирующей map при "попадании" цены "нового" продукта в количество отобранных: 
  - было: заносим цену в  очередь цен, затем заносим продукт в результирующий map;
  - стало: заносим продукт в результирующий map, если удачно ->  заносим цену в  очередь цен.

Переменные :
  delimiter - разделитель данных в файлах с первичными данными;
  waitTime - максимальное время ожидания работы программы, по истечении которого, она попытается завершить исполнение, в минутах;
  maxSizeMap - максимальное количество продуктов в результирующем файле, по заданию 1000;
  maxRep - максимальное количество продуктов с одинаковым ID, по заданию 20
  (Utility Result:
                Output CSV file that meets the following criteria:
                no more than 1000 products sorted by Price from all files;
                no more than 20 products with the same ID.
  )  
  
  directoryPath - путь к файлам с данным;
  outDirName - путь для записи результирующего файла.
  
Функции:
  класс UI(работа с консолью и диском)
    uiAnswer и uiNumberAnswer - запрос у оператора данных для даботы с утилитой (delimiter, directoryPath, outDirName, waitTime, maxSizeMap, maxRep);
    lokForDir -  проверка на существования директории с файлами данных;
    filesList - создание списка файлов для дальнейшей обработки, результат - список файлов;
    makerToMap - форммирует список(очередь) из maxSizeMap(максимальный размер) элементов с самой низкой из текущего(обрабатываемого) файла, результат - очередь;
    dataOutput - запись результирующего файла на диск, результат - удачно или нет.
    
  класс Least(обработка файла в Tread и формирование результирующего списка)
    leastN -  выбирает из полностью считанного в буфер файла maxSizeMap элементов с самой низкой ценой во временную очередь
            (допущения: в файле нет элементов с одинаковым ID, проверка на их наличие не ведется), результат - очередь;
    checkAndChangeBean - делает попытку внести продукт в "ветку" результирующей map
            ("ветка" : ключ - ID продукта, значения  - очередь размером не более maxRep элементов с таким же ID), результат - удалось или нет
    mapResultFillIn - создание или попытка заполнения существующих "веток" в результирующей map с проверкой через checkAndChangeBea , результат - удачно или нет;   
    makeFinalList - получает из makerToMap данные и пытается занести их в результирующий map, а цены в очередь цен.
    
  класс ImportCsv
    processDirectory - запуск утилит для запроса параметров работы и формирование пула Tread'ов для заполнения результирующей map,
            выгрузка данных в результирующий список и запуск утилиты записи результирующего файла на диск;
    main - запуск программы.  
  

Описание алгоритма работы:

  1. Запрос разделителя данных(delimiter), параметров для доступа к входящим данным (directoryPath).       
      
  2. lokForDir проверяет на существование указанного оператором пути (directoryPath), если папка существует - запускает основную утилиту(processDirectory),
      если нет - выводит соответствующее сообщение.
      
  3. processDirectory запускает утилиту filesList, котораю формирует список файлов для обработки. Если список пустой - обработка заканчивается. Если список
    не пустой - запрос дополнительных данных для работы: времени ожидания работы программы (waitTime),
                                                         параметров результирующего файла: максимального количества строк в файле(maxSizeMap),
                                                         максимального количества элементов с одинаковым ID (maxRep),
                                                         выгрузки результирующего файла (outDirName).
  4. processDirectory считает оптимальное количество трэдов для формирования пула, создает результирующий map(mapResult) и очередь цен(priceQueue). 
    Результирующий map будет формироваться по принципу(ключ: ID продукта, значение : очередь из продуктов с таким ID, не более maxRep элементов).
    Очередь цен определяет общее количество элементов в результирующей map, размер очереди не более maxSizeMap,
      цены заносятся при соблюдении условий: размер очереди менее maxSizeMap и элемент добавлен в результирующий map, либо, если размер очереди равен maxSizeMap, а
      цена "нового" продукта ниже самой высокой в очереди, то проверяем возможность добавить продукт в его ветку в результирующей map, если удачно(продукт добавлен),
      то обновляем очередь цен(удаляем самый дорогой и заносим цену "нового").
      
  5. processDirectory запусает сервис исполнения и добавляет в него задачи makeFinalList для каждого файла из списка утилиты filesList и определяет сервису
    время на выполнение задач(список задач закрыт и новые не принимаются).
    
  6. makeFinalList в каждой задаче получает входящую коллекцию из makerToMap.
  
  7. makerToMap формирует коллекцию размером не более maxSizeMap путем с помощью leastN.
  
  8. leastN считывает из файла (передан путь к файлу) данные и формирует коллекцию максимум из maxSizeMap самых дешевых элементов. Передает ее в makerToMap.
  
  9. makerToMap передает коллекцию в makeFinalList.
  
  10. makeFinalList перебирает коллекцию пытаясь занести элементы в очередь цен и результирующий map по принципам: 
      есть место в очереди цен -> mapResultFillIn пытается внести продукт в результирующий map, если удачно -> обновляем очередь цен;
      очередь цен заполнена, но цена продукта ниже самой высокой цены в очереди -> 
                                                                mapResultFillIn пытается внести продукт в результирующий map, если удачно -> обновляем очередь цен;
                                                                
  11. mapResultFillIn проверяет, если уже есть "ветка" с ключом ID данного продукта -> 
                                                                передает в checkAndChangeBean данный продукт для попытки внести его в результирующий map,
                                                                
                                 если  ветки нет -> создает новую с ключом ID продукта и заносит в значение (результирующую очередь) новый элемент,
        и извещает вызывающую функцию о результате операции;                           
                                 
  12. checkAndChangeBean пытается занести в "ветку" новый элемент(продукт) по принципам: размер "ветки" меньше maxRep -> вносит, если равен maxRep, то проверяет 
                                                                                        цены самого дорого продукта с таким ID и "нового",
                                                                                        если новый дешевле -> удаляет дорогой и заносит "новый",
        и извещает вызывающую функцию о результате операции;
        
  13. processDirectory ожидает окончания работы сервиса (ExecutorService service) и затем формирует при помощи resultList результирующую коллекцию размером не более maxSizeMap.
  
  14. processDirectory запускает dataOutput для записи результатов работы на диск.
  
  15. dataOutput пытается сформировать и записать файл с именем "work_result.csv" в outDirName и сообщает результат вызывающей функции. 
  
  16. processDirectory сообщает о результатах записи файла оператору.
                                              
                                              
                                                         
 В программе  есть счетчики времени работы основной части программы (строки 62, 95,96 в классе ImportCsv).
 Результат тестов: многопоточность при работе с диском снижает производительность программы.
 
 
