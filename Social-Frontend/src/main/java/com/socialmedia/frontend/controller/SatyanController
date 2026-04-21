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
@RequestMapping("/satyan")
public class SatyanController {

    private final RestClient restClient;
    private static final int MOCK_USER_ID = 1;

    public SatyanController(RestClient restClient) {
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

        return "satyan";
    }

    @PostMapping("/execute")
    public String executeEndpoint(
            @RequestParam String endpoint,
            @RequestParam(required = false) String postID,
            @RequestParam(required = false) String commentID,
            @RequestParam(required = false) String targetUserId,
            @RequestParam(required = false) String content,
            HttpSession session, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttrs) {

        int activeId = getActiveUserId(session);
        redirectAttrs.addFlashAttribute("executedEndpoint", endpoint);

        try {
            switch (endpoint) {
                // LIKES
                case "toggle-like":
                    String toggle = restClient.post().uri("/likes/toggle?postID={p}&userID={u}", postID, activeId).retrieve().body(String.class);
                    redirectAttrs.addFlashAttribute("resultData", toggle);
                    redirectAttrs.addFlashAttribute("resultType", "string");
                    break;
                case "get-post-likes":
                    Object likesList = restClient.get().uri("/likes/post/{p}", postID).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", likesList);
                    redirectAttrs.addFlashAttribute("resultType", "likeList");
                    break;
                case "check-like-status":
                    Boolean status = restClient.get().uri("/likes/check?postID={p}&userID={u}", postID, activeId).retrieve().body(Boolean.class);
                    redirectAttrs.addFlashAttribute("resultData", status);
                    redirectAttrs.addFlashAttribute("resultType", "boolean");
                    break;
                case "get-like-post-user":
                    Object singleLike = restClient.get().uri("/likes/post/{p}/user/{u}", postID, targetUserId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", singleLike);
                    redirectAttrs.addFlashAttribute("resultType", "singleLike");
                    break;

                // COMMENTS
                 case "add-comment":
                    java.util.Map<String, String> body = new java.util.HashMap<>();
                    body.put("commentText", content);
                    String addedCmt = restClient.post().uri("/comments/add?postID={p}&userID={u}", postID, activeId).body(body).retrieve().body(String.class);
                    redirectAttrs.addFlashAttribute("resultData", addedCmt);
                    redirectAttrs.addFlashAttribute("resultType", "string");
                    break;
                case "get-post-comments":
                    Object cmtList = restClient.get().uri("/comments/post/{p}", postID).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", cmtList);
                    redirectAttrs.addFlashAttribute("resultType", "commentList");
                    break;
                case "get-like-count":
                    Object likeCount = restClient.get().uri("/likes/count/{id}", postID).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", likeCount);
                    redirectAttrs.addFlashAttribute("resultType", "count");
                    break;
                case "total-likes-given":
                    Object totalLikes = restClient.get().uri("/likes/user/{id}/count", activeId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", totalLikes);
                    redirectAttrs.addFlashAttribute("resultType", "count");
                    break;
                case "comment-count":
                    Object cmtCount = restClient.get().uri("/comments/count/{id}", postID).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", cmtCount);
                    redirectAttrs.addFlashAttribute("resultType", "count");
                    break;
                case "top-posts":
                    Object topPosts = restClient.get().uri("/likes/top-posts").retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", topPosts);
                    redirectAttrs.addFlashAttribute("resultType", "topPostList");
                    break;
                case "get-comment":
                    Object singleComment = restClient.get().uri("/comments/{id}", commentID).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", singleComment);
                    redirectAttrs.addFlashAttribute("resultType", "singleComment");
                    break;
                case "get-user-comments":
                    Object userComments = restClient.get().uri("/comments/user/{id}", targetUserId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", userComments);
                    redirectAttrs.addFlashAttribute("resultType", "commentList");
                    break;
                case "get-post-user-comments":
                    Object puComments = restClient.get().uri("/comments/post/{pid}/user/{uid}", postID, targetUserId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", puComments);
                    redirectAttrs.addFlashAttribute("resultType", "commentList");
                    break;
                case "user-comment-count":
                    Object uCommentCount = restClient.get().uri("/comments/user/{id}/count", targetUserId).retrieve().body(Object.class);
                    redirectAttrs.addFlashAttribute("resultData", uCommentCount);
                    redirectAttrs.addFlashAttribute("resultType", "count");
                    break;
                case "update-comment":
                    java.util.Map<String, String> upBody = new java.util.HashMap<>();
                    upBody.put("commentText", content);
                    String updated = restClient.put().uri("/comments/{cid}?userID={uid}", commentID, activeId)
                            .body(upBody).retrieve().body(String.class);
                    redirectAttrs.addFlashAttribute("resultData", updated);
                    redirectAttrs.addFlashAttribute("resultType", "string");
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

        return "redirect:/satyan/services";
    }
}
