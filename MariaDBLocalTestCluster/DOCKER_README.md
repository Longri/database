run docker mariaDB cluster local

```bash
cd MariaDBLocalTestCluster
docker compose up -d 
```

# Create database user

open terminal and connect

```bash
docker exec -it local-mariadb-node1 /bin/bash
```

login to mariaDB

```bash
mysql -u root -p
```

enter password from docker-compose.yml   ' ***iamgroot*** '

create testuser with all privileges

```bash
CREATE USER 'admin'@'%' IDENTIFIED BY 'admin-pw';
GRANT GRANT OPTION ON *.* TO 'admin'@'%';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%';
FLUSH PRIVILEGES;
\q
```
