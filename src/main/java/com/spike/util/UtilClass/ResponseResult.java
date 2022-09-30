package com.spike.util.UtilClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

@JsonInclude(Include.NON_NULL)
@ApiModel(
        value = "响应结果",
        description = ""
)
public class ResponseResult<T> implements Serializable {
    private static final long serialVersionUID = -5734417299472900553L;
    @ApiModelProperty(
            value = "错误码,成功返回0",
            dataType = "integer"
    )
    private Long errcode;
    @ApiModelProperty(
            value = "提示消息",
            dataType = "string"
    )
    private String errmsg;
    @ApiModelProperty(
            value = "返回结果",
            dataType = "object"
    )
    private T data;
    private String traceId;

    ResponseResult(final Long errcode, final String errmsg, final T data, final String traceId) {
        this.errcode = errcode;
        this.errmsg = errmsg;
        this.data = data;
        this.traceId = traceId;
    }

    public static <T> ResponseResultBuilder<T> builder() {
        return new ResponseResultBuilder();
    }

    public Long getErrcode() {
        return this.errcode;
    }

    public String getErrmsg() {
        return this.errmsg;
    }

    public T getData() {
        return this.data;
    }

    public String getTraceId() {
        return this.traceId;
    }

    public void setErrcode(final Long errcode) {
        this.errcode = errcode;
    }

    public void setErrmsg(final String errmsg) {
        this.errmsg = errmsg;
    }

    public void setData(final T data) {
        this.data = data;
    }

    public void setTraceId(final String traceId) {
        this.traceId = traceId;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ResponseResult)) {
            return false;
        } else {
            ResponseResult<?> other = (ResponseResult)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label59: {
                    Object this$errcode = this.getErrcode();
                    Object other$errcode = other.getErrcode();
                    if (this$errcode == null) {
                        if (other$errcode == null) {
                            break label59;
                        }
                    } else if (this$errcode.equals(other$errcode)) {
                        break label59;
                    }

                    return false;
                }

                Object this$errmsg = this.getErrmsg();
                Object other$errmsg = other.getErrmsg();
                if (this$errmsg == null) {
                    if (other$errmsg != null) {
                        return false;
                    }
                } else if (!this$errmsg.equals(other$errmsg)) {
                    return false;
                }

                Object this$data = this.getData();
                Object other$data = other.getData();
                if (this$data == null) {
                    if (other$data != null) {
                        return false;
                    }
                } else if (!this$data.equals(other$data)) {
                    return false;
                }

                Object this$traceId = this.getTraceId();
                Object other$traceId = other.getTraceId();
                if (this$traceId == null) {
                    if (other$traceId != null) {
                        return false;
                    }
                } else if (!this$traceId.equals(other$traceId)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ResponseResult;
    }

    public int hashCode() {
        boolean PRIME = true;
        int result = 1;
        Object $errcode = this.getErrcode();
        result = result * 59 + ($errcode == null ? 43 : $errcode.hashCode());
        Object $errmsg = this.getErrmsg();
        result = result * 59 + ($errmsg == null ? 43 : $errmsg.hashCode());
        Object $data = this.getData();
        result = result * 59 + ($data == null ? 43 : $data.hashCode());
        Object $traceId = this.getTraceId();
        result = result * 59 + ($traceId == null ? 43 : $traceId.hashCode());
        return result;
    }

    public String toString() {
        return "ResponseResult(errcode=" + this.getErrcode() + ", errmsg=" + this.getErrmsg() + ", data=" + this.getData() + ", traceId=" + this.getTraceId() + ")";
    }

    public static class ResponseResultBuilder<T> {
        private Long errcode;
        private String errmsg;
        private T data;
        private String traceId;

        ResponseResultBuilder() {
        }

        public ResponseResultBuilder<T> errcode(final Long errcode) {
            this.errcode = errcode;
            return this;
        }

        public ResponseResultBuilder<T> errmsg(final String errmsg) {
            this.errmsg = errmsg;
            return this;
        }

        public ResponseResultBuilder<T> data(final T data) {
            this.data = data;
            return this;
        }

        public ResponseResultBuilder<T> traceId(final String traceId) {
            this.traceId = traceId;
            return this;
        }

        public ResponseResult<T> build() {
            return new ResponseResult(this.errcode, this.errmsg, this.data, this.traceId);
        }

        public String toString() {
            return "ResponseResult.ResponseResultBuilder(errcode=" + this.errcode + ", errmsg=" + this.errmsg + ", data=" + this.data + ", traceId=" + this.traceId + ")";
        }
    }
}
