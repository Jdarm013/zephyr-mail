package net.zephyrlink.zephyrmail.contact;

import net.zephyrlink.zephyrmail.contact.Contact;
import net.zephyrlink.zephyrmail.user.User;
import net.zephyrlink.zephyrmail.contact.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Contact save(User owner, String emailAddress, String displayName, String notes) {
        Optional<Contact> existing = contactRepository.findByOwnerAndEmailAddress(owner, emailAddress);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Contact already exists for this address.");
        }
        Contact contact = new Contact();
        contact.setOwner(owner);
        contact.setEmailAddress(emailAddress);
        contact.setDisplayName(displayName);
        contact.setNotes(notes);
        return contactRepository.save(contact);
    }

    public List<Contact> getByOwner(User owner) {
        return contactRepository.findByOwnerOrderByDisplayNameAsc(owner);
    }

    public List<Contact> search(User owner, String query) {
        return contactRepository.findByOwnerAndEmailAddressContainingOrOwnerAndDisplayNameContaining(
                owner, query, owner, query);
    }

    // Returns false if contact does not belong to the requesting user
    public boolean delete(Long contactId, User requestingUser) {
        Contact contact = contactRepository.findById(contactId).orElseThrow();
        if (!contact.getOwner().getId().equals(requestingUser.getId())) {
            return false;
        }
        contactRepository.deleteById(contactId);
        return true;
    }

    // Returns null if contact does not belong to the requesting user
    public Contact update(Long contactId, String displayName, String notes, User requestingUser) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found."));
        if (!contact.getOwner().getId().equals(requestingUser.getId())) {
            return null;
        }
        contact.setDisplayName(displayName);
        contact.setNotes(notes);
        return contactRepository.save(contact);
    }
}