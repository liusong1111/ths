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