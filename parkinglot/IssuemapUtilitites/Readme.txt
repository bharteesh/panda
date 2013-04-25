Readme



1. First Execute Issuemap Directory Crawler


2. Then execute Issuemap DB Organizer



What are we doing.

We need to see various versions of issues falling under certain criteria's to see if article para are different.

Criteria's are- 

1: Check if there are multiple versions of the issuemaps existing. 

2: Then check, if the primary issuemap has version 0.9 and falls under GIG 5.0, 5.1, 5.2

3: Then for all the revisions, save the article p elements

4: Check to see if there are any descripency between the count of p elements.

5: Generate a report for users to manually inspect.



Execution Sequence:

First Execute MasterController, which will crawel through PHX Repo and then put them into respective Issuemap Parent - Child Relationship.

Second Execute IssuemapGeneralMatcherController to update the database for match status = true, plus find if there is any conflict between various revision issuemap and its respective article.
If there is any conflict, we ill insert the para details to its corresponding tables, and also have an entry of the primary issuemap id in the conflict master tabel


//OK we all all done with the process. No it is the reporting process.

First, find all the conflict primary issue map.

For each issue map uid execute the below SQL

SELECT jm.journal_id, ip.issue_master_uid, ip.issue_id, ip.journal_master_uid, ic.issue_parent_uid, ic.filename, ic.abspath, ia.issue_article_uid, ia.issue_child_uid, ia.articleDOI, 
ia.numberofparas FROM issuemapupdater.issue_child ic, issuemapupdater.issue_article ia, issuemapupdater.issue_parent ip, issuemapupdater.journal_master jm
WHERE articleDOI in (SELECT articleDOI FROM issuemapupdater.issue_article where issue_child_uid = 15) 
AND ic.issue_child_uid = ia.issue_child_uid 
AND ip.issue_master_uid= ic.issue_parent_uid 
AND jm.journal_master_uid = ip.journal_master_uid ORDER BY ia.articleDOI ASC, ia.issue_article_uid ASC;


Now have them displayed in an HTML with the following style.

Your Report will have 

Journal ID, Issue ID, ArticleDOI #
Issuemap.xml revision Info,  Number of Para - GREEN Color if Primary


Also Provide a statistics of how many issues are having this problem.
Third - Since all para has been inserted succesfully, let us create a new table which show the difference in para elemenents between revisions

