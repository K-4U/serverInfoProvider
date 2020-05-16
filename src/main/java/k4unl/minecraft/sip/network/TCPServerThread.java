package k4unl.minecraft.sip.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import k4unl.minecraft.k4lib.network.EnumSIPValues;
import k4unl.minecraft.sip.lib.Log;
import k4unl.minecraft.sip.lib.SIPRequest;
import k4unl.minecraft.sip.lib.Values;
import k4unl.minecraft.sip.lib.config.SIPConfig;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Koen Beckers (K-4U)
 */
public class TCPServerThread implements Runnable {
    
    private static int port;
    private static ServerSocket serverSocket;
    private static boolean keepRunning = true;
    
    public static final ThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Listener #%d").setDaemon(true).build());
    
    static {
        port = SIPConfig.port.get();
    }
    
    @Override
    public void run() {
        
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(SIPConfig.port.get());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.info("SIP listening on " + serverSocket.getLocalSocketAddress().toString() + ":" + serverSocket.getLocalPort());
        }
        
        while (keepRunning) {
            try {
                if (serverSocket == null) {
                    break;
                }
                final Socket connectionSocket = serverSocket.accept();
                threadPoolExecutor.submit(() -> {
                    try {
                        connectionSocket.setSoTimeout(5000);
                        
                        Log.debug("New connection from " + connectionSocket.getRemoteSocketAddress().toString());
                        
                        BufferedReader inFromClient =
                                new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                        
                        String msg = inFromClient.readLine();
                        
                        //Handle message:
                        String retMsg = handleMessage(msg);
                        Log.debug("SEND: " + retMsg);
                        
                        outToClient.writeBytes(retMsg);
                        //And close the connection:
                        connectionSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    /**
     * Handles a raw string and returns the stuff to re-send
     *
     * @param message
     * @return
     */
    private static String handleMessage(String message) {
        //See if this works:
        List<SIPRequest> valuesRequested = new ArrayList<>();
        
        Log.debug("RECV: " + message);
        Gson nGson = new Gson();
        try {
            List<Object> jsonList = nGson.fromJson(message, List.class);
            if (jsonList != null) {
                for (Object jsonObject : jsonList) {
                    if (jsonObject instanceof String) {
                        valuesRequested.add(new SIPRequest(jsonObject.toString(), null));
                        
                    } else if (jsonObject instanceof LinkedTreeMap) {
                        LinkedTreeMap jsonMap = (LinkedTreeMap) jsonObject;
                        if (jsonMap.containsKey("key") && jsonMap.containsKey("args")) {
                            String key = jsonMap.get("key").toString();
                            valuesRequested.add(new SIPRequest(key, jsonMap.get("args")));
                        }
                    }
                }
            } else {
                valuesRequested.add(new SIPRequest(EnumSIPValues.MISFORMED.toString(), 0));
            }
        } catch (JsonSyntaxException e) {
            valuesRequested.add(new SIPRequest(EnumSIPValues.MISFORMED.toString(), 0));
            Log.error(e.getMessage());
        }
        
        
        return Values.writeToOutputStream(valuesRequested);
    }
    
    public static void stop() {
        
        try {
            serverSocket.close();
            keepRunning = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static int getPort(){
        return port;
    }
}
