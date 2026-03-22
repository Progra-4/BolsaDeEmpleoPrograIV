package progra4.controller;

import jakarta.servlet.http.HttpSession;
import progra4.model.Caracteristica;
import progra4.model.Oferente;
import progra4.model.OferenteCaracteristica;
import progra4.service.CaracteristicaService;
import progra4.service.OferenteCaracteristicaService;
import progra4.service.OferenteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import progra4.model.Curriculum;
import progra4.service.CurriculumService;

@Controller
@RequestMapping("/oferente")
public class OferenteAuthController {

    private final OferenteService oferenteService;
    private final CaracteristicaService caracteristicaService;
    private final OferenteCaracteristicaService ocService;
    private final CurriculumService curriculumService;

    public OferenteAuthController(OferenteService oferenteService,
                                  CaracteristicaService caracteristicaService,
                                  OferenteCaracteristicaService ocService,
                                  CurriculumService curriculumService) {
        this.oferenteService = oferenteService;
        this.caracteristicaService = caracteristicaService;
        this.ocService = ocService;
        this.curriculumService = curriculumService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("OFERENTE")) {
            return "redirect:/login";
        }
        Long id = (Long) session.getAttribute("usuarioId");
        Oferente oferente = oferenteService.obtenerPorId(id).orElse(null);
        model.addAttribute("oferente", oferente);
        return "oferente/dashboard";
    }
    @GetMapping("/habilidades")
    public String habilidades(@RequestParam(required = false) Long actualId,
                              HttpSession session, Model model) {
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("OFERENTE")) {
            return "redirect:/login";
        }

        Long oferenteId = (Long) session.getAttribute("usuarioId");
        Oferente oferente = oferenteService.obtenerPorId(oferenteId).orElse(null);

        // Habilidades actuales del oferente
        model.addAttribute("habilidades", ocService.obtenerPorOferente(oferenteId));

        // Navegación jerárquica
        List<Caracteristica> todasCaracteristicas = caracteristicaService.obtenerTodas();
        model.addAttribute("todasCaracteristicas", todasCaracteristicas);

        if (actualId != null) {
            Caracteristica actual = todasCaracteristicas.stream()
                    .filter(c -> c.getId().equals(actualId))
                    .findFirst().orElse(null);
            model.addAttribute("actual", actual);

            // Subcategorías del nodo actual
            List<Caracteristica> subs = todasCaracteristicas.stream()
                    .filter(c -> c.getPadre() != null && c.getPadre().getId().equals(actualId))
                    .toList();
            model.addAttribute("subcategorias", subs);

            // Ruta desde raíz hasta el nodo actual
            List<Caracteristica> ruta = new java.util.ArrayList<>();
            Caracteristica nodo = actual;
            while (nodo != null) {
                ruta.add(0, nodo);
                nodo = nodo.getPadre();
            }
            model.addAttribute("ruta", ruta);
        } else {
            // Raíces
            List<Caracteristica> raices = todasCaracteristicas.stream()
                    .filter(c -> c.getPadre() == null)
                    .toList();
            model.addAttribute("subcategorias", raices);
            model.addAttribute("ruta", List.of());
        }

        model.addAttribute("oferente", oferente);
        return "oferente/habilidades";
    }

    @PostMapping("/habilidades/agregar")
    public String agregarHabilidad(@RequestParam Long caracteristicaId,
                                   @RequestParam Integer nivel,
                                   HttpSession session) {
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("OFERENTE")) {
            return "redirect:/login";
        }
        Long oferenteId = (Long) session.getAttribute("usuarioId");

        OferenteCaracteristica oc = new OferenteCaracteristica();
        oc.setOferente(oferenteService.obtenerPorId(oferenteId).orElseThrow());
        oc.setCaracteristica(caracteristicaService.obtenerPorId(caracteristicaId).orElseThrow());
        oc.setNivel(nivel);
        ocService.guardar(oc);

        return "redirect:/oferente/habilidades?actualId=" + caracteristicaId;
    }
    @GetMapping("/curriculum")
    public String curriculum(HttpSession session, Model model) {
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("OFERENTE")) {
            return "redirect:/login";
        }
        Long id = (Long) session.getAttribute("usuarioId");
        Oferente oferente = oferenteService.obtenerPorId(id).orElse(null);
        model.addAttribute("oferente", oferente);
        model.addAttribute("curriculum", curriculumService.obtenerPorOferente(id).orElse(null));
        return "oferente/curriculum";
    }

    @PostMapping("/curriculum/subir")
    public String subirCurriculum(@RequestParam("archivo") MultipartFile archivo,
                                  HttpSession session, Model model) {
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("OFERENTE")) {
            return "redirect:/login";
        }
        Long id = (Long) session.getAttribute("usuarioId");
        Oferente oferente = oferenteService.obtenerPorId(id).orElseThrow();
        try {
            curriculumService.guardar(oferente, archivo);
        } catch (IOException e) {
            model.addAttribute("error", "Error al subir el archivo.");
            model.addAttribute("oferente", oferente);
            return "oferente/curriculum";
        }
        return "redirect:/oferente/curriculum";
    }

    @GetMapping("/curriculum/ver")
    public String verCurriculum(HttpSession session) {
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("OFERENTE")) {
            return "redirect:/login";
        }
        Long id = (Long) session.getAttribute("usuarioId");
        Optional<Curriculum> curriculum = curriculumService.obtenerPorOferente(id);
        if (curriculum.isEmpty()) return "redirect:/oferente/curriculum";

        return "redirect:/uploads/curriculum/" + curriculum.get().getArchivo();
    }
}