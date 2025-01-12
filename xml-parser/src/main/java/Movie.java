import java.util.List;

public class Movie {

    private String title;
    private String director;
    private String id;
    private Integer year;
    private List<String> genres;

    public Movie(){}

    public Movie(String title, String director, String id, Integer year, List<String> genres) {
        this.title = title;
        this.director = director;
        this.id = id;
        this.year = year;
        this.genres = genres;
    }

    public Integer getYear() { return year; }

    public void setYear(Integer year) { this.year = year; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDirector() { return director; }

    public void setDirector(String director) { this.director = director; }

    public List<String> getGenres() { return genres; }

    public void setGenres(List<String> genres) { this.genres = genres; }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("Title: " + (getTitle().isEmpty() ? "null" : getTitle()));
        sb.append(", ");
        sb.append("Director: " + (getDirector().isEmpty() ? "null" : getDirector()));
        sb.append(", ");
        sb.append("Id: " + getId());
        sb.append(", ");
        sb.append("Year: " + (getYear() == null || getYear() == 0 ? "null" : getYear()));
        sb.append(", ");
        sb.append("Genres: [");
        sb.append(String.join(", ", genres));
        sb.append("].");


        return sb.toString();
    }
}
