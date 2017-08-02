
package uk.gov.dvla.osg.ukmail.resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.gov.dvla.osg.common.classes.Customer;
import uk.gov.dvla.osg.common.classes.PostageConfiguration;
import uk.gov.dvla.osg.common.classes.ProductionConfiguration;

public class CreateUkMailResources {
	private static final Logger LOGGER = LogManager.getLogger();
	private static String ukmailBatchTypes="";
	private static String mAccNo="";
	private static String fAccNo="";
	private float minimumCompliance=0;
	private String mTrayLookup="";
	private String fTrayLookup="";
	private static float weight;

	private static int minimumTrayVolume=20;
	private String itemIdLookup="";
	
	private String morristonNextItemDate;
	private static String fforestfachNextItemDate;
	private static String ukMailManifestConsignorPath;
	private static String ukMailManifestArchivePath;
	private static String soapFilePath;
	private static String soapFileArchivePath;
	private static String runNo;
	private static String runDate;
	private static String manifestTimestamp;
	private static String actualProduct;
	private String resourcePath;
	private static String[] morristonNextItemDetails;
	private static String[] fforestfachNextItemDetails;
	private static Integer morristonNextItemRef;
	private static Integer fforestfachNextItemRef;
	private static Integer nextItemId;
	private static InputFileHandler fh = new InputFileHandler();
	private static ArrayList<UkMailManifest> ukmList = new ArrayList<UkMailManifest>();
	private static ArrayList<Customer> ukMailCustomers;
	private static HashSet<String> ukMailManifestPaths;
	private static HashMap<String,Integer> ukmMap = null;
	static boolean processMailmark = false;
	static boolean processUkMail = false;
	private static PostageConfiguration postConfig = null;
	private static ProductionConfiguration prodConfig = null;
	
	public CreateUkMailResources(ArrayList<Customer> customers, 
			PostageConfiguration postConfig, 
			ProductionConfiguration prodConfig, 
			float mailMarkComplianceLevel, 
			String runNo,
			String actualProduct) {
		
		this.resourcePath=postConfig.getUkmResourcePath();
		ukmailBatchTypes=postConfig.getUkmBatchTypes();
		mAccNo=postConfig.getUkmMAcc();
		fAccNo=postConfig.getUkmFAcc();
		this.minimumCompliance=postConfig.getUkmMinimumCompliance();
		this.mTrayLookup=resourcePath + postConfig.getUkmMTrayLookupFile();
		this.fTrayLookup=resourcePath + postConfig.getUkmFTrayLookupFile();
		minimumTrayVolume=postConfig.getUkmMinimumTrayVolume();
		this.itemIdLookup=postConfig.getUkmItemIdLookupFile();
		CreateUkMailResources.prodConfig=prodConfig;
		CreateUkMailResources.postConfig=postConfig;
		CreateUkMailResources.runNo=runNo;
		this.actualProduct=actualProduct;
		
		CreateUkMailResources.soapFilePath=postConfig.getUkmSoapDestination() + "SOAP.DAT"; 
		CreateUkMailResources.soapFileArchivePath=postConfig.getUkmSoapArchive() + "SOAP_ARCH.DAT";
		ukMailManifestArchivePath=postConfig.getUkmManifestArchive();
		

		runDate = new SimpleDateFormat("ddMMyy").format(new Date());
		manifestTimestamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
		

		try {
			//Lookup the next item reference and date for each account number format of these files is:
			//DDMMYY:NEXT_REF_NUMBER (190815:154)
			morristonNextItemDetails = fh.getNextBagRef(mTrayLookup).split(":");
			fforestfachNextItemDetails = fh.getNextBagRef(fTrayLookup).split(":");
			
			morristonNextItemDate = morristonNextItemDetails[0];
			fforestfachNextItemDate = fforestfachNextItemDetails[0];
			
			morristonNextItemRef = Integer.parseInt(morristonNextItemDetails[1]);
			fforestfachNextItemRef = Integer.parseInt(fforestfachNextItemDetails[1]);
			
			//If the date at runtime is different to the lookup date then reset the item reference
			if(!morristonNextItemDate.equals(runDate.toString())){
				morristonNextItemRef = 1;
			}
			if(!fforestfachNextItemDate.equals(runDate.toString())){
				fforestfachNextItemRef = 1;
			}

			//Get the next item ID from the lookup file
			nextItemId=Integer.parseInt(fh.getNextBagRef(resourcePath + itemIdLookup));
			if(nextItemId == 100000000){
				nextItemId = 1;
			}						
			
			//Check to see if this application requires UKMAIL resources
			processMailmark = false;
			processUkMail = false;
			
			if("OCR".equalsIgnoreCase(actualProduct)){
				processUkMail = true;
			}
			if("MM".equalsIgnoreCase(actualProduct)){
				processUkMail = true;
				processMailmark = true;
			}
			
			
			LOGGER.info("processUkMail={} processMailmark={}",processUkMail, processMailmark);
			if((!processMailmark) && (!processUkMail)){
				LOGGER.info("No UKMAIL customers to process");
			}else{
				//Create a sub-set of customers that are to be sent via UKMAIL
				ukMailCustomers = getUkMailCustomers(customers);
				
				//Cleanup from any previously run attempts
				cleanup();
				
				//Main methods
				createUkMailManifest(ukMailCustomers);
				createKickfile(ukmList, ukMailCustomers);
				
				if(processMailmark){
					createSOAPfile(ukmList, ukMailCustomers); //Also creates barcode lookup file
					
					//Update item numbers
					fh.writeReplace(resourcePath + itemIdLookup, "" + (nextItemId ++));
								
					//Update bag reference
					
					//Could be a different path for a different account number
					fh.writeReplace(mTrayLookup, runDate.toString() + ":" + morristonNextItemRef);
					fh.writeReplace(fTrayLookup, runDate.toString() + ":" + fforestfachNextItemRef);
					
				}
			}
			
		} catch (NumberFormatException e1) {
			LOGGER.fatal("ERROR:'{}'",e1.getMessage());
			e1.printStackTrace();
			System.exit(3);
		} catch (IOException e2) {
			LOGGER.fatal("ERROR:'{}'",e2.getMessage());
			e2.printStackTrace();
			System.exit(3);
		}
		
		LOGGER.info("SOAP file, '{}' exists {}",soapFileArchivePath ,fh.checkFileExists(soapFileArchivePath));
		
		if(ukMailManifestPaths != null){
			for(String s : ukMailManifestPaths){
				LOGGER.info("MANIFEST, '{}' exists {}", s , fh.checkFileExists(s));
			}
		}
	}
	
	private static void cleanup() {
		fh.deleteFile(soapFilePath);
		fh.deleteFile(soapFileArchivePath);
	}

	private static void createSOAPfile(ArrayList<UkMailManifest> ukmm,ArrayList<Customer> customers) {
		ArrayList<SoapFileEntry> sf = new ArrayList<SoapFileEntry>();
		ArrayList<BarcodeLookup> bcl = new ArrayList<BarcodeLookup>();
		Integer pid = 0;
		String jid ="";
		Integer j = 0;
		String itemId;
		for(Customer customer : customers){
			//if(mailMarkOtts.contains(customer.getOtt())){
				pid = customer.getSequence();
				// MP
				//jid = customer.getJid();
				jid = customer.getTenDigitJidStr();
				for(Integer i = 0; i < ukmm.size(); i++){
					if(i == ukmm.size()-1){
						j=i;
					}else{
						j=i+1;
					}
					if(("X".equalsIgnoreCase(customer.getEog())) &&
						(jid.equals(ukmm.get(i).getJid())) && 
						(((pid >= ukmm.get(i).getFirstPieceId()) && (pid < ukmm.get(j).getFirstPieceId())) || 
						((pid >= ukmm.get(i).getFirstPieceId()) && (ukmm.get(j).getFirstPieceId().equals(1))) || 
						((pid >= ukmm.get(i).getFirstPieceId()) && (i == ukmm.size()-1)))){
						SoapFileEntry sfe = new SoapFileEntry();
						BarcodeLookup bc = new BarcodeLookup();
						
						if("MULTI".equalsIgnoreCase(customer.getBatchType())){
							//Need to go back up the customer list to find the previous eog
							Integer startIdx = getCustomerIndexFromPidFaster(customers,jid,pid);
							for(int z = (startIdx-1); z >= 0; z--){
								if(("X".equalsIgnoreCase(customers.get(z).getEog())) || (z == 0)  ){
									if(!(z==0)){
										z=z+1;
									}
									itemId=getItemId();
									
									// MP
									//bc.setJid(customers.get(z).getJid);
									bc.setJid(customers.get(z).getTenDigitJidStr());
									bc.setPid(customers.get(z).getSequence());
									bc.setItemNo(itemId);
									//Setting MM barcode content
									customers.get(z).setMmBarcodeContent(getMmBarcodeContent(itemId,customers.get(z)));
									
									
									sfe.setRunNo(runNo);
									// MP
									//sfe.setJid(customers.get(z).getJid());
									sfe.setJid(customers.get(z).getTenDigitJidStr());
									sfe.setPid(customers.get(z).getSequence());
									sfe.setClasz(postConfig.getMmClass());
									sfe.setDps(customers.get(z).getDps());
									sfe.setItemId(itemId);
									sfe.setFormat(postConfig.getMmXmlFormat());
									sfe.setMachineable(postConfig.getMmMachineable());
									sfe.setMailType(postConfig.getMmMailType());
									sfe.setNoOfAddressLines(getNumberOfAddressLines(customers.get(z)));
									sfe.setPostcode(getPostCode(customers.get(z)));
									sfe.setProduct(postConfig.getMmXmlProduct());
									sfe.setWeight((int) customers.get(z).getWeight());
									sfe.setSpare8(ukmm.get(i).getAltRef());
									sfe.setAppName(postConfig.getMmAppname());
									// MP
									//sfe.setBatchRef(ukmm.get(i).getMailingId() + "_" + customers.get(z).getJid().substring(0,7) + "000_" + manifestTimestamp);
									sfe.setBatchRef(ukmm.get(i).getMailingId() + "_" + customers.get(z).getTenDigitJidStr().substring(0,7) + "000_" + manifestTimestamp);
									sfe.setScid(postConfig.getMmScid());
									
									sf.add(sfe);
									bcl.add(bc);
									break;
								}
							}
						}else{
							itemId=getItemId();
							
							bc.setJid(jid);
							bc.setPid(pid);
							bc.setItemNo(itemId);
							//Setting MM barcode content
							customer.setMmBarcodeContent(getMmBarcodeContent(itemId,customer));
							
							
							sfe.setRunNo(runNo);
							sfe.setJid(jid);
							sfe.setPid(pid);
							sfe.setClasz(postConfig.getMmClass());
							sfe.setDps(customer.getDps());
							sfe.setItemId(itemId);
							sfe.setFormat(postConfig.getMmXmlFormat());
							sfe.setMachineable(postConfig.getMmMachineable());
							sfe.setMailType(postConfig.getMmMailType());
							sfe.setNoOfAddressLines(getNumberOfAddressLines(customer));
							sfe.setPostcode(getPostCode(customer));
							sfe.setProduct(postConfig.getMmXmlProduct());
							sfe.setWeight((int) customer.getWeight());
							sfe.setSpare8(ukmm.get(i).getAltRef());
							sfe.setAppName(postConfig.getMmAppname());
							// MP
							//sfe.setBatchRef(ukmm.get(i).getMailingId() + "_" + customer.getJid().substring(0,7) + "000_" + manifestTimestamp);
							sfe.setBatchRef(ukmm.get(i).getMailingId() + "_" + customer.getTenDigitJidStr().substring(0,7) + "000_" + manifestTimestamp);
							sfe.setScid(postConfig.getMmScid());
							
							sf.add(sfe);
							bcl.add(bc);
							break;
						}
					}
				}
			//}
		}
		
		PrintWriter pw1 = fh.createOutputFileWriter(soapFilePath);
		PrintWriter pw2 = fh.createOutputFileWriter(soapFileArchivePath);
		
		for(SoapFileEntry sfee : sf){
			fh.appendToFile(pw1, sfee.print());
			fh.appendToFile(pw2, sfee.print());
		}
		fh.closeFile(pw1);
		fh.closeFile(pw2);
		
	}

	private static String getMmBarcodeContent(String itemId, Customer cus) {
		String customerContent = "";
		if( cus.getMmCustomerContent() == null || cus.getMmCustomerContent().trim().isEmpty() ){
//			customerContent = String.format("%-5.5s", runNo) + cus.getJid() + cus.getSequence();
			customerContent = String.format("%-5.5s", runNo) + cus.getTenDigitJidStr() + cus.getSequence();
		} else {
			customerContent = cus.getMmCustomerContent();
		}
		
		String str = String.format("%-4.4s%-1.1s%-1.1s%-1.1s%-7.7s%-8.8s%-9.9s%-1.1s%-7.7s%-6.6s%-25.25s",
				postConfig.getMmUpuCountryId(),
				postConfig.getMmInfoType(),
				postConfig.getMmVersionId(),
				postConfig.getMmClass(),
				postConfig.getMmScid(),
				itemId,
				cus.getPostcode().replace(" ", "") + cus.getDps(),
				postConfig.getMmReturnMailFlag(),
				postConfig.getMmReturnMailPc(),
				postConfig.getMmReserved(),
				customerContent);
		return str;
	}

	private static String getPostCode(Customer customer) {
		return String.format("%-7s", customer.getPostcode()).replace(" ", "").replace(" ", "0");
	}

	private static Integer getNumberOfAddressLines(Customer customer) {
		Integer count=0;
		if(!customer.getName1().equals("")){
			count++;
			if(customer.getName1().contains("*")){
				count++;
			}
		}
		if(!customer.getAdd1().equals("")){
			count++;
		}
		if(!customer.getAdd2().equals("")){
			count++;
		}
		if(!customer.getAdd3().equals("")){
			count++;
		}
		if(!customer.getAdd4().equals("")){
			count++;
		}
		if(!customer.getAdd5().equals("")){
			count++;
		}
		if(!customer.getPostcode().equals("")){
			count++;
		}
		return count;
		
	}

	private static String getItemId() {
		if(nextItemId == 100000000){
			nextItemId = 1;
		}else{
			nextItemId++;
		}
		return String.format("%08d", nextItemId);
	}

	private static void createKickfile(ArrayList<UkMailManifest> ukmm, ArrayList<Customer> customers){
		LOGGER.info("Running createKickfile, list size={}, customer size={}",ukmm.size(),customers.size());
		Integer customerIndex = 0;
		String prevJid="";

		for(int i = 0; i < ukmm.size(); i++){
			if(((ukmm.get(i).getJid().equals(prevJid)) || (ukmm.size() == 1)) && !(ukmm.get(i).getFirstPieceId()==1)){		
				customerIndex = getCustomerIndexFromPidFaster(customers, ukmm.get(i).getJid(), ukmm.get(i).getFirstPieceId());
				LOGGER.debug("customer idx={} jid={} pid={}",customerIndex,ukmm.get(i).getJid(),ukmm.get(i).getFirstPieceId());
				for(int j = customerIndex; j >= 0 ; j++){
					if(customers.get(j).getEog().equals("X")){
						LOGGER.debug("Customer with doc ref '{}' and idx '{}' has EOG of '{}'", customers.get(j).getDocRef(), j, customers.get(j).getEog());
						customers.get(j).setSot("X");
						break;
					}
				}
			}
			prevJid=ukmm.get(i).getJid();
		}
	}

	/**
	 * @param ukMailCustomer
	 */
	private void createUkMailManifest(ArrayList<Customer> ukMailCustomer) {
		Integer itemCount = 0;
		int j = 0;
		Integer startPID = 1;
		Integer endPID = 1;
		Customer nextCustomer = null;
		float currentTraySize = 0;
		float currentTrayWeight =0;
		
		for(Integer i = 0; i < ukMailCustomer.size();i++){
			j = i + 1;
			Customer customer = ukMailCustomer.get(i);
			currentTraySize = currentTraySize + customer.getSize();
			incrementTrayWeight(customer.getWeight());
			//LOGGER.debug("Tray weight is now {}",getTrayWeight());
			if(i.equals(ukMailCustomer.size()-1)){
				//If last customer then create manifest object
				itemCount ++;
				if("MULTI".equalsIgnoreCase(customer.getBatchType())){
					int l=i-1;
					for(int k=l; k >= 0; k--){
						Customer mulitCustomer = ukMailCustomer.get(k);
						if("X".equalsIgnoreCase(mulitCustomer.getEog())){
							endPID = ukMailCustomer.get(k + 1).getSequence();
							LOGGER.debug("Setting Manifest END PID to:{}",ukMailCustomer.get(k + 1).getSequence());
							break;
						}
					}
				}else{
					endPID = customer.getSequence();
				}
				ukmList.add(getItemManifest(customer,itemCount,startPID, endPID));
				if(itemCount < minimumTrayVolume){
					LOGGER.debug("Too few items in tray {}, minimum set to {}",itemCount,minimumTrayVolume);
					adjustTrayVolume(ukMailCustomer, ukmList);
				}
			}else{

				nextCustomer = ukMailCustomer.get(j);
				if(ukMailCustomer.get(i).getEog().equals("X")){
					itemCount ++;
					//MP
					//if(!(customer.getJid().equals(nextCustomer.getJid())) || 
					if(!(customer.getTenDigitJidStr().equals(nextCustomer.getTenDigitJidStr())) || 
						!(customer.getMsc().equals(nextCustomer.getMsc())) ||
						((currentTraySize + nextCustomer.getSize()) > prodConfig.getTraySize()  ) ){

						if("MULTI".equalsIgnoreCase(customer.getBatchType())){
							int l=i-1;
							for(int k=l; k >= 0; k--){
								Customer mulitCustomer = ukMailCustomer.get(k);
								if("X".equalsIgnoreCase(mulitCustomer.getEog())){
									endPID = ukMailCustomer.get(k + 1).getSequence();
									//LOGGER.debug("Setting Manifest END PID to:{}",ukMailCustomer.get(k + 1).getSequence());
									break;
								}
							}
						}else{
							endPID = customer.getSequence();
						}
						
						ukmList.add(getItemManifest(customer,itemCount,startPID, endPID));
						if(itemCount < minimumTrayVolume){
							LOGGER.debug("Too few items in tray {}, minimum set to {}",itemCount,minimumTrayVolume);
							adjustTrayVolume(ukMailCustomer, ukmList);
						}
						startPID = nextCustomer.getSequence();
						itemCount=0;
						currentTraySize=0;
						resetWeight();
					}
				}
			}
		}
		
		ukMailManifestPaths = new HashSet<String>();
		for(UkMailManifest ukmm : ukmList){
			
			String output = ukmm.print(processMailmark);
			//System.out.println(output); 
			fh.write(ukMailManifestArchivePath + ukmm.getManifestFilename(), output);
			fh.write(ukMailManifestConsignorPath + ukmm.getManifestFilename(), output);
			//fh.write(spoolDir + jobId + "." + ukmm.getManifestFilename() + "A", output);
			ukMailManifestPaths.add(ukMailManifestArchivePath + ukmm.getManifestFilename());
		}
		
	}

	

	/**
	 * @param customer
	 * @param ukmList
	 */
	private static void adjustTrayVolume(ArrayList<Customer> customers, ArrayList<UkMailManifest> ukmList) {
		UkMailManifest lastEntry = null, penultimateEntry = null;
		
		try{
			lastEntry = ukmList.get(ukmList.size()-1);
			penultimateEntry = ukmList.get(ukmList.size()-2);
		}catch(IndexOutOfBoundsException e){
			LOGGER.fatal("Error when creating manifest, check configuration for tray minimum");
			System.exit(1);
		}
		Integer numberOfItemsToMove = penultimateEntry.getTrayVol() / 2;
		Integer[] itemWeight = {penultimateEntry.getTrayWeight() / penultimateEntry.getTrayVol(),
				lastEntry.getTrayWeight() / lastEntry.getTrayVol()};
		Integer itemsFound = 0;
		Integer customerIndex = getCustomerIndexFromPidFaster(customers, penultimateEntry.getJid(), penultimateEntry.getLastPieceId());
		
		for(Integer count = customerIndex ; count > 0; count --){
			if(customers.get(count).getEog().equals("X")){
				itemsFound ++;
			}

			if(itemsFound == numberOfItemsToMove+1){
				penultimateEntry.setTrayVol(penultimateEntry.getTrayVol() - numberOfItemsToMove);
				penultimateEntry.setLastPieceId(customers.get(count).getSequence());
				penultimateEntry.setTrayWeight(penultimateEntry.getTrayVol() * itemWeight[0]);
				
				lastEntry.setFirstPieceId(customers.get(count + 1).getSequence());
				break;
			}
		}

		lastEntry.setTrayVol(lastEntry.getTrayVol() + numberOfItemsToMove);
		
		lastEntry.setTrayWeight(lastEntry.getTrayVol() * itemWeight[1]);

	}
	
	private static Integer getCustomerIndexFromPidFaster(ArrayList<Customer> customers, String jid, Integer pid){
		int result = 0;
		if(ukmMap==null){
			ukmMap = new HashMap<String, Integer>();
			for(Integer i = 0; i < customers.size();i++){
				// MP
				//ukmMap.put(customers.get(i).getJid() + "~" + customers.get(i).getSequence() , i);
				ukmMap.put(customers.get(i).getTenDigitJidStr() + "~" + customers.get(i).getSequence() , i);
			}
		}
		String key = jid + "~"+ pid;
		result = ukmMap.get(key);
		LOGGER.debug("getCustomerIndexFromPidFaster(ArrayOf {} customers,{},{}) returned '{}'", customers.size(), jid, pid, result);
		return result;
	}

	/**
	 * @param customer
	 * @param itemCount
	 * @param startPID
	 * @return
	 */
	private static UkMailManifest getItemManifest(Customer customer, Integer itemCount, Integer startPID, Integer endPID){
		UkMailManifest ukm = new UkMailManifest();
		ukm.setAccountNo(getAccountNo());
		ukm.setAppName(getAppName(customer));
		ukm.setRunNo(runNo);
		ukm.setFirstPieceId(startPID);
		
		ukm.setLastPieceId(endPID);
		// MP
		ukm.setJid(Integer.toString(customer.getTenDigitJid()));
		//ukm.setJid(customer.getJid());
		ukm.setRunDate(runDate);
		if(processMailmark){
			ukm.setItemId(getTrayId());
		}
		ukm.setCollectionDate("");
		ukm.setFormat(getFormat());
		ukm.setMachinable("Y");
		ukm.setMsc(customer.getMsc());
		ukm.setServiceCode(getProductCode());
		ukm.setTrayVol(itemCount);
		ukm.setTrayWeight((int)getTrayWeight());
		ukm.setManifestFilename(getManifestFilename(customer));
		
		return ukm;
	}
		
	private static String getManifestFilename(Customer customer) {
		String productionArea = postConfig.getUkmConsignorDestinationDepartment();
		return prodConfig.getMailingSite().toUpperCase() + "." + productionArea + "." + getAppName(customer) + "." + runNo + "." + manifestTimestamp + ".DAT";
	}

	private static String getAppName(Customer customer) {
		return customer.getSelectorRef();
	}

	/**
	 * @param customer
	 * @param itemCount
	 * @return
	 */
	private static float getTrayWeight() {
		return weight;
	}
	private void incrementTrayWeight(float weight){
		this.weight = this.weight + weight;
	}
	private void resetWeight(){
		this.weight=0;
	}

	/**
	 * @param customer
	 * @return
	 */
	private static String getProductCode() {
		if("MM".equalsIgnoreCase(actualProduct)){
			return postConfig.getMmProduct();
		} else{
			return postConfig.getOcrProduct();
		}
	}

	/**
	 * @param customer
	 * @return
	 */
	private static String getFormat(){
		if("MM".equalsIgnoreCase(actualProduct)){
			return postConfig.getMmFormat();
		} else {
			return postConfig.getOcrFormat();
		}
		
	}
	/**
	 * @param customer
	 * @return
	 */

	private static Integer getTrayId(){
		String accountNo = getAccountNo();
		Integer itemId;
		
		if(accountNo.equals(mAccNo)){
			itemId = morristonNextItemRef;
			morristonNextItemRef ++;
		}else{
			itemId = fforestfachNextItemRef;
			fforestfachNextItemRef ++;
		}
		return itemId;
	}

	/**
	 * @param customer
	 * @return
	 */
	private static String getAccountNo(){
		
		if( "M".equalsIgnoreCase(prodConfig.getMailingSite()) ){
			return mAccNo;
		}else{
			return fAccNo;
		}

	}


	public static ArrayList<Customer> getUkMailCustomers(ArrayList<Customer> allCustomers){
		ArrayList<Customer> ukMailCustomers = new ArrayList<Customer>();
		for(Customer customer : allCustomers){
			if(ukmailBatchTypes.contains(customer.getBatchType())){
				ukMailCustomers.add(customer);
			}
		}
		return ukMailCustomers;
	}
	
	public static boolean isNumeric(String str){
		try{
			double d = Double.parseDouble(str);
		}catch(NumberFormatException nfe){
			return false;
		}
		return true;
	}
	public static boolean isCustomerUkMail(Customer customer){
		
		if(ukmailBatchTypes.contains(customer.getBatchType())){
			return true;
		}else{
			return false;
		}
	}

}
