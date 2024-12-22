package vn.hoidanit.jobhunter.domain.dto;

public class ResultPaginationDTO {
    private Meta meta;
    private Object data;
    
    public Meta getMeta() {
        return meta;
    }
    public void setMeta(Meta meta) {
        this.meta = meta;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }

    
}
