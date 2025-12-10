INSERT INTO snapshot (id, eventOffset, timestamp) VALUES
(0, -1, CURRENT_TIMESTAMP);

INSERT INTO snapshotProduct (id, id_snapshot, id_izdelek, zaloga, rezervirano) VALUES
(100, 0, 100, 100, 0),
(101, 0, 101, 100, 0);