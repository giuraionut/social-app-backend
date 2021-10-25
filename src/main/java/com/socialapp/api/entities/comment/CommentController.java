package com.socialapp.api.entities.comment;

import com.socialapp.api.entities.post.Post;
import com.socialapp.api.entities.post.PostService;
import com.socialapp.api.entities.user.User;
import com.socialapp.api.entities.user.UserService;
import com.socialapp.api.jwt.JwtUtils;
import com.socialapp.api.response.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "comment")
@AllArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @PostMapping(path = "post/{postId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> createComment(@RequestBody Comment comment, @PathVariable("postId") String postId, HttpServletRequest request) {
        Response response = new Response();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(HttpStatus.OK);
        response.setError("none");
        response.setMessage("Comment added successfully");
        String userId = jwtUtils.decodeToken(request, "jwt", "userId");
        User user = this.userService.getById(userId);

        Post post = this.postService.getById(postId);
        comment.setParent(true);
        post.addComment(comment);
        user.addComment(comment);
        Comment addedComment = this.commentService.add(comment);

        response.setPayload(addedComment);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "reply/{commentId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> replyToComment(@RequestBody Comment comment, @PathVariable("commentId") String commentId, HttpServletRequest request) {
        Response response = new Response();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(HttpStatus.OK);
        response.setError("none");
        response.setMessage("Comment added successfully");

        String userId = jwtUtils.decodeToken(request, "jwt", "userId");
        User user = this.userService.getById(userId);

        Comment parentComment = this.commentService.getById(commentId);
        comment.setParent(parentComment);
        comment.setAuthor(user);
        comment.setPost(parentComment.getPost());
        parentComment.addChild(comment);
        Comment childComment = this.commentService.add(comment);
        response.setPayload(childComment);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "post/{postId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> getByPost(@PathVariable("postId") String postId, HttpServletRequest request) {
        Response response = new Response();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(HttpStatus.OK);
        response.setError("none");
        response.setMessage("Comments {by postId} obtained successfully");

        List<Comment> comments = this.commentService.getByPostId(postId);

        response.setPayload(comments);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "{commentId}/childs")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> getChilds(@PathVariable("commentId") String commentId, HttpServletRequest request) {
        Response response = new Response();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(HttpStatus.OK);
        response.setError("none");
        response.setMessage("Comments {by postId} obtained successfully");

        Comment parentComment = this.commentService.getById(commentId);
        List<Comment> comments = parentComment.getChilds();

        response.setPayload(comments);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "owned")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> getOwnedComments(HttpServletRequest request) {
        Response response = new Response();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(HttpStatus.OK);
        response.setError("none");
        String userId = jwtUtils.decodeToken(request, "jwt", "userId");
        User user = this.userService.getById(userId);

        List<Comment> comments = user.getOwnedComments();
        if (!comments.isEmpty()) {
            response.setPayload(comments);
            response.setMessage("Owned comments by user obtained successfully");
        } else {
            response.setMessage("No owned communities found for user");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
