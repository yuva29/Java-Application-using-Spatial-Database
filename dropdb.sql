alter session set current_schema=system;
DROP TABLE Building;
DROP TABLE Students;
DROP TABLE TramStops;

--Indexed will be removed once these entires are removed
DELETE FROM user_sdo_geom_metadata WHERE TABLE_NAME='BUILDING' AND COLUMN_NAME='BGEO';
DELETE FROM user_sdo_geom_metadata WHERE TABLE_NAME='STUDENTS' AND COLUMN_NAME='SLOC';
DELETE FROM user_sdo_geom_metadata WHERE TABLE_NAME='TRAMSTOPS' AND COLUMN_NAME='TSLOC';