package com.kailas.locationmapper;

/**
 * Created by kailasl on 5/10/2015.
 */
public interface ServerAuthenticate {
    public String userSignUp(final String name, final String email, final String pass, String authType) throws Exception;
    public String userSignIn(final String user, final String pass, String authType) throws Exception;
}