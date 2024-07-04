package com.picobase.console;

import com.picobase.PbManager;
import com.picobase.PbUtil;
import com.picobase.model.CollectionModel;
import com.picobase.persistence.repository.StorageContextHolder;
import org.springframework.boot.CommandLineRunner;


/**
 * 初始化Pb数据库系统表
 */

public class DatabaseInitializer implements CommandLineRunner {


    @Override
    public void run(String... args) throws Exception {
        PbManager.getLog().info("Initializing PB System Tables ...");
        createPbTables();
        initialTableData();
    }


    /**
     * 创建 PB 依赖表
     */
    private static void createPbTables() {
        String adminTable = """
                create table if not exists pb_admin
                (
                    id              char(20)      not null
                        primary key,
                    avatar          int default 0 not null,
                    email           varchar(100)  not null,
                    tokenKey        varchar(100)  null,
                    passwordHash    varchar(100)  not null,
                    lastResetSentAt datetime      null,
                    created         datetime      null,
                    updated         datetime      null,
                    constraint email
                        unique (email),
                    constraint token_key
                        unique (tokenKey)
                );
                                
                """;


        String collectionTable = """
                                create table if not exists pb_collection
                                (
                                    id         char(20)     null,
                                    name       varchar(255) null,
                                    type       varchar(255) null,
                                    `system`   bit          null,
                                    `schema`   json         null,
                                    indexes    json         null,
                                    listRule   varchar(255) null,
                                    viewRule   varchar(255) null,
                                    createRule varchar(255) null,
                                    updateRule varchar(255) null,
                                    deleteRule varchar(255) null,
                                    options    json         null,
                                    created    datetime     null,
                                    updated    datetime     null
                                );
                """;

        String logTable = """
                create table if not exists pb_log
                (
                    id      varchar(32)                            not null
                        primary key,
                    created datetime     default CURRENT_TIMESTAMP not null,
                    updated datetime     default CURRENT_TIMESTAMP not null,
                    message varchar(255) default ''                null,
                    data    json                                   null,
                    level   decimal      default 0                 null,
                    rowid   decimal(13)  default 0                 null
                )
                    engine = MyISAM;
                """;

        String userTable = """
                create table if not exists users
                (
                    avatar                 varchar(100) null,
                    created                datetime     null,
                    email                  varchar(100) null,
                    emailVisibility        tinyint      null,
                    id                     varchar(100) null,
                    lastResetSentAt        datetime     null,
                    lastVerificationSentAt datetime     null,
                    name                   varchar(100) null,
                    passwordHash           varchar(100) null,
                    tokenKey               varchar(100) null,
                    updated                datetime     null,
                    username               varchar(100) null,
                    verified               tinyint      null
                );
                                
                                
                """;

        StorageContextHolder.addSqlContext(adminTable);
        StorageContextHolder.addSqlContext(collectionTable);
        StorageContextHolder.addSqlContext(logTable);
        StorageContextHolder.addSqlContext(userTable);

        PbManager.getPbDatabaseOperate().blockUpdate(); //先创建表
    }

    /**
     * 初始化 Collection 中数据（用户表、日志表）
     */
    private static void initialTableData() {
        if (PbUtil.findById(CollectionModel.class, "_pb_users_auth_") == null) {
            String insertUserCollection = """
                    INSERT INTO pb_collection (id, name, type, `system`, `schema`, indexes, listRule, viewRule, createRule, updateRule, deleteRule, options, created, updated) VALUES ('_pb_users_auth_', 'users', 'auth', false, '[{"id": "users_name", "name": "name", "type": "text", "system": false, "unique": false, "options": {"max": null, "min": null, "pattern": ""}, "required": false, "presentable": false}, {"id": "users_avatar", "name": "avatar", "type": "file", "system": false, "unique": false, "options": {"thumbs": [], "maxSize": 5242880, "maxSelect": 1, "mimeTypes": ["image/jpeg", "image/png", "image/svg+xml", "image/gif", "image/webp"], "protected": false}, "required": false, "presentable": false}]', '[]', 'id = @request.auth.id', 'id = @request.auth.id', '', 'id = @request.auth.id', 'id = @request.auth.id', '{"manageRule": null, "onlyVerified": false, "requireEmail": false, "allowEmailAuth": true, "allowOAuth2Auth": true, "onlyEmailDomains": null, "allowUsernameAuth": true, "minPasswordLength": 8, "exceptEmailDomains": null}', null, null);
                    """;
            StorageContextHolder.addSqlContext(insertUserCollection);
        }

        if (PbUtil.findById(CollectionModel.class, "_pb_log_") == null) {
            String insertLogCollection = """
                    INSERT INTO pb_collection (id, name, type, `system`, `schema`, indexes, listRule, viewRule, createRule, updateRule, deleteRule, options, created, updated) VALUES ('_pb_log_', 'pb_log', 'base', true, '[{"id": "SDOr9yk4", "name": "message", "type": "text", "system": false, "options": {}, "required": false, "presentable": false}, {"id": "weRqt73h", "name": "data", "type": "json", "system": false, "options": {"maxSize": "2000000"}, "required": false, "presentable": false}, {"id": "TZzwlAi5", "name": "level", "type": "number", "system": false, "options": {}, "required": false, "presentable": false}, {"id": "5KobWFmH", "name": "rowid", "type": "number", "system": false, "options": {}, "required": false, "presentable": false}]', '[]', null, null, null, null, null, '{}', '2024-06-27 13:52:05', '2024-06-28 10:39:38');
                    """;

            StorageContextHolder.addSqlContext(insertLogCollection);
        }

        PbManager.getPbDatabaseOperate().blockUpdate();
    }
}
