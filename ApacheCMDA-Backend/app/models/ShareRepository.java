package models;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Share CRUD method implementation
 * Created by qiuzhexin on 11/16/15.
 */
public interface ShareRepository extends CrudRepository<Share, Long> {
    // find shares by user in time descending order
    List<Share> findByUserOrderByCreateTimeDesc(User user);
    Share findByPost(Post post);
}
