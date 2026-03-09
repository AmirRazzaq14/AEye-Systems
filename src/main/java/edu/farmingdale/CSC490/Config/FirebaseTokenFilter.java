package edu.farmingdale.CSC490.Config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

@Component
public class FirebaseTokenFilter {

    /**
     * Call this at the start of any controller method.
     * Pass in the Authorization header value.
     * Returns the uid if valid, throws if not.
     */
    public String verifyAndGetUid(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception("Missing or invalid Authorization header");
        }
        String idToken = authHeader.substring(7); // strip "Bearer "
        FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
        return decoded.getUid();   // this is the uid to save under
    }
}