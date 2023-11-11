#login postgres database with user postgres
CREATE USER sky_app WITH ENCRYPTED PASSWORD '{password}';
CREATE DATABASE sky_apps;

#login sky_apps database with user postgres
GRANT ALL ON SCHEMA public TO sky_app;

