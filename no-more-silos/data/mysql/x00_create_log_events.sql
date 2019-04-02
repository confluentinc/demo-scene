use security;

create table log_events (
	host VARCHAR(20),
	app VARCHAR(50),
	severity VARCHAR(5),
	message VARCHAR(255)
);
