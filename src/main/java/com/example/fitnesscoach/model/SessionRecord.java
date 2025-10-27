package com.example.fitnesscoach.model;
import java.time.LocalDateTime;

public class SessionRecord {

    private Long id;
    private String userAudioPath;
    private String transcript;
    private String responseText;
    private String responseAudio; //
    private LocalDateTime timestamp;

    public SessionRecord(String userAudioPath, String transcript,
                         String responseText, String responseAudio,
                         LocalDateTime timestamp) {
        this.userAudioPath = userAudioPath;
        this.transcript = transcript;
        this.responseText = responseText;
        this.responseAudio = responseAudio;
        this.timestamp = timestamp;
    }

    public String getUserAudioPath() { return userAudioPath; }
    public String getTranscript() { return transcript; }
    public String getResponseText() { return responseText; }
    public String getResponseAudio() { return responseAudio; }
    public LocalDateTime getTimestamp() { return timestamp; }

}
