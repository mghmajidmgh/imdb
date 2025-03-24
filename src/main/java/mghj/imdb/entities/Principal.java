package mghj.imdb.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "principals")
public class Principal {
    @Id
    private String tconst;
    private int ordering;
    private String nconst;
    private String category;
    private String job;
    private String characters;
}
