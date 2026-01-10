package si.nakupify.service.dto;

public class ZalogaDTO {

    private Long id_product;

    private Integer stock;

    private Integer reserved;

    public ZalogaDTO() {}

    public ZalogaDTO(Long id_product, Integer stock, Integer reserved) {
        this.id_product = id_product;
        this.stock = stock;
        this.reserved = reserved;
    }

    public Long getId_product() {
        return id_product;
    }

    public void setId_product(Long id_product) {
        this.id_product = id_product;
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
