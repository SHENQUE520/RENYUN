## 架构概览

| 端           | 技术栈                         | 端口   | 职责 |
|-------------|-----------------------------|------|---|
| Java 主后端    | Spring Boot 4.1.1 + Java 17 | 8080 | 在线业务：登录注册、JWT 鉴权、Redis 缓存、AOP 限流、AI 网关 |

---

## Java 后端

### 本地运行
```bash
mvn clean package -DskipTests
```
确保安装和运行了：
* Redis
* MySQL 8.0+
* Java 17+

运行
```bash
java -DDATABASE_PASSWORD=你的数据库密码 -DDATABASE_HOST=你的数据库主机URL -DDATABASE_USERNAME=你的数据库用户名 -DREDIS_HOST=你的Redis主机 -DREDIS_PASSWORD=你的Redis密码 -DJWT_SECRET=不少于32字符的jwt密钥 -jar target/ReYun-0.0.1-SNAPSHOT.jar
```

---

## Ubuntu 部署

> 下面 11 步是**完整的部署流程**，不需要看上面的「本地运行」章节。

假设你是一台干净的 Ubuntu 22.04 / 24.04 服务器的 SSH 登录用户。

### 1. 安装依赖

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven mysql-server redis-server git curl
```

验证版本：
```bash
java -version    # 应输出 openjdk version "17.x.x"
mvn -version
mysql --version
redis-server --version
```

### 2. 初始化 MySQL

Ubuntu 上 MySQL 默认启用 `auth_socket`，所以用 `sudo mysql`（不需要 `-uroot -p`）登录。

先在当前 shell 生成一个随机密码，**不要跳步，后面还会用到**：

```bash
MP=$(openssl rand -hex 16)
echo "本次生成的 MySQL 密码：$MP"       # 记下来！环境变量文件要用
```

然后执行建库建用户：

```bash
sudo mysql -e "
CREATE DATABASE IF NOT EXISTS renyun DEFAULT CHARSET utf8mb4;
CREATE USER IF NOT EXISTS 'renyun'@'localhost' IDENTIFIED BY '$MP';
GRANT ALL PRIVILEGES ON renyun.* TO 'renyun'@'localhost';
FLUSH PRIVILEGES;
"
```

测试连通性：
```bash
mysql -urenyun -p"$MP" -e "SELECT 1"    # 应输出 1
```

> 如果是远程数据库，把 `@'localhost'` 换成 `@'%'` 并在 `/etc/mysql/mysql.conf.d/mysqld.cnf` 里把 `bind-address` 改成 `0.0.0.0`。

### 3. 初始化 Redis

```bash
RDP=$(openssl rand -hex 16)
echo "本次生成的 Redis 密码：$RDP"       # 记下来！环境变量文件要用

sudo sed -i "s/^# requirepass .*/requirepass $RDP/" /etc/redis/redis.conf
sudo sed -i 's/^bind 127.0.0.1 -::1/bind 127.0.0.1/' /etc/redis/redis.conf
sudo systemctl enable redis-server
sudo systemctl restart redis-server

redis-cli -a "$RDP" ping                 # 应返回 PONG
```

> `MP` 和 `RDP` 变量只在当前 shell 会话里存在。到第 6 步写 `/etc/renyun.env` 时，如果还在同一终端可以直接用 `$MP` / `$RDP`，或者粘贴刚才 echo 出来的值。

### 4. 创建专用服务用户 & 目录

用专用用户跑后端，避免直接用 root 或你的 SSH 账号：

```bash
sudo useradd -r -s /bin/bash -M renyun    # 系统用户，有 bash，不自动建 home
sudo mkdir -p /opt/renyun && sudo chown renyun:renyun /opt/renyun
```

### 5. 拉代码并打包

```bash
cd /opt/renyun
sudo -u renyun git clone <你的仓库地址> backend
cd backend
sudo -u renyun chmod +x ./mvnw
sudo -u renyun ./mvnw clean package -DskipTests
```

打包产物：`/opt/renyun/backend/target/ReYun-0.0.1-SNAPSHOT.jar`。
用 `sudo -u renyun` 跑是为了让产物归 `renyun` 用户所有，避免 systemd 启动时 `permission denied`。

### 6. 准备环境变量文件

> 所有密钥都不要硬编码进 systemd 文件，单独放一个 EnvironmentFile 并 `chmod 600`。
> **务必把占位符替换成真实值**，下面 heredoc 里的中文注释只是提示，别直接带着注释启动。

```bash
sudo tee /etc/renyun.env <<EOF
# —— MySQL ——
DATABASE_HOST=jdbc:mysql://localhost:3306/renyun?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
DATABASE_USERNAME=renyun
DATABASE_PASSWORD=$MP

# —— Redis ——
REDIS_HOST=127.0.0.1
REDIS_PASSWORD=$RDP

# —— JWT ——
JWT_SECRET=$(openssl rand -hex 32)
JWT_EXPIRATION=86400000

# —— AI ——
DEEPSEEK_API_KEY=请替换为你的 deepseek key
EOF

sudo chmod 600 /etc/renyun.env
sudo chown root:root /etc/renyun.env
cat /etc/renyun.env   # 检查一遍，确保没有中文占位符残留
```

> 如果不在生成密码的同一个 shell 会话里，把上面 `$MP` / `$RDP` 直接写死成具体值。`DEEPSEEK_API_KEY` 不配也能启动，只是 AI 接口会返回 502。

### 7. 创建 systemd 服务

```bash
sudo tee /etc/systemd/system/renyun.service <<'EOF'
[Unit]
Description=ReYun Backend Service
After=network.target mysql.service redis-server.service

[Service]
Type=simple
User=renyun
Group=renyun
WorkingDirectory=/opt/renyun/backend
EnvironmentFile=/etc/renyun.env
ExecStart=/usr/bin/java -Xms256m -Xmx1024m -jar /opt/renyun/backend/target/ReYun-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=5
StandardOutput=journal
StandardError=journal
LimitNOFILE=65535

[Install]
WantedBy=multi-user.target
EOF

# 语法自检（可选但强烈推荐）
sudo systemd-analyze verify /etc/systemd/system/renyun.service
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable renyun
sudo systemctl start renyun
```

### 8. 查看状态与日志

```bash
sudo systemctl status renyun       # Active: active (running) 表示启动成功
sudo journalctl -u renyun -f       # 实时追踪日志
sudo journalctl -u renyun -n 200  # 最近 200 行
```

正常启动后应该能看到类似 `Started platform in X seconds` 的日志。

如果启动失败，优先查：
```bash
sudo journalctl -u renyun -n 100 --no-pager
```
常见原因：MySQL/Redis 密码没对上、`/etc/renyun.env` 里留着中文占位符、`renyun` 用户对 JAR 没读权限。

### 9. 放行防火墙（可选）

```bash
sudo ufw allow 8080/tcp
sudo ufw allow 22/tcp
sudo ufw --force enable
sudo ufw status
```

### 10. 常见运维操作

```bash
# 重新打包并发布
cd /opt/renyun/backend
sudo -u renyun git pull
sudo -u renyun ./mvnw clean package -DskipTests
sudo systemctl restart renyun

# 查看实时日志
sudo journalctl -u renyun -f

# 一键重启 Redis / MySQL
sudo systemctl restart redis-server
sudo systemctl restart mysql

# 健康检查
curl http://127.0.0.1:8080/actuator/health
```

### 11. 环境变量速查表

| 变量 | 是否必填 | 默认值 | 说明 |
|------|---------|-------|------|
| `DATABASE_HOST` | 否 | `jdbc:mysql://localhost:3306/renyun` | 完整 JDBC URL |
| `DATABASE_USERNAME` | 否 | `root` | MySQL 用户名 |
| `DATABASE_PASSWORD` | 是 | 空 | MySQL 密码 |
| `REDIS_HOST` | 是 | - | Redis 地址 |
| `REDIS_PASSWORD` | 是 | - | Redis 密码 |
| `JWT_SECRET` | 否 | 项目内置默认值 | 至少 32 字符，**生产务必覆盖** |
| `JWT_EXPIRATION` | 否 | `86400000` | Token 有效期（毫秒） |
| `DEEPSEEK_API_KEY` | 否 | 占位字符串 | 不配则 AI 接口返回 502 |

> `REDIS_HOST` 和 `REDIS_PASSWORD` 没有默认值，少配一个 Spring Boot 启动就会报错，这是故意的——提醒部署者不要用不安全的默认 Redis。
