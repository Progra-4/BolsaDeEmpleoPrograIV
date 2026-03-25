package progra4.controller;

import progra4.model.Empresa;
import progra4.model.Puesto;
import progra4.model.Oferente;
import progra4.model.PuestoCaracteristica;
import progra4.model.Caracteristica;
import progra4.service.EmpresaService;
import progra4.service.PuestoService;
import progra4.service.MatchingService;
import progra4.service.CaracteristicaService;
import progra4.service.OferenteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    private static final Logger logger = LoggerFactory.getLogger(EmpresaController.class);

    private final EmpresaService empresaService;
    private final PuestoService puestoService;
    private final MatchingService matchingService;
    private final CaracteristicaService caracteristicaService;
    private final OferenteService oferenteService;

    public EmpresaController(EmpresaService empresaService,
                            PuestoService puestoService,
                            MatchingService matchingService,
                            CaracteristicaService caracteristicaService,
                            OferenteService oferenteService) {
        this.empresaService = empresaService;
        this.puestoService = puestoService;
        this.matchingService = matchingService;
        this.caracteristicaService = caracteristicaService;
        this.oferenteService = oferenteService;
    }


    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Verificar que es empresa logueada
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("EMPRESA")) {
            return "redirect:/login";
        }

        Long empresaId = (Long) session.getAttribute("usuarioId");
        Optional<Empresa> empresa = empresaService.obtenerPorId(empresaId);

        if (empresa.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("empresa", empresa.get());
        return "empresa/dashboard";
    }


    @GetMapping("/puestos")
    public String misPuestos(HttpSession session, Model model) {
        logger.info("=== ENTRANDO A /empresa/puestos ===");

        // Verificar que es empresa logueada
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("EMPRESA")) {
            logger.warn("No hay rol EMPRESA en sesión, redirigiendo a login");
            return "redirect:/login";
        }

        try {
            Long empresaId = (Long) session.getAttribute("usuarioId");
            logger.info("empresaId desde sesión: " + empresaId);

            if (empresaId == null) {
                logger.warn("empresaId es NULL en sesión");
                return "redirect:/login";
            }

            logger.info("Buscando empresa con ID: " + empresaId);
            Optional<Empresa> empresa = empresaService.obtenerPorId(empresaId);

            if (empresa.isEmpty()) {
                logger.warn("Empresa NO encontrada para ID: " + empresaId);
                return "redirect:/login";
            }

            logger.info("Empresa encontrada: " + empresa.get().getNombre());
            logger.info("Llamando a obtenerPorEmpresa con empresa: " + empresa.get().getId());

            List<Puesto> puestos = puestoService.obtenerPorEmpresa(empresa.get());
            logger.info("Puestos obtenidos: " + (puestos != null ? puestos.size() : "NULL"));

            model.addAttribute("empresa", empresa.get());
            model.addAttribute("puestos", puestos != null ? puestos : new java.util.ArrayList<>());
            logger.info("Retornando vista empresa/puestos");
            return "empresa/puestos";
        } catch (Exception e) {
            logger.error("EXCEPCIÓN EN misPuestos: " + e.getClass().getName() + " - " + e.getMessage(), e);
            e.printStackTrace();
            return "redirect:/login";
        }
    }


    @GetMapping("/puestos/nuevo")
    public String crearPuestoForm(HttpSession session, Model model) {
        // Verificar que es empresa logueada
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("EMPRESA")) {
            return "redirect:/login";
        }

        Long empresaId = (Long) session.getAttribute("usuarioId");
        Optional<Empresa> empresa = empresaService.obtenerPorId(empresaId);

        if (empresa.isEmpty()) {
            return "redirect:/";
        }

        List<Caracteristica> caracteristicas = caracteristicaService.obtenerTodas();

        model.addAttribute("empresa", empresa.get());
        model.addAttribute("puesto", new Puesto());
        model.addAttribute("caracteristicas", caracteristicas);
        return "empresa/crearPuesto";
    }


    @PostMapping("/puestos")
    public String crearPuesto(
            HttpSession session,
            @ModelAttribute Puesto puesto,
            @RequestParam(value = "caracteristicas", required = false) List<Long> caracteristicaIds,
            @RequestParam(value = "niveles", required = false) List<Integer> niveles,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Verificar que es empresa logueada
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("EMPRESA")) {
            return "redirect:/login";
        }

        try {
            Long empresaId = (Long) session.getAttribute("usuarioId");
            if (empresaId == null) {
                return "redirect:/login";
            }

            Optional<Empresa> empresa = empresaService.obtenerPorId(empresaId);

            if (empresa.isEmpty()) {
                return "redirect:/login";
            }

            // Validaciones
            if (puesto.getTitulo() == null || puesto.getTitulo().trim().isEmpty()) {
                model.addAttribute("error", "El título del puesto es requerido");
                model.addAttribute("empresa", empresa.get());
                model.addAttribute("puesto", puesto);
                model.addAttribute("caracteristicas", caracteristicaService.obtenerTodas());
                return "empresa/crearPuesto";
            }

            if (puesto.getDescripcion() == null || puesto.getDescripcion().trim().isEmpty()) {
                model.addAttribute("error", "La descripción del puesto es requerida");
                model.addAttribute("empresa", empresa.get());
                model.addAttribute("puesto", puesto);
                model.addAttribute("caracteristicas", caracteristicaService.obtenerTodas());
                return "empresa/crearPuesto";
            }

            if (puesto.getSalario() == null || puesto.getSalario() <= 0) {
                model.addAttribute("error", "El salario debe ser mayor a 0");
                model.addAttribute("empresa", empresa.get());
                model.addAttribute("puesto", puesto);
                model.addAttribute("caracteristicas", caracteristicaService.obtenerTodas());
                return "empresa/crearPuesto";
            }

            // Validar que hay al menos una característica
            List<PuestoCaracteristica> puestoCaracteristicas = new java.util.ArrayList<>();
            if (caracteristicaIds != null && niveles != null) {
                for (int i = 0; i < caracteristicaIds.size() && i < niveles.size(); i++) {
                    Long caractId = caracteristicaIds.get(i);
                    Integer nivel = niveles.get(i);

                    if (caractId != null && caractId > 0 && nivel != null && nivel > 0 && nivel <= 5) {
                        Optional<Caracteristica> caract = caracteristicaService.obtenerPorId(caractId);
                        if (caract.isPresent()) {
                            PuestoCaracteristica pc = new PuestoCaracteristica();
                            pc.setCaracteristica(caract.get());
                            pc.setNivel(nivel);
                            puestoCaracteristicas.add(pc);
                        }
                    }
                }
            }

            if (puestoCaracteristicas.isEmpty()) {
                model.addAttribute("error", "Debes agregar al menos una característica");
                model.addAttribute("empresa", empresa.get());
                model.addAttribute("puesto", puesto);
                model.addAttribute("caracteristicas", caracteristicaService.obtenerTodas());
                return "empresa/crearPuesto";
            }

            puesto.setEmpresa(empresa.get());
            puesto.setActivo(true);
            puesto.setTipoPublicacion(puesto.getTipoPublicacion() != null ? puesto.getTipoPublicacion() : "PUBLICO");
            puestoService.guardarConCaracteristicas(puesto, puestoCaracteristicas);
            redirectAttributes.addFlashAttribute("exito", "¡Puesto creado correctamente!");

            return "redirect:/empresa/puestos";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al crear el puesto: " + e.getMessage());
            model.addAttribute("empresa", empresaService.obtenerPorId((Long) session.getAttribute("usuarioId")).orElse(null));
            model.addAttribute("puesto", puesto);
            model.addAttribute("caracteristicas", caracteristicaService.obtenerTodas());
            return "empresa/crearPuesto";
        }
    }

    @GetMapping("/puestos/{id}/candidatos")
    public String verCandidatos(
            HttpSession session,
            @PathVariable Long id,
            Model model) {

        // Verificar que es empresa logueada
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("EMPRESA")) {
            return "redirect:/login";
        }

        Long empresaId = (Long) session.getAttribute("usuarioId");
        Optional<Empresa> empresa = empresaService.obtenerPorId(empresaId);

        if (empresa.isEmpty()) {
            return "redirect:/";
        }

        // Verificar que el puesto pertenece a la empresa
        Optional<Puesto> puesto = puestoService.obtenerPorIdYEmpresa(id, empresa.get());

        if (puesto.isEmpty()) {
            return "redirect:/empresa/puestos";
        }

        List<Oferente> candidatos = matchingService.obtenerCandidatos(puesto.get());

        model.addAttribute("empresa", empresa.get());
        model.addAttribute("puesto", puesto.get());
        model.addAttribute("candidatos", candidatos);
        model.addAttribute("matchingService", matchingService);

        return "empresa/candidatos";
    }


    @GetMapping("/candidatos/{id}")
    public String verDetalleCandidato(
            HttpSession session,
            @PathVariable Long id,
            Model model) {

        // Verificar que es empresa logueada
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("EMPRESA")) {
            return "redirect:/login";
        }

        Long empresaId = (Long) session.getAttribute("usuarioId");
        Optional<Empresa> empresa = empresaService.obtenerPorId(empresaId);

        if (empresa.isEmpty()) {
            return "redirect:/";
        }

        Optional<Oferente> oferente = oferenteService.obtenerPorId(id);

        if (oferente.isEmpty()) {
            return "redirect:/empresa/dashboard";
        }

        model.addAttribute("empresa", empresa.get());
        model.addAttribute("candidato", oferente.get());

        return "empresa/detalleCandidato";
    }


    @PostMapping("/puestos/{id}/cambiar-estado")
    public String cambiarEstadoPuesto(
            HttpSession session,
            @PathVariable Long id,
            @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {

        // Verificar que es empresa logueada
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("EMPRESA")) {
            return "redirect:/login";
        }

        Long empresaId = (Long) session.getAttribute("usuarioId");
        Optional<Empresa> empresa = empresaService.obtenerPorId(empresaId);

        if (empresa.isEmpty()) {
            return "redirect:/";
        }

        // Verificar que el puesto pertenece a la empresa
        Optional<Puesto> puesto = puestoService.obtenerPorIdYEmpresa(id, empresa.get());

        if (puesto.isPresent()) {
            puestoService.activarDesactivar(id, activo);
            String mensaje = activo ? "¡Puesto activado correctamente!" : "¡Puesto desactivado correctamente!";
            redirectAttributes.addFlashAttribute("exito", mensaje);
        }

        return "redirect:/empresa/puestos";
    }


    @GetMapping
    public String listar(Model model) {
        model.addAttribute("empresas", empresaService.obtenerTodas());
        return "empresas/lista";
    }

    @GetMapping("/nuevo")
    public String formulario(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "empresas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Empresa empresa) {
        empresaService.guardar(empresa);
        return "redirect:/empresas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        empresaService.eliminar(id);
        return "redirect:/empresas";
    }

}