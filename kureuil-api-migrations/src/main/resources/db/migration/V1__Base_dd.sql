--
-- EXTENSIONS

-- check that the pgcrypto extension is enabled
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE api_tokens
( id     BIGSERIAL    PRIMARY KEY
, uid    uuid  UNIQUE NOT NULL
, token  CHAR(64)  NOT NULL
, read   BOOLEAN   NOT NULL DEFAULT(FALSE)
, write  BOOLEAN   NOT NULL DEFAULT(FALSE)
, admin  BOOLEAN   NOT NULL DEFAULT(FALSE)
, "comment" text DEFAULT NULL
);

-- INSERT RANDOM API KEYS

INSERT INTO api_tokens (uid, token, read, write, admin, "comment")
VALUES ( gen_random_uuid()
       , encode(digest(gen_random_bytes(64), 'sha256'), 'hex')::character(64)
       , true
       , true
       , true
       , 'admin'
       ),
       ( gen_random_uuid()
       , encode(digest(gen_random_bytes(64), 'sha256'), 'hex')::character(64)
       , true
       , false
       , false
       , 'debugging read-only access'
       );

-- CREATE BASE TABLES
CREATE TABLE users
( id BIGSERIAL PRIMARY KEY
, name VARCHAR(255) NOT NULL
, email VARCHAR(255) NOT NULL
, password VARCHAR(256) NOT NULL
, admin BOOLEAN NOT NULL DEFAULT(FALSE)
, UNIQUE(name, email)
);

CREATE TABLE channels
( id BIGSERIAL PRIMARY KEY
, name VARCHAR(255) NOT NULL
, query text NOT NULL
, owner BIGINT NOT NULL REFERENCES users(id)
, UNIQUE(name, query, owner)
);

CREATE TABLE user_channels
( id_user BIGINT NOT NULL REFERENCES users (id)
, id_channel BIGINT NOT NULL REFERENCES channels (id)

, PRIMARY KEY  (id_user, id_channel)
, UNIQUE       (id_user, id_channel)
);

CREATE TABLE links
( id BIGSERIAL PRIMARY KEY
, url VARCHAR(2083)
, UNIQUE (url)
);

CREATE TABLE tags
( id BIGSERIAL PRIMARY KEY
, name VARCHAR(255)
, UNIQUE (name)
);

CREATE TABLE link_tags
( id_link BIGINT NOT NULL REFERENCES links (id)
, id_tag BIGINT NOT NULL REFERENCES tags (id)
, PRIMARY KEY (id_link, id_tag)
, UNIQUE (id_link, id_tag)
);