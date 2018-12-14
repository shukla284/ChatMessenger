package com.chatapp;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{

    private final int port;
    private ArrayList<ServerProcessor> serverProcessorList=new ArrayList<>();

    Server(int port){
        this.port =port;
    }
    public List<ServerProcessor> getProcessors(){
        return serverProcessorList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {
                System.out.println("About to accept a new connection from client ");
                final Socket clientSocket = serverSocket.accept();
                System.out.println("Connection Accepted from " + clientSocket);
                ServerProcessor serverProcessor = new ServerProcessor(this,clientSocket);
                serverProcessorList.add(serverProcessor);
                serverProcessor.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeProcessor(ServerProcessor serverProcessor) {
        serverProcessorList.remove(serverProcessor);
    }
}
