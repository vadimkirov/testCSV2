package utils;

import model.Product;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import static utils.UI.makerToMap;

public class Least {

    //отбираем из коллекции( 1 загруженный файл)только те элементы, которые входят в
    //заданное число самых дешевых
    public static Queue<Product> leastN(Collection<Product> input,int maxCount,int maxRep) {
        assert maxCount > 0;
        Queue<Float> priceQueue = new PriorityBlockingQueue<>(maxRep, Collections.reverseOrder());
        Queue<Product> resultCollection = new PriorityBlockingQueue<>(maxCount,(x,y) ->
                                                        Float.compare (x.getPrice(),y.getPrice()));

        Map<Integer, PriorityBlockingQueue<Product>> tempMap = new HashMap<>();
        for (Product product : input) {
            checkAndFillInMap(tempMap,priceQueue,product,maxCount,maxRep);

        }
        tempMap.forEach((key, value) -> resultCollection.addAll(value));
        return resultCollection;
    }

    //В результирующей map(по ключу ID) проверяем разрешенное число повторов ID,
    //при превышении сравниваем цену и при необходимости меняем самый дорогой элемент на текущий
    //возвращаем результат выполнения операции(удалось внести продукт в результирующий map или нет)
    public static boolean checkAndChangeBean(Product p,
                                             PriorityBlockingQueue<Product> product,
                                             int maxRep){
        boolean success = false;
        if(product.size ()<maxRep){
            product.offer(p);
            success = true;
        }else {
            assert product.peek() != null;
            if ( product.peek ().compareTo (p) > 0) {
                product.poll ();
                product.offer (p);
                success = true;
            }
        }
        return success;
    }




    //заполнение результирующей map
    //возвращаем результат занесения - успешно или нет
    public static boolean mapResultFillIn(Product p,
                                          int maxRep,
                                          Map<Integer, PriorityBlockingQueue<Product>> mapResult){
        boolean success = false;
        if (mapResult.containsKey(p.getId())) {
            //если в результирующей map уже есть элементы с таким ID
            //проверяем возможность внести продукт в результирующий map
            if(checkAndChangeBean(p, mapResult.get(p.getId()), maxRep)){
                success = true;
            }
        } else {
            //если продуктов с таким ID еще нет, создаем новый элемент в результирующей map
            //ключ - ID, значение - очередь из продуктов с таким ID
            PriorityBlockingQueue<Product> queue =
                    new PriorityBlockingQueue<>(maxRep, Collections.reverseOrder());
            queue.offer(p);
            mapResult.put(p.getId(), queue);
            success = true;
        }
        return success;
    }


    //заполнение "очереди цен" и
    //согласно её("очереди цен") данным, результирующей map
    public static void makeFinalList(Path f,
                                     Character delimiter,
                                     Map<Integer, PriorityBlockingQueue<Product>> mapResult,
                                     int maxSizeOutFile,
                                     int maxRep,
                                     PriorityBlockingQueue<Float> priceQueue) throws FileNotFoundException {

        //получаем из файла коллекцию самых дешевых элементов,
        //максимальный размер коллекции не более maxSizeOutFile -
        //максимальное число элементов в финальном файле
        Queue<Product> inputList = makerToMap (f, delimiter, maxSizeOutFile, maxRep);

        //пока входящая коллекция не пуста
        while (!inputList.isEmpty()){
            Product p = inputList.poll();
            checkAndFillInMap(mapResult,priceQueue,p,maxSizeOutFile,maxRep);
        }
    }


    //выгрузка данных из результирующей map в list для операции вывода
    public static List<Product> resultList(ConcurrentHashMap<Integer,
                                           PriorityBlockingQueue<Product>> mapResult,
                                           int maxSizeMap){
        Queue<Product> res = new PriorityQueue<>();
        mapResult.forEach((key, value) -> res.addAll(value));
        List<Product> beans = new ArrayList<>();
        int sizeOutputData = Math.min(res.size(), maxSizeMap);
        for (int i = 0; i < sizeOutputData; i++) {
            beans.add(res.poll());
        }
        return beans;
    }


    //Заполнение map с проверкой на повторяемость ID(maxRep) и
    // "попадания" цены продукта в число (maxSizeOutFile) самых дешевых
    private static void checkAndFillInMap(Map<Integer, PriorityBlockingQueue<Product>> mapResult,
                                          Queue<Float> priceQueue,
                                          Product product,
                                          int maxSizeOutFile,
                                          int maxRep){

        //проверяем коллекцию цен priceQueue на наполненность
        if(priceQueue.size() < maxSizeOutFile){
            //если элемент "попал" в результирующий map
            if(mapResultFillIn(product,maxRep,mapResult)){
                //обновляем коллекцию цен
                priceQueue.offer(product.getPrice());
            }
        }else {
            assert priceQueue.peek() != null;
            //если размер коллекции цен достиг предельного значения
            //сравниваем цену продукта с максимальной ценой коллекции
            if (priceQueue.peek().compareTo(product.getPrice()) > 0) {
                //цена продукта ниже- пытаемся внести продукт в результирующую коллекцию
                if(mapResultFillIn(product,maxRep,mapResult)){
                    //если удалось - обновляем коллекцию цен
                    priceQueue.poll();
                    priceQueue.offer(product.getPrice());
                }
            }
        }
    }


}
