package progra4.service;

import progra4.model.Puesto;
import progra4.model.Oferente;
import progra4.model.PuestoCaracteristica;
import progra4.model.OferenteCaracteristica;
import progra4.repository.OferenteRepository;
import progra4.repository.OferenteCaracteristicaRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final OferenteRepository oferenteRepository;
    private final OferenteCaracteristicaRepository oferenteCaracteristicaRepository;

    public MatchingService(OferenteRepository oferenteRepository,
                          OferenteCaracteristicaRepository oferenteCaracteristicaRepository) {
        this.oferenteRepository = oferenteRepository;
        this.oferenteCaracteristicaRepository = oferenteCaracteristicaRepository;
    }


    public List<Oferente> obtenerCandidatos(Puesto puesto) {
        List<Oferente> oferentes = oferenteRepository.findByAprobado(true);

        return oferentes.stream()
            .filter(oferente -> cumpleRequisitos(oferente, puesto))
            .collect(Collectors.toList());
    }


    public boolean cumpleRequisitos(Oferente oferente, Puesto puesto) {
        List<PuestoCaracteristica> requisitos = puesto.getRequisitos();

        if (requisitos == null || requisitos.isEmpty()) {
            return true; // Si no hay requisitos, todos califican
        }

        List<OferenteCaracteristica> habilidades = oferente.getHabilidades();
        if (habilidades == null || habilidades.isEmpty()) {
            return false; // Si no tiene habilidades y hay requisitos, no cumple
        }

        Map<Long, Integer> habilidadesMap = new HashMap<>();
        for (OferenteCaracteristica h : habilidades) {
            if (h.getCaracteristica() != null) {
                habilidadesMap.put(h.getCaracteristica().getId(), h.getNivel());
            }
        }

        for (PuestoCaracteristica requisito : requisitos) {
            if (requisito.getCaracteristica() == null) {
                continue;
            }

            Long caractId = requisito.getCaracteristica().getId();
            Integer nivelRequerido = requisito.getNivel();

            if (!habilidadesMap.containsKey(caractId) ||
                habilidadesMap.get(caractId) < nivelRequerido) {
                return false;
            }
        }

        return true;
    }


    public int calcularPorcentajeMatch(Oferente oferente, Puesto puesto) {
        List<PuestoCaracteristica> requisitos = puesto.getRequisitos();

        if (requisitos == null || requisitos.isEmpty()) {
            return 100;
        }

        List<OferenteCaracteristica> habilidades = oferente.getHabilidades();
        if (habilidades == null || habilidades.isEmpty()) {
            return 0;
        }

        Map<Long, Integer> habilidadesMap = new HashMap<>();
        for (OferenteCaracteristica h : habilidades) {
            if (h.getCaracteristica() != null) {
                habilidadesMap.put(h.getCaracteristica().getId(), h.getNivel());
            }
        }

        int cumplidos = 0;
        for (PuestoCaracteristica requisito : requisitos) {
            if (requisito.getCaracteristica() == null) {
                continue;
            }

            Long caractId = requisito.getCaracteristica().getId();
            Integer nivelRequerido = requisito.getNivel();

            if (habilidadesMap.containsKey(caractId) &&
                habilidadesMap.get(caractId) >= nivelRequerido) {
                cumplidos++;
            }
        }

        return (cumplidos * 100) / requisitos.size();
    }
}

