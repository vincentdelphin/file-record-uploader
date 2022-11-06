package com.example.uploadingfiles;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.springframework.web.multipart.MultipartFile;

@Entity
public class Records {
    

    // UniqueIdentifier (some way to uniquely identify the record):
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    private long id;

    // Company Name (taken directly from the text file, i.e. ASH RAIL LTD).
    // Company Number (“”, i.e. 11467106).
    // EventType (The code should be mapped to the index at the beginning of the file. I.e. C2 should be mapped to “NOTIFICATION OF ANY CHANGE AMONG THE COMPANY'S DIRECTORS.”).
    // EventDate (directly from the code).
    
    private String companyName;
    private String companyNumber;
    private String eventType;
    private String eventDate;

    public Records() {
		//default constructor
	}
    
    public Records(String companyName, String companyNumber, String eventType, String eventDate) {
        this.companyName = companyName;
        this.companyNumber = companyNumber;
        this.eventType = eventType;
        this.eventDate = eventDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    @Override
    public String toString() {
      return "company entry [id=" + id + ", company name =" + companyName + ", company name =" + companyName + ", event type =" + eventType + ", event date =" + eventDate + "]";
    }
    
    public Map<String, String> readAllLinesArray(MultipartFile file){
        BufferedReader br;
        List<String> result = new ArrayList<>();
        try {
        String line;
        InputStream is = file.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));
        String regex = "\\s*[(][A-Z]\\d*[)]";
        Pattern pattern = Pattern.compile(regex);
        HashMap<String,String> map = new HashMap<String,String>();

        while ((line = br.readLine()) != null) {

            Matcher m = pattern.matcher(line);
            if((line.contains("LOUISE SMYTH") || line.contains("REGISTRAR OF COMPANIES"))){
                // stop reading through file since we finished reading through event type mappings
                break;
            }

            if(m.find() == false){
                // we haven't found an event type mapping so the line needs to be appended to the previous mapping
                // for example (G10)  STATEMENT OF CAPITAL ACCOMPANYING RETURN DELIVERED UNDER SECTION 708 (NOTICE OF CANCELLATION OF SHARES ON
                // PURCHASE OF OWN SHARES) OR 730 (NOTICE OF CANCELLATION OF SHARES HELD AS TREASURY SHARES).
                // needs to be updated to 1 line : STATEMENT OF CAPITAL ACCOMPANYING RETURN DELIVERED UNDER SECTION 708 
                // (NOTICE OF CANCELLATION OF SHARES ON PURCHASE OF OWN SHARES) OR 730 (NOTICE OF CANCELLATION OF SHARES HELD AS TREASURY SHARES)
                String line2WithNoSpaces = (result.get(result.size() - 1)).trim();
                String currentLine = line.trim();
                String combinedLines = line2WithNoSpaces + " " + currentLine;
                result.remove(result.size() - 1);
                result.add(result.size() - 1, combinedLines);
            }else{
            // Found line containing event type e.g. (B1)...
             String currentLine = line.trim();
             result.add(currentLine);
            }
        }
        
        // Hash map key : (A) value = everything after without spaces or full stop.
        for (String s:result){
            if (s.contains(" DOCUMNENTS ISSUED ")){
                break;
            }
            // check if string has (A) etc
            Matcher m = pattern.matcher(s);
            if(m.find()){
                map.put(m.group(), s.substring(m.end(), s.length()-1));
            }
        }
        // map contains all event types
        return map;
        } 	catch (IOException e) {
        System.err.println(e.getMessage());       
        }
        return null;
    }

    public List<List<String>> splitDocuments(MultipartFile file){
        List<String> documentColumnOne = new ArrayList<>();
        List<String> documentColumnTwo = new ArrayList<>();
        List<List<String>> combinedDocumentColumns = new ArrayList<>();
        BufferedReader br;
        try {
            String line;
            String record;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                // we want to only find documents, they only exist after the documents issued line
                if((line.contains("******************************************************* DOCUMENTS ISSUED ***********************************************************"))){
                    while((record = br.readLine()) != null){
                        // if we hit limited or a line without any 8 digit number then it belongs to previous size-2 element
                        // since every company requires a company number which is 8 digits
                        if(record.length() != 0){
                            documentColumnOne.add(record.substring(0, 74));
                            documentColumnTwo.add(record.substring(74, record.length()));
                        }
                    }
                    break;
                }
            }
        } catch(IOException e)  
        {  
        e.printStackTrace();  
        }
        // we now have one map with the combination of columns from documents
        combinedDocumentColumns.add(documentColumnOne);
        combinedDocumentColumns.add(documentColumnTwo);
    return combinedDocumentColumns;
    }

    public Map<String, String> readRecord(List<String> recordDocument){
            String regex = "[0-9]{8}";
            String spaceRegex = "\\s{2,}";
            Pattern pattern = Pattern.compile(regex);
            Pattern spacePattern = Pattern.compile(spaceRegex);
            int counter = 0;
            int companyNameCounter = 0;
            int counterForRecordMap = 0;
            List<String> sortedArrayList = new ArrayList<>();
            HashMap<String, String> recordMap = new HashMap<String,String>();
    
            // loop through array list from {x1,x2} to {x1+x2}  element of array list to new array list to have format of [company1,limited+limited,company2,limited+incoporated]
            while (counter < recordDocument.size()){
                String currentLine = recordDocument.get(counter).trim();
                Matcher numberMatcher = pattern.matcher(currentLine);
                if(numberMatcher.find()){
                    // found company record
                    sortedArrayList.add(currentLine);
                    counter++;
                }  else if(!numberMatcher.find()){
                        // we have found an entry of the record document that is not a company, this could be 'LIMITED' which needs to be appended to the previous company
                        // create list and iterate through to x1,y1 = different companies and x2,x3 = additional company name which need to be appended
                        // an example in this instance could be x1 = ALPHABETICALLY AUTISTIC COMMUNITY INTEREST, x2 = COMPANY/THE LIFE OF, x3 = REI REILLY NE CIC
                        // this way we can go from a list of {x1,x2,x3,y1} -> {x1+x2+x3,y1}
                        Map<Integer, String> companyNameList = new HashMap<Integer, String>();
                        companyNameCounter=0;
                        String combinedCompanyNames = "";
                        while(counter < recordDocument.size()){
                        String currentLineCompanyName = recordDocument.get(counter).trim();
                        Matcher numberMatcherCompanyName = pattern.matcher(currentLineCompanyName);
                                if(!numberMatcherCompanyName.find()){
                                // not a company record, we append to the list so we can get all 'LIMITED'
                                companyNameList.put(companyNameCounter, currentLineCompanyName);
                                companyNameCounter++;
                                counter++;
                                }else{
                                // found a company record so we stop looking for 'LIMITED' 
                                break;
                                }
                            }
                            // minus 1 because we use current element for first list whilst adding non company elements to list
                            counter--;
                            for (String s :companyNameList.values()){
                                combinedCompanyNames = combinedCompanyNames + " " + s;
                                }
                            // loop through list to combine values to get {x1+x2+x3}
                            sortedArrayList.add(combinedCompanyNames);
                            counter++;
                    }else{
                    counter++;
                }
            }

            // Now we have an array list that is essentially cleaned, the documents are appended to the record map which includes the company record as the key and the company name as the value
            // the value is null for company records that do not contain names that needed to be appended together in the previous loop
            // for example {x1,x1+x2} where x1 = record and x1 + x2 is the company name that has the appended x2 
            // so x1 =  ALPHABETICALLY AUTISTIC COMMUNITY    10347751         04/05/2020 and the value would be the appended extra names found in the previous loop
            // key =  ALPHABETICALLY AUTISTIC COMMUNITY    10347751         04/05/2020, value = ALPHABETICALLY AUTISTIC COMMUNITY INTEREST COMPANY/THE LIFE OF  REI REILLY NE CIC

            while(counterForRecordMap<sortedArrayList.size()){
                String currentLineInSortedArrayList = sortedArrayList.get(counterForRecordMap).trim();
                Matcher companyNumberMatcher = pattern.matcher(currentLineInSortedArrayList);
                // if we find an 8 digit number, we know this is the company number so we append this record to the record map
                if(companyNumberMatcher.find()){
                    recordMap.put(currentLineInSortedArrayList, null);
                    counterForRecordMap++;
                }else if(!companyNumberMatcher.find()){
                    // we have encountered 
                    if (sortedArrayList.get(counterForRecordMap) == "GAP"){
                        counterForRecordMap++;
                    }else{
                        // 
                        String previous = sortedArrayList.get(counterForRecordMap - 1).trim();
                        Matcher spaceM = spacePattern.matcher(previous);
                        spaceM.find();
                        // setting the value of the key to the full company name
                        // since the sorted array list is in the form {x1,x2,y1,y2}
                        // the extended company names do not exist after more than one index from the original company record
                        // we know we can extract the company name from the previous entry (x1) and the extended company name (x2)
                        // giving us x1 + x2
                        // ALPHABETICALLY AUTISTIC COMMUNITY + INTEREST COMPANY/THE LIFE OF  REI REILLY NE CIC
                        recordMap.put(previous,previous.substring(0,spaceM.start()) + " " + currentLineInSortedArrayList);
                        counterForRecordMap++;}
                }else{
                    counterForRecordMap++;
                }
            }
            return recordMap;
        }

    public ArrayList<Records> publishRecords(Map<String,String> recordList, Map<String,String> eventTypeMap){
            String spaceRegex = "\\s{2,}";
            ArrayList<Records> records = new ArrayList<Records>();
            String regex = "[0-9]{8}";
            Pattern pattern = Pattern.compile(regex);
            Pattern spacePattern = Pattern.compile(spaceRegex);
            // the date regular expression did not work when developing the project initially
            // having a regular expression that matches the date exactly means that we don't have to
            // take the last 10 indexes of the string because when a company record does not contain the date we run into the bug of uploading the last
            // characters of the string for example "2    (G10)"" for the record OVT OBERFLAECHENVEREDELUNGSTECHNIK LIMITED
            // feel free to uncomment lines testing the date matcher to observe the date matcher regular expression not working despite it being correct
            for (String s:recordList.keySet()){
                String name = "";
                String eventType = "";
                String eventDate = "";
                String companyNumber= "";
                Matcher spaceM = spacePattern.matcher(s);
                Matcher m = pattern.matcher(s);
                m.find();
                spaceM.find();
                // Pattern pattern = Pattern.compile(regex);
                // Pattern datePattern = Pattern.compile(dateRegex);
                // String dateRegex = "([0-2][0-9]||3[0-1])\\/(0[0-9]||1[0-2])\\/([0-9][0-9])?[0-9][0-9]$";
                // String practice = " ASHRAJCARE LIMITED                   08608329    (D1) 30/04/2020";
                // Matcher dateMatcher = datePattern.matcher(practice);
                // dateMatcher.find();
    
                // Find date:
                // while(dateMatcher.matches()){
                //     eventDate = dateMatcher.group(0);
                // }
                // System.out.println("EVENT DATE VIA DATE MATCHER = " + eventDate);
    
                // uncomment everything above to test date matcher ^^
                
                // check if the company record has a longer company name than the original company record
                if (recordList.get(s) == null){
                    // there is no extended company name so we use key name
                    eventDate = s.substring(s.length() - 10, s.length());
                    companyNumber = m.group();
                    // check if the record has an event type.
                        if (s.contains("(") && s.contains(")")) {
                            // find the event type between the first and last bracket
                            // we will use this when mapping the event type later
                            eventType = s.substring(s.lastIndexOf("("), s.lastIndexOf(")")+1);
                        }else{
                            // no event type
                            eventType = "";
                        }
                    String[] partsOfRecord = s.split(companyNumber);
                    name = partsOfRecord[0];
                
                    // check that event date is not the same as company number, this can occur if record ends at the company number because it doesn't contain the date or event type field
                    // such as "ABRASUMENTE LTD                      11832181"
                    // we map these incorreclty overlapped elements to N/A
                    if(eventDate.contains(companyNumber)){
                        eventDate = "N/A";
                    }
    
                    // if there is no event type found in record then we map to N/A
                    if(eventType.equals("")){
                        eventType = "N/A";
                    }                    
                }else{
                    // Longer name exists
                    name = recordList.get(s);
                    eventDate = s.substring(s.length() - 10, s.length());
                    companyNumber = m.group();
    
                    // System.out.println("Company number = " + companyNumber);
    
                    // Check if the record has an event type. 
                        if (s.contains("(") && s.contains(")")) {
                            eventType = s.substring(s.lastIndexOf("("), s.lastIndexOf(")")+1);
                        }
                    
                     // Check that event date is not the same as company number.
                     if(eventDate.contains(companyNumber)){
                        eventDate = "N/A";
                    }
    
                    // no event type
                        if(eventType.equals("")){
                            eventType = "N/A";
                        }
                }
                // if the event type is not N/A then we know it can be mapped
                if (!(eventType == "N/A")){
                    // map the event type to the corresponding event type mapping using the eventTypeMap 
                    eventType = eventTypeMap.get(eventType).trim();
                }
            // create a record object for record which contains correct name, company number etc.
            Records record = new Records(name, companyNumber, eventType, eventDate);
            records.add(record);
            }
            return records;
            }    
}

