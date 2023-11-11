create DATABASE sky_apps;
CREATE USER sky_app WITH ENCRYPTED PASSWORD '{password}';
GRANT ALL PRIVILEGES ON DATABASE sky_apps TO sky_app;
