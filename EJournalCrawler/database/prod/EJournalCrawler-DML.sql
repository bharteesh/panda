Insert into CRAWLER_CONFIG 
   	(ID,PROVIDER,WARC_FILE_LOCATION,DOWNLOAD_DIR_LOCATION,PROVIDER_BASE_URL,RSYNC_COMMAND,DELETE_FILES_FROM_DOWNLOAD_DIR) 
   values 
	(1,'MLR','/cmjp/EJournalCrawler/Heritrix/jobs/MonthlyLaborReview/latest/warcs','/cmjp/EJournalCrawler/Content','http://www.bls.gov','jstor@aa2ptcthumper05.ithaka.org:/csp_content/ongoing/backfileCSP/PAGE_IMAGES/MonthlyLaborReview','Y');