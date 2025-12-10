package si.nakupify.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class Event implements Serializable {

    private String id_request;

    private String type;

    private Long id_product;

    private Long id_user;

    private Integer quantityAdd;

    private Integer quantityRemove;

    private Timestamp timestamp;

    private Boolean success;

    public Event() {}

    public Event(String id_request, String type, Long id_product, Long id_user, Integer quantityAdd, Integer quantityRemove, Boolean success) {
        this.id_request = id_request;
        this.type = type;
        this.id_product = id_product;
        this.id_user = id_user;
        this.quantityAdd = quantityAdd;
        this.quantityRemove = quantityRemove;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.success = success;
    }

    public String getId_request() {
        return id_request;
    }

    public void setId_request(String id_request) {
        this.id_request = id_request;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId_product() {
        return id_product;
    }

    public void setId_product(Long id_product) {
        this.id_product = id_product;
    }

    public Long getId_user() {
        return id_user;
    }

    public void setId_user(Long id_user) {
        this.id_user = id_user;
    }

    public Integer getQuantityAdd() {
        return quantityAdd;
    }

    public void setQuantityAdd(Integer quantityAdd) {
        this.quantityAdd = quantityAdd;
    }

    public Integer getQuantityRemove() {
        return quantityRemove;
    }

    public void setQuantityRemove(Integer quantityRemove) {
        this.quantityRemove = quantityRemove;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
