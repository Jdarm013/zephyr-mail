package net.zephyrlink.zephyrmail.contact;

import net.zephyrlink.zephyrmail.contact.Contact;
import net.zephyrlink.zephyrmail.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByOwnerOrderByDisplayNameAsc(User owner);

    List<Contact> findByOwnerAndEmailAddressContainingOrOwnerAndDisplayNameContaining(
            User owner, String emailQuery, User owner2, String nameQuery);

    Optional<Contact> findByOwnerAndEmailAddress(User owner, String emailAddress);
}