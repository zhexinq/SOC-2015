package controllers;

import com.google.gson.Gson;
import models.*;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.follow;

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
public class FollowController extends Controller {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    @Inject
    public FollowController(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

   // provide form for adding a new follow
    public Result newFollow() {
        return ok(follow.render(""));
    }

    // follow a user
    public Result addFollow() {
        FollowBean followBean = Form.form(FollowBean.class).bindFromRequest().get();
        String followerEmail = followBean.getFollowerEmail();
        String followeeEmail = followBean.getFolloweeEmail();

        try {
            User follower = userRepository.findByEmail(followerEmail);
            User followeee = userRepository.findByEmail(followeeEmail);
            Follow newFollow = new Follow(follower, followeee);
            Follow savedFollow = followRepository.save(newFollow);
            System.out.println("Follow relationship saved: "
                    + savedFollow.toString());
            return created(new Gson().toJson(savedFollow.getId()));
        } catch (PersistenceException pe) {
            pe.printStackTrace();
            System.out.println("Post not saved: follower email:" + followerEmail);
            return badRequest("Post not saved: follower email:" + followerEmail);
        }
    }

    // get all followers of a user by id
    public Result getFolloweesByUserId(Long id, String format) {
        if (id == null) {
            System.out.println("User id is null or empty!");
            return badRequest("User id is null or empty!");
        }

        User user = userRepository.findOne(id);
        List<Follow> follows = followRepository.findByFollowee(user);
        if (follows == null) {
            System.out.println("Follows not found with user id: " + id);
            return notFound("Follows not found with user id: " + id);
        }

        // get a list of users who are following the request user
        List<User> followers = new ArrayList<>();
        for (Follow follow : follows) {
            followers.add(follow.getFollower());
        }

        // package to JSON response
        String result = new String();
        if (format.equals("json")) {
            result = new Gson().toJson(followers);
        }
        return ok(result);
    }
}
