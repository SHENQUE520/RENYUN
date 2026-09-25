## 架构概览

| 端           | 技术栈                         | 端口   | 职责 |
|-------------|-----------------------------|------|---|
| Java 主后端    | Spring Boot 4.1.1 + Java 21 | 8080 | 在线业务：登录注册、JWT 鉴权、Redis 缓存、AOP 限流、AI 网关 |

---

## Java 后端

### 如何运行？
```bash
mvn clean package -DskipTests
```
确保安装和运行了：
* Redis 8.0.x
* MySQL
* Java 21+

运行
```bash
java -DDATABASE_PASSWORD=你的数据库密码 -DDATABASE_HOST=你的数据库主机URL -DDATABASE_USERNAME=你的数据库用户名 -DREDIS_HOST=你的Redis主机 -DREDIS_PASSWORD=你的Redis密码 -DJWT_SECRET=不少于32字符的jwt密钥 -jar target/build-0.0.1-SNAPSHOT.jar
```
