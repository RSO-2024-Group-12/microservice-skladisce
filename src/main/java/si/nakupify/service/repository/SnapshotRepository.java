package si.nakupify.service.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import si.nakupify.entity.Snapshot;

@ApplicationScoped
public class SnapshotRepository implements PanacheRepository<Snapshot> {

    public Snapshot getLatestSnapshot() {
        return find("ORDER BY timestamp DESC").firstResult();
    }
}
