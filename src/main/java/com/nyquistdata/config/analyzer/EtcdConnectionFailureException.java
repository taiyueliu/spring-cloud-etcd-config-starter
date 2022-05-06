package com.nyquistdata.config.analyzer;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description
 */
public class EtcdConnectionFailureException extends RuntimeException{
    private final String serverAddr;

    public EtcdConnectionFailureException(String serverAddr,String message){
        super(message);
        this.serverAddr = serverAddr;
    }

    public EtcdConnectionFailureException(String serverAddr,String message,
                                          Throwable cause){
        super(message, cause);
        this.serverAddr = serverAddr;
    }
    public String getServerAddr() {
        return serverAddr;
    }
}
