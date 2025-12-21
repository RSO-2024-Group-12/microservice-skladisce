package si.nakupify.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class SnapshotProduct extends PanacheEntity {

    public Long id_snapshot;

    public Long id_izdelek;

    public Integer zaloga;

    public Integer rezervirano;

    public SnapshotProduct() {}

    public SnapshotProduct(Long id_snapshot, Long id_izdelek, Integer zaloga, Integer rezervirano) {
        this.id_snapshot = id_snapshot;
        this.id_izdelek = id_izdelek;
        this.zaloga = zaloga;
        this.rezervirano = rezervirano;
    }
}
