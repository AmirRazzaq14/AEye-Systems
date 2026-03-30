package edu.farmingdale.CSC490.Coach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/coach")
@CrossOrigin(origins = "*")
public class CoachController {

    @Autowired
    private GeminiCoachService coachService;

    private List<CoachChatRequest.ChatTurn> parseHistory(String historyJson) {
        if (historyJson == null || historyJson.isBlank()) return new ArrayList<>();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(historyJson, new com.fasterxml.jackson.core.type.TypeReference<List<CoachChatRequest.ChatTurn>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<CoachChatResponse> chat(
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "history", required = false) String historyJson
    ) {
        try {
            List<CoachChatRequest.ChatTurn> history = parseHistory(historyJson);
            String reply = coachService.chat(message, history, image);
            return ResponseEntity.ok(new CoachChatResponse(reply));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CoachChatResponse("Coach unavailable. Please try again later."));
        }
    }
}