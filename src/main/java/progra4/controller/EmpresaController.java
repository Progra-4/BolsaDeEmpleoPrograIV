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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    private final EmpresaService empresaService;
    private final PuestoService puestoService;
    private final MatchingService matchingService;
    private final CaracteristicaService caracteristicaService;
    private final OferenteService oferenteService;

    // Empresa quemada mientras ttanto
    private static final Long EMPRESA_LOGUEADA_ID = 1L;

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
    public String dashboard(Model model) {
        Optional<Empresa> empresa = empresaService.obtenerPorId(EMPRESA_LOGUEADA_ID);

        if (empresa.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("empresa", empresa.get());
        return "empresa/dashboard";
    }


    @GetMapping("/puestos")
    public String misPuestos(Model model) {
        Optional<Empresa> empresa = empresaService.obtenerPorId(EMPRESA_LOGUEADA_ID);

        if (empresa.isEmpty()) {
            return "redirect:/";
        }

        List<Puesto> puestos = puestoService.obtenerPorEmpresa(empresa.get());
        model.addAttribute("empresa", empresa.get());
        model.addAttribute("puestos", puestos);
        return "empresa/puestos";
    }


    @GetMapping("/puestos/nuevo")
    public String crearPuestoForm(Model model) {
        Optional<Empresa> empresa = empresaService.obtenerPorId(EMPRESA_LOGUEADA_ID);

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
            @ModelAttribute Puesto puesto,
            @RequestParam(value = "caracteristicas", required = false) List<Long> caracteristicaIds,
            @RequestParam(value = "niveles", required = false) List<Integer> niveles,
            Model model) {

        Optional<Empresa> empresa = empresaService.obtenerPorId(EMPRESA_LOGUEADA_ID);

        if (empresa.isEmpty()) {
            return "redirect:/";
        }

        puesto.setEmpresa(empresa.get());


        List<PuestoCaracteristica> puestoCaracteristicas = new java.util.ArrayList<>();

        if (caracteristicaIds != null && niveles != null) {
            for (int i = 0; i < caracteristicaIds.size() && i < niveles.size(); i++) {
                Long caractId = caracteristicaIds.get(i);
                Integer nivel = niveles.get(i);

                if (caractId != null && caractId > 0 && nivel != null && nivel > 0) {
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

        puestoService.guardarConCaracteristicas(puesto, puestoCaracteristicas);

        return "redirect:/empresa/puestos";
    }

    @GetMapping("/puestos/{id}/candidatos")
    public String verCandidatos(@PathVariable Long id, Model model) {
        Optional<Empresa> empresa = empresaService.obtenerPorId(EMPRESA_LOGUEADA_ID);

        if (empresa.isEmpty()) {
            return "redirect:/";
        }

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
    public String verDetalleCandidato(@PathVariable Long id, Model model) {
        Optional<Empresa> empresa = empresaService.obtenerPorId(EMPRESA_LOGUEADA_ID);

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
    public String cambiarEstadoPuesto(@PathVariable Long id, @RequestParam boolean activo) {
        Optional<Empresa> empresa = empresaService.obtenerPorId(EMPRESA_LOGUEADA_ID);

        if (empresa.isEmpty()) {
            return "redirect:/";
        }

        Optional<Puesto> puesto = puestoService.obtenerPorIdYEmpresa(id, empresa.get());

        if (puesto.isPresent()) {
            puestoService.activarDesactivar(id, activo);
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