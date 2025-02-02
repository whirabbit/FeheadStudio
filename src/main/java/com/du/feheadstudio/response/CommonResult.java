package com.du.feheadstudio.response;

import lombok.Builder;

/**
 * @Author DU425
 * @Date 2022/1/13 9:37
 * @Version 1.0
 */
@Builder
public class CommonResult {
    private long code;
    private String message;
    private Object data;
    private String status;

    public CommonResult() {
    }

    public CommonResult(long code, String message, Object data, String status) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public CommonResult(long code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }


    public static CommonResult ok(){
        return new CommonResult(ResultType.SUCCESS.getCode(),ResultType.SUCCESS.getMessage(),null,"ok");
    }
    public static CommonResult ok(Object data  ){
        return new CommonResult(ResultType.SUCCESS.getCode(),ResultType.SUCCESS.getMessage(),data,"ok");
    }
    /**
     * 返回成功消息
     * @return
     */
    public static CommonResult success(){
        return new CommonResult(ResultType.SUCCESS.getCode(),ResultType.SUCCESS.getMessage(),null);
    }

    /**
     * 返回成功消息
     * @param message 消息
     * @return
     */
    public static CommonResult success(String message){
        return new CommonResult(ResultType.SUCCESS.getCode(),message,null);
    }

    /**
     * 返回成功消息带数据
     * @param data 数据
     * @return
     */
    public static CommonResult success(Object data){
        return new CommonResult(ResultType.SUCCESS.getCode(),ResultType.SUCCESS.getMessage(),data);
    }

    /**
     * 返回成功消息带数据
     * @param message 消息
     * @param data 数据
     * @return
     */
    public static CommonResult success(String message,Object data){
        return new CommonResult(ResultType.SUCCESS.getCode(),message,data);
    }

    /**
     * 失败返回消息
     * @return
     */
//    public static CommonResult failed(){
//        return new CommonResult(ResultType.COMMON_FAIL,ResultType.COMMON_FAIL.getMessage(),null);
//    }

    /**
     * 失败返回消息
     * @param message 消息
     * @return
     */
    public static CommonResult failed(String message){
        return new CommonResult(ResultType.COMMON_FAIL.getCode(),message,null);
    }

    /**
     * 失败返回消息带数据
     * @param data 数据
     * @return
     */
    public static CommonResult failed(Object data){
        return new CommonResult(ResultType.COMMON_FAIL.getCode(),ResultType.COMMON_FAIL.getMessage(),data);
    }

    /**
     * 失败返回消息带数据
     * @param message 消息
     * @param data 数据
     * @return
     */
    public static CommonResult failed(String message,Object data){
        return new CommonResult(ResultType.COMMON_FAIL.getCode(),message,data);
    }

    /**
     * 失败返回消息
     * @param errorType 错误类型
     * @return
     */
    public static CommonResult failed(ErrorType errorType){
        return new CommonResult(errorType.getCode(),errorType.getMessage(),null);
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CommonResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", status='" + status + '\'' +
                '}';
    }
}