import com.baoan.opc.OpcUaClientFactory;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;

/**
 * @author wang xiao
 * @date 2022/7/15
 */
public class OpcUaClientTest {
    public static void main(String[] args) throws Exception {
        OpcUaClient opcClient = OpcUaClientFactory.createOpcClient("opcua.demo-this.com", 51210);
        opcClient.connect().get();
    }
}
