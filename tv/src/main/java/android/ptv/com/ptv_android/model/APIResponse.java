package android.ptv.com.ptv_android.model;


public class APIResponse {
    private String status;

    private String errorMsg;

    public boolean isSuccess() {
        return this.status != null && this.status.equals("success");
    }

    public boolean isFailed() {
        return !this.isSuccess();
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
