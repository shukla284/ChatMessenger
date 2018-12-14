package com.chatapp;

public class ServerMain {
    public static void main(String args[]){
        int port=8880;
        Server server=new Server(port);
        server.start();
    }
}
