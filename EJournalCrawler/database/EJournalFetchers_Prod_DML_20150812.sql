CREATE SEQUENCE  "EJF_DESTINATION_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;


Insert into EJF_TITLES (TITLE_ID,TITLE_JOURNAL_ID,TITLE_NAME,IS_ACTIVE,CREATE_TIMESTAMP) values (EJF_TITLES_SEQ.NEXTVAL,'NPH','New Phytologist','Y',CURRENT_TIMESTAMP);
Insert into EJF_TITLES (TITLE_ID,TITLE_JOURNAL_ID,TITLE_NAME,IS_ACTIVE,CREATE_TIMESTAMP) values (EJF_TITLES_SEQ.NEXTVAL,'ECOG','Ecography','Y',CURRENT_TIMESTAMP);
Insert into EJF_TITLES (TITLE_ID,TITLE_JOURNAL_ID,TITLE_NAME,IS_ACTIVE,CREATE_TIMESTAMP) values (EJF_TITLES_SEQ.NEXTVAL,'WSB','Wildlife Society Bulletin','Y',CURRENT_TIMESTAMP);
Insert into EJF_TITLES (TITLE_ID,TITLE_JOURNAL_ID,TITLE_NAME,IS_ACTIVE,CREATE_TIMESTAMP) values (EJF_TITLES_SEQ.NEXTVAL,'TGER','Unterrichtspraxis / Teaching German','Y',CURRENT_TIMESTAMP);
Insert into EJF_TITLES (TITLE_ID,TITLE_JOURNAL_ID,TITLE_NAME,IS_ACTIVE,CREATE_TIMESTAMP) values (EJF_TITLES_SEQ.NEXTVAL,'hpib','Harvard Papers in Botany','Y',CURRENT_TIMESTAMP);
Insert into EJF_TITLES (TITLE_ID,TITLE_JOURNAL_ID,TITLE_NAME,IS_ACTIVE,CREATE_TIMESTAMP) values (EJF_TITLES_SEQ.NEXTVAL,'pnas','Proceedings of the National Academy of Sciences ','Y',CURRENT_TIMESTAMP);
Insert into EJF_TITLES (TITLE_ID,TITLE_JOURNAL_ID,TITLE_NAME,IS_ACTIVE,CREATE_TIMESTAMP) values (EJF_TITLES_SEQ.NEXTVAL,'ebul','Bulletin of the Ecological Society of America','Y',CURRENT_TIMESTAMP);
Insert into EJF_TITLES (TITLE_ID,TITLE_JOURNAL_ID,TITLE_NAME,IS_ACTIVE,CREATE_TIMESTAMP) values (EJF_TITLES_SEQ.NEXTVAL,'er','Environmental Reviews','Y',CURRENT_TIMESTAMP);


Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,1,'FEED_ID','46');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,1,'IGNORE_UPDATES','true');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,1,'REGEX_RULE','.*/jid?_(vol?)_(iss?)\.zip');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,1,'VOL_RANGE','194-208');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,1,'ISSUE_RANGE','1-4');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,2,'FEED_ID','46');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,2,'IGNORE_UPDATES','true');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,2,'REGEX_RULE','.*/jid?_(vol?)_(iss?)(\.zip|_\d{6,}\.zip)');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,2,'VOL_RANGE','36-40');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,2,'ISSUE_RANGE','1-12');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,3,'FEED_ID','46');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,3,'IGNORE_UPDATES','true');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,3,'REGEX_RULE','.*/jid?_(vol?)_(iss?)\.zip');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,3,'VOL_RANGE','36-42');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,3,'ISSUE_RANGE','1-4');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,4,'FEED_ID','46');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,4,'IGNORE_UPDATES','true');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,4,'REGEX_RULE','.*/jid?_(vol?)_(iss?)\.zip');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,4,'VOL_RANGE','45-50');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,4,'ISSUE_RANGE','1-2');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,5,'FEED_ID','10');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,5,'IGNORE_UPDATES','true');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,5,'REGEX_RULE','.*_jid?_10\.3100%2f025\.0(vol?)\.0(iss?)00_\d{6,}\.zip');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,5,'VOL_RANGE','19-21');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,5,'ISSUE_RANGE','1-2');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,6,'FEED_ID','155');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,6,'IGNORE_UPDATES','true');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,6,'REGEX_RULE','.*/jid?[_|-](vol?)[_|-](iss?)[\.|-].*[\.|-]pdfs');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,6,'VOL_RANGE','112-114');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,6,'ISSUE_RANGE','1-18');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,7,'FEED_ID','11,170');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,7,'IGNORE_UPDATES','true');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,7,'REGEX_RULE','.*_jid?_10\.1890%2f00129623-(vol?)\.(iss?)_\d{6,}\.zip');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,7,'VOL_RANGE','80-89,93-97');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,7,'ISSUE_RANGE','1-4');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,8,'FEED_ID','17');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,8,'IGNORE_UPDATES','true');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,8,'REGEX_RULE','.*_jid?_10\.1139%2fer((\.\d{4}\.)|)(vol?)(0(iss?)|na)_\d{6,}\.zip');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,8,'VOL_RANGE','15-18,20,22-25');
Insert into EJF_TITLE_CONFIG (CONFIG_ID,TITLE_ID,NAME,VALUE) values (EJF_TITLE_CONFIG_SEQ.NEXTVAL,8,'ISSUE_RANGE','1-4');


Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,1,'aa1thumper01.jstor.org','jstor','/z/cmshared/Production/BornDigitalRepository/UNALTERED_SUPPLIED_FILES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,1,'aa2ptcthumper05.ithaka.org','jstor','/csp_content/ongoing/backfileCSP/PAGE_IMAGES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,2,'aa1thumper01.jstor.org','jstor','/z/cmshared/Production/BornDigitalRepository/UNALTERED_SUPPLIED_FILES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,2,'aa2ptcthumper05.ithaka.org','jstor','/csp_content/ongoing/backfileCSP/PAGE_IMAGES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,3,'aa1thumper01.jstor.org','jstor','/z/cmshared/Production/BornDigitalRepository/UNALTERED_SUPPLIED_FILES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,3,'aa2ptcthumper05.ithaka.org','jstor','/csp_content/ongoing/backfileCSP/PAGE_IMAGES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,4,'aa1thumper01.jstor.org','jstor','/z/cmshared/Production/BornDigitalRepository/UNALTERED_SUPPLIED_FILES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,4,'aa2ptcthumper05.ithaka.org','jstor','/csp_content/ongoing/backfileCSP/PAGE_IMAGES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,5,'aa1thumper01.jstor.org','jstor','/z/cmshared/Production/BornDigitalRepository/UNALTERED_SUPPLIED_FILES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,5,'aa2ptcthumper05.ithaka.org','jstor','/csp_content/ongoing/backfileCSP/PAGE_IMAGES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,6,'aa1thumper01.jstor.org','jstor','/z/cmshared/Production/BornDigitalRepository/UNALTERED_SUPPLIED_FILES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,6,'aa2ptcthumper05.ithaka.org','jstor','/csp_content/ongoing/backfileCSP/PAGE_IMAGES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,7,'aa1thumper01.jstor.org','jstor','/z/cmshared/Production/BornDigitalRepository/UNALTERED_SUPPLIED_FILES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,7,'aa2ptcthumper05.ithaka.org','jstor','/csp_content/ongoing/backfileCSP/PAGE_IMAGES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,8,'aa1thumper01.jstor.org','jstor','/z/cmshared/Production/BornDigitalRepository/UNALTERED_SUPPLIED_FILES',CURRENT_TIMESTAMP,null);
Insert into EJF_DESTINATIONS (DESTINATION_ID,TITLE_ID,SERVER_NAME,SERVER_USER,SERVER_PATH,CREATED_TIMESTAMP,MODIFIED_TIMESTAMP) values (EJF_DESTINATION_SEQ.NEXTVAL,8,'aa2ptcthumper05.ithaka.org','jstor','/csp_content/ongoing/backfileCSP/PAGE_IMAGES',CURRENT_TIMESTAMP,null);
