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
@RequestMapping("/gaurav")
public class GauravController {

    private final RestClient restClient;
    private static final int MOCK_USER_ID = 1;

    public GauravController(RestClient restClient) {
        this.restClient = restClient;
    }

    private int getActiveUserId(HttpSession session) {
        Integer id = (Integer) session.getAttribute("currentUserId");
        return id != null ? id : MOCK_USER_ID;
    }

    @GetMapping("/services")
    public String index(@RequestParam(required = false, defaultValue = "idle") String action, HttpSession session, Model model) {
        if ("get-all-users".equals(action) && session.getAttribute("currentUserId") == null) {
            return "redirect:/login";
        }

        int activeId = getActiveUserId(session);
        model.addAttribute("activeUserId", activeId);

        try {
            java.util.List<?> postList = restClient.get().uri("/posts/user/{id}", activeId).retrieve().body(java.util.List.class);
            model.addAttribute("statPosts", postList != null ? postList.size() : 0);
        } catch (Exception e) {
            model.addAttribute("statPosts", 0);
        }

        model.addAttribute("mode", model.containsAttribute("executedEndpoint") ? "result" : "configure");
        model.addAttribute("configureEndpoint", action);

        return "gaurav";
    }

    @PostMapping("/execute")
    public String executeEndpoint(
            @RequestParam String endpoint,
            @RequestParam(required = false) String targetUserId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String postID,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String keyword,
            HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {

        if ("get-all-users".equals(endpoint) && session.getAttribute("currentUserId") == null) {
            return "redirect:/login";
        }

        int activeId = getActiveUserId(session);
        redirectAttrs.addFlashAttribute("executedEndpoint", endpoint);

        try {
            switch (endpoint) {
                // USERS
                case "get-all-users":
                    Object allUsers = restClient.get().uri("/users/all").retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", allUsers);
                    redirectAttrs.addFlashAttribute("resultType", "userList");
                    break;
                case "get-user":
                    Object singleUser = restClient.get().uri("/users/{id}", targetUserId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", singleUser);
                    redirectAttrs.addFlashAttribute("resultType", "singleUser");
                    break;
                case "get-user-by-username":
                    Object singleUserByUn = restClient.get().uri("/users/username/{un}", username).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", singleUserByUn);
                    redirectAttrs.addFlashAttribute("resultType", "singleUser");
                    break;
                case "search-users":
                    Object searchUsers = restClient.get().uri("/users/search?query={q}", keyword).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", searchUsers);
                    redirectAttrs.addFlashAttribute("resultType", "userList");
                    break;
                case "get-me":
                    Object me = restClient.get().uri("/users/{id}", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", me);
                    redirectAttrs.addFlashAttribute("resultType", "singleUser");
                    break;
                case "update-user":
                    java.util.Map<String, String> updateBody = new java.util.HashMap<>();
                    if(username != null && !username.isEmpty()) {
						updateBody.put("username", username);
					}
                    if(email != null && !email.isEmpty()) {
						updateBody.put("email", email);
					}
                    if(password != null && !password.isEmpty()) {
						updateBody.put("password", password);
					}
                    Object updatedUser = restClient.put().uri("/users/{id}", activeId).body(updateBody).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", updatedUser);
                    redirectAttrs.addFlashAttribute("resultType", "singleUser");
                    break;

                // POSTS
                case "get-all-posts":
                    Object allPosts = restClient.get().uri("/posts/all").retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", allPosts);
                    redirectAttrs.addFlashAttribute("resultType", "postList");
                    break;
                case "get-user-posts":
                    Object userPosts = restClient.get().uri("/posts/username/{un}", username).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", userPosts);
                    redirectAttrs.addFlashAttribute("resultType", "postList");
                    break;
                case "get-single-post":
                    Object singlePost = restClient.get().uri("/posts/{id}", postID).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", singlePost);
                    redirectAttrs.addFlashAttribute("resultType", "singlePost");
                    break;
                case "create-post":
                    java.util.Map<String, Object> body = new java.util.HashMap<>();
                    body.put("content", content);
                    String crPost = restClient.post().uri("/posts/create?userID={id}", activeId).body(body).retrieve().body(String.class);
                    redirectAttrs.addFlashAttribute("resultData", crPost);
                    redirectAttrs.addFlashAttribute("resultType", "string");
                    break;
                case "update-post":
                    java.util.Map<String, Object> ubody = new java.util.HashMap<>();
                    ubody.put("content", content);
                    Object upPost = restClient.put().uri("/posts/id/{id}", postID).body(ubody).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", upPost);
                    redirectAttrs.addFlashAttribute("resultType", "singlePost");
                    break;
                case "delete-post":
                    String delPost = restClient.delete().uri("/posts/id/{id}", postID).retrieve().body(String.class);
                    redirectAttrs.addFlashAttribute("resultData", delPost);
                    redirectAttrs.addFlashAttribute("resultType", "string");
                    break;
                case "search-posts":
                    Object searchResults = restClient.get().uri("/posts/search?keyword={k}", keyword).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", searchResults);
                    redirectAttrs.addFlashAttribute("resultType", "postList");
                    break;

                case "get-post-by-user-and-id":
                    Object singlePostByUser = restClient.get().uri("/posts/username/{un}/{id}", username, postID).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", singlePostByUser);
                    redirectAttrs.addFlashAttribute("resultType", "singlePost");
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

        return "redirect:/gaurav/services";
    }
}
