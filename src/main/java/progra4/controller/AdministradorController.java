package progra4.controller;

import progra4.model.Administrador;
import progra4.service.AdministradorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdministradorController {

    private final AdministradorService administradorService;

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Verificar que es admin
        if (session.getAttribute("usuarioRol") == null ||
                !session.getAttribute("usuarioRol").equals("ADMIN")) {
            return "redirect:/login";
        }

        Long adminId = (Long) session.getAttribute("usuarioId");
        String adminNombre = (String) session.getAttribute("usuarioNombre");

        model.addAttribute("adminId", adminId);
        model.addAttribute("adminNombre", adminNombre);
        model.addAttribute("totalAdmins", administradorService.obtenerTodos().size());

        return "admin/admin";
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("administradores", administradorService.obtenerTodos());
        return "admin/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("administrador", new Administrador());
        return "admin/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Administrador administrador) {
        administradorService.guardar(administrador);
        return "redirect:/admin";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        administradorService.eliminar(id);
        return "redirect:/admin";
    }
}