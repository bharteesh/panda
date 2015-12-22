Insert into CRAWLER_CONFIG 
   	(ID,PROVIDER,WARC_FILE_LOCATION,DOWNLOAD_DIR_LOCATION,PROVIDER_BASE_URL,RSYNC_COMMAND,DELETE_FILES_FROM_DOWNLOAD_DIR) 
   values 
	(1,'MLR','/cmjq/Heritrix/Software/heritrix-3.2.0/jobs/MonthlyLaborReview/latest/warcs','/cmjd/workarea/Heritrix/Jobs/MonthlyLaborReview/downloaded_files','http://www.bls.gov','jstor@aa2ptcthumper05.ithaka.org:/csp_content/WIP/QA/WebCrawler/MonthlyLaborReview','Y');