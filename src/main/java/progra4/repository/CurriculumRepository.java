package progra4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import progra4.model.Curriculum;
import progra4.model.Oferente;
import java.util.Optional;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

    Curriculum findByOferente(Oferente oferente);
    Optional<Curriculum> findByOferenteId(Long oferenteId);

}