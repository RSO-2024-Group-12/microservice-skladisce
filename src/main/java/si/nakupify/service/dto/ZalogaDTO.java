package si.nakupify.service.dto;

public class ZalogaDTO {

    private Long id_product;

    private String tenant;

    private Integer stock;

    private Integer reserved;

    public ZalogaDTO() {}

    public ZalogaDTO(Long id_product, String tenant, Integer stock, Integer reserved) {
        this.id_product = id_product;
        this.tenant = tenant;
        this.stock = stock;
        this.reserved = reserved;
    }

    public Long getId_product() {
        return id_product;
    }

    public void setId_product(Long id_product) {
        this.id_product = id_product;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getReserved() {
        return reserved;
    }

    public void setReserved(Integer reserved) {
        this.reserved = reserved;
    }
}
