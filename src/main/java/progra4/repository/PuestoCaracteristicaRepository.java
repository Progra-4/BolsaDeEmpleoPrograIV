package progra4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import progra4.model.PuestoCaracteristica;
import progra4.model.Puesto;
import java.util.List;

public interface PuestoCaracteristicaRepository extends JpaRepository<PuestoCaracteristica, Long> {

    List<PuestoCaracteristica> findByPuesto(Puesto puesto);

}