package com.dong.djudge.exception;

import lombok.Data;

@Data
public class UrlCheckException extends Exception  {

    private String message;
    private String caseName;

    public UrlCheckException(String message,String caseName){
        super(message + " " + caseName);
        this.message = message;
        this.caseName = caseName;
    }

}
