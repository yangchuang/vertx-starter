ALTER TABLE app_daily_poetry DROP COLUMN popularity;
ALTER TABLE app_daily_poetry DROP COLUMN match_tags;
ALTER TABLE app_daily_poetry DROP COLUMN ip_address;
ALTER TABLE app_daily_poetry ALTER COLUMN dynasty DROP NOT NULL;
ALTER TABLE app_daily_poetry ALTER COLUMN author DROP NOT NULL;
