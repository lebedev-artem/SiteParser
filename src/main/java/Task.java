import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Task {

    private final String URL;
    private final ArrayList<Task> subTasks;
    private int level = 0;
    private final Map<String, Integer> links = new TreeMap<>();

    public Task(String url) {
        this.URL = url;
        subTasks = new ArrayList<>();
    }

    public String getURL() {
        return URL;
    }

    public void addSubTask(Task task) {
        task.setLevel(level + 1);
        subTasks.add(task);
    }

    public ArrayList<Task> getSubTasks() {
        return subTasks;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Map<String, Integer> getLinksTask() {
        return links;
    }

    public void setLinksTask(Map<String, Integer> linksOfTask) {
        this.links.putAll(linksOfTask);
    }
}
