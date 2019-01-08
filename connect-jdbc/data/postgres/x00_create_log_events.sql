create table security.log_events (
	host VARCHAR(20),
	app VARCHAR(50),
	severity VARCHAR(5),
	message VARCHAR(255)
);

CREATE TRIGGER log_events_updated_at_modtime BEFORE UPDATE ON security.log_events FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

GRANT SELECT ON security.log_events TO connect_user;
