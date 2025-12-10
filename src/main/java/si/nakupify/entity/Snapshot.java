package si.nakupify.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.sql.Timestamp;

@Entity
public class Snapshot extends PanacheEntity {

    public Long eventOffset;

    public Timestamp timestamp;

    public Snapshot () {}

    public Snapshot (Long eventOffset) {
        this.eventOffset = eventOffset;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }
}
