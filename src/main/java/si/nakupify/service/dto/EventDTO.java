package si.nakupify.service.dto;

public class EventDTO {

    private Long id_request;

    private String type;

    private Long id_product;

    private Long id_user;

    private String tenant;

    private Integer quantityAdd;

    private Integer quantityRemove;

    public EventDTO() {}

    public EventDTO(Long id_request, String type, Long id_product, Long id_user, String tenant, Integer quantityAdd, Integer quantityRemove) {
        this.id_request = id_request;
        this.type = type;
        this.id_product = id_product;
        this.id_user = id_user;
        this.quantityAdd = quantityAdd;
        this.quantityRemove = quantityRemove;
    }

    public Long getId_request() {
        return id_request;
    }

    public void setId_request(Long id_request) {
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

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
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
}
