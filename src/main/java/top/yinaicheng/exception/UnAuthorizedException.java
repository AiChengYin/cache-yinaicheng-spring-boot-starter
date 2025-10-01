package top.yinaicheng.exception;

/**
 * 未授权异常类
 * @author yinaicheng
 */
public class UnAuthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 构造函数
     */
    public UnAuthorizedException() {
        super();
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public UnAuthorizedException(String message) {
        super(message);
        this.errorMessage = message;
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原因
     */
    public UnAuthorizedException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
    }

    /**
     * 构造函数
     * 
     * @param cause 原因
     */
    public UnAuthorizedException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     */
    public UnAuthorizedException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     * @param cause 原因
     */
    public UnAuthorizedException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 设置错误代码
     * 
     * @param errorCode 错误代码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 设置错误消息
     * 
     * @param errorMessage 错误消息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "UnAuthorizedException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                "} " + super.toString();
    }
}