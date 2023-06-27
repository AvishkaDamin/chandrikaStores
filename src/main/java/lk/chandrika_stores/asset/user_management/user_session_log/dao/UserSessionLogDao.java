package lk.chandrika_stores.asset.user_management.user_session_log.dao;

import lk.chandrika_stores.asset.user_management.user.entity.User;
import lk.chandrika_stores.asset.user_management.user_session_log.entity.UserSessionLog;
import lk.chandrika_stores.asset.user_management.user_session_log.entity.enums.UserSessionLogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionLogDao extends JpaRepository<UserSessionLog, Integer > {
    UserSessionLog findByUserAndUserSessionLogStatus(User user, UserSessionLogStatus userSessionLogStatus);
}
