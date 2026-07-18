package net.zephyrlink.zephyrmail.mail.flow;

import net.zephyrlink.zephyrmail.mail.flow.MailFlowRule;
import net.zephyrlink.zephyrmail.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailFlowRuleRepository extends JpaRepository<MailFlowRule, Long> {

    List<MailFlowRule> findByOwnerAndIsActiveTrue(User owner);

    List<MailFlowRule> findByOwnerOrderByIdDesc(User owner);
}