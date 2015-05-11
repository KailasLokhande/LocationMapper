package com.kailas.frienzo.model;

import java.util.List;

/**
 * Created by kailasl on 5/11/2015.
 */
public class UserResponse {

    public class Entry
    {
        private String userId;
        private User user;

        public String getUserId() {
            return userId;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public User getUser() {
            return user;
        }
    }

    private List<Entry> users;

    public List<Entry> getUsers() {
        return users;
    }

    public void setUsers(List<Entry> users) {
        this.users = users;
    }
}
