package com.baoan.opc;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscriptionManager;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author wang xiao
 * @date 2022/7/15
 */

public class OpcUaOperationSupport {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaOperationSupport.class);


    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);


    /**
     * 遍历树形节点
     * @param client opc ua client
     * @param uaNode 节点
     */
    public  List<UaNode> browseNode(OpcUaClient client, UaNode uaNode) throws Exception {
        List<UaNode> uaNodes = new ArrayList<>();
        List<? extends UaNode> nodes;
        if (uaNode == null) {
            nodes = client.getAddressSpace().browseNodes(Identifiers.ObjectsFolder);
        } else {
            nodes = client.getAddressSpace().browseNodes(uaNode);
        }
        for (UaNode nd : nodes) {
            if (Objects.requireNonNull(nd.getBrowseName().getName()).contains("_")) {
                continue;
            }
            logger.info("Node is: [{}]",nd);
            uaNodes.add(nd);
            browseNode(client, nd);
        }
        return uaNodes;
    }

    /**
     * 读取节点数据
     *
     * @param client opc ua client
     * @param nameSpaceInx nameSpaceInx
     * @param identifier identifier
     */
    public   DataValue readNode(OpcUaClient client,int nameSpaceInx, String identifier ) throws Exception {
        NodeId nodeId = new NodeId(nameSpaceInx, identifier);
        DataValue value = client.readValue(0.0, TimestampsToReturn.Neither, nodeId).get();
        logger.info("identifier :[{}]  value : [{}]",nodeId.getIdentifier(),value);
        return value.copy().build();
    }


    /**
     * 写入节点数据
     *
     * @param client opc ua client
     * @param nameSpaceInx nameSpaceInx
     * @param identifier identifier
     */
    public   boolean writeNodeValue(OpcUaClient client,int nameSpaceInx, String identifier,Object value) {
        NodeId nodeId = new NodeId(nameSpaceInx, identifier);
        DataValue nowValue = new DataValue(new Variant(value), StatusCode.GOOD, DateTime.now());
        StatusCode statusCode = client.writeValue(nodeId, nowValue).join();
        return statusCode.isGood();
    }


    /**
     * 订阅(单个)
     *
     */
    public static void subscribe(OpcUaClient client, int nameSpaceInx, String identifier, double timeInterval, UaMonitoredItem.ValueConsumer consumer) throws Exception {

        client
                .getSubscriptionManager()
                .createSubscription(timeInterval)
                .thenAccept(t -> {
                    NodeId nodeId = new NodeId(nameSpaceInx, identifier);
                    ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, null);
                    MonitoringParameters parameters = new MonitoringParameters(UInteger.valueOf(ATOMIC_INTEGER.get()), (double) timeInterval, null, UInteger.valueOf(10), true);

                    MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);
                    List<MonitoredItemCreateRequest> requests = new ArrayList<>();
                    requests.add(request);
                    t.createMonitoredItems(
                            TimestampsToReturn.Both,
                            requests,
                            (item, id) -> item.setValueConsumer(consumer)
                    );
                }).get();
        Thread.sleep(Long.MAX_VALUE);
    }



    private static void handlerNode(OpcUaClient client, int nameSpaceInx, Consumer<DataValue> consumer,String ...keys) {
        try {
            ManagedSubscription subscription = ManagedSubscription.create(client);
            List<NodeId> nodeIdList = new ArrayList<>();
            for (String s : keys) {
                nodeIdList.add(new NodeId(nameSpaceInx, s));
            }
            List<ManagedDataItem> dataItemList = subscription.createDataItems(nodeIdList);
            for (ManagedDataItem managedDataItem : dataItemList) {
                managedDataItem.addDataValueListener(consumer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static void managedSubscriptionEvent(OpcUaClient client,int nameSpaceInx, Consumer<DataValue> consumer,String ...key) {
        final CountDownLatch eventLatch = new CountDownLatch(1);
        client.getSubscriptionManager().addSubscriptionListener(new CustomSubscriptionListener(client,nameSpaceInx,key,consumer));
        handlerNode(client,nameSpaceInx,consumer,key);

        try {
            eventLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 自定义订阅监听
     */
    private static class CustomSubscriptionListener implements UaSubscriptionManager.SubscriptionListener {

        private final OpcUaClient client;

        private final int nameSpaceInx;

        private final String[]  key;

        private final Consumer<DataValue> consumer;

        public CustomSubscriptionListener(OpcUaClient client, int nameSpaceInx, String[] key, Consumer<DataValue> consumer) {
            this.client = client;
            this.nameSpaceInx = nameSpaceInx;
            this.key = key;
            this.consumer = consumer;
        }

        public void onKeepAlive(UaSubscription subscription, DateTime publishTime) {
            logger.debug("onKeepAlive");
        }

        public void onStatusChanged(UaSubscription subscription, StatusCode status) {
            logger.debug("onStatusChanged");
        }

        public void onPublishFailure(UaException exception) {
            logger.debug("onPublishFailure");
        }

        public void onNotificationDataLost(UaSubscription subscription) {
            logger.debug("onNotificationDataLost");
        }

        /**
         * 重连时 尝试恢复之前的订阅失败时 会调用此方法
         * @param uaSubscription 订阅
         * @param statusCode 状态
         */
        public void onSubscriptionTransferFailed(UaSubscription uaSubscription, StatusCode statusCode) {
            logger.debug("恢复订阅失败 需要重新订阅");
            handlerNode(client,nameSpaceInx,consumer,key);
        }
    }




}
