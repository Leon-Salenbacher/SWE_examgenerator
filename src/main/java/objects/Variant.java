package objects;

import javax.xml.crypto.Data;

public class Variant implements ChildObject {
    private int id;
    private String question;
    private String solution;

    public Variant(int id, String question, String solution) {
        this.id = id;
        this.question = question;
        this.solution = solution;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    @Override
    public String getTitle() {
        return String.valueOf(this.getId());
    }

}
