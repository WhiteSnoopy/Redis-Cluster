/**
 * @(#)Cluster.java, 2017/9/23.
 * <p/>
 * Copyright 2017 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package controller;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.util.JedisClusterCRC16;

/**
 * @author chanyun(hzchenyun1@corp.netease.com)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:Spring-redis.xml"})
public class Cluster {

    @Autowired
    private JedisCluster jedisCluster;

    @Test
    public void testSpringJedisSingle(){
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:Spring-redis.xml");
        JedisPool pool= (JedisPool) applicationContext.getBean("redisClient");
        Jedis jedis= pool.getResource();
        String string= jedis.get("key1");
        System.out.println(string);
        jedis.close();
        pool.close();
    }


    @Test
    public void testSpringJedisCluster() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:Spring-redis.xml");
        JedisCluster jedisCluster= (JedisCluster) applicationContext.getBean("jedisCluster");
        String string= jedisCluster.get("chenyun");
        System.out.println(string);
    }

    @Test
    public void test(){
        int num = 100;
        String key = "chenyun";
        String value = "";
        for (int i=1; i <= num; i++) {
            jedisCluster.set(key + i, "hello" + i);
            value = jedisCluster.get(key + i);
            System.out.println(value);
        }
    }

    @Test
    public void testNodeInfo(){
        Map<String, JedisPool> clusterMap = jedisCluster.getClusterNodes();
        for (Map.Entry<String, JedisPool> entry : clusterMap.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    public static void main(String args[]){
        System.out.println(JedisClusterCRC16.getSlot("chanyun"));
    }
}