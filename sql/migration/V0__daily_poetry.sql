CREATE TABLE app_daily_poetry (
    id SERIAL PRIMARY KEY,
    date TEXT NOT NULL,
    content TEXT NOT NULL,
    popularity INTEGER NULL,
    title TEXT NOT NULL,
    dynasty TEXT NOT NULL,
    author TEXT NOT NULL,
    origin_content TEXT[] NOT NULL,
    match_tags TEXT[] NULL,
    img_list TEXT[],
    ip_address INET,
    create_time TIMESTAMPTZ DEFAULT NOW(),
    update_time TIMESTAMPTZ DEFAULT NOW()
);
COMMENT ON TABLE app_daily_poetry IS '每日诗词表';
COMMENT ON COLUMN app_daily_poetry.id IS '自增ID';
COMMENT ON COLUMN app_daily_poetry.date IS '日期';
COMMENT ON COLUMN app_daily_poetry.content IS '核心诗句';
COMMENT ON COLUMN app_daily_poetry.popularity IS '诗句的流行度评价';
COMMENT ON COLUMN app_daily_poetry.title IS '诗句的标题';
COMMENT ON COLUMN app_daily_poetry.dynasty IS '作者所属的朝代';
COMMENT ON COLUMN app_daily_poetry.author IS '作者姓名';
COMMENT ON COLUMN app_daily_poetry.origin_content IS '诗句的原始内容';
COMMENT ON COLUMN app_daily_poetry.match_tags IS '与诗句相关的标签';
COMMENT ON COLUMN app_daily_poetry.img_list IS '核心诗句DALL-E API创建的图片地址列表';
COMMENT ON COLUMN app_daily_poetry.ip_address IS '调用今日诗词API的IP地址';
COMMENT ON COLUMN app_daily_poetry.create_time IS '创建时间';
COMMENT ON COLUMN app_daily_poetry.update_time IS '更新时间';

CREATE TABLE app_daily_poetry_user (
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
COMMENT ON TABLE app_daily_poetry_user IS '每日诗词用户表';
COMMENT ON COLUMN app_daily_poetry_user.id IS '自增ID';
COMMENT ON COLUMN app_daily_poetry_user.open_id IS '用户的OpenID';
COMMENT ON COLUMN app_daily_poetry_user.union_id IS '用户的UnionID';
COMMENT ON COLUMN app_daily_poetry_user.nick_name IS '用户的昵称';
COMMENT ON COLUMN app_daily_poetry_user.avatar_url IS '用户的头像URL';
COMMENT ON COLUMN app_daily_poetry_user.create_time IS '创建时间';
COMMENT ON COLUMN app_daily_poetry_user.update_time IS '更新时间';
COMMENT ON COLUMN app_daily_poetry_user.last_login_time IS '最后登录时间';

CREATE TABLE app_daily_poetry_user_access_log (
      id SERIAL PRIMARY KEY,
      date TEXT NOT NULL,
      open_id TEXT NOT NULL,
      first_login_time TIMESTAMPTZ DEFAULT NOW(),
      last_login_time TIMESTAMPTZ DEFAULT NOW(),
      access_count smallint DEFAULT 1
);
COMMENT ON TABLE app_daily_poetry_user_access_log IS '每日诗词用户访问记录表';
COMMENT ON COLUMN app_daily_poetry_user_access_log.id IS '自增ID';
COMMENT ON COLUMN app_daily_poetry_user_access_log.date IS '日期';
COMMENT ON COLUMN app_daily_poetry_user_access_log.open_id IS '用户open_id';
COMMENT ON COLUMN app_daily_poetry_user_access_log.first_login_time IS '当天首次登录时间';
COMMENT ON COLUMN app_daily_poetry_user_access_log.last_login_time IS '当天最后登录时间';
COMMENT ON COLUMN app_daily_poetry_user_access_log.access_count IS '当天访问次数';

--
--CREATE TABLE app_daily_poetry_api_token(
--  id SMALLINT PRIMARY KEY,
--  jrsc_token TEXT NOT NULL,
--  oai_token TEXT NOT NULL
--);
--COMMENT ON TABLE app_daily_poetry_api_token IS 'API token表';
--COMMENT ON COLUMN app_daily_poetry_api_token.id IS '自增ID';
--COMMENT ON COLUMN app_daily_poetry_api_token.jrsc_token IS '今日诗词 API token';
--COMMENT ON COLUMN app_daily_poetry_api_token.oai_token IS 'OpenAI API token';
