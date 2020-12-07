package endless.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <pre>
 * 
 * 自定义响应结构
 * </pre>
 * 
 * @author Aron
 * @date 2017年5月9日
 */
@ApiModel
public class ExtResult<T>{

	public static String DEFAULT_OK_MESSAGE = "成功";
	public static String DEFAULT_ERROR_MESSAGE = "失败";
	//public static int DEFAULT_OK_MESSAGE = 200;
	
    @ApiModelProperty(value="是否成功")
    public boolean success = true;
    /** 响应业务状态 200 成功， 其他失败. */
    @ApiModelProperty(value="响应业务状态 200 成功， 其他失败")
    public long code;
    
    public static long RESULT_OK_CODE = 200;
    public static long RESULT_ERROR_CODE = -200;

    /** 响应消息. */
    @ApiModelProperty(value="响应消息")
    public String msg;

    public long getCode() {
        return code;
    }



    public void setCode(long code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public ExtResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    /** 响应中的对象.*/
    @ApiModelProperty(value="响应中的对象")
    protected T data;

    public ExtResult() {
        this.code = -200;
        this.msg =DEFAULT_OK_MESSAGE;
    }

    public ExtResult(T data) {
        this.code =RESULT_OK_CODE;
        this.msg =DEFAULT_OK_MESSAGE;
        this.data = data;
    }

    public ExtResult(long code, String msg,boolean success) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }

    public ExtResult(long code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ExtResult<T> ok() {
        return new ExtResult<T>(RESULT_OK_CODE,DEFAULT_OK_MESSAGE,true);
    }

    public static <T> ExtResult<T> ok_msg(String msg) {
        return new ExtResult<T>(RESULT_OK_CODE, msg,true);
    }

    public static <T> ExtResult<T> ok(T data) {
        return new ExtResult<T>(RESULT_OK_CODE,DEFAULT_OK_MESSAGE, data);
    }

    public static <T> ExtResult<T> fail() {
        return new ExtResult<T>(RESULT_ERROR_CODE,DEFAULT_ERROR_MESSAGE,false);
    }

    public static <T> ExtResult<T> fail(long code, String msg) {
        return new ExtResult<T>(code,msg,false);
    }

    public static <T> ExtResult<T> fail_msg(String msg) {
        return new ExtResult<T>(RESULT_ERROR_CODE, msg,false);
    } 
}
