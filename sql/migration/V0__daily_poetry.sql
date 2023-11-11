CREATE TABLE app_aphorism_poetry_common (
    id SERIAL PRIMARY KEY,
    date TEXT,
    aphorism TEXT,
    aphorism_author TEXT,
    poetry TEXT,
    poetry_author TEXT,
    img_list JSONB,
    create_time TIMESTAMPTZ DEFAULT NOW(),
    update_time TIMESTAMPTZ DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE app_aphorism_poetry_common IS '每日金句诗词公共数据表';
COMMENT ON COLUMN app_aphorism_poetry_common.id IS '自增ID';
COMMENT ON COLUMN app_aphorism_poetry_common.date IS '日期';
COMMENT ON COLUMN app_aphorism_poetry_common.aphorism IS '格言';
COMMENT ON COLUMN app_aphorism_poetry_common.aphorism_author IS '格言作者';
COMMENT ON COLUMN app_aphorism_poetry_common.poetry IS '诗词';
COMMENT ON COLUMN app_aphorism_poetry_common.poetry_author IS '诗词作者';
COMMENT ON COLUMN app_aphorism_poetry_common.img_list IS '图片地址列表';
COMMENT ON COLUMN app_aphorism_poetry_common.create_time IS '创建时间';
COMMENT ON COLUMN app_aphorism_poetry_common.update_time IS '更新时间';
COMMENT ON COLUMN app_aphorism_poetry_common.deleted IS '逻辑删除标识';

CREATE TABLE app_aphorism_poetry_user (
    id SERIAL PRIMARY KEY,
    open_id TEXT,
    union_id TEXT,
    nick_name TEXT,
    avatar_url TEXT,
    create_time TIMESTAMPTZ DEFAULT NOW(),
    update_time TIMESTAMPTZ DEFAULT NOW(),
    last_login_time TIMESTAMPTZ DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE app_aphorism_poetry_user IS '每日金句诗词用户表';
COMMENT ON COLUMN app_aphorism_poetry_user.id IS '自增ID';
COMMENT ON COLUMN app_aphorism_poetry_user.open_id IS '用户的OpenID';
COMMENT ON COLUMN app_aphorism_poetry_user.union_id IS '用户的UnionID';
COMMENT ON COLUMN app_aphorism_poetry_user.nick_name IS '用户的昵称';
COMMENT ON COLUMN app_aphorism_poetry_user.avatar_url IS '用户的头像URL';
COMMENT ON COLUMN app_aphorism_poetry_user.create_time IS '创建时间';
COMMENT ON COLUMN app_aphorism_poetry_user.update_time IS '更新时间';
COMMENT ON COLUMN app_aphorism_poetry_user.last_login_time IS '最后登录时间';
COMMENT ON COLUMN app_aphorism_poetry_user.deleted IS '逻辑删除标识';

-- 测试数据
INSERT INTO app_aphorism_poetry_common (date, aphorism, aphorism_author, poetry, poetry_author, img_list) VALUES
('2023-11-09', '“人生最美妙的风景，竟是内心的淡定与从容。我们曾如此期盼外界的认可，到最后才知道，世界是自己的，与他人毫无关系。”', '杨绛', '醉后不知天在水，满船清梦压星河', NULL, '["https://skytools.cn/images/poetry/1.jpeg", "https://skytools.cn/images/poetry/2.jpeg"]')
