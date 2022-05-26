package model;

public class Comment {

    private String id;
    private String idLlibre;
    private String user;
    private String date;
    private String comment;

    public String getId() {
        return id;
    }

    public String getIdLlibre() {
        return idLlibre;
    }

    public String getUser() {
        return user;
    }

    public String getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIdLlibre(String idLlibre) {
        this.idLlibre = idLlibre;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
