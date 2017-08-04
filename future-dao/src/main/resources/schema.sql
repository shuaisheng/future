DROP DATABASE IF EXISTS future;

CREATE DATABASE future
  DEFAULT CHARACTER SET utf8
  COLLATE utf8_general_ci;

USE future;

-- ----------------------------
--  Table structure for role
-- ----------------------------
DROP TABLE
IF EXISTS role;

CREATE TABLE role
(
  id           BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL
  COMMENT '主键, 自增',
  code         VARCHAR(32)                           NOT NULL
  COMMENT '角色代码',
  name         VARCHAR(32)                           NOT NULL
  COMMENT '角色名称',
  is_deleted   TINYINT                               NOT NULL                DEFAULT 0
  COMMENT '逻辑删除:{0:未删除, 1:已删除}',
  created_time TIMESTAMP                             NOT NULL                DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  updated_time TIMESTAMP                             NOT NULL                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  COMMENT '更新时间'
)
  COMMENT '角色表';
CREATE UNIQUE INDEX id_UNIQUE
  ON role (id);
CREATE INDEX created_time_ix
  ON role (created_time);
CREATE UNIQUE INDEX code_UNIQUE
  ON role (code);

-- ----------------------------
--  Table structure for menu
-- ----------------------------
DROP TABLE
IF EXISTS menu;

CREATE TABLE menu
(
  id           BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL
  COMMENT '主键, 自增',
  code         VARCHAR(32)                           NOT NULL
  COMMENT '菜单代码',
  name         VARCHAR(32)                           NOT NULL
  COMMENT '菜单名称',
  pcode        VARCHAR(32)                           NOT NULL                DEFAULT ''
  COMMENT '父菜单代码',
  url          VARCHAR(128)                          NOT NULL                DEFAULT ''
  COMMENT '菜单地址',
  sort         INT(11)                               NOT NULL                DEFAULT 0
  COMMENT '菜单排序(从0开始)',
  icon         VARCHAR(128)                          NOT NULL                DEFAULT ''
  COMMENT '菜单图标的样式',
  is_deleted   TINYINT                               NOT NULL                DEFAULT 0
  COMMENT '逻辑删除:{0:未删除, 1:已删除}',
  created_time TIMESTAMP                             NOT NULL                DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  updated_time TIMESTAMP                             NOT NULL                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  COMMENT '更新时间'
)
  COMMENT '菜单表';
CREATE UNIQUE INDEX id_UNIQUE
  ON menu (id);
CREATE INDEX created_time_ix
  ON menu (created_time);
CREATE INDEX sort_ix
  ON menu (sort);
CREATE UNIQUE INDEX code_UNIQUE
  ON menu (code);

-- ----------------------------
--  Table structure for user_role
-- ----------------------------
DROP TABLE
IF EXISTS user_role;

CREATE TABLE user_role
(
  username  VARCHAR(15) NOT NULL
  COMMENT '用户名',
  role_code VARCHAR(32) NOT NULL
  COMMENT '角色代码',
  PRIMARY KEY (username, role_code)
)
  COMMENT '用户角色表';

-- ----------------------------
--  Table structure for role_menu
-- ----------------------------
DROP TABLE
IF EXISTS role_menu;

CREATE TABLE role_menu
(
  role_code VARCHAR(32) NOT NULL
  COMMENT '角色代码',
  menu_code VARCHAR(32) NOT NULL
  COMMENT '菜单代码',
  PRIMARY KEY (role_code, menu_code)
)
  COMMENT '角色菜单表';

-- ----------------------------
--  Table structure for dictionary
-- ----------------------------
DROP TABLE
IF EXISTS dictionary;

CREATE TABLE dictionary
(
  id           BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL
  COMMENT '主键, 自增',
  code         VARCHAR(32)                           NOT NULL
  COMMENT '代码',
  value        VARCHAR(128)                          NOT NULL
  COMMENT '值',
  type         VARCHAR(16)                           NOT NULL
  COMMENT '类型',
  sort         INT(11)                               NOT NULL                DEFAULT 0
  COMMENT '排序(从0开始)',
  is_deleted   TINYINT                               NOT NULL                DEFAULT 0
  COMMENT '逻辑删除:{0:未删除, 1:已删除}',
  created_time TIMESTAMP                             NOT NULL                DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  updated_time TIMESTAMP                             NOT NULL                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  COMMENT '更新时间'
)
  COMMENT '字典表';
CREATE UNIQUE INDEX id_UNIQUE
  ON dictionary (id);
CREATE UNIQUE INDEX type_code_UNIQUE
  ON dictionary (type, code);
CREATE INDEX created_time_ix
  ON dictionary (created_time);
CREATE INDEX type_ix
  ON dictionary (type);
CREATE INDEX sort_ix
  ON dictionary (sort);

-- ----------------------------
--  Table structure for user
-- ----------------------------
DROP TABLE
IF EXISTS user;

CREATE TABLE user
(
  id            BIGINT(20) PRIMARY KEY AUTO_INCREMENT NOT NULL
  COMMENT '主键, 自增',
  username      VARCHAR(20)                           NOT NULL
  COMMENT '用户名（手机号）',
  realname      VARCHAR(32)                           NOT NULL                    DEFAULT ''
  COMMENT '真实姓名',
  password      VARCHAR(64)                           NOT NULL
  COMMENT '密码',
  salt          VARCHAR(64)                           NOT NULL
  COMMENT '密码盐',
  small_avatar  VARCHAR(256)                          NOT NULL                    DEFAULT ''
  COMMENT '小头像',
  medium_avatar VARCHAR(256)                          NOT NULL                    DEFAULT ''
  COMMENT '中头像',
  large_avatar  VARCHAR(256)                          NOT NULL                    DEFAULT ''
  COMMENT '大头像',
  email         VARCHAR(64)                           NOT NULL                    DEFAULT ''
  COMMENT '电子邮箱',
  is_deleted    TINYINT                               NOT NULL                    DEFAULT 0
  COMMENT '逻辑删除:{0:未删除, 1:已删除}',
  created_time  TIMESTAMP                             NOT NULL                    DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  updated_time  TIMESTAMP                             NOT NULL                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
  COMMENT '更新时间'
)
  COMMENT '用户表';
CREATE UNIQUE INDEX id_UNIQUE
  ON user (id);
CREATE INDEX created_time_ix
  ON user (created_time);
CREATE UNIQUE INDEX username_UNIQUE
  ON user (username);

#====================初始数据====================#

# 用户 admin
INSERT INTO user
(username, realname, password, salt, email)
VALUES
  ('15121149571', '管理员', '73905d6309ff111668671f2dea975cf30e3a05ae', 'fee043a9a167f462', 'java@kangyonggan.com');

-- ----------------------------
--  data for role
-- ----------------------------
INSERT INTO role
(code, name)
VALUES
  ('ROLE_ADMIN', '管理员'),
  ('ROLE_USER', '普通用户');

-- ----------------------------
--  data for menu
-- ----------------------------
INSERT INTO menu
(code, name, pcode, url, sort, icon)
VALUES
  ('DASHBOARD', '工作台', '', 'index', 0, 'menu-icon fa fa-dashboard'),

  ('SYSTEM', '系统', 'DASHBOARD', 'system', 1, 'menu-icon fa fa-cogs'),
  ('SYSTEM_USER', '用户管理', 'SYSTEM', 'system/user', 0, ''),
  ('SYSTEM_ROLE', '角色管理', 'SYSTEM', 'system/role', 1, ''),
  ('SYSTEM_MENU', '菜单管理', 'SYSTEM', 'system/menu', 2, ''),
  ('SYSTEM_CACHE', '缓存管理', 'SYSTEM', 'system/cache', 3, ''),
  ('SYSTEM_DICTIONARY', '数据字典', 'SYSTEM', 'system/dictionary', 4, ''),

  ('USER', '用户', 'DASHBOARD', 'user', 1, 'menu-icon fa fa-user'),
  ('USER_INFO', '基本信息', 'USER', 'user/info', 0, '');

-- ----------------------------
--  data for user_role
-- ----------------------------
INSERT INTO user_role
VALUES
  ('15121149571', 'ROLE_ADMIN');

-- ----------------------------
--  data for role_menu
-- ----------------------------
INSERT INTO role_menu SELECT
                        'ROLE_ADMIN',
                        code
                      FROM menu
                      WHERE code LIKE 'SYSTEM%' OR code = 'DASHBOARD';

INSERT INTO role_menu SELECT
                        'ROLE_USER',
                        code
                      FROM menu
                      WHERE code LIKE 'USER%' OR code = 'DASHBOARD';
