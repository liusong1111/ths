CREATE TABLE users (
  id         INTEGER PRIMARY KEY,
  username   char(255),
  password   char(255),
  email      char(255),
  phone      char(255),
  sex        char(255),
  birth      char(255),
  image      char(255),
  created_at timestamp DEFAULT current_timestamp
);

CREATE TABLE labels (
  id         INTEGER PRIMARY KEY,
  label_name char(255),
  created_at timestamp DEFAULT current_timestamp
);

CREATE TABLE topics (
  id         INTEGER PRIMARY KEY,
  subject    char(255),
  body       TEXT,
  label_name char(255),
  creator_id INTEGER,
  created_at timestamp DEFAULT current_timestamp
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