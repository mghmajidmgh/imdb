package mghj.imdb.entities;



import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "people")
public class Person {
    @Id
    private String nconst;
    private String primaryName;
    private int birthYear;
    private int deathYear;
    private String primaryProfession;
    private String knownForTitles;
}
