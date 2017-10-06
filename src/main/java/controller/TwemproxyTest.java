/**
 * @(#)TwemproxyTest.java, 2017/9/29.
 * <p/>
 * Copyright 2017 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package controller;

import redis.clients.jedis.Jedis;

/**
 * @author chanyun(hzchenyun1@corp.netease.com)
 */
public class TwemproxyTest {

    public static void main(String args[]){
        Jedis jedis = new Jedis("47.94.252.20", 8888);
        for(int i=1;i<=10;i++)
        {
            jedis.set("hello_"+i, "ABC"+i);
        }
        jedis.close();
    }
}