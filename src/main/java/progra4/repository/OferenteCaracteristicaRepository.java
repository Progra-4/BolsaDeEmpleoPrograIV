package progra4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import progra4.model.OferenteCaracteristica;
import progra4.model.Oferente;
import java.util.List;

public interface OferenteCaracteristicaRepository extends JpaRepository<OferenteCaracteristica, Long> {

    List<OferenteCaracteristica> findByOferente(Oferente oferente);
    List<OferenteCaracteristica> findByOferenteId(Long oferenteId);
}