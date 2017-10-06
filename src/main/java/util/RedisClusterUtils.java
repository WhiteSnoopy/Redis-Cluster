/**
 * @(#)RedisClusterUtils.java, 2017/9/26.
 * <p/>
 * Copyright 2017 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisClusterException;
import redis.clients.util.ClusterNodeInformation;
import redis.clients.util.ClusterNodeInformationParser;

/**
 * @author chanyun(hzchenyun1@corp.netease.com)
 */
public class RedisClusterUtils {

    public static final String SLOT_IN_TRANSITION_IDENTIFIER = "[";
    public static final String SLOT_IMPORTING_IDENTIFIER = "--<--";
    public static final String SLOT_MIGRATING_IDENTIFIER = "-->--";
    public static final long CLUSTER_SLEEP_INTERVAL = 100;
    public static final int CLUSTER_DEFAULT_TIMEOUT = 300;
    public static final int CLUSTER_MIGRATE_NUM = 100;
    public static final int CLUSTER_DEFAULT_DB = 0;
    public static final String UNIX_LINE_SEPARATOR = "\n";
    public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
    public static final String COLON_SEPARATOR = ":";


    /**
     * 获取节点的哈希槽(available、importing、migrating)
     *
     * @param nodeInfo
     * @return
     */
    public static ClusterNodeInformation getNodeSlotsInfo(final HostAndPort nodeInfo) {

        ClusterNodeInformationParser parser = new ClusterNodeInformationParser();
        String nodeInfoLine = getNodeInfo(nodeInfo);
        System.out.println(nodeInfoLine);
        ClusterNodeInformation nodeInformation = parser.parse(nodeInfoLine, new HostAndPort(nodeInfo.getHost(), nodeInfo.getPort()));
        System.out.println(nodeInformation.getAvailableSlots());
        System.out.println(nodeInformation.getNode());
        return nodeInformation;
    }


    /**
     * 获取输入节点信息
     *
     * @param nodeInfo
     * @return
     */
    public static String getNodeInfo(final HostAndPort nodeInfo) {
        Jedis node = new Jedis(nodeInfo.getHost(), nodeInfo.getPort());
        String[] clusterInfo = node.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String lineInfo: clusterInfo) {
            if (lineInfo.contains("myself")) {
                return lineInfo;
            }
        }
        node.close();
        return "";
    }


    /**
     * 获取节点的nodeId
     *
     * @param nodeInfo
     * @return
     */
    public static String getNodeId(final HostAndPort nodeInfo) {
        Jedis jedis = new Jedis(nodeInfo.getHost(), nodeInfo.getPort());
        String[] clusterInfo = jedis.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String lineInfo: clusterInfo) {
            if (lineInfo.contains("myself")) {
                return lineInfo.split(COLON_SEPARATOR)[0];
            }
        }
        return "";
    }

    /**
     * 获取所有的集群节点
     *
     * @param nodeInfo
     * @return
     */
    public static List<HostAndPort> getAllNodesOfCluster(final HostAndPort nodeInfo) {
        Jedis node = new Jedis(nodeInfo.getHost(), nodeInfo.getPort());
        List<HostAndPort> clusterNodeList = new ArrayList<HostAndPort>();
        clusterNodeList.add(nodeInfo);
        String[] clusterNodesOutput = node.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String infoLine: clusterNodesOutput) {
            if (infoLine.contains("myself")) {
                continue;
            }
            String[] hostAndPort = infoLine.split(" ")[1].split(":");
            HostAndPort hnp = new HostAndPort(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
            clusterNodeList.add(hnp);
        }
        for(HostAndPort hostAndPort : clusterNodeList){
            System.out.println(hostAndPort.getHost());
            System.out.println(hostAndPort.getPort());
        }
        return clusterNodeList;
    }

    /**
     * 获取集群的所有主节点
     *
     * @param nodeInfo
     * @return
     */
    public static List<HostAndPort> getMasterNodesOfCluster(final HostAndPort nodeInfo) {
        Jedis node = new Jedis(nodeInfo.getHost(), nodeInfo.getPort());
        List<HostAndPort> masterNodeList = new ArrayList<HostAndPort>();

        String[] clusterNodesOutput = node.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String infoLine: clusterNodesOutput) {
            if (infoLine.contains("master")) {
                if (infoLine.contains("myself")) {
                    masterNodeList.add(nodeInfo);
                } else {
                    String[] hostAndPort = infoLine.split(" ")[1].split(":");
                    masterNodeList.add(new HostAndPort(hostAndPort[0], Integer.valueOf(hostAndPort[1])));
                }
            }
        }
        node.close();
        for(HostAndPort hostAndPort : masterNodeList){
            System.out.println(hostAndPort.getHost());
            System.out.println(hostAndPort.getPort());
        }
        return masterNodeList;
    }


    /**
     * 将节点加入到集群中
     *
     * @param clusterNodeInfo   one node in the cluster
     * @param nodeToJoin        the node to join
     * @return
     */
    public static boolean joinCluster(final HostAndPort clusterNodeInfo, final HostAndPort nodeToJoin) {
        return joinCluster(clusterNodeInfo, nodeToJoin, CLUSTER_DEFAULT_TIMEOUT);
    }


    /**
     *
     * @param clusterNodeInfo
     * @param nodeToJoin
     * @param timeout
     * @return
     */
    private static boolean joinCluster(final HostAndPort clusterNodeInfo, final HostAndPort nodeToJoin, final long timeout) {
        Jedis clusterNode = new Jedis(clusterNodeInfo.getHost(), clusterNodeInfo.getPort());
        clusterNode.clusterMeet(nodeToJoin.getHost(), nodeToJoin.getPort());
        List<HostAndPort> clusterNodes = getAllNodesOfCluster(clusterNodeInfo);

        boolean joinOk = false;
        long sleepTime = 0;
        while (!joinOk && sleepTime < timeout) {
            joinOk = true;
            // check
            for (HostAndPort hostAndPort: clusterNodes) {
                if (!isNodeKnown(hostAndPort, nodeToJoin)) {
                    joinOk = false;
                    break;
                }
            }
            if (joinOk) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(CLUSTER_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new JedisClusterException("joinCluster timeout.", e);
            }
            sleepTime += CLUSTER_SLEEP_INTERVAL;
        }
        clusterNode.close();
        return joinOk;
    }

    /**
     * 检查节点是否加入到集群中
     *
     * @param srcNodeInfo
     * @param tarNodeInfo
     * @return
     */
    public static boolean isNodeKnown(final HostAndPort srcNodeInfo, final HostAndPort tarNodeInfo) {
        Jedis srcNode = new Jedis(srcNodeInfo.getHost(), srcNodeInfo.getPort());
        String tarNodeId = getNodeId(tarNodeInfo);
        String[] clusterInfo = srcNode.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String infoLine : clusterInfo) {
            if (infoLine.contains(tarNodeId)) {
                srcNode.close();
                return true;
            }
        }
        srcNode.close();
        return false;
    }


    /**
     * 检查集群是否准备就绪
     *
     * @param clusterNodes
     * @return
     */
    public static boolean isReady(final Set<HostAndPort> clusterNodes) {
        boolean clusterOk = false;
        while (!clusterOk) {
            clusterOk = true;
            for (HostAndPort hostAndPort: clusterNodes) {
                Jedis node = new Jedis(hostAndPort.getHost(), hostAndPort.getPort());
                String clusterInfo = node.clusterInfo();
                String firstLine = clusterInfo.split(UNIX_LINE_SEPARATOR)[0];
                node.close();
                String[] firstLineArr = firstLine.split(COLON_SEPARATOR);
                if (firstLineArr[0].equalsIgnoreCase("cluster_state") &&
                        firstLineArr[1].equalsIgnoreCase("ok")) {
                    continue;
                }
                clusterOk = false;
                break;
            }
            if (clusterOk) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(CLUSTER_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new JedisClusterException("waitForClusterReady", e);
            }
        }
        return clusterOk;
    }


/*
    public void initCluster(Jedis jedis) {
        if (jedis instanceof BinaryJedisCluster) {
            BinaryJedisCluster jedisCluster = (BinaryJedisCluster) jedis;

            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();

            Map<String, ClusterNodeObject> hpToNodeObjectMap = new HashMap<>(clusterNodes.size());
            for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
                JedisPool jedisPool = entry.getValue();
                Jedis jedis = jedisPool.getResource();

                String clusterNodesCommand = jedis.clusterNodes();

                String[] allNodes = clusterNodesCommand.split("\n");
                for (String allNode : allNodes) {
                    String[] splits = allNode.split(" ");

                    String hostAndPort = splits[1];
                    ClusterNodeObject clusterNodeObject =
                            new ClusterNodeObject(splits[0], splits[1], splits[2].contains("master"), splits[3],
                                    Long.parseLong(splits[4]), Long.parseLong(splits[5]), splits[6],
                                    splits[7].equalsIgnoreCase("connected"), splits.length == 9 ? splits[8] : null);

                    hpToNodeObjectMap.put(hostAndPort, clusterNodeObject);
                }
            }
            List<Integer> slotStarts = new ArrayList<>();
            for (ClusterNodeObject clusterNodeObject : hpToNodeObjectMap.values()) {
                if (clusterNodeObject.isConnected() && clusterNodeObject.isMaster()) {
                    String slot = clusterNodeObject.getSlot();
                    String[] slotSplits = slot.split("-");
                    int slotStart = Integer.parseInt(slotSplits[0]);
//                    int slotEnd = Integer.parseInt(slotSplits[1]);
                    slotStarts.add(slotStart);
                }
            }
            Collections.sort(slotStarts);
            this.slotStarts = slotStarts;
        }*/

}