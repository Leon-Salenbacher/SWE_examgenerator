package objects;

import java.util.List;

public class Subtask implements ParentObject<Variant> {
    private int id;
    private String title;
    private int points;
    private int chapterId;

    private List<Variant> variants;

    public Subtask(int id, String title, int points, int chapterId, List<Variant> variants) {
        this.id = id;
        this.title = title;
        this.points = points;
        this.chapterId = chapterId;
        this.variants = variants;
    }

    public Subtask(int id, String title, int points, int chapterId) {
        this.id = id;
        this.title = title;
        this.points = points;
        this.chapterId = chapterId;
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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

    @Override
    public List<Variant> getChildElements() {
        return this.getVariants();
    }


}
