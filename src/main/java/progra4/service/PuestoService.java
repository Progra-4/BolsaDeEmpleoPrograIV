package progra4.service;

import progra4.model.Puesto;
import progra4.model.Empresa;
import progra4.model.PuestoCaracteristica;
import progra4.model.Caracteristica;
import progra4.repository.PuestoRepository;
import progra4.repository.PuestoCaracteristicaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PuestoService {

    private final PuestoRepository puestoRepository;
    private final PuestoCaracteristicaRepository puestoCaracteristicaRepository;

    public PuestoService(PuestoRepository puestoRepository,
                        PuestoCaracteristicaRepository puestoCaracteristicaRepository) {
        this.puestoRepository = puestoRepository;
        this.puestoCaracteristicaRepository = puestoCaracteristicaRepository;
    }

    public List<Puesto> obtenerTodos() {
        return puestoRepository.findAll();
    }

    public Optional<Puesto> obtenerPorId(Long id) {
        return puestoRepository.findById(id);
    }

    public Puesto guardar(Puesto puesto) {
        if (puesto.getFecha() == null) {
            puesto.setFecha(LocalDateTime.now());
        }
        if (!puesto.isActivo()) {
            puesto.setActivo(true);
        }
        return puestoRepository.save(puesto);
    }

    public void eliminar(Long id) {
        puestoRepository.deleteById(id);
    }

    public List<Puesto> obtenerUltimosCincoPuestos() {
        return puestoRepository.findTop5ByTipoPublicacionAndActivoOrderByFechaDesc("PUBLICO", true);
    }

    public List<Puesto> buscarPorCaracteristicas(List<Long> ids) {
        return puestoRepository.buscarPorCaracteristicas(ids);
    }

    public List<Puesto> obtenerPorEmpresa(Long empresaId) {
        return puestoRepository.findByEmpresaId(empresaId);
    }

    public List<Puesto> obtenerPorEmpresa(Empresa empresa) {
        return puestoRepository.findByEmpresa(empresa);
    }

    public Optional<Puesto> obtenerPorIdYEmpresa(Long id, Empresa empresa) {
        return puestoRepository.findByIdAndEmpresa(id, empresa);
    }

    public void activarDesactivar(Long id, boolean activo) {
        puestoRepository.findById(id).ifPresent(puesto -> {
            puesto.setActivo(activo);
            puestoRepository.save(puesto);
        });
    }

    public void guardarConCaracteristicas(Puesto puesto, List<PuestoCaracteristica> caracteristicas) {
        Puesto puestoGuardado = guardar(puesto);

        puestoCaracteristicaRepository.deleteAll(
            puestoCaracteristicaRepository.findByPuesto(puestoGuardado)
        );

        for (PuestoCaracteristica pc : caracteristicas) {
            if (pc.getCaracteristica() != null && pc.getNivel() != null) {
                pc.setPuesto(puestoGuardado);
                puestoCaracteristicaRepository.save(pc);
            }
        }
    }

    public List<PuestoCaracteristica> obtenerCaracteristicas(Long puestoId) {
        return puestoRepository.findById(puestoId)
            .map(puestoCaracteristicaRepository::findByPuesto)
            .orElse(List.of());
    }
}