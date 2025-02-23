package com.dmg.resserver.utils;

import lombok.Data;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 返回对象
 * @param <T>
 */
@Data
public class Result<T> {

    private String code;

    private String msg;

    private T data;

    private static String SUCCESS="200";

    private static String ERROR="500";

    //禁止外部new对象
    private Result(){

    }

    public static <T> Result<T> success(String code,String msg,T data){
        Result result=new Result();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String msg,T data){
        Result result=new Result();
        result.setCode(SUCCESS);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(T data){
        Result result=new Result();
        result.setCode(SUCCESS);
        result.setMsg(null);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(){
        Result result=new Result();
        result.setCode(SUCCESS);
        result.setMsg(null);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> error(String code,String msg){
        Result result=new Result();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> error(String msg){
        Result result=new Result();
        result.setCode(ERROR);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> error(){
        Result result=new Result();
        result.setCode(ERROR);
        result.setMsg(null);
        result.setData(null);
        return result;
    }

    public static void main(String[] args) {
        CopyOnWriteArraySet<String> copy = new CopyOnWriteArraySet();
        copy.add("aaa");
    }
}
