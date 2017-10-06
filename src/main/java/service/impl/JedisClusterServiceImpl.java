/**
 * @(#)JedisClusterServiceImpl.java, 2017/9/23.
 * <p/>
 * Copyright 2017 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.JedisCluster;
import service.JedisClusterService;

/**
 * @author chanyun(hzchenyun1@corp.netease.com)
 */
@Service("jedisClusterService")
public class JedisClusterServiceImpl implements JedisClusterService {

    @Autowired
    private JedisCluster jedisCluster;

    public Object get(String key) {
        return jedisCluster.get(key);
    }

    public void set(String key, String value) {
        jedisCluster.set(key, value);
    }

    public void set(String key, List<String> list) {
        jedisCluster.rpush(key, (String[]) list.toArray());
    }

    public void del(String key) {
        jedisCluster.del(key);
    }
}