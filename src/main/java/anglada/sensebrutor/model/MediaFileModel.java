package anglada.sensebrutor.model;

/**
 * Clase que donat un arxiu retorna nom, ruta, pes, tipus i data
 * @author Andreu
 */
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MediaFileModel {
    private final String name;
    private final String path;
    private final long size;
    private final String mimeType;
    private final LocalDateTime date;

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
