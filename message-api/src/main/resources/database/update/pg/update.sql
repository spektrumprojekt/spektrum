ALTER TABLE messagerelation RENAME COLUMN releatedmessageglobalids TO relatedmessageglobalids;

ALTER TABLE term ADD COLUMN termcount integer;
update public.term set termcount = 1;