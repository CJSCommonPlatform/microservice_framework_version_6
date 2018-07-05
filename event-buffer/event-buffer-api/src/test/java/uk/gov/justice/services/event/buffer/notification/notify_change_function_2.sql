CREATE OR REPLACE FUNCTION public.notify_change()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
 
DECLARE
data json;
 
BEGIN
 
-- Convert the old or new row to JSON, based on the kind of action.
-- Action = DELETE? -&gt; OLD row
-- Action = INSERT or UPDATE? -&gt; NEW row
IF (TG_OP = 'DELETE') THEN
data = row_to_json(OLD);
ELSE
data = row_to_json(NEW);
END IF;
 
INSERT into notification_log values(TG_TABLE_NAME, TG_OP, data);
 
-- Result is ignored since this is an AFTER trigger
RETURN NULL;
END;
 
$function$
