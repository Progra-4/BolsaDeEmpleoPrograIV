package progra4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import progra4.model.Caracteristica;

public interface CaracteristicaRepository extends JpaRepository<Caracteristica, Long> {

     Caracteristica findByNombre(String nombre);

}