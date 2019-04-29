use security;

create table firewall (
	fw_timestamp DATE,
	source_ip VARCHAR(20),
	dest_ip VARCHAR(20),
	source_prt INT,
	dest_prt INT
);
