import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class ParseLink extends RecursiveTask<Map<String, Integer>> {

    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private final Task rootTask;
    private final String zeroLevelURL;
    String regexLinkIsFile = "http[s]?:/(?:/[^/]+){1,}/[А-Яа-яёЁ\\w ]+\\.[a-z]{3,5}(?![/]|[\\wА-Яа-яёЁ])";
    String regexValidURL = "^(ht|f)tp(s?)://[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:(0-9)*)*(/?)([a-zA-Z0-9\\-.?,'/\\\\+&%_]*)?$";
    URLNameFormatter URLFormatter = new URLNameFormatter();

    public ParseLink(Task t) {
        this.rootTask = t;
        String URL = rootTask.getURL();
        zeroLevelURL = URLFormatter.setZeroLevelURL(URL);
    }

    @Override
    protected Map<String, Integer> compute() {
        Date date = new Date();
        Map<String, Integer> links = new TreeMap<>();
        String urlTask = rootTask.getURL(); //получаем у задачи ссылку
        if (urlTask.matches(regexLinkIsFile)) { //проверяем если ссылка - файл, то записываем в Map и возвращаем
            links.put(rootTask.getURL(), URLFormatter.getLevel(rootTask.getURL()));
            rootTask.setLinksTask(links);
            return rootTask.getLinksTask();
        }
        List<ParseLink> subTasks = new LinkedList<>(); //Создаем List для подзадач
        Map<String, Integer> subLinks = null; //Создаем Map для ссылок от вызвавшей ссылки
        try {
            subLinks = getChildLinksFromElements(urlTask); //Получаем все ссылки от вызвавшей ссылки
        } catch (InterruptedException ex) {
            System.out.println(formatter.format(date) + " Error in " + " subLinks = getChildLinksFromElements(urlTask)\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            System.out.println(formatter.format(date) + " java.io.IOException: Underlying input stream returned zero bytes");
        }
        if (subLinks != null) {
            for (String subLink : subLinks.keySet()) {
                Task subTask = new Task(subLink); //Создаем подзадачу для каждой ссылки
                ParseLink f_task = new ParseLink(subTask); //Рекурсия. Создаем экземпляр ParseLink для FJP от подзадачи
                f_task.fork();
                subTasks.add(f_task); //Добавляем задачу в список задач
                rootTask.addSubTask(subTask); //добавляем подзадачу в список подзадач вызвавшей задачи
            }
            for (ParseLink j_task : subTasks) {
                links.putAll(j_task.join());
            }
        }
        rootTask.setLinksTask(links);
        rootTask.setLinksTask(subLinks);
        return rootTask.getLinksTask();
    }

    private Map<String, Integer> getChildLinksFromElements(String url) throws InterruptedException, IOException {
        Date date = new Date();
        Map<String, Integer> t_links = new TreeMap<>();
        try {
            Elements t_elements = getElementsFromURL(url);
            if (t_elements != null && t_elements.size() != 0) {
                for (Element element : t_elements) {
                    String s = URLFormatter.extractLink(element);
                    String cleanS = URLFormatter.cleanURLName(s);
                    if (s.matches(regexValidURL) //Проверяем валидность сслыки
                            && s.contains(URLFormatter.cleanURLName(zeroLevelURL)) //Ссылка того же домена как и домен вызвавшей
                            && !cleanS.equals(URLFormatter.cleanURLName(url)) //Ссылка не равна вызвавшей ссылке, loop
                            && cleanS.indexOf(URLFormatter.cleanURLName(url)) == 0 //Ссылка того же уровня как вызвавшая, тоже устраняет loop
                            //как проверять итоговвую коллекцию на предмет наличия ссылки еще не додумал
                            //под ТЗ подходит. Ссылки не уходят на уровень вниз, т.е. строят дерево вперед, и за стартовую страницу
                            //берут тот уровень ссылки, которая на входе
                            && !t_links.containsKey(s)) //Ссылка еще не добавлена во временную Map
                    {
                        if (s.matches(regexLinkIsFile)) {
                            t_links.put(s, URLFormatter.getLevel(cleanS) - 1);
//                            System.out.println("Added " + s);
                        } else {
                            t_links.put(s, URLFormatter.getLevel(cleanS));
//                            System.out.println("Added " + s);
                        }
                    }
                }
            } else {
                System.out.println(formatter.format(date) + " Elements object is null from " + url);
            }
        } catch (NullPointerException ex) {
//            ex.printStackTrace();
            System.out.println(formatter.format(date) + " Error in <getChildLinksFromElements>. Elements from URL is empty\n");
        }
        if (t_links.size() > 0) {
            System.out.println(formatter.format(date) + " " + Thread.currentThread().getName() + ", parsed " + t_links.size() + " links <- " + url);
        }
        return t_links;
    }

    private Elements getElementsFromURL(String URL){
        Date date = new Date();
        Elements elements = null;
        try {
            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com")
                    .timeout(1000 * 10) //it's in milliseconds, so this means 5 seconds.
                    .ignoreHttpErrors(true)
                    .get();
            elements = doc.select("a[href]");
        } catch (UnknownHostException ex) {
//            ex.printStackTrace();
            System.out.println(formatter.format(date) + " Exception in <getElementsFromURL>. " + URL + " not available");
        } catch (SocketTimeoutException ex) {
//            ex.printStackTrace();
            System.out.println(formatter.format(date) + " Connect timed out / Read timed out");
        } catch (UnsupportedMimeTypeException ex) {
//            ex.printStackTrace();
            System.out.println(formatter.format(date) + " Unhandled content type. Must be text/*, application/xml, or application/*+xml. Mimetype=application/json, URL= " + URL);
        } catch (IOException ex) {
            System.out.println(formatter.format(date) + " Too many redirects occurred trying to load URL " + URL);
        }
        return elements;
    }
}



