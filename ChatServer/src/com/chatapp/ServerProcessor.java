package com.chatapp;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ServerProcessor extends Thread{

    private final Socket clientSocket;
    private String loginUser=null;
    private final Server server;
    private OutputStream outputStream;
    private HashSet<String> groupSet=new HashSet<>();

    public String getLoginUser(){
        return loginUser;
    }
    public ServerProcessor(Server server,Socket clientSocket){
        this.server=server;
        this.clientSocket=clientSocket;
    }

    @Override
    public void run() {
        try {
            clientSocketHandler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void clientSocketHandler() throws IOException {
        InputStream inputStream=clientSocket.getInputStream();
        this.outputStream=clientSocket.getOutputStream();

        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line=bufferedReader.readLine())!=null){
            String params[]= line.split("\\s+",3);
            if (params!=null && params.length>0) {
                if ("logoff".equalsIgnoreCase(params[0]) || "quit".equalsIgnoreCase(params[0])) {
                    System.out.println(loginUser+" logged off!");
                    clientLogOffHandler();break;
                }
                else if ("login".equalsIgnoreCase(params[0])){
                    clientLoginHandler(params,outputStream);
                }
                else if ("joinGroup".equalsIgnoreCase(params[0])){
                    clientJoinGroupHandler(params,outputStream);
                }
                else if ("sendto".equalsIgnoreCase(params[0])){
                    clientMessageHandler(params);
                }
                else {
                    String msg="You typed "+line+"\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    public boolean isInGroupSet(String groupname){
        return groupSet.contains(groupname);
    }
    private void clientJoinGroupHandler(String[] params, OutputStream outputStream) {
        if (params.length>1) {
            String groupname=params[1];
            groupSet.add(groupname);
        }
    }

    private void clientMessageHandler(String[] params) throws IOException {
        if (params.length==3){
            String receiver=params[1],message=params[2];

            boolean isGroup=params[1].charAt(0)=='~';
            if (isGroup)
                broadcastMessageToGroup(message,receiver);
            else
                send(message,receiver);
        }
        else {
            System.out.println("Invalid command entered by "+loginUser);
            this.outputStream.write("You typed a invalid command\nPlease enter a valid command to continue\n".getBytes());
        }
    }

    private void broadcastMessageToGroup(String message,String receiver) throws IOException {
        List<ServerProcessor> serverProcessorList=server.getProcessors();
        for (ServerProcessor serverProcessor:serverProcessorList)
            if (serverProcessor.isInGroupSet(receiver))
                send(message,receiver);
    }

    private void send(String message, String receiver) throws IOException {
        List<ServerProcessor> serverProcessorList=this.server.getProcessors();
        for (ServerProcessor serverProcessor:serverProcessorList)
            if (serverProcessor.getLoginUser().equals(receiver))
                serverProcessor.send(this.loginUser+": "+message);
    }

    private void clientLogOffHandler() throws IOException {
        this.server.removeProcessor(this);
        this.outputStream.write("You have been logged off sucessfully!".getBytes());
        broadcastOfflineStatus();
        clientSocket.close();
    }

    private void broadcastOfflineStatus() throws IOException {
        List<ServerProcessor> processorList=server.getProcessors();
        for (ServerProcessor serverProcessor:processorList)
            if (loginUser!=null && !loginUser.equals(serverProcessor.getLoginUser()))
                serverProcessor.send(loginUser+" is offline ");
    }

    private void clientLoginHandler(String[] params, OutputStream outputStream) throws IOException {
        if (params.length==3){
            if ("login".equalsIgnoreCase(params[0])){
                String username=params[1],password=params[2];
                if (username.equalsIgnoreCase("guest") && password.equalsIgnoreCase("guest") || username.equalsIgnoreCase("shiv") && password.equalsIgnoreCase("shiv")) {
                    outputStream.write(("You have successfully logged in "+username).getBytes());
                    this.loginUser=username;
                    System.out.println(loginUser+" Logged in successfully");

                    broadcastOnlineStatus(username);
                    getOnlineUsers();
                }
                else
                    outputStream.write("Username or Password is incorrect".getBytes());
            }
        }
        else {
            System.out.println("Invalid command!!");
            System.out.println("Enter valid command to continue...");
        }
    }

    private void getOnlineUsers() throws IOException {
        List<ServerProcessor> serverProcessorList=this.server.getProcessors();
        for (ServerProcessor serverProcessor:serverProcessorList)
            if (!this.loginUser.equals(serverProcessor.getLoginUser()) && serverProcessor.getLoginUser()!=null)
                this.send(serverProcessor.getLoginUser() + " is online ");
    }

    private void broadcastOnlineStatus(String username) throws IOException {
        List<ServerProcessor> processorList=server.getProcessors();
        for (ServerProcessor serverProcessor:processorList)
            if (loginUser!=null && !loginUser.equals(serverProcessor.getLoginUser()))
                     serverProcessor.send(loginUser+" is online ");
    }

    private void send(String message) throws IOException {
        if (loginUser!=null)
             this.outputStream.write(message.getBytes());
    }

}
