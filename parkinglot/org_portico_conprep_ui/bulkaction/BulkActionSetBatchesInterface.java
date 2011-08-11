/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project          ConPrep WebTop
 * Module
 * File             BulkActionSetBatchesInterface.java
 * Created on       Feb 18, 2008
 *
 */
package org.portico.conprep.ui.bulkaction;

import java.util.Hashtable;
import java.util.TreeMap;

/**
 * Description  An Interface which has to be implemented by all the bulk actions.
 * Author       Ranga
 * Type         BulkActionSetBatchesInterface
 */
public interface BulkActionSetBatchesInterface
{
    public boolean processPrecondition();
	public boolean processUpdate(Hashtable addlnData);
	public boolean processAnnotation(Hashtable addlnData);
	public TreeMap getSuccessfulPreconditionBatchList();
	public TreeMap getFailedPreconditionBatchList();
	public TreeMap getSuccessfulUpdateBatchList();
	public TreeMap getFailedUpdateBatchList();
	public void clearData();
}