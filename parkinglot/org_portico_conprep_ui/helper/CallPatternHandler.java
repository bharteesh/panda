package org.portico.conprep.ui.helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

public class CallPatternHandler{

    /**
    * List of String values to run the pattern
    * logic against.
    */
    private ArrayList masterInputList = new ArrayList();
    // private ArrayList inputList = new ArrayList();
    private String patternString = "";
    private String rootName = "";
    private String originalFileSeparator = "";

    // Result has objects as 'token' string, its children ArrayList
    private ArrayList resultList = new ArrayList();

    /**
    * The default regular expression pattern. May or
    * may not have capture sections depending if it
    * is a complex or simple operation.
    */
    private String fileSeparator = "/";
    private char fileSeparatorChar = '/';
    private int tabCount = 0;

    /**
    * This method is used for testing purposes only.
    */
    BufferedReader listIn = null;
    BufferedWriter listOut = null;

    public static void main(String[] args){
		CallPatternHandler tCallPatternHandler = new CallPatternHandler();
		tCallPatternHandler.initiateProcessHandler();
    }

    public CallPatternHandler()
    {
	}

    public CallPatternHandler(String tRootName)
    {
		super();
		setRootName(tRootName);
	}

    public void setRootName(String tRootName)
    {
		rootName = tRootName;
	}

    public String getRootName()
    {
		return this.rootName;
	}

	public void setOriginalFileSeparator(String itemName)
	{
		if(itemName.indexOf("\\") != -1)
		{
	    	originalFileSeparator = "\\";
	    }
	    else
	    {
			originalFileSeparator = "/";
		}
	}

	public String getOriginalFileSeparator()
	{
		return this.originalFileSeparator;
	}

    public void initiateProcessHandler()
    {
        try
        {
            listIn = new BufferedReader(
		        new FileReader("in.txt"));
            listOut = new BufferedWriter(
		        new FileWriter("out.txt"));
		    String line = null;
		    while((line = listIn.readLine()) != null)
		    {
    	        addProcessingItems(line);
			}
    		processHandler();
			if(resultList != null && resultList.size() > 0)
			{
				HelperClass.porticoOutput("Start print Results");
		    	for(int resultIndx=0; resultIndx < resultList.size(); resultIndx++)
		    	{
					HelperClass.porticoOutput("----------------------------------------------");
				    SubmissionPatternResultItem tItem = (SubmissionPatternResultItem)resultList.get(resultIndx);
				    String currentItem = tItem.getThisToken();
				    String parentItem = tItem.getParentToken();
					HelperClass.porticoOutput("currentItem="+currentItem);
					HelperClass.porticoOutput("parentItem="+parentItem);
					HelperClass.porticoOutput("----------------------------------------------");
				}
				HelperClass.porticoOutput("End print Results");


				SubmissionPatternResultItem tItem = null;
				String rootName = "";
				for(int idx=0; idx < resultList.size(); idx++)
				{
					 tItem = (SubmissionPatternResultItem)resultList.get(idx);
					 if(tItem.getParentToken().equals(""))
					 {
						 rootName = tItem.getThisToken();
						 break;
					 }
				}

				// Create root node
				HelperClass.porticoOutput("RootNode="+rootName);

				HelperClass.porticoOutput("Start Tree Results");
			    boolean tStatus = getChildren(rootName, getTabCount());
				    // Need not create node , already created in 'getChildren'
				HelperClass.porticoOutput("End Tree Results");
		    }
		}
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try{listIn.close();}catch(Exception e){}
            try{listOut.flush();}catch(Exception e){}
            try{listOut.close();}catch(Exception e){}
        }
	}

	public void addProcessingItems(String itemName)
	{
		if(originalFileSeparator.equals(""))
		{
			setOriginalFileSeparator(itemName);
		}
		String massagedString = itemName;
		massagedString = massageInputString(massagedString);
		masterInputList.add(massagedString);
	}

	public void addProcessingItems(ArrayList c)
	{
		for(int cindx=0; cindx < c.size(); cindx++)
		{
			addProcessingItems((String)c.get(cindx));
		}
	}

	public ArrayList getProcessedItems()
	{
		return resultList;
	}

	public ArrayList populateInputListFromMasterList(int patternCounter)
	{
		ArrayList currentInputList = new ArrayList();
		StringTokenizer sTokenizer = null;
		for(int tindx=0; tindx < masterInputList.size(); tindx++)
		{
			String str = (String)masterInputList.get(tindx);
			sTokenizer = new StringTokenizer(str, getFileSeparator());
			int runningCount = sTokenizer.countTokens()-1; // c:/a/b = 2(windows), /a/b = 2(unix)
			if(runningCount == patternCounter)
			{
				currentInputList.add(str);
			}
		}
		return currentInputList;
	}

	public void processHandler()
	{
        try
        {
    	    // FolderPatternHandler "(.*/.*/.*/.*/.*/.*/).*" used for "c:/temp/willey/ajp/bb/dd/l.pdf", patternCounter=tokencount=6-1=5
    	    // For next iteration pick all the LeftOvers, tokens, do till no LeftOvers and token group is count 1,
    	    // then process is completed.
    	    // Also the tokens to be processed
    	    // int patternCounter = 2; // Asset that has max number of separators on it, this max count-1 is used

    	    setFileSeparator("/");
    	    int patternMaxCounter = getMaxPatternCount();
    	    int patternCounter = patternMaxCounter;
    	    ArrayList toProcess = null;
    	    while(patternCounter > 0)
    	    {
				setPatternString(patternCounter);
				ArrayList inputList = populateInputListFromMasterList(patternCounter);
				if(inputList == null)
				{
				    inputList = new ArrayList();
			    }


			    if(toProcess != null && toProcess.size() > 0)
			    {
			    	inputList.addAll(toProcess);
			    }

				toProcess = processAndReturnUnprocessedTokens(inputList);
/*
				if(toProcess.size() <= 1)
				{
					HelperClass.porticoOutput("Done - toProcess size is < = 1 count");
					break;
				}
*/
			    // inputList.clear();
			    // inputList.addAll(toProcess);
			    patternCounter--;
			}

			SubmissionPatternResultItem thisItem = null;
			if(toProcess != null && toProcess.size() > 0)
			{
				for(int tindx=0; tindx < toProcess.size(); tindx++)
				{
					// all items falling directly underneath the rootname(batch_name)
					String tItem = (String)toProcess.get(tindx);
			    	thisItem = new SubmissionPatternResultItem();
           			thisItem.setThisToken(tItem);
			    	String tOrgFileSeparator = getOriginalFileSeparator();
			    	if(tOrgFileSeparator != null && tOrgFileSeparator.equals("\\"))
			    	{
						char massagedChar = '/';
						char originalChar = '\\';
			        	tItem = tItem.replace(massagedChar, originalChar);
				    }
           			thisItem.setOriginalToken(tItem);
           	    	thisItem.setParentToken(getRootName());
           	    	resultList.add(thisItem);
			    }
			}

            // This is added, to take care of filename(s)/objectname(s) without "/" separators, just filename(s)
            // These go directly underneath the Batch('getRootName()')
			ArrayList leftOverNoSeparator = populateInputListFromMasterList(0);
			if(leftOverNoSeparator != null && leftOverNoSeparator.size() > 0)
			{
				for(int lindx=0; lindx < leftOverNoSeparator.size(); lindx++)
				{
					String tItem = (String)leftOverNoSeparator.get(lindx);
			    	thisItem = new SubmissionPatternResultItem();
           			thisItem.setThisToken(tItem);
           			thisItem.setOriginalToken(tItem);
           	    	thisItem.setParentToken(getRootName());
           	    	resultList.add(thisItem);
				}
			}

			thisItem = new SubmissionPatternResultItem();
   			thisItem.setThisToken(getRootName());
   	    	thisItem.setParentToken("");
   	    	resultList.add(thisItem);
        }
        catch(Exception e)
        {
			HelperClass.porticoOutput("Exception in CallPatternHandler-processHandler()="+e.getMessage());
            e.printStackTrace();
        }
        finally
        {
        }
	}

	public String massageInputString(String inputString)
	{
		char oldChar = '\\';
		char newChar = fileSeparatorChar;
		String massagedString = inputString;
		massagedString = massagedString.replace(oldChar, newChar);
		if(massagedString.startsWith(getFileSeparator()))
		{
			// massagedString = " "+massagedString;
			try
			{
		    	massagedString = massagedString.substring(massagedString.indexOf(getFileSeparator())+1, massagedString.length());
		    }
		    catch(Exception e)
		    {
                HelperClass.porticoOutput("Exception in CallPatternHandler-massageInputString()-inputString="+inputString+":Exception="+e.getMessage());
			}
		}
		return massagedString;
	}

	public boolean getChildren(String parentName, int currentTabCount)
	{
		boolean tStatus = true;

		createThisNode(parentName, currentTabCount);

		int childTabCount = getTabCount();
		SubmissionPatternResultItem tItem = null;
		for(int indx=0; indx < resultList.size(); indx++)
		{
			tItem = (SubmissionPatternResultItem)resultList.get(indx);
			String currentTokenName = tItem.getThisToken();
			if(tItem.getParentToken().equals(parentName))
			{
				tStatus = getChildren(tItem.getThisToken(), childTabCount);
			}
		}
		return tStatus;
	}

	public void createThisNode(String nodeName, int currentTabCount)
	{
		String tTabSpace = "";
		for(int tabIndx=0; tabIndx < currentTabCount; tabIndx++)
		{
		    tTabSpace += "    ";
		}
		HelperClass.porticoOutput(tTabSpace + nodeName);
	}

	public int getTabCount()
	{
		return tabCount++;
	}

	public int getMaxPatternCount()
	{
		int maxPatternCount = 0;
		int runningCount = 0;
		StringTokenizer sTokenizer = null;
		for(int tindx=0; tindx < masterInputList.size(); tindx++)
		{
			sTokenizer = new StringTokenizer((String)masterInputList.get(tindx), getFileSeparator());
			runningCount = sTokenizer.countTokens()-1; // c:/a/b = 2(windows), /a/b = 2(unix)
			if(runningCount > maxPatternCount)
			{
				maxPatternCount = runningCount;
			}
		}
		return maxPatternCount;
	}


	public void setPatternString(int pCount)
	{
		StringBuffer sBuf = new StringBuffer();
		sBuf.append("(");
		for(int idx=0; idx < pCount; idx++)
		{
			sBuf.append(".*");
			sBuf.append(getFileSeparator());
		}
		sBuf.append(")");
		sBuf.append(".*");
		patternString = sBuf.toString();
		HelperClass.porticoOutput("patternString="+patternString);
	}

	public void setFileSeparator(String tSeparator)
	{
		fileSeparator = tSeparator;
	}

	public String getFileSeparator()
	{
		return this.fileSeparator;
	}

	public ArrayList processAndReturnUnprocessedTokens(ArrayList inputList)
	{
		ArrayList toProcess = new ArrayList();
	    FolderPatternHandler handler = new FolderPatternHandler();
	    handler.setPattern(patternString);
	    handler.addItems(inputList);
	    HelperClass.porticoOutput("Capture Pattern: " + patternString);
        try
        {
		    Collection collection = handler.processComplex();
		    if(collection != null && collection.size() > 0){
		        Iterator iterator = collection.iterator();
		        while(iterator.hasNext()){
					String parentToken = "";
		            FolderPatternHandler.Group group =
                        (FolderPatternHandler.Group) iterator.next();
                    FolderPatternHandler.Identifier identifier =
                        group.getIdentifier();
                    if(identifier == null){
                    }else{
                        Iterator innerIterator = identifier.getTokens();
                        while(innerIterator.hasNext()){
							String tToken = (String) innerIterator.next();
                            parentToken = tToken;
                            if(tToken.endsWith(getFileSeparator()))
                            {
							   parentToken = tToken.substring(0, tToken.length()-1); // ,lastIndexOf(getFileSeparator());
							   HelperClass.porticoOutput("parentToken=" + parentToken);
							}
                            toProcess.add(parentToken);
                        }
                    }
                    Iterator innerIterator = group.getItems();
                    if(innerIterator != null){
						SubmissionPatternResultItem thisItem = null;
                        while(innerIterator.hasNext()){
							String childToken = (String) innerIterator.next();
                            if(parentToken.equals(""))
                            {
								toProcess.add(childToken);
								HelperClass.porticoOutput("(parent space - Inner="+childToken);
								HelperClass.porticoOutput("parent space - parentToken="+parentToken);
							}
							else
							{
								HelperClass.porticoOutput("(parent not space - Inner="+childToken);
								HelperClass.porticoOutput("parent not space - parentToken="+parentToken);

        				    	thisItem = new SubmissionPatternResultItem();
        				    	thisItem.setThisToken(childToken);
        				    	thisItem.setParentToken(parentToken);
        				    	resultList.add(thisItem);
						    }
                        }
                    }else{
                    }

                    //toProcess.size() <= 1, we are done
		        }
		    }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
        }
        return toProcess;
    }
}
