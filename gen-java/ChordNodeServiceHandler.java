import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.*;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.TException;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Node;

import java.util.*;
import java.io.*;
import java.math.BigInteger;

public class ChordNodeServiceHandler implements ChordNodeService.Iface{
	
    private Map<String,String> map;
    private List<FingerTableInfo> fingerTable;
    private String hostname;
    private int portnumber;
    private String masterhostname;
    private int masterportname;
    public int nodekey;
    private ChordNode predecessor;
    private ChordNode successor;
    private boolean isMaster;
    public static int m = 32;
    public TreeMap<Integer,ChordNode> nodeMap;
    
    public ChordNodeServiceHandler(String hostname, int portnumber){
        map = new HashMap<String,String>();
        this.hostname = hostname;
        this.portnumber = portnumber;
        isMaster = false;
        nodekey = getHashCode(hostname + ":" + portnumber);
        System.out.println("INCONSTRUCTOR:  "+ nodekey);
        nodeMap = new TreeMap<Integer,ChordNode>();
        successor = new ChordNode();
        fingerTable = new ArrayList<FingerTableInfo>();
        
    }

    private static int getHashCode(String url){
        int hash = url.hashCode() % (int)Math.pow(2, m);

        if (hash < 0) {
            hash = hash >>> 1;
        }
        hash = hash % (int)Math.pow(2, m);
        return hash;

    }
    
    private int BigIntWrapAround(int nodekey, int i) {
    	
    	int temp = nodekey + (int)Math.pow(2, i - 1);
    	
    	if (temp < 0) {
    		temp = (temp >>> 1) % (int)Math.pow(2, m);
    	} else {
    		temp = temp % (int)Math.pow(2, m);
    	}
    	
    	return temp;
    	
    }
    
    public void setIsMaster (boolean flag) {
    	isMaster = flag;
    }
    
    public void setMasterURL (String url) {
        String [] urlinfo = url.split(":");
        masterhostname = urlinfo[0];
        masterportname = Integer.parseInt(urlinfo[1]);
    }
    
    public String getHostname() {
        return hostname;
    }

    public int getPortnumber() {
        return portnumber;
    }

    public ChordNode get_successor() {
        return successor;
    }

    public ChordNode get_predecessor() {
        return predecessor;
    }

    public void set_predecessor(ChordNode node) {
        this.predecessor = node;
    }
    
    public void insert(String word, String meaning){
        map.put(word,meaning);
        System.out.println("Inserted word " + word);
    }

    public String lookup(String word){
        if(map.containsKey(word)){
            return map.get(word);
        }

        return "Not_Found";
    }

    public void printFingerTable(){
    	
    	for (int i = 1; i <= m; i++) {
        	System.out.println("Iteration: "+ i+ ": "+fingerTable.get(i).start+ ": "+ fingerTable.get(i).node.getKey());
        }
    }
    
    public String find_node(int key, boolean traceFlag){
    	
    	ChordNode n = find_successorDHT(key);
    	
    	return n.getHostname() + ":" + n.getPortnumber();
    }
    
    public void joinDHT (boolean isMaster) {

        //Case: If the current node is not Node-0
        if (!isMaster) {

            try {
                NodeInfo nodeinfo;

                TTransport transport;
                transport = new TSocket(masterhostname, masterportname);
                System.out.println("Master: "+ masterhostname + " :" + masterportname);
                transport.open();
                //System.out.println("Open.transport sucess!!");

                TProtocol protocol = new  TBinaryProtocol(transport);
                ChordNodeService.Client client = new ChordNodeService.Client(protocol);
                
                nodeinfo = client.join(hostname + ":" + portnumber);
                
                this.nodekey = nodeinfo.getKey();
                //this.predecessor = nodeinfo.getPredecessor();
                //this.fingerTable = nodeinfo.getFingertable();
                //this.successor = nodeinfo.getFingertable().get(1).node ;
                System.out.println(nodeinfo.getNodeMap().keySet());
                this.nodeMap = new TreeMap<Integer,ChordNode>(nodeinfo.getNodeMap());
                
                constructInitTable();
                
                System.out.println("New Node Finger Table after Initialization: ");
                printFingerTable();
                
                transport.close();
            } catch (TTransportException e) {e.printStackTrace();}
            catch (TException e){e.printStackTrace();}    
        } else {//Case: The current node is Node-0

        	ChordNode n = new ChordNode();
            n.setKey(nodekey);
            n.setHostname(hostname);
            n.setPortnumber(portnumber);

            fingerTable.add(new FingerTableInfo());
            
            for (int i = 1; i <= m; i++) {
                fingerTable.add(createFingerTableInfo(BigIntWrapAround(nodekey, i), n));
            }

            this.predecessor = n;
            this.successor = n;
            nodeMap.put(nodekey, n);
        }
    }
    
    private void call_insert_newnode (String hostname, int portnumber, ChordNode n) {
    	try {
            TTransport transport;
            transport = new TSocket(hostname, portnumber);
            transport.open();
            //System.out.println("Open.transport sucess!!");

            TProtocol protocol = new  TBinaryProtocol(transport);
            ChordNodeService.Client client = new ChordNodeService.Client(protocol);
            client.insertNewNode(n);

            transport.close();
        } catch (TTransportException e) {e.printStackTrace();}
        catch (TException e){e.printStackTrace();}   
    }
    
    public void insertNewNode (ChordNode node) {
    	nodeMap.put(node.getKey(), node);
    	constructInitTable();
    	
    	System.out.println("Finger Table after Adding New Node: ");
        printFingerTable();
    }
    
    public NodeInfo join (String url) {
    	NodeInfo info = new NodeInfo();
    	
    	info.setKey(getHashCode(url));
    	
    	String [] urlinfo = url.split(":");
        String temphostname = urlinfo[0];
        int tempportname = Integer.parseInt(urlinfo[1]);
        
        ChordNode n = new ChordNode();
        n.setKey(getHashCode(url));
        n.setHostname(temphostname);
        n.setPortnumber(tempportname);
        
        for(Map.Entry<Integer,ChordNode> entry : nodeMap.entrySet()){
        	ChordNode val = entry.getValue();
			
			call_insert_newnode(val.getHostname(), val.getPortnumber(), n);
		}
        
        nodeMap.put(getHashCode(url), n);
        
        constructInitTable();
        
        System.out.println("Master Finger Table after Adding New Node: ");
        printFingerTable();
        
        info.setNodeMap(nodeMap);
        return info;
    }
    
    private FingerTableInfo createFingerTableInfo (int start, ChordNode n) {
    	
    	return new FingerTableInfo(start, n);
    	
    }
    
    public ChordNode find_successorDHT (int key) {
    	ChordNode temp = null;
    	boolean successorfound = false;
    	
    	for(Map.Entry<Integer,ChordNode> entry : nodeMap.entrySet()){
    		ChordNode val = entry.getValue();
			
			if(val.getKey() >= key){
				temp = val;
				successorfound = true;
				break;
			}
		}
    	
    	if (!successorfound) {
			temp = nodeMap.firstEntry().getValue();
		}
    	
    	return temp;
    }
    
    public ChordNode find_predecessorDHT (int key) {
    	
    	ChordNode temp = null;
    	boolean predecessorfound = false;
    	ArrayList<Integer> keys = new ArrayList<Integer>(nodeMap.keySet());
        
    	ChordNode first = nodeMap.get(keys.get(keys.size() -1));
		
		for(int i=keys.size()-1; i>=0;i--){
			
			if (keys.get(i) < key) {
				temp = nodeMap.get(keys.get(i));
				predecessorfound = true;
				break;
			}
        }
        
		if (!predecessorfound) {
			temp = first;
		}
		
		return temp;
    }
    
    public void constructInitTable(){
    	
    	fingerTable = new ArrayList<FingerTableInfo>();
    	
    	boolean successorfound = false;
    	boolean predecessorfound = false;
    	
		for(Map.Entry<Integer,ChordNode> entry : nodeMap.entrySet()){
			ChordNode val = entry.getValue();
			
			if(val.getKey() >= nodekey){
				successor = val;
				successorfound = true;
				break;
			}
		}
		
		if (!successorfound) {
			successor = nodeMap.firstEntry().getValue();
		}
		
		ArrayList<Integer> keys = new ArrayList<Integer>(nodeMap.keySet());
        
		ChordNode first = nodeMap.get(keys.get(keys.size() -1));
		
		for(int i=keys.size()-1; i>=0;i--){
			
			if (keys.get(i) < nodekey) {
				predecessor = nodeMap.get(keys.get(i));
				predecessorfound = true;
				break;
			}
        }
        
		if (!predecessorfound) {
			predecessor = first;
		}
		
		fingerTable.add(new FingerTableInfo());
		
		for (int i = 1; i <= m; i++) {
			int start = BigIntWrapAround(nodekey, i);
			ChordNode n = find_successorDHT(start);
			fingerTable.add(createFingerTableInfo(start, n));
		}
		
    }
	
}