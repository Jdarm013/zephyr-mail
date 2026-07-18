package net.zephyrlink.zephyrmail.mail;

import net.zephyrlink.zephyrmail.mail.Attachment;
import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByEmail(Email email);

    List<Attachment> findByEmailOwnerOrderByUploadedAtDesc(User owner);
}