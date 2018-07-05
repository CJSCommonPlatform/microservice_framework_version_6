-- noinspection SqlNoDataSourceInspectionForFile

CREATE TRIGGER queue_notify_event
AFTER INSERT ON dm_queue
FOR EACH ROW EXECUTE PROCEDURE notify_change();
