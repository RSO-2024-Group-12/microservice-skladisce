package si.nakupify.service.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import si.nakupify.entity.SnapshotProduct;

import java.util.List;

@ApplicationScoped
public class SnapshotProductRepository implements PanacheRepository<SnapshotProduct> {

    public List<SnapshotProduct> findBySnapshotId(Long id_snapshot) {
        return list("id_snapshot", id_snapshot);
    }
}
