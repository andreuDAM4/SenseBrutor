package anglada.sensebrutor.model;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
/**
 * Model que representa un fitxer multimèdia i proporciona informació bàsica
 * sobre ell, com el nom, la ruta absoluta, la mida, el tipus MIME i la data
 * de la última modificació.
 * 
 * @author Andreu
 */
public class MediaFileModel {
    private final String name;
    private final String path;
    private final long size;
    private final String mimeType;
    private final LocalDateTime date;
    /**
     * Crea un MediaFileModel a partir d'un fitxer.
     * Recupera el nom, la ruta absoluta, la mida en bytes, el tipus MIME
     * (o "unknown" si no es pot detectar) i la data de última modificació.
     * 
     * @param file El fitxer del qual es vol obtenir la informació.
     */
    public MediaFileModel(File file) {
        this.name = file.getName();
        this.path = file.getAbsolutePath();
        this.size = file.length();

        String detectedMime = "unknown"; 
        try {
            detectedMime = Files.probeContentType(file.toPath());
            if (detectedMime == null) {
                detectedMime = "unknown";
            }
        } catch (IOException e) {
            detectedMime = "unknown";
        }
        this.mimeType = detectedMime;  

        this.date = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(file.lastModified()),
            ZoneId.systemDefault()
        );
    }
    public String getName() { return name; }
    public String getPath() { return path; }
    public long getSize() { return size; }
    public String getMimeType() { return mimeType; }
    public LocalDateTime getDate() { return date; }

}
