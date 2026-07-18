package net.zephyrlink.zephyrmail.mail;

import net.zephyrlink.zephyrmail.mail.Label;
import net.zephyrlink.zephyrmail.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {

    // Grabs all custom labels/folders created by a specific user to populate their sidebar
    List<Label> findByOwner(User owner);

    // Looks up a label by owner and name for rule engine TAG action
    Optional<Label> findByOwnerAndName(User owner, String name);
}