package com.techzellent.hicycle.wsCalling;

/**
 * Created by SONI on 7/19/2018.
 */

public interface WsReponse {
    public void successReposse(int responseCode, String response);
    public void errorResponse(int responseCode, String exception);


}
