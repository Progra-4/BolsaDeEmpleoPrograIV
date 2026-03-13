package progra4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import progra4.model.Puesto;
import progra4.model.Empresa;
import java.util.List;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {

    List<Puesto> findByEmpresa(Empresa empresa);

    List<Puesto> findByActivo(boolean activo);

}