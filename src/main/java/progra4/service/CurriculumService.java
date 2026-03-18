package progra4.service;

import progra4.model.Curriculum;
import progra4.repository.CurriculumRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import progra4.model.Oferente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



@Service
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;

    public CurriculumService(CurriculumRepository curriculumRepository) {
        this.curriculumRepository = curriculumRepository;
    }

    public List<Curriculum> obtenerTodos() {
        return curriculumRepository.findAll();
    }

    public Optional<Curriculum> obtenerPorId(Long id) {
        return curriculumRepository.findById(id);
    }

    public Curriculum guardar(Curriculum curriculum) {
        return curriculumRepository.save(curriculum);
    }

    public void eliminar(Long id) {
        curriculumRepository.deleteById(id);
    }

    @Value("${app.upload.dir}")
    private String uploadDir;


    public Optional<Curriculum> obtenerPorOferente(Long oferenteId) {
        return curriculumRepository.findByOferenteId(oferenteId);
    }

    public void guardar(Oferente oferente, MultipartFile archivo) throws IOException {
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String nombreArchivo = "cv_" + oferente.getId() + ".pdf";
        Path destino = dirPath.resolve(nombreArchivo);
        archivo.transferTo(destino.toFile());

        Curriculum curriculum = curriculumRepository
                .findByOferenteId(oferente.getId())
                .orElse(new Curriculum());
        curriculum.setOferente(oferente);
        curriculum.setArchivo(nombreArchivo);
        curriculumRepository.save(curriculum);
    }
}