package utils;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import model.Product;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

import static servise.ImportCsv.processDirectory;
import static servise.ImportCsv.sc;
import static utils.Least.leastN;

public class UI {


    //запрос-ответ в текстовом формате
    public static String uiAnswer(String question){
        String answer="";
        while (answer.equals("")){
            System.out.print(question);
            answer =sc.next();
        }
        return answer;
    }


    //запрос-ответ в числовом формате
    public static int uiNumberAnswer(String question, int defaultValue){
        int temp;
        try{
            temp = Integer.parseInt(uiAnswer(question));
        }catch (NumberFormatException nfe){
            temp = -1;
        }
        return    temp>0 ? temp : defaultValue;
    }


    //ввод и проверка на валидность пути к файлам, которые надо обработать
    public static void lokForDir(Character delimiter){
        // Считываем исходный каталог для поиска файлов.
        String temp   = uiAnswer("Введите исходную директорию для поиска файлов:");
        String directoryPath = temp.length()>0? temp: "";
        File directory = new File(directoryPath);
        // Убедимся, что директория найдена и это реально директория, а не файл.
        if (directory.exists() && directory.isDirectory()) {
            processDirectory(directory,delimiter);
        } else {
            System.out.println("Не удалось найти директорию по указанному пути.");
        }
    }

    //создаем список обрабатываемых файлов
    public static List<Path> filesList (final String filters, File directory) throws IOException {
        // обход папок в глубину
        return  Files.walk(Paths.get(String.valueOf(directory)))
                .filter(Files::isRegularFile)
                .filter(f-> f.toString().endsWith(filters))
                .collect(Collectors.toList());
    }


    //Считывание файла в очередь обработки
    //так как работа с диском- "дорогое удовольствие",
    // загрузка одного файла идет в одном потоке
    public static PriorityBlockingQueue<Product> makerToMap(Path path, Character delimiter, int maxSizeMap) throws FileNotFoundException {

        return  leastN(
                new CsvToBeanBuilder(new FileReader(path.toFile())).withSeparator(delimiter)
                        .withType(Product.class).build().parse(),

                maxSizeMap);
    }


    //вывод итоговых данных в файл
    public static boolean dataOutput(String outDirName, List<Product> beans){
        boolean flag = false;

        File outFile = new File(outDirName,"work_result.csv");
        try {
            Writer writer = new FileWriter(outFile);
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            beanToCsv.write(beans);
            writer.close();
            flag = true;
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            e.printStackTrace();
        }
        return flag;
    }

}
