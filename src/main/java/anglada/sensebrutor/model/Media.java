package anglada.sensebrutor.model;

// Created by Github Copilot
import com.fasterxml.jackson.annotation.JsonProperty;

public class Media {
    @JsonProperty("id")
    public int id;
    @JsonProperty("userId")
    public int userId;
    @JsonProperty("downloadedFromUrl")
    public String downloadedFromUrl;
    @JsonProperty("mediaFileName")
    public String mediaFileName;
    @JsonProperty("mediaMimeType")
    public String mediaMimeType;
    @JsonProperty("blobNameGuid")
    public String blobNameGuid;
    @JsonProperty("blobUrl")
    public String blobUrl;

    @Override
    public String toString() {
        return String.format("Media{id=%d, userId=%d, file=%s, blob=%s}", id, userId, mediaFileName, blobNameGuid);
    }
}