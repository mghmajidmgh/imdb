package mghj.imdb.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "alternate_titles")
public class AlternateTitle {
    @Id
    private String titleId;
    private int ordering;
    private String title;
    private String region;
    private String language;
    private boolean isOriginalTitle;
}
