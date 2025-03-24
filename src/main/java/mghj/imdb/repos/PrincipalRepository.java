package mghj.imdb.repos;

import mghj.imdb.entities.Principal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrincipalRepository extends JpaRepository<Principal, Integer> {
}
