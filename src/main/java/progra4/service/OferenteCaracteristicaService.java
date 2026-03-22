package progra4.service;

import progra4.model.OferenteCaracteristica;
import progra4.repository.OferenteCaracteristicaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OferenteCaracteristicaService {

    private final OferenteCaracteristicaRepository repository;

    public OferenteCaracteristicaService(OferenteCaracteristicaRepository repository) {
        this.repository = repository;
    }

    public List<OferenteCaracteristica> obtenerPorOferente(Long oferenteId) {
        return repository.findByOferenteId(oferenteId);
    }

    public void guardar(OferenteCaracteristica oc) {
        repository.save(oc);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}