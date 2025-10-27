package com.example.fitnesscoach.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class AIService {


    @Value("${openai.api.key}")
    private String openAiApiKey;
    @Value("${elevenlabs.api.key}")
    private String elevenLabsApiKey;
    @Value("${elevenlabs.voice.id}")
    private String voiceId;
    @Value("${elevenlabs.model.id:eleven_multilingual_v2}")
    private String ttsModelId;
    @Value("${openai.whisper.model:whisper-1}")
    private String whisperModel;


    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Transcribe audio file to text using OpenAI Whisper API.
     * @param audioFile the audio file to transcribe (must be in a supported format).
     * @return transcribed text
     */
    public String transcribeAudio(File audioFile) throws IOException {

        byte[] audioBytes = Files.readAllBytes(audioFile.toPath());
        ByteArrayResource fileAsResource = new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {

                return audioFile.getName() != null ? audioFile.getName() : "audio.webm";
            }
        };


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileAsResource);
        body.add("model", whisperModel);
        body.add("response_format", "text");  // ask for plain text response for simplicity


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(openAiApiKey); // "Authorization: Bearer <API_KEY>"

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String transcribeUrl = "https://api.openai.com/v1/audio/transcriptions";

        ResponseEntity<String> response = restTemplate.postForEntity(transcribeUrl, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String transcript = response.getBody();
            return transcript != null ? transcript.trim() : "";
        } else {

            throw new IllegalStateException("Whisper API call failed: " + response.getStatusCode());
        }
    }

    /**
     * Generate speech audio from text using ElevenLabs TTS API.
     * @param text the text to convert to speech.
     * @return File object pointing to the generated audio file (MP3).
     */
    public File synthesizeSpeech(String text) throws IOException {
        // Prepare JSON payload for ElevenLabs
        Map<String, Object> ttsRequest = Map.of(
                "text", text,
                "model_id", ttsModelId    // e.g., "eleven_multilingual_v2"

        );

        // Set headers for ElevenLabs API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("xi-api-key", elevenLabsApiKey);
        headers.setAccept(java.util.List.of(MediaType.valueOf("audio/mpeg")));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(ttsRequest, headers);

        String ttsUrl = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId;
        ResponseEntity<byte[]> response = restTemplate.postForEntity(ttsUrl, requestEntity, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("TTS API call failed: " + response.getStatusCode());
        }

        byte[] audioData = response.getBody();
        // Save audioData to an MP3 file
        File audioFile = new File("response_audio_" + System.currentTimeMillis() + ".mp3");
        Files.write(audioFile.toPath(), audioData);

        return audioFile;
    }

    /**
     * High-level method to process user audio: transcribe it, get AI response, and synthesize the reply.
     * (This method integrates the transcription and TTS steps, plus any AI logic for the coach's response.)
     */
    public File processUserAudio(File userAudioFile) throws IOException {
        String userQuery = transcribeAudio(userAudioFile);
        String coachResponseText = generateCoachResponse(userQuery);
        File responseAudio = synthesizeSpeech(coachResponseText);
        return responseAudio;
    }


    /**
     * Stub for generating the coach's response text from user query.
     * In the real application, this might call an AI API (e.g., OpenAI GPT) or use some logic.
     */
    private String generateCoachResponse(String userQuery) {
       
        return "You said: \"" + userQuery + "\". Keep up the good work!";
    }
}
