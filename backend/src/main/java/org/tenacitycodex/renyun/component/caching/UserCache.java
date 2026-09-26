package org.tenacitycodex.renyun.component.caching;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tenacitycodex.renyun.common.exceptions.CacheMissedException;
import org.tenacitycodex.renyun.component.redis.AbstractCacheEngine;
import org.tenacitycodex.renyun.component.redis.RedisService;
import org.tenacitycodex.renyun.module.user.entity.User;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class UserCache extends AbstractCacheEngine<User, Long> {

    private String usernamePrefix = "username";

    public UserCache(ObjectMapper objectMapper, RedisService redisService) {
        super(objectMapper, redisService);
        usernamePrefix = getKeyPrefix() + usernamePrefix;
    }

    @Override
    public User deserializeCachedObject(Object json) {
        User user = objectMapper.readValue((String) json, User.class);
        log.info("Deserialized as {}", user);
        return user;
    }

    public User getUserByUsername(@NonNull String username) {
        String usernameKey = usernamePrefix + username;
        Object userIdStr = redisService.getValue(usernameKey);

        if (userIdStr == null) {
            throw new CacheMissedException(null);
        }
        try {
            return getCachedById(Long.parseLong((String) userIdStr));
        } catch (NumberFormatException e) {
            redisService.deleteValue(usernameKey);
            throw new CacheMissedException(e.getMessage());
        }
    }

    public void setUserUsernameKey(User user) {
        redisService.setValue(usernamePrefix + user.getUsername(), String.valueOf(user.getId()));
    }

    @Override
    public String getId(User user) {
        return user.getId().toString();
    }

    @Override
    public String getKeyPrefix() {
        return "user";
    }
}
