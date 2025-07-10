package com.javarecipe.backend.comment.controller;

import com.javarecipe.backend.comment.dto.CommentDTO;
import com.javarecipe.backend.comment.dto.CommentRequest;
import com.javarecipe.backend.comment.entity.Comment;
import com.javarecipe.backend.comment.service.CommentService;
import com.javarecipe.backend.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<?> createComment(
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal User currentUser) {

        try {
            Comment comment;
            
            // Check if it's a reply or a top-level comment
            if (commentRequest.getParentCommentId() != null) {
                comment = commentService.createReply(
                        commentRequest.getParentCommentId(),
                        commentRequest.getContent(),
                        currentUser.getId());
            } else if (commentRequest.getRecipeId() != null) {
                comment = commentService.createComment(
                        commentRequest.getRecipeId(),
                        commentRequest.getContent(),
                        currentUser.getId());
            } else {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Either recipeId or parentCommentId must be provided")
                );
            }
            
            CommentDTO commentDTO = convertToDTO(comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(commentDTO);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to create comment: " + e.getMessage())
            );
        }
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<?> getCommentsByRecipe(
            @PathVariable Long recipeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            Page<Comment> comments = commentService.getCommentsByRecipeId(recipeId, pageable);
            
            Page<CommentDTO> commentDTOs = comments.map(this::convertToDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("comments", commentDTOs.getContent());
            response.put("currentPage", commentDTOs.getNumber());
            response.put("totalItems", commentDTOs.getTotalElements());
            response.put("totalPages", commentDTOs.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to retrieve comments: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<?> getRepliesByComment(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
            Page<Comment> replies = commentService.getRepliesByCommentId(commentId, pageable);
            
            Page<CommentDTO> replyDTOs = replies.map(this::convertToDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("replies", replyDTOs.getContent());
            response.put("currentPage", replyDTOs.getNumber());
            response.put("totalItems", replyDTOs.getTotalElements());
            response.put("totalPages", replyDTOs.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to retrieve replies: " + e.getMessage())
            );
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest commentRequest,
            @AuthenticationPrincipal User currentUser) {

        try {
            Comment updatedComment = commentService.updateComment(
                    commentId,
                    commentRequest.getContent(),
                    currentUser.getId());
            
            return ResponseEntity.ok(convertToDTO(updatedComment));
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to update comment: " + e.getMessage())
            );
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {

        try {
            commentService.deleteComment(commentId, currentUser.getId());
            return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to delete comment: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> getCommentById(@PathVariable Long commentId) {
        try {
            Comment comment = commentService.getCommentById(commentId);
            return ResponseEntity.ok(convertToDTO(comment));
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Failed to retrieve comment: " + e.getMessage())
            );
        }
    }

    // Helper method to convert Comment entity to CommentDTO
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userAvatarUrl(comment.getUser().getAvatarUrl())
                .recipeId(comment.getRecipe().getId())
                .likeCount(comment.getLikeCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
        
        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }
        
        // Count replies if it's a parent comment
        if (comment.getReplies() != null) {
            dto.setReplyCount(comment.getReplies().size());
        } else {
            dto.setReplyCount(0);
        }
        
        return dto;
    }
} 