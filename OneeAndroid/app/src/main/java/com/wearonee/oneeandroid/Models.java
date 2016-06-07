package com.wearonee.oneeandroid;

/**
 * Created by George on 4/10/2016.
 */
public class Models {


    public class User{
        private String username;
        //private String password;
        private String name;
        private String phone;
        private String photo;
        private String braceletId;
        private Position location;
        private int shareLocation;
        private int status;
        //private String changePassword;
        //private String verifyCode;
        private int verified;

        public String getUsername() {
            return username;
        }

        public String getName() {
            return name;
        }

        public String getPhoto() {
            return photo;
        }

        public String getPhone() {
            return phone;
        }

        public int getVerified() {
            return verified;
        }

        public int getStatus() {
            return status;
        }

        public int getShareLocation() {
            return shareLocation;
        }

        public Position getLocation() {
            return location;
        }

        public String getBraceletId() {
            return braceletId;
        }
    }

    public class Connection{
        private String _id;
        private String creator;
        private String creatorName;
        private String buddy;
        private String buddyName;
        private double created;
        private double accepted;
        private double ended;
        private int creator_safe;
        private int buddy_safe;
        private int creator_inquire;
        private int buddy_inquire;
        private int creator_acknowledge;
        private int buddy_acknowledge;
        private Message[] history;
        public final static int CONTEXT_BUDDY_UNSAFE = 1;
        public final static int CONTEXT_BUDDY_INQUIRING = 2;
        public final static int CONTEXT_BUDDY_NORMAL = 3;
        public final static int CONTEXT_USER_INQUIRING = 4;
        public final static int CONTEXT_USER_UNSAFE = 5;

        public String getCreator() {
            return creator;
        }

        public Message[] getHistory() {
            return history;
        }

        public String getBuddy() {
            return buddy;
        }

        public String getBuddyName() {

            if (this.buddyName != null) {
                return buddyName;
            } else {
                return "Someone";
            }
        }

        public String getCreatorName() {

            if (this.creatorName != null) {
                return creatorName;
            } else {
                return "Someone";
            }
        }

        public String getConnectionId(){
            return this._id;
        }

        public double getCreated() {
            return created;
        }

        public double getAccepted() {
            return accepted;
        }

        public double getEnded() {
            return ended;
        }

        public int getUserSafe(String user){
            if(user.equals(this.creator)){
                return this.creator_safe;
            } else {
                return this.buddy_safe;
            }
        }

        public int getUserInquire(String user){
            if(user.equals(this.creator)){
                return this.creator_inquire;
            } else {
                return this.buddy_inquire;
            }
        }

        public int getUserAcknowledge(String user){
            if(user.equals(this.creator)){
                return this.creator_acknowledge;
            } else {
                return this.buddy_acknowledge;
            }
        }

        public int getBuddySafe(String user){
            if(user.equals(this.creator)){
                return this.buddy_safe;
            } else {
                return this.creator_safe;
            }
        }

        public int getBuddyInquire(String user){
            if(user.equals(this.creator)){
                return this.buddy_inquire;
            } else {
                return this.creator_inquire;
            }
        }

        public int getBuddyAcknowledge(String user){
            if(user.equals(this.creator)){
                return this.buddy_acknowledge;
            } else {
                return this.creator_acknowledge;
            }
        }

        public int getConnectionContext(String username){

            if(this.getUserSafe(username) == 0){
                return CONTEXT_USER_UNSAFE;
            } else {
                if (this.getBuddySafe(username) == 1) {
                    // buddy is safe
                    if (this.getBuddyInquire(username) == 1) {
                        return CONTEXT_BUDDY_INQUIRING;
                    } else {
                        if (this.getUserInquire(username) == 1){
                            return CONTEXT_USER_INQUIRING;
                        }
                        return CONTEXT_BUDDY_NORMAL;
                    }
                } else {
                    // buddy is unsafe
                    return CONTEXT_BUDDY_UNSAFE;
                }
            }
        }


    }

    public class Position {
        private double time;
        private double lat;
        private double lon;

        public double getLon() {
            return lon;
        }

        public double getLat() {
            return lat;
        }

        public double getTime() {
            return time;
        }
    }

    public class Message {
        private int type;
        private double sent;
        private double received;
        private String originUser;
        private String destinUser;

        public int getType() {
            return type;
        }

        public double getSent() {
            return sent;
        }

        public double getReceived() {
            return received;
        }

        public String getOriginUser() {
            return originUser;
        }

        public String getDestinUser() {
            return destinUser;
        }
    }
}
