package com.socialmedia.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/shrushti")
public class ShrushtiController {

    private final RestClient restClient;
    private static final int MOCK_USER_ID = 1;

    public ShrushtiController(RestClient restClient) {
        this.restClient = restClient;
    }

    private int getActiveUserId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("currentUserId");
        return id != null ? id : MOCK_USER_ID;
    }

    @GetMapping("/services")
    public String index(@RequestParam(required = false, defaultValue = "idle") String action, HttpSession session, Model model) {
        int activeId = getActiveUserId(session);
        model.addAttribute("activeUserId", activeId);

        model.addAttribute("mode", model.containsAttribute("executedEndpoint") ? "result" : "configure");
        model.addAttribute("configureEndpoint", action);

        return "shrushti";
    }

    @PostMapping("/execute")
    public String executeEndpoint(
            @RequestParam String endpoint,
            @RequestParam(required = false) String targetUserId,
            @RequestParam(required = false) String user1ID,
            @RequestParam(required = false) String user2ID,
            @RequestParam(required = false) String keyword,
            HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {

        int activeId = getActiveUserId(session);
        redirectAttrs.addFlashAttribute("executedEndpoint", endpoint);

        try {
            switch (endpoint) {
                // FRIENDSHIPS
                case "get-friends":
                    Object friends = restClient.get().uri("/v1/friendships/{id}/list", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", friends);
                    redirectAttrs.addFlashAttribute("resultType", "friendList");
                    break;
                case "get-pending-requests":
                    Object requests = restClient.get().uri("/v1/friendships/{id}/requests/incoming", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", requests);
                    redirectAttrs.addFlashAttribute("resultType", "friendList");
                    break;
                case "check-status":
                    // Use user1ID and user2ID as sent from the form
                    String status = restClient.get().uri("/v1/friendships/status?userID={u}&targetID={t}", user1ID, user2ID).retrieve().body(String.class);
                    redirectAttrs.addFlashAttribute("resultData", status);
                    redirectAttrs.addFlashAttribute("resultType", "string");
                    break;
                case "send-request":
                    java.util.Map<String, Integer> dto = new java.util.HashMap<>();
                    dto.put("senderId", activeId);
                    dto.put("receiverId", Integer.parseInt(targetUserId));
                    // Generating a unique ID for the friendship manually here to satisfy the backend
                    dto.put("friendshipId", (int) (System.currentTimeMillis() % 1000000000));
                    String res = restClient.post().uri("/v1/friendships/request").body(dto).retrieve().body(String.class);
                    redirectAttrs.addFlashAttribute("resultData", res);
                    redirectAttrs.addFlashAttribute("resultType", "string");
                    break;
                case "accept-request":
                    String accReq = restClient.put().uri("/v1/friendships/{id}/accept", targetUserId).retrieve().body(String.class);
                    redirectAttrs.addFlashAttribute("resultData", accReq);
                    redirectAttrs.addFlashAttribute("resultType", "string");
                    break;
                case "reject-request":
                    // Using reject mapping
                    String rem = restClient.patch().uri("/v1/friendships/{id}/reject", targetUserId).retrieve().body(String.class);
                    redirectAttrs.addFlashAttribute("resultData", rem);
                    redirectAttrs.addFlashAttribute("resultType", "string");
                    break;
                case "get-incoming":
                    Object incoming = restClient.get().uri("/v1/friendships/{id}/requests/incoming", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", incoming);
                    redirectAttrs.addFlashAttribute("resultType", "friendshipList");
                    break;
                case "get-outgoing":
                    Object outgoing = restClient.get().uri("/v1/friendships/{id}/requests/outgoing", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", outgoing);
                    redirectAttrs.addFlashAttribute("resultType", "friendshipList");
                    break;
                case "get-friends-list":
                    Object friendsRes = restClient.get().uri("/v1/friendships/{id}/list", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", friendsRes);
                    redirectAttrs.addFlashAttribute("resultType", "friendshipList");
                    break;
                case "get-friend-notifications":
                    Object fNotifs = restClient.get().uri("/v1/friendships/{id}/notifications", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", fNotifs);
                    redirectAttrs.addFlashAttribute("resultType", "genericList");
                    break;
                case "friend-count":
                    Object fCount = restClient.get().uri("/v1/friendships/{id}/count", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", fCount);
                    redirectAttrs.addFlashAttribute("resultType", "count");
                    break;
                case "get-status":
                    Object fStatus = restClient.get().uri("/v1/friendships/status?userId1={u1}&userId2={u2}", activeId, targetUserId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", fStatus);
                    redirectAttrs.addFlashAttribute("resultType", "string");
                    break;
                case "are-friends":
                    Boolean areFr = restClient.get().uri("/v1/friendships/are-friends?userId1={a}&userId2={b}", activeId, targetUserId).retrieve().body(Boolean.class);
                    redirectAttrs.addFlashAttribute("resultData", areFr);
                    redirectAttrs.addFlashAttribute("resultType", "boolean");
                    break;
                case "get-mutual":
                    Object mutual = restClient.get().uri("/v1/friendships/{id}/mutual/{tid}", activeId, targetUserId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", mutual);
                    redirectAttrs.addFlashAttribute("resultType", "userList");
                    break;
                case "search-friendships":
                    Object fSearch = restClient.get().uri("/v1/friendships/{id}/search?query={q}", activeId, keyword).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", fSearch);
                    redirectAttrs.addFlashAttribute("resultType", "friendshipList");
                    break;
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String cleanMsg = "Failed executing request";
            try {
                com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(e.getResponseBodyAsString());
                if (node.has("message")) {
					cleanMsg = node.get("message").asText();
				} else if (node.has("error")) {
					cleanMsg = node.get("error").asText();
				}
            } catch (Exception ex) {
                cleanMsg = e.getMessage();
            }
            redirectAttrs.addFlashAttribute("resultData", cleanMsg);
            redirectAttrs.addFlashAttribute("resultType", "error");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("resultData", "Terminal Error: " + e.getMessage());
            redirectAttrs.addFlashAttribute("resultType", "error");
        }

        return "redirect:/shrushti/services";
    }
}
