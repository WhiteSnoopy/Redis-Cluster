/**
 * @(#)JedisClusterClient.java, 2017/9/24.
 * <p/>
 * Copyright 2017 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package controller;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author chanyun(hzchenyun1@corp.netease.com)
 */
public class JedisClusterClient {

    private static final JedisClusterClient redisClusterClient = new JedisClusterClient();

    public static JedisClusterClient getInstance() {
        return redisClusterClient;
    }

    private JedisPoolConfig getPoolConfig(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(1000);
        config.setMaxIdle(100);
        config.setTestOnBorrow(true);
        return config;
    }

    public void SaveRedisCluster() {
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        jedisClusterNodes.add(new HostAndPort("47.94.252.20", 7001));
        jedisClusterNodes.add(new HostAndPort("47.94.252.20", 7002));
        jedisClusterNodes.add(new HostAndPort("47.94.252.20", 7003));
        jedisClusterNodes.add(new HostAndPort("47.94.252.20", 7004));
        jedisClusterNodes.add(new HostAndPort("47.94.252.20", 7005));
        jedisClusterNodes.add(new HostAndPort("47.94.252.20", 7006));


        JedisCluster jc = new JedisCluster(jedisClusterNodes,getPoolConfig());
        System.out.println(jc);
        jc.set("cluster", "this is a redis cluster");
        String result = jc.get("cluster");
        System.out.println(result);
    }



    public static void main(String args[]) throws Exception {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        HostAndPort hostAndPort1 = new HostAndPort("47.94.252.20", 7001);
        HostAndPort hostAndPort2 = new HostAndPort("47.94.252.20", 7002);
        HostAndPort hostAndPort3 = new HostAndPort("47.94.252.20", 7003);
        HostAndPort hostAndPort4 = new HostAndPort("47.94.252.20", 7004);
        HostAndPort hostAndPort5 = new HostAndPort("47.94.252.20", 7005);
        HostAndPort hostAndPort6 = new HostAndPort("47.94.252.20", 7006);
        nodes.add(hostAndPort1);
        nodes.add(hostAndPort2);
        nodes.add(hostAndPort3);
        nodes.add(hostAndPort4);
        nodes.add(hostAndPort5);
        nodes.add(hostAndPort6);
        JedisCluster jedisCluster = new JedisCluster(nodes, poolConfig);
        String string = jedisCluster.get("chanyun");
        System.out.println(string);
    }

  /*  public static void main(String args[]){
        // 创建一个jedis的对象。
        Jedis jedis= new Jedis("47.94.252.20", 6379);
        // 调用jedis对象的方法，方法名称和redis的命令一致。
        jedis.set("key1", "jedis test");
        String string= jedis.get("key1");
        System.out.println(string);
        // 关闭jedis。每次用完之后都应该关闭jedis
        jedis.close();
    }*/



}