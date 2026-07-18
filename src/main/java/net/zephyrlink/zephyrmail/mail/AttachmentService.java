package net.zephyrlink.zephyrmail.mail;

import net.zephyrlink.zephyrmail.mail.Attachment;
import net.zephyrlink.zephyrmail.mail.Email;
import net.zephyrlink.zephyrmail.mail.AttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AttachmentService {

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024;

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public Attachment save(MultipartFile file, Email email) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Attachment exceeds 25MB limit.");
        }

        Attachment attachment = new Attachment();
        attachment.setEmail(email);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setMimeType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setFlaggedUnchecked(false);

        return attachmentRepository.save(attachment);
    }

    public List<Attachment> getByEmail(Email email) {
        return attachmentRepository.findByEmail(email);
    }

    public List<Attachment> getVaultByOwner(net.zephyrlink.zephyrmail.user.User owner) {
        return attachmentRepository.findByEmailOwnerOrderByUploadedAtDesc(owner);
    }
}