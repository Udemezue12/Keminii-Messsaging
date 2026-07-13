package com.astrotech.chat.migrations;


import com.astrotech.chat.entites.User;
import com.astrotech.chat.enums.UserRole;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ChangeUnit(id = "migration-v1-first-migration", order = "001", author = "com.astrotech")
public class FirstMigration {
    @Execution
    public void execution(MongoTemplate template){
        var query = new Query(Criteria.where("role").exists(false));
        var update = new Update()
                .set("role", UserRole.USER)
                .set("suspended", false)
                .set("deleted", false)
                .set("verified", false)
                .set("email", "")
                .set("phone_number", "")
                .set("password", "")
                .set("last_login", null)
                .set("deleted_at", null)
                        .set("verified_at", null)
                                .set("suspended_at", null);

        template.updateMulti(query, update, User.class);
    }
    @RollbackExecution
    public void rollbackExecution(MongoTemplate mongoTemplate) {

        var query = new Query(Criteria.where("role").is(UserRole.USER));
        var update = new Update()
                .unset("role")
                .unset("suspended")
                .unset("deleted")
                .unset("verified")
                .unset("email")
                .unset("phoneNumber")
                .unset("password")
                .unset("isActive")
                .unset("deletedAt")
                .unset("suspendedAt")
                .unset("verifiedAt");

        mongoTemplate.updateMulti(query, update, User.class);
    }
}
