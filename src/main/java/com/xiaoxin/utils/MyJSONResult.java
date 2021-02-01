package com.xiaoxin.utils;


/**
* @Description: 自定义响应数据结构
*               200：表示成功
 *         	    500：表示错误，错误信息在msg字段中
 *  			501：bean验证错误，不管多少个错误都以map形式返回
 *  			502：拦截器拦截到用户token出错
 *   		    555：异常抛出信息
*/
public class MyJSONResult {

    // 响应业务状态
    private Integer status;

    // 响应消息
    private String msg;

    // 响应中的数据
    private Object data;

    private String ok;	// 不使用

    public MyJSONResult(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public MyJSONResult(Object data) {
        this.data = data;
        this.msg = "ok";
        this.status = 200;
    }

    public static MyJSONResult build(Integer status, String msg, Object data) {
        return new MyJSONResult(status, msg, data);
    }

    public static MyJSONResult ok(Object data) {
        return new MyJSONResult(data);
    }

    public static MyJSONResult ok() {
        return new MyJSONResult(null);
    }

    public static MyJSONResult errorMsg(String msg) {
        return new MyJSONResult(500, msg, null);
    }

    public static MyJSONResult errorMap(Object data) {
        return new MyJSONResult(501, "error", data);
    }

    public static MyJSONResult errorTokenMsg(String msg) {
        return new MyJSONResult(502, msg, null);
    }

    public static MyJSONResult errorException(String msg) {
        return new MyJSONResult(555, msg, null);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getOk() {
        return ok;
    }

    public void setOk(String ok) {
        this.ok = ok;
    }
}
