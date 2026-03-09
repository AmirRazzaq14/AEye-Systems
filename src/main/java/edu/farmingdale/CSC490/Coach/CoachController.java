package edu.farmingdale.CSC490.Coach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coach")
@CrossOrigin(origins = "*")
public class CoachController {

    @Autowired
    private GeminiCoachService coachService;

    @PostMapping("/chat")
    public ResponseEntity<CoachChatResponse> chat(@RequestBody CoachChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(new CoachChatResponse("Please send a message."));
        }
        try {
            String reply = coachService.chat(request.getMessage(), request.getHistory());
            return ResponseEntity.ok(new CoachChatResponse(reply));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new CoachChatResponse("Coach is not configured. Please set the API key."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CoachChatResponse("Coach unavailable. Please try again later."));
        }
    }
}
