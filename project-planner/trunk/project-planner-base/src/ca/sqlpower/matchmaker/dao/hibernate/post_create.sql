-- post_create.sql $Id:$
--
-- This SQL script bootstraps a newly-created empty matchmaker schema
-- with the basic data it needs in order for the MatchMaker to log in
--
-- In order for automatic execution to work properly from within the app,
-- every statement must be terminated by a semicolon on a line by itself.
--
-- The following strings will be substituted before being passed to the
-- Database:
--         {USER}   The database user name for the current connection

insert into mm_schema_info values ('schema_version', '6.0.0')
;

INSERT INTO pl_group(group_name,group_desc,last_update_date,last_update_user,create_date)
VALUES('PL_ADMIN','Special PL group with ALL privileges',CURRENT_TIMESTAMP,{USER},CURRENT_TIMESTAMP)
;

INSERT INTO pl_user (user_id, last_update_date, last_update_user, 
default_kpi_frequency, show_red_ind, show_yellow_ind, show_green_ind, show_grey_ind)
VALUES (upper({USER}), CURRENT_TIMESTAMP, {USER}, 'MONTHLY', 'Y', 'Y', 'Y', 'Y')
;

INSERT INTO user_group (user_id, group_name, last_update_date, last_update_user) 
VALUES (upper({USER}), 'PL_ADMIN', CURRENT_TIMESTAMP, {USER})
;
