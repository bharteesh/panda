Insert into CRAWLER_CONFIG 
   	(ID,PROVIDER,WARC_FILE_LOCATION,DOWNLOAD_DIR_LOCATION,PROVIDER_BASE_URL,RSYNC_COMMAND,DELETE_FILES_FROM_DOWNLOAD_DIR) 
   values 
	(1,'MLR','/cmjq/Heritrix/Software/heritrix-3.2.0/jobs/MonthlyLaborReview/latest/warcs','/cmjq/Heritrix/Software/heritrix-3.2.0/jobs/MonthlyLaborReview/download_dir','http://www.bls.gov',null,null);