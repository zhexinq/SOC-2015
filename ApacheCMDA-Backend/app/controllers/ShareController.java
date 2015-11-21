package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import models.*;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.share;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for processing request concerning sharing a post
 * Created by qiuzhexin on 11/16/15.
 */
@Named
@Singleton
public class ShareController extends Controller {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ShareRepository shareRepository;

    @Inject
    public ShareController(PostRepository postRepository, UserRepository userRepository, ShareRepository shareRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.shareRepository = shareRepository;
    }

    // provide form for adding a new share
    public Result newShare() {
        return ok(share.render(""));
    }

    // share a post of others
    public Result addSharePost() {
        /* for debug */
        ShareBean shareBean = Form.form(ShareBean.class).bindFromRequest().get();
        String sharerEmail = shareBean.getSharerEmail();
        Long postId = Long.parseLong(shareBean.getPostId());
//        JsonNode json = request().body().asJson();
//        if (json == null) {
//            System.out.println("Share not created, expecting Json data");
//            return badRequest("Share not created, expecting Json data");
//        }
//
//        String sharerEmail = json.path("sharerEmail").asText();
//        Long postId = json.path("postId").asLong();

        try {
            User sharer = userRepository.findByEmail(sharerEmail);
            Post post = postRepository.findOne(postId);
            Share share = new Share(post, sharer);
            Share savedShare = shareRepository.save(share);
            System.out.println("Share relationship saved: "
                    + savedShare.toString());
            return created(new Gson().toJson(savedShare.getId()));
        } catch (PersistenceException pe) {
            pe.printStackTrace();
            System.out.println("Post not saved: sharer email:" + sharerEmail);
            return badRequest("Post not saved: sharer email:" + sharerEmail);
        }
    }

    // get all posts shared by a user in time descending order provided user id
    public Result getSharedPostsByUserId(Long id, String format) {
        if (id == null) {
            System.out.println("User id is null or empty!");
            return badRequest("User id is null or empty!");
        }

        User user = userRepository.findOne(id);
        List<Share> shares = shareRepository.findByUserOrderByCreateTimeDesc(user);
        if (shares == null) {
            System.out.println("Shares not found with user id: " + id);
            return notFound("Shares service not found with user id: " + id);
        }

        // get a list of posts
        List<Post> posts = new ArrayList<Post>();
        for (Share share : shares) {
            posts.add(share.getPost());
        }

        // get a list of creators associated with the posts
        List<User> users = new ArrayList<>();
        for (Post post : posts) {
            System.out.println("user: " + post.getUser());
            users.add(post.getUser());
        }

        // package to JSON response
        String result_posts = new String();

        if (format.equals("json")) {
            result_posts = new Gson().toJson(posts);
        }
        
        return ok(result_posts);
    }


    // delete a share by postId
    public Result deleteShareByPostId(Long postId) {
        Post post = postRepository.findOne(postId);
        Share share = shareRepository.findByPost(post);
        shareRepository.delete(share);
        return ok("delete");
    }
}
