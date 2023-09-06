package whu.edu.cs.transitnet.result;

/**
 * @author Ria
 */

public enum ResultCode {
    // 表示成功
    SUCCESS(200),
    // 表示失败
    FAIL(400),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500),
    WRONG_PASSWORD(300);

    public Integer code;
    ResultCode(int code) {
        this.code = code;
    }
}
