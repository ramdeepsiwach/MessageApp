package com.se_p2.messageapp.model;

public class MessageModel {
    String sender,receiver,message,isSeen;

    public MessageModel(String sender, String receiver, String message,String isSeen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen=isSeen;
    }


    public MessageModel() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(String isSeen) {
        this.isSeen = isSeen;
    }
}
