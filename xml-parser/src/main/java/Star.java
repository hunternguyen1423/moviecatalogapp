import java.util.ArrayList;
import java.util.List;

public class Star {

    private String name;
    private String id;
    private Integer birthyear;
    private List<String> movies = new ArrayList<String>();

    public Star(){}

    public Star(String name, String id, Integer birthyear) {
        this.name = name;
        this.id = id;
        this.birthyear = birthyear;
    }

    public Integer getBirthyear() { return birthyear; }

    public void setBirthyear(Integer birthyear) { this.birthyear = birthyear; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public List<String> getMovies() { return movies; }

    public void setMovies(List<String> movies) { this.movies = movies; }
    public void addMovie(String movie) { this.movies.add(movie); }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star Details - ");
        sb.append("Name: " + (getName().isEmpty() ? "null" : getName()));
        sb.append(", ");
        sb.append("Id: " + getId());
        sb.append(", ");
        sb.append("Birthyear: " + (getBirthyear() == null || getBirthyear() == 0 ? "null" : getBirthyear()));
        sb.append(", ");
        sb.append("Movies: [");
        sb.append(String.join(", ", movies));
        sb.append("].");


        return sb.toString();
    }
}
