
create table security.firewall (
	fw_timestamp DATE,
	source_ip VARCHAR(20),
	dest_ip VARCHAR(20),
	source_prt INT,
	dest_prt INT
);

CREATE TRIGGER firewall_updated_at_modtime BEFORE UPDATE ON security.firewall FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

GRANT SELECT ON security.firewall TO connect_user;
