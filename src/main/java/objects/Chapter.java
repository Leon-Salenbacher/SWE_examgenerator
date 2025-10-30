package objects;

import java.util.List;

public class Chapter implements DataObject{
    private int id;
    private String title;
    private List<Subtask> subtasks;

    public Chapter(int id, String title, List<Subtask> subtasks) {
        this.id = id;
        this.title = title;
        this.subtasks = subtasks;
    }

    public Chapter(int id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }
}
