package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.PersistenceException;

import models.*;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import util.Common;
import views.html.index;

/**
 * The main set of web services.
 */
@Named
@Singleton
public class PostController extends Controller {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // We are using constructor injection to receive a repository to support our
    // desire for immutability.
    @Inject
    public PostController(final PostRepository postRepository, final UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // add a post to the database using http POST method
    public Result addPost() {
        // TODO: connect with front end data
        PostBean postBean = Form.form(PostBean.class).bindFromRequest().get();

//        JsonNode json = request().body().asJson();
//        if (json == null) {
//            System.out
//                    .println("Post not added, expecting Json data");
//            return badRequest("Post not added, expecting Json data");
//        }
//
//        // Parse JSON file
//        String content = json.findPath("content").asText();
//        int likes = json.findPath("likes").asInt();
//        Date createTime = new Date();
//        SimpleDateFormat format = new SimpleDateFormat(Common.DATE_PATTERN);
//        try {
//            createTime = format.parse(json.findPath("createTime").asText());
//        } catch (ParseException e) {
//            System.out.println("No creation date specified, set to current time");
//        }
//        String email = json.findPath("email").asText();

        String content = postBean.getContent();
        int likes = Integer.parseInt(postBean.getLikes());
        String email = postBean.getEmail();
        Date createTime = new Date();

        try {
            User user = userRepository.findByEmail(email);
            Post newPost = new Post(user, content, likes, createTime);
            Post savedPost = postRepository.save(newPost);
            System.out.println("Climate Service saved: "
                    + savedPost.getContent());
            return created(new Gson().toJson(savedPost.getId()));
        } catch (PersistenceException pe) {
            pe.printStackTrace();
            System.out.println("Post not saved: " + content);
            return badRequest("Post not saved: " + content);
        }
    }

    // get a post by post id
    public Result getPost(Long id, String format) {
        if (id == null) {
            System.out.println("Post id is null or empty!");
            return badRequest("Post id is null or empty!");
        }

        Post post = postRepository.findOne(id);

        if (post == null) {
            System.out.println("Post not found with with id: " + id);
            return notFound("Post not found with with id: " + id);
        }
        String result = new String();
        if (format.equals("json")) {
            result = new Gson().toJson(post);
        }

        return ok(result);
    }

    // get all posts by a user id
    public Result getAllPostsByUserId(Long id, String format) {
        if (id == null) {
            System.out.println("User id is null or empty!");
            return badRequest("User id is null or empty!");
        }

        User user = userRepository.findOne(id);
        List<Post> posts = postRepository.findAllByUser(user);
        if (posts == null) {
            System.out.println("Posts not found with user id: " + id);
            return notFound("Posts service not found with user id: " + id);
        }

        String result = new String();
        if (format.equals("json")) {
            result = new Gson().toJson(posts);
        }

        return ok(result);
    }


    // provide form for backend testing
    public Result index() {
        return ok(index.render("I am the backend"));
    }



}
