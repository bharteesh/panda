CREATE OR REPLACE PACKAGE ejf_reg_sp
AS
  /* TODO enter package declarations (types, exceptions, methods etc) here */
  PROCEDURE insert_mock_data(
      test_case_id IN VARCHAR2);
  PROCEDURE delete_mock_data(
      test_case_id IN VARCHAR2);
  PROCEDURE verify_results(
      test_case_id IN VARCHAR2);
  END ejf_reg_sp;
/


CREATE OR REPLACE PACKAGE BODY ejf_reg_sp
AS
  PROCEDURE insert_mock_data(
      test_case_id IN VARCHAR2)
  AS
  BEGIN
    INSERT
    INTO PCL_DATA_UNIT
      (
        DATA_UNIT_ID,
        FEED_ID,
        SRC_FILE_NAME,
        SRC_UNIQUE_FILE_NAME,
        DEST_FILE_NAME,
        FILE_SIZE,
        RECEIPT_DATE,
        RECEIPT_DATE_TYPE,
        IS_PREPOPULATED,
        IS_UPDATE,
        IS_ACTIVE,
        INACTIVATION_REASON_TYPE,
        STATUS,
        SUCCESSOR_ID,
        PREDECESSOR_ID,
        CREATED_DATE,
        MODIFIED_DATE,
        RUN_ID,
        CHECKSUM,
        CHECKSUM_TYPE
      )
    SELECT DATA_UNIT_ID,
      FEED_ID,
      SRC_FILE_NAME,
      SRC_UNIQUE_FILE_NAME,
      DEST_FILE_NAME,
      FILE_SIZE,
      RECEIPT_DATE,
      RECEIPT_DATE_TYPE,
      IS_PREPOPULATED,
      IS_UPDATE,
      IS_ACTIVE,
      INACTIVATION_REASON_TYPE,
      STATUS,
      SUCCESSOR_ID,
      PREDECESSOR_ID,
      CREATED_DATE,
      MODIFIED_DATE,
      RUN_ID,
      CHECKSUM,
      CHECKSUM_TYPE
    FROM PCL_DATA_UNIT_REG
    WHERE REG_TEST_CASE_ID = test_case_id
    AND MOCK_OPERATION_TYPE='COPY';
    INSERT
    INTO EJF_TITLE_FETCHED
      (
        TITLE_FETCHED_ID,
        TITLE_ID,
        DATA_UNIT_ID,
        CREATED_TIMESTAMP,
        MODIFIED_TIMESTAMP
      )
    SELECT TITLE_FETCHED_ID,
      TITLE_ID,
      DATA_UNIT_ID,
      CREATED_TIMESTAMP,
      MODIFIED_TIMESTAMP
    FROM EJF_TITLE_FETCHED_REG
    WHERE REG_TEST_CASE_ID = test_case_id
    AND MOCK_OPERATION_TYPE='COPY';
  END insert_mock_data;
-- DELETE MOCK DATA
  PROCEDURE delete_mock_data(
      test_case_id IN VARCHAR2)
  AS
  BEGIN
    DELETE
    FROM PCL_DATA_UNIT
    WHERE DATA_UNIT_ID IN
      (SELECT data_unit_id
      FROM PCL_DATA_UNIT_REG
      WHERE REG_TEST_CASE_ID = test_case_id
      AND MOCK_OPERATION_TYPE='COPY'
      );
    DELETE
    FROM EJF_TITLE_FETCHED
    WHERE DATA_UNIT_ID IN
      (SELECT data_unit_id
      FROM EJF_TITLE_FETCHED_REG
      WHERE REG_TEST_CASE_ID   = test_case_id
      AND MOCK_OPERATION_TYPE IN ('COPY','VERIFY')
      );
  END delete_mock_data;
-- VERIFY RESULTS
  PROCEDURE verify_results(
      test_case_id IN VARCHAR2)
    --output_message OUT VARCHAR2)
  AS
  BEGIN
    DECLARE
      verify_failure    EXCEPTION ;
      fetched_reg_count VARCHAR(50);
      fetched_count     VARCHAR(50);
    BEGIN
      SELECT COUNT(*)
      INTO fetched_count
      FROM ejf_title_fetched
      WHERE EXISTS
        (SELECT data_unit_id,
          title_id
        FROM ejf_title_fetched_reg
        WHERE mock_operation_type          = 'VERIFY'
        AND reg_test_case_id               = test_case_id
        AND ejf_title_fetched.data_unit_id = ejf_title_fetched_reg.data_unit_id
        AND ejf_title_fetched.title_id     = ejf_title_fetched_reg.title_id
        );
      SELECT COUNT(*)
      INTO fetched_reg_count
      FROM ejf_title_fetched_reg
      WHERE mock_operation_type = 'VERIFY'
      AND reg_test_case_id      = test_case_id;
      IF fetched_reg_count      = fetched_count THEN
        dbms_output.put_line('data matched successfully');
      ELSE
        RAISE verify_failure ;
      END IF;
    END ;
  END verify_results;
END ejf_reg_sp;
/
