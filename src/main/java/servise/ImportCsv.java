package servise;

import model.Product;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import static utils.Least.*;
import static utils.UI.*;

/**
 *
 * Пример  оформления
 */
public class ImportCsv {

    public static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        String temp = uiAnswer("Введите символ разделителя данных в файле(ах)(пример разделителя  ;    :");
        char delimiter = temp.length() > 0 ? temp.charAt(0) : ',';
        lokForDir(delimiter);
        sc.close();
    }


    //создаем фиксированный пул, так как потоки "долгие"
    // помещаем список файлов в пул и обрабатываем
    public static void processDirectory(final File directory, final Character delimiter) {


        // Получаем список доступных файлов в указанной директории.
        List<Path> files = null;
        try {
            files = filesList(".csv", directory);
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (files == null) {
            System.out.println("Нет доступных файлов для обработки.");
            return;
        } else {
            System.out.println("Количество файлов для обработки: " + files.size());
        }
        int waitTime = uiNumberAnswer("Введите ожидаемое время работы программы в минутах(по умолчанию 1 000):", 1000);

        int maxSizeMap = uiNumberAnswer("Введите максимальное количество выводимых данных(строк,по умолчанию 1 000):", 1000);

        int maxRep = uiNumberAnswer("Введите максимальное количество элементов с одинаковым ID(строк,по умолчанию 20):", 20);

        System.out.println("Имя файла результатов: work_result.csv");
        String outDirName = uiAnswer("Введите путь для выгрузки результатов работы утилиты:");
        //!!!!!!!!!!!!!!!!!!!!!! убрать, считаем время работы

//        long startTime = System.currentTimeMillis();

        // Непосредственно многопоточная обработка файлов.
        final int treadCount = Runtime.getRuntime().availableProcessors() + 1; // количество потоков, которые могу запустить

        ConcurrentHashMap<Integer, PriorityBlockingQueue<Product>> mapResult = new ConcurrentHashMap<>(maxSizeMap, 0.5f, treadCount);

        PriorityBlockingQueue<Float> priceQueue = new PriorityBlockingQueue<>(maxSizeMap, Comparator.reverseOrder());

        ExecutorService service = Executors.newFixedThreadPool(treadCount);//стоит убрать

        for (Path f : files) {
            service.execute(() -> {                                           //стоит убрать
                try {
                    makeFinalList(f, delimiter, mapResult, maxSizeMap, maxRep, priceQueue);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });                                                               //стоит убрать
        }
        // Новые задачи более не принимаем, выполняем только оставшиеся.
        service.shutdown();                                                   //стоит убрать
        // Ждем завершения выполнения потоков не более waitTime минут.
        try {                                                                 //стоит убрать
           service.awaitTermination(waitTime, TimeUnit.MINUTES); //Максимальное время обработки,стоит убрать
        } catch (InterruptedException e) {                                      //стоит убрать
           e.printStackTrace();                                                 //стоит убрать
        }                                                                         //стоит убрать

        List<Product> beans = resultList(mapResult, maxSizeMap);
        String report = dataOutput(outDirName, beans) ? "Операция выполнена!" : "!!! Операция НЕ выполнена";

        System.out.println(report);
//        long endTime = System.currentTimeMillis();
//        System.out.println("Total execution time: " + (endTime-startTime) + "ms");
    }

}
