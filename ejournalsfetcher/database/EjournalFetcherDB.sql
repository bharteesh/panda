  CREATE TABLE "EJF_TITLES" 
   (	"TITLE_ID" NUMBER NOT NULL ENABLE, 
	"TITLE_JOURNAL_ID" VARCHAR2(255 BYTE) NOT NULL ENABLE, 
	"TITLE_NAME" VARCHAR2(500 BYTE), 
	"IS_ACTIVE" CHAR(1 BYTE) NOT NULL ENABLE, 
	"CREATE_TIMESTAMP" TIMESTAMP (6) NOT NULL ENABLE, 
	 CONSTRAINT "EJF_TITLES_PK" PRIMARY KEY ("TITLE_ID")
   );

  CREATE TABLE "EJF_TITLE_CONFIG" 
   (	"CONFIG_ID" NUMBER NOT NULL ENABLE, 
	"TITLE_ID" NUMBER NOT NULL ENABLE, 
	"NAME" VARCHAR2(255 BYTE) NOT NULL ENABLE, 
	"VALUE" VARCHAR2(1000 BYTE) NOT NULL ENABLE, 
	 CONSTRAINT "EJF_TITLE_CONFIG_PK" PRIMARY KEY ("CONFIG_ID"), 
	 CONSTRAINT "EJF_TITLE_CONFIG_EJF_TITL_FK1" FOREIGN KEY ("TITLE_ID")
	 REFERENCES "EJF_TITLES" ("TITLE_ID")
   );

  CREATE TABLE "EJF_TITLE_FETCHED" 
   (	"TITLE_FETCHED_ID" NUMBER NOT NULL ENABLE, 
	"TITLE_ID" NUMBER NOT NULL ENABLE, 
	"DATA_UNIT_ID" NUMBER NOT NULL ENABLE, 
	"CREATED_TIMESTAMP" TIMESTAMP (6), 
	"MODIFIED_TIMESTAMP" TIMESTAMP (6), 
	"VSTAMP" TIMESTAMP (6), 
	CONSTRAINT "EJF_TITLE_FETCHED_PK" PRIMARY KEY ("TITLE_FETCHED_ID"), 
	CONSTRAINT "EJF_TITLE_FETCHED_EJF_TIT_FK1" FOREIGN KEY ("TITLE_ID")
	REFERENCES "EJF_TITLES" ("TITLE_ID")
   );

  CREATE TABLE "EJF_DESTINATIONS" 
   (	"DESTINATION_ID" NUMBER NOT NULL ENABLE, 
	"TITLE_ID" NUMBER NOT NULL ENABLE, 
	"SERVER_NAME" VARCHAR2(255 BYTE) NOT NULL ENABLE, 
	"SERVER_USER" VARCHAR2(255 BYTE) NOT NULL ENABLE, 
	"SERVER_PATH" VARCHAR2(1000 BYTE) NOT NULL ENABLE, 
	"CREATED_TIMESTAMP" TIMESTAMP (6), 
	"MODIFIED_TIMESTAMP" TIMESTAMP (6), 
	 CONSTRAINT "EJF_DESTINATIONS_PK" PRIMARY KEY ("DESTINATION_ID"), 
	 CONSTRAINT "EJF_DESTINATIONS_EJF_TITL_FK1" FOREIGN KEY ("TITLE_ID")
	 REFERENCES "EJF_TITLES" ("TITLE_ID") ENABLE
   );



   CREATE SEQUENCE  "EJF_TITLE_CONFIG_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

   CREATE SEQUENCE  "EJF_TITLES_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

   CREATE SEQUENCE  "EJF_TITLE_FETCHED_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;


   CREATE INDEX TITLE_CONFIG_TITLE_ID_IND ON EJF_TITLE_CONFIG(TITLE_ID);
   CREATE INDEX TITLE_FETCHED_TITLE_ID_IND ON EJF_TITLE_FETCHED(DATA_UNIT_ID);