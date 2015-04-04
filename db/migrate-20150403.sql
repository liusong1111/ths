ALTER TABLE users ADD COLUMN isonline char(255);
--是否在线  值：在线、离线
ALTER TABLE users ADD COLUMN status char(255);
--账号状态，值：封号、正常


-- 封号记录
CREATE TABLE isolatelog (
  id            INT(11) PRIMARY KEY,
  admin_id      INT(11),
  user_id       INT(11),
  operated_time timestamp DEFAULT CURRENT_TIMESTAMP,
  begin_time    timestamp DEFAULT CURRENT_TIMESTAMP,
  end_time      timestamp DEFAULT CURRENT_TIMESTAMP,
  reason        TEXT
);

-- 管理员表
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

INSERT INTO admins (idcard, worker_id, adder, username, password, provence, power, email, sex, phone, picture)
VALUES ('111', 1, 'root', 'admin', 'admin', 'TJ', 1, 'admin@tonghang.com', '男', '111', '11.jpg');
