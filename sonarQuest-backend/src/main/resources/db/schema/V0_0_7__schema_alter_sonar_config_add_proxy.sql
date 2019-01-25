-- Proxy configuration option for SonarQube Server --
ALTER TABLE Sonar_Config ADD COLUMN proxy VARCHAR(128);
ALTER TABLE Sonar_Config ADD COLUMN proxy_port VARCHAR(10);