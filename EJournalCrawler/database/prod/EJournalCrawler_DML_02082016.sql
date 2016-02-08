Insert into CRAWLER_CONFIG 
   	(ID,PROVIDER,WARC_FILE_LOCATION,DOWNLOAD_DIR_LOCATION,PROVIDER_BASE_URL,RSYNC_COMMAND,DELETE_FILES_FROM_DOWNLOAD_DIR) 
   values 
	(2,'CJS','/cmjp/EJournalCrawler/Heritrix/Software/heritrix-3.2.0/jobs/canajsocicahican/latest/warcs','/cmjp/EJournalCrawler/Content','https://ejournals.library.ualberta.ca/index.php/CJS/issue/archive',
	'jstor@aa2ptcthumper05.ithaka.org:/csp_content/ongoing/backfileCSP/PAGE_IMAGES','Y');
	

Insert into CRAWLER_CONFIG 
   	(ID,PROVIDER,WARC_FILE_LOCATION,DOWNLOAD_DIR_LOCATION,PROVIDER_BASE_URL,RSYNC_COMMAND,DELETE_FILES_FROM_DOWNLOAD_DIR) 
   values 
	(3,'CJPH','/cmjp/EJournalCrawler/Heritrix/Software/heritrix-3.2.0/jobs/canajpublheal/latest/warcs','/cmjp/EJournalCrawler/Content','http://journal.cpha.ca/index.php/cjph/issue/archive',
	'jstor@aa2ptcthumper05.ithaka.org:/csp_content/ongoing/backfileCSP/PAGE_IMAGES','Y');	

UPDATE CRAWLER_CONFIG SET EXTRACTOR_NAME='MLRPDFExtractor' WHERE PROVIDER='MLR';
UPDATE CRAWLER_CONFIG SET EXTRACTOR_NAME='CJSPDFExtractor' WHERE PROVIDER='CJS';
UPDATE CRAWLER_CONFIG SET EXTRACTOR_NAME='CJSPDFExtractor' WHERE PROVIDER='CJPH';

UPDATE CRAWLER_CONFIG SET JOURNAL_CODE='monthlylaborrev' WHERE PROVIDER='MLR';
UPDATE CRAWLER_CONFIG SET JOURNAL_CODE='canajsocicahican' WHERE PROVIDER='CJS';
UPDATE CRAWLER_CONFIG SET JOURNAL_CODE='canajpublheal' WHERE PROVIDER='CJPH';

update CRAWLER_CONFIG set start_year=2008, start_volume=33, start_issue=1 where JOURNAL_CODE='canajsocicahican'
update CRAWLER_CONFIG set start_year=2012,  start_volume=103, start_issue=4  where JOURNAL_CODE='canajpublheal'


COMMIT;