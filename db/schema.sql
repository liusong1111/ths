CREATE TABLE users (
  id               INTEGER PRIMARY KEY,
  username         char(255),
  password         char(255),
  email            char(255),
  phone            char(255),
  sex              char(255),
  birth            char(255),
  city             char(255),
  image            char(255),
  huanxin_username char(255),
  isonline         char(255) DEFAULT '离线',
  status           char(255) DEFAULT '正常',
  created_at       timestamp DEFAULT current_timestamp,
  last_login_at    timestamp
);

CREATE TABLE labels (
  id         INTEGER PRIMARY KEY,
  label_name char(255),
  created_at timestamp DEFAULT current_timestamp
);

CREATE TABLE topics (
  id               INTEGER PRIMARY KEY,
  subject          char(255),
  body             TEXT,
  label_name       char(255),
  user_id          INTEGER,
  huanxin_group_id char(255),
  created_at       timestamp DEFAULT current_timestamp
);

CREATE TABLE replies (
  id         INTEGER PRIMARY KEY,
  topic_id   INTEGER,
  body       TEXT,
  user_id    INTEGER,
  created_at timestamp DEFAULT current_timestamp
);

CREATE TABLE user_labels (
  id         INTEGER PRIMARY KEY,
  user_id    INTEGER,
  label_name char(255),
  created_at timestamp DEFAULT current_timestamp
);

CREATE TABLE invitations (
  id         INTEGER PRIMARY KEY,
  inviter_id INTEGER,
  invitee_id INTEGER,
  reason     char(255),
  status     char(255) DEFAULT 'pending', --enum('pending', 'refused', 'agreed')
  created_at timestamp DEFAULT current_timestamp
);

CREATE TABLE friends (
  id         INTEGER PRIMARY KEY,
  user_id    INTEGER,
  friend_id  INTEGER,
  created_at timestamp DEFAULT current_timestamp
);

CREATE TABLE feedbacks (
  id         INTEGER PRIMARY KEY,
  user_id    INTEGER,
  content    TEXT,
  created_at timestamp DEFAULT current_timestamp
);

CREATE TABLE isolatelog (
  id            INT(11) PRIMARY KEY,
  admin_id      INT(11),
  user_id       INT(11),
  operated_time timestamp DEFAULT CURRENT_TIMESTAMP,
  begin_time    timestamp DEFAULT CURRENT_TIMESTAMP,
  end_time      timestamp DEFAULT CURRENT_TIMESTAMP,
  reason        TEXT
);

CREATE TABLE admins (
  id           INT(11) PRIMARY KEY,
  idcard       char(32),
  worker_id    INT(11),
  adder        char(255),
  username     char(255),
  password     char(255),
  provence     char(255),
  power        INT(10),
  email        char(255),
  sex          char(255),
  phone        char(255),
  picture      char(255),
  forget_time  timestamp,
  forget_token char(255),
  created_at   timestamp DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE system_config (
  id                   INTEGER PRIMARY KEY,
  can_register_user    BOOLEAN DEFAULT TRUE,
  can_login            BOOLEAN DEFAULT TRUE,
  can_upgrade_silently BOOLEAN DEFAULT TRUE
);

INSERT INTO admins (idcard, worker_id, adder, username, password, provence, power, email, sex, phone, picture)
VALUES ('111', 1, 'root', 'admin', 'admin', 'TJ', 1, 'admin@tonghang.com', '男', '111', '11.jpg');


INSERT INTO system_config (can_register_user, can_login, can_upgrade_silently) VALUES (1, 1, 0);