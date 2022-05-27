package model;

/**
 * author Marçal Gonzalez Vilarrasa
 */
public class Book {
    private String title;
    private String author;
    private String publishDate;
    private String genre;
    private String createDate;
    private String cover;
    private String description;
    private String rate;
    private String id;
    private String bookedBy;

    public Book(String title, String author, String publishDate, String genre, String createDate, String cover, String description, String rate, String id) {
        this.title = title;
        this.author = author;
        this.publishDate = publishDate;
        this.genre = genre;
        this.createDate = createDate;
        this.cover = cover;
        this.description = description;
        this.rate = rate;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public String getGenre() {
        return genre;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getCover() {
        return cover;
    }

    public String getDescription() {
        return description;
    }

    public String getRate() {
        return rate;
    }

    public String getId() {
        return id;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public void setId(String id) {
        this.id = id;
    }
}
