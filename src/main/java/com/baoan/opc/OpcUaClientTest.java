package com.baoan.opc;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author wang xiao
 * @date 2022/7/18
 */
public class OpcUaClientTest {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaClientTest.class);

    public static void main(String[] args) throws Exception {
//        OpcUaClient opcClient = OpcUaClientFactory.createOpcClientWithUserName("opc.tcp://192.168.3.17:49350","OPCUAxiao","!123abcABC123！");
//        opcClient.connect().get();
//
//        OpcUaOperationSupport opcUaOperationSupport = new OpcUaOperationSupport();
//        List<UaNode> uaNodes = opcUaOperationSupport.browseNode(opcClient,null,new ArrayList<>());
//        System.out.printf("ua node size is %d",uaNodes.size());

//        for (UaNode uaNode: uaNodes){
//            System.out.println(uaNode.getNodeId().getNamespaceIndex());
//            System.out.println(uaNode.getNodeId().getIdentifier());
//        }
//        DataValue dataValue = opcUaOperationSupport.readNode(opcClient, 2, "S71200-通道 1.设备 1.距离");
//        System.out.printf("1 data value is {},value type is {}",dataValue.getValue().getValue(),dataValue.getValue().getDataType());


//        float i = 488;
//        NodeId nodeId = new NodeId(2, "S71200-通道 1.设备 1.速度");
//
//        //创建数据对象,此处的数据对象一定要定义类型，不然会出现类型错误，导致无法写入
//        DataValue nowValue = new DataValue(new Variant(i), null, null);
//        //写入节点数据
//        StatusCode statusCode = opcClient.writeValue(nodeId, nowValue).get();
//        System.out.println(statusCode);
//        System.out.println("结果：" + statusCode.isGood());
//        float a = 325;
//        boolean b = opcUaOperationSupport.writeNodeValue(opcClient, 2, "S71200-通道 1.设备 1.速度", a);
//        System.out.println(b);
//        float a1 = 7901;
//        boolean b1 = opcUaOperationSupport.writeNodeValue(opcClient, 2, "S71200-通道 1.设备 1.距离", a1);
//        System.out.println(b1);
//        boolean enable = true;
//        boolean b2 = opcUaOperationSupport.writeNodeValue(opcClient, 2, "S71200-通道 1.设备 1.伺服电机启动", enable);
//        System.out.println(b2);


//        opcUaOperationSupport.subscribe(opcClient, 2, "S71200-通道 1.设备 1.速度", 500, new UaMonitoredItem.ValueConsumer() {
//            @Override
//            public void onValueArrived(UaMonitoredItem uaMonitoredItem, DataValue dataValue) {
//                System.out.println(dataValue.getValue().getValue());
//            }
//        });

    }
}
