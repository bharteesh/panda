package org.portico.conprep.ui.helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FolderPatternHandler{

    /**
    * List of String values to run the pattern
    * logic against.
    */
    private ArrayList fullList = new ArrayList();

    /**
    * List of fileter patterns to run before the
    * default pattern.
    */
    private ArrayList filterList = new ArrayList();

    /**
    * The default regular expression pattern. May or
    * may not have capture sections depending if it
    * is a complex or simple operation.
    */
    private String pattern = null;

    /**
    * This method is used for testing purposes only.
    */
    public static void main(String[] args){
        BufferedReader listIn = null;
        BufferedWriter listOut = null;
        try{
            listIn = new BufferedReader(
		        new FileReader("in.txt"));
            listOut = new BufferedWriter(
		        new FileWriter("out.txt"));
		    FolderPatternHandler handler = new FolderPatternHandler();
		    handler.setPattern(args[0]);
		    HelperClass.porticoOutput("Capture Pattern: " + args[0]);
		    if(args.length > 1){
		        for(int counter = 1; counter < args.length; counter++){
		            handler.addFilter(args[counter]);
		        }
		    }
		    String line = null;
		    while((line = listIn.readLine()) != null)
		        handler.addItem(line);
		    Collection collection = handler.processComplex();
		    if(collection != null && collection.size() > 0){
		        Iterator iterator = collection.iterator();
		        while(iterator.hasNext()){
		            FolderPatternHandler.Group group =
                        (FolderPatternHandler.Group) iterator.next();
                    FolderPatternHandler.Identifier identifier =
                        group.getIdentifier();
                    if(identifier == null){
                        listOut.write("Leftovers:");
                        listOut.newLine();
                    }else{
                        listOut.write("Tokens: ");
                        Iterator innerIterator = identifier.getTokens();
                        while(innerIterator.hasNext()){
                            listOut.write("'" + (String) innerIterator.next() + "' ");
                        }
                        listOut.newLine();
                    }
                    Iterator innerIterator = group.getItems();
                    if(innerIterator != null){
                        while(innerIterator.hasNext()){
                            listOut.write((String) innerIterator.next());
                            listOut.newLine();
                        }
                    }else{
                        listOut.write("Empty");
                        listOut.newLine();
                    }
                    listOut.newLine();
		        }
		    }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{listIn.close();}catch(Exception e){}
            try{listOut.flush();}catch(Exception e){}
            try{listOut.close();}catch(Exception e){}
        }
    }

    /**
    * This method returns a FolderPatternHandler.Group from the collection
    * containing items that were not classified by the
    * defined complex pattern.
    */
    public static FolderPatternHandler.Group getLeftovers(Collection collection){
        if(collection != null && collection.size() > 0){
            Iterator iterator = collection.iterator();
            while(iterator.hasNext()){
                Object object = iterator.next();
                if(object instanceof FolderPatternHandler.Group){
                    FolderPatternHandler.Group group =
                        (FolderPatternHandler.Group) object;
                    if(group.getIdentifier() == null) return group;
                }
            }
        }
        return null;
    }

    /**
    * This method returns a FolderPatternHandler.Group from the collection
    * containing items that were not classified by the
    * defined complex pattern and remove it from the collection.
    */
    public static FolderPatternHandler.Group removeLeftovers(Collection collection){
        if(collection != null && collection.size() > 0){
            Iterator iterator = new ArrayList(collection).iterator();
            while(iterator.hasNext()){
                Object object = iterator.next();
                if(object instanceof FolderPatternHandler.Group){
                    FolderPatternHandler.Group group =
                        (FolderPatternHandler.Group) object;
                    if(group.getIdentifier() == null){
                        collection.remove(group);
                        return group;
                    }
                }
            }
        }
        return null;
    }

    /**
    * This method sets the pattern. It should be a valid
    * Java regular expression. It should contain capture patterns
    * if a complex match is desired.
    */
    public void setPattern(String pattern){
        this.pattern = pattern;
    }

    public void addFilter(String filter){
        if(filter != null) filterList.add(filter);
    }

    public void addFilters(Collection filters){
        if(filters != null) filterList.addAll(filters);
    }

    public void addItem(String item){
        if(item != null) fullList.add(item);
    }

    public void addItems(Collection items){
        if(items != null) fullList.addAll(items);
    }

    public void resetCriteria(){
        pattern = null;
        filterList.clear();
    }

    public void clearList(){
        fullList.clear();
    }

    public Collection processSimple(){
        ArrayList resultList = new ArrayList();
        Iterator iterator = getItems();
        if(iterator != null){
            Pattern p = Pattern.compile(pattern);
            while(iterator.hasNext()){
                String item = (String) iterator.next();
                Matcher m = p.matcher(item);
                if(m.matches()) resultList.add(item);
            }
        }
        return resultList;
    }

    public Collection processComplex(){
        ArrayList workingList = new ArrayList(fullList);
        HashMap resultMap = new HashMap();
        initGroups(workingList, resultMap);
        if(resultMap.size() == 0) return resultMap.values();
        Iterator iterator = getFilters();
        if(iterator != null){
            while(iterator.hasNext()){
                String filter = (String) iterator.next();
                processFilters(workingList, resultMap, filter);
            }
        }
        processDefault(workingList, resultMap);
        if(workingList.size() > 0) processLeftovers(workingList, resultMap);
        return resultMap.values();
    }

    private Iterator getItems(){
        if(fullList.size() == 0) return null;
        return fullList.iterator();
    }

    private Iterator getFilters(){
        if(filterList.size() == 0) return null;
        return filterList.iterator();
    }

    private void processLeftovers(ArrayList workingList, HashMap resultMap){
        FolderPatternHandler.Group group = new FolderPatternHandler.Group();
        group.addAll(workingList);
        resultMap.put(null, group);
    }

    private void processDefault(ArrayList workingList, HashMap resultMap){
        Iterator iterator = resultMap.keySet().iterator();
        while(iterator.hasNext()){
            FolderPatternHandler.Identifier identifier = (FolderPatternHandler.Identifier) iterator.next();
            FolderPatternHandler.Group group = (FolderPatternHandler.Group) resultMap.get(identifier);
            String concreteDefault = concretizeDefault(identifier);
            Iterator innerIterator = new ArrayList(workingList).iterator();
            if(innerIterator != null){
                Pattern p = Pattern.compile(concreteDefault);
                while(innerIterator.hasNext()){
                    String item = (String) innerIterator.next();
                    Matcher m = p.matcher(item);
                    if(m.matches()){
                        group.addItem(item);
                        workingList.remove(item);
                    }
                }
            }
        }
    }

    private void processFilters(ArrayList workingList, HashMap resultMap, String filter){
        Iterator iterator = resultMap.keySet().iterator();
        while(iterator.hasNext()){
            FolderPatternHandler.Identifier identifier = (FolderPatternHandler.Identifier) iterator.next();
            FolderPatternHandler.Group group = (FolderPatternHandler.Group) resultMap.get(identifier);
            String concreteFilter = concretizeFilter(identifier, filter);
            Iterator innerIterator = new ArrayList(workingList).iterator();
            if(innerIterator != null){
                Pattern p = Pattern.compile(concreteFilter);
                while(innerIterator.hasNext()){
                    String item = (String) innerIterator.next();
                    Matcher m = p.matcher(item);
                    if(m.matches()){
                        group.addItem(item);
                        workingList.remove(item);
                    }
                }
            }
        }
    }

    private String concretizeFilter(FolderPatternHandler.Identifier identifier, String filter){
        String concreteFilter = filter;
        Iterator iterator = identifier.getTokens();
        if(iterator != null){
            int counter = 0;
            while(iterator.hasNext()){
                String token = (String) iterator.next();
                String variable = "<" + counter + ">";
                concreteFilter = concreteFilter.replaceAll(variable , token);
                counter++;
            }
        }
        return concreteFilter;
    }

    private String concretizeDefault(FolderPatternHandler.Identifier identifier){
        StringBuffer concreteDefault = new StringBuffer();
        concreteDefault.append(".*");
        Iterator iterator = identifier.getTokens();
        if(iterator != null){
            while(iterator.hasNext()){
                String token = (String) iterator.next();
                concreteDefault.append(token);
                concreteDefault.append(".*");
            }
        }
        return concreteDefault.toString();
    }

    private void initGroups(ArrayList workingList, HashMap resultMap){
        Iterator iterator = getItems();
        if(iterator != null){
            Pattern p = Pattern.compile(pattern);
            while(iterator.hasNext()){
                String item = (String) iterator.next();
                Matcher m = p.matcher(item);
                if(m.matches()){
                    int tokenCount = m.groupCount();
                    FolderPatternHandler.Identifier identifier = new FolderPatternHandler.Identifier();
                    for(int counter = 1; counter <= tokenCount; counter++)
                        identifier.addToken(m.group(counter));
                    if(resultMap.size() == 0 || isNew(resultMap, identifier))
                        resultMap.put(identifier, new FolderPatternHandler.Group(identifier));
                };
            }
        }
    }

    private boolean isNew(HashMap resultMap, FolderPatternHandler.Identifier identifier){
        Iterator iterator = resultMap.keySet().iterator();
        while(iterator.hasNext()){
            if(identifier.equals(iterator.next())) return false;
        }
        return true;
    }

    public static class Group{

        private FolderPatternHandler.Identifier _identifier = null;
        private ArrayList _itemList = new ArrayList();

        public Group(){}

        public Group(FolderPatternHandler.Identifier identifier){
            _identifier = identifier;
        }

        public FolderPatternHandler.Identifier getIdentifier(){
            return _identifier;
        }

        public void addItem(String item){
            _itemList.add(item);
        }

        public void addAll(Collection collection){
            _itemList.addAll(collection);
        }

        public int getSize(){
            return _itemList.size();
        }

        public Iterator getItems(){
            if(_itemList.size() == 0) return null;
            return _itemList.iterator();
        }

        public List getItemList(){
            return _itemList;
        }

        public String getItem(int index){
            if(index < 0 || index >= _itemList.size()) return null;
            return (String) _itemList.get(index);
        }

    }

    public static class Identifier{

        private ArrayList _tokenList = new ArrayList();

        public void addToken(String token){
            _tokenList.add(token);
        }

        public int getSize(){
            return _tokenList.size();
        }

        public Iterator getTokens(){
            if(_tokenList.size() == 0) return null;
            return _tokenList.iterator();
        }

       public String getToken(int index){
            if(index < 0 || index >= _tokenList.size()) return null;
            return (String) _tokenList.get(index);
        }

        public boolean equals(Object object){
            boolean flag = false;
            if(object instanceof Identifier){
                Identifier identifier = (Identifier) object;
                int size = getSize();
                int identifierSize = identifier.getSize();
                if(size == identifierSize){
                    if(size > 0){
                        for(int counter = 0; counter < size;){
                            String value = getToken(counter);
                            String identifierValue = identifier.getToken(counter);
                            if(!value.equals(identifierValue)) break;
                            counter++;
                            if(counter == size) flag = true;
                        }
                    }else if(size == 0) flag = true;
                }
            }
            return flag;
        }

    }

}
