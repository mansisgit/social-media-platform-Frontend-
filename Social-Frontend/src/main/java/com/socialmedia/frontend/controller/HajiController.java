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
@RequestMapping("/haji")
public class HajiController {

    private final RestClient restClient;
    private static final int MOCK_USER_ID = 1;

    public HajiController(RestClient restClient) {
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

        return "haji";
    }

    @PostMapping("/execute")
    public String executeEndpoint(
            @RequestParam String endpoint,
            @RequestParam(required = false) String targetUserId,
            @RequestParam(required = false) String groupID,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String description,
            HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {

        int activeId = getActiveUserId(session);
        redirectAttrs.addFlashAttribute("executedEndpoint", endpoint);

        try {
            switch (endpoint) {
                // GROUPS
                case "get-all-groups":
                    Object allGroups = restClient.get().uri("/groups").retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", allGroups);
                    redirectAttrs.addFlashAttribute("resultType", "groupList");
                    break;
                case "get-single-group":
                    Object singleGroup = restClient.get().uri("/groups/{id}", groupID).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", singleGroup);
                    redirectAttrs.addFlashAttribute("resultType", "singleGroup");
                    break;
                case "get-user-groups":
                    Object userGroups = restClient.get().uri("/groups/user/{id}", targetUserId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", userGroups);
                    redirectAttrs.addFlashAttribute("resultType", "groupList");
                    break;
                case "create-group":
                    java.util.Map<String, String> body = new java.util.HashMap<>();
                    body.put("groupName", groupName);
                    String createdGroup = restClient.post().uri("/groups/create?adminID={id}", activeId).body(body).retrieve().body(String.class);
                    redirectAttrs.addFlashAttribute("resultData", createdGroup);
                    redirectAttrs.addFlashAttribute("resultType", "string");
                    break;
                case "get-admin-groups":
                    Object adminGroups = restClient.get().uri("/groups/admin/{id}", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", adminGroups);
                    redirectAttrs.addFlashAttribute("resultType", "groupList");
                    break;
                case "get-admin-count":
                    Object adminCount = restClient.get().uri("/groups/admin/{id}/count", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", adminCount);
                    redirectAttrs.addFlashAttribute("resultType", "count");
                    break;
                case "search-groups":
                    Object searchRes = restClient.get().uri("/groups/search?name={n}", groupName).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", searchRes);
                    redirectAttrs.addFlashAttribute("resultType", "groupList");
                    break;
                case "update-group":
                    Object updatedGroup = restClient.put().uri("/groups/{id}?newName={n}", groupID, groupName).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", updatedGroup);
                    redirectAttrs.addFlashAttribute("resultType", "singleGroup");
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

        return "redirect:/haji/services";
    }
}
