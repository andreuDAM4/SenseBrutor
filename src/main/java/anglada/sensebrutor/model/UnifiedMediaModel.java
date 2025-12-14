package anglada.sensebrutor.model;

import anglada.dimedianetpollingcomponent.model.Media;
import java.io.File;
import java.time.LocalDateTime;

public class UnifiedMediaModel {

    public enum Estado {
        LOCAL,
        REMOTO,
        AMBOS
    }

    private String nombre;
    private long size;
    private String mime;
    private LocalDateTime date;

    private File localFile;      // null si no está en local
    private Media remoteMedia;   // null si no está en red

    private Estado estado;

    // -------- getters / setters --------

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getMime() { return mime; }
    public void setMime(String mime) { this.mime = mime; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public File getLocalFile() { return localFile; }
    public void setLocalFile(File localFile) { this.localFile = localFile; }

    public Media getRemoteMedia() { return remoteMedia; }
    public void setRemoteMedia(Media remoteMedia) { this.remoteMedia = remoteMedia; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
