package com.baoan.opc;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;

/**
 * OpcClient Factory
 * @author wang xiao
 * @date 2022/7/15
 */
public class OpcUaClientFactory {


    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    private static final String FORMAT = "opc.tcp://%s:%s";


    public static OpcUaClient createOpcClient(String opcHost,int opcPort) throws Exception {

        String serverUrl = String.format(FORMAT,opcHost,opcPort);
        checkSecurityTmpDir();
        return OpcUaClient.create(serverUrl,
                endpoints ->
                        endpoints.stream()
                                .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
                                .findFirst(),
                configBuilder ->
                        configBuilder
                                .setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
                                .setApplicationUri(serverUrl)

                                .setIdentityProvider(new AnonymousProvider())
                                .setRequestTimeout(UInteger.valueOf(5000))
                                .build()
        );
    }

    public static OpcUaClient createOpcClientWithUserName(String serverUrl,String userName,String password) throws Exception {

        checkSecurityTmpDir();
        return OpcUaClient.create(serverUrl,
                endpoints ->
                        endpoints.stream()
                                .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
                                .findFirst(),
                configBuilder ->
                        configBuilder
                                .setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
                                .setApplicationUri(serverUrl)
                                .setIdentityProvider(new UsernameProvider(userName,password))
                                .setIdentityProvider(new AnonymousProvider())
                                .setRequestTimeout(UInteger.valueOf(5000))
                                .build()
        );
    }

    private static void checkSecurityTmpDir() throws Exception {
        Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "security");
        Files.createDirectories(securityTempDir);
        if (!Files.exists(securityTempDir)) {
            throw new Exception("unable to create security dir: " + securityTempDir);
        }
    }
}
