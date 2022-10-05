import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class Main {


    public static String outputPathFile = "./src/main/resources/temp.txt"; //Output folder for txt file
    public volatile static Map<String, Integer> linksMain = new TreeMap<>(); //all links will be here

    public static void setOutputPathFile(String outputPathFile) {
        Main.outputPathFile = outputPathFile;
    }

    public static void main(String[] args) {
        URLNameFormatter URLf = new URLNameFormatter();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String URL = "http://lenta.ru/";
        setOutputPathFile("./src/main/resources/sitemap_" + URLf.setZeroLevelURL(URL) + ".txt");
        System.out.println(formatter.format(date) + " Nice to see, Artem!");
        System.out.println(formatter.format(date) + " Now I will try to parse all links from " + URL);

        Task rootT = new Task(URL);
        ParseLink parseLinks = new ParseLink(rootT);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(parseLinks);
        if (!rootT.getLinksTask().isEmpty()) {
            System.out.println(formatter.format(date) + " Done");
            linksMain.put(URL, 0);
            linksMain.putAll(rootT.getLinksTask());
            System.out.printf(formatter.format(date) + " Number of links: " + rootT.getLinksTask().size() + "\n");//Получаес с рутовой задачи коллекцию ссылок
//            Сортируем коллекцию
            Map<String, Integer> sortedMap = linksMain.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (a, b) -> {
                                throw new AssertionError();
                            },
                            LinkedHashMap::new
                    ));

//            Выводим в файл
            try {
                PrintWriter writer = new PrintWriter(outputPathFile);
                for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
                    writer.println("        ".repeat(entry.getValue()) + entry.getKey());
//                System.out.println("        ".repeat(entry.getValue()) + entry.getKey());
                }
                writer.flush();
                writer.close();

            } catch (FileNotFoundException ex) {
                System.out.println(formatter.format(date) + " File " + outputPathFile + " not found");
            } catch (NullPointerException ex) {
                System.out.println(formatter.format(date) + " Error while writting file");
            }
            System.out.println(formatter.format(date) + " Map site you can see in " + outputPathFile);
        } else {
            System.out.println(formatter.format(date) + " No links found in " + URL + ", or exception(s) while parsing");
        }
    }
}

// TODO: 27.09.2022
//  Научиться использовать ForkJoinPool для решения рекурсивных задач.
//  напишите приложение, которое в многопоточном режиме сформирует
//  карту заданного сайта (список ссылок), и запишите её в файл.
//  Ссылки на дочерние страницы должны располагаться в файле с
//  отступами на одну табуляцию относительно родительских.
//
//  https://skillbox.ru/
//      https://skillbox.ru/media/
//             https://skillbox.ru/media/management/
//                    https://skillbox.ru/media/management/kak_rat_podkhod/