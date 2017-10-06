/**
 * @(#)JedisClusterService.java, 2017/9/23.
 * <p/>
 * Copyright 2017 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package service;

import java.util.List;

/**
 * @author chanyun(hzchenyun1@corp.netease.com)
 */
public interface JedisClusterService {

    /**
     * 得到指定key值的value
     * @param key
     */
    Object get(String key);


    /**
     * 保存指定key值的value
     * @param key
     * @param value
    */
    void set(String key, String value);

    /**
     * 保存指定key值的value
     * @param key
     * @param list
     */
    void set(String key, List<String> list);

    /**
     * 删除指定key的value
     * @param key
     */
    void del(String key);
}