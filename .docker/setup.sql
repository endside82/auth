CREATE DATABASE IF NOT EXISTS mydatabase;

CREATE USER 'myuser'@'%' IDENTIFIED BY 'mypass';
GRANT CREATE, ALTER, INDEX, LOCK TABLES, REFERENCES, UPDATE, DELETE, DROP, SELECT, INSERT ON `mydatabase`.* TO 'myuser'@'%';

FLUSH PRIVILEGES;