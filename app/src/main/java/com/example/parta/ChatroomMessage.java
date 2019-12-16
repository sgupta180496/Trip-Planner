package com.example.parta;

public class ChatroomMessage {

        private String messageText;
        private String messageUser;
        private String messageUserId;
        private long messageTiming;
        private String imageURL;


        public ChatroomMessage() {

        }
    public ChatroomMessage(String messageText, String messageUser, String messageUserId,
                           long messageTiming, String imageURL) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.messageUserId = messageUserId;
        this.messageTiming = messageTiming;
        this.imageURL = imageURL;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public String getMessageUserId() {
        return messageUserId;
    }

    public void setMessageUserId(String messageUserId) {
        this.messageUserId = messageUserId;
    }

    public long getMessageTiming() {
        return messageTiming;
    }

    public void setMessageTiming(long messageTiming) {
        this.messageTiming = messageTiming;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
