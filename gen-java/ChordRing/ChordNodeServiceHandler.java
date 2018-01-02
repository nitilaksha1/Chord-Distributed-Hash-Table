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
import java.util.concurrent.locks.ReentrantLock;
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
    public volatile TreeMap<Integer,ChordNode> nodeMap;
    private ReentrantLock lock = new ReentrantLock();
    public static PrintWriter writer;
    private String nodename;
    
    public ChordNodeServiceHandler(String hostname, int portnumber, String nodename){
        map = new HashMap<String,String>();
        this.hostname = hostname;
        this.portnumber = portnumber;
        isMaster = false;
        nodekey = getHashCode(hostname + ":" + portnumber);
        nodeMap = new TreeMap<Integer,ChordNode>();
        successor = new ChordNode();
        fingerTable = new ArrayList<FingerTableInfo>();
        this.nodename = nodename;
        
        try {
        	writer = new PrintWriter(nodename+".txt","UTF-8");
        } catch (UnsupportedEncodingException e){}
        catch (FileNotFoundException e) {}
        
        
    }

	public void printDHT () {
		
		System.out.println();
		System.out.println("The DHT Information for " + this.nodename);
		System.out.println("Hash key:  " + this.nodekey);
		System.out.println("Successor:  " + this.successor);
		System.out.println("Predecessor: " + this.predecessor);
		System.out.println("Word Count: " + map.size());
		printFingerTable();
		
		System.out.println();

		if (isMaster) {
			multicastprintDHT();
		}
				
	}

	public void multicastprintDHT () {

		for(Map.Entry<Integer,ChordNode> entry : nodeMap.entrySet()){
            	ChordNode val = entry.getValue();
    			if(val.getPortnumber() != this.portnumber && this.hostname.equals(val.getHostname()))
    				call_print_DHT(val.getHostname(), val.getPortnumber());
    	}
	}

	private void call_print_DHT (String hostname, int portname) {

		try {
				TTransport transport;
				transport = new TSocket(hostname, portname);
				transport.open();
				TProtocol protocol = new  TBinaryProtocol(transport);
				ChordNodeService.Client client = new ChordNodeService.Client(protocol);
				client.printDHT();
				transport.close();
			} 
            catch (TTransportException e) {e.printStackTrace();}
			catch (TException e){e.printStackTrace();}   
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
    	
    	if (temp < 0)
    		temp = (temp >>> 1) % (int)Math.pow(2, m);
        else
    		temp = temp % (int)Math.pow(2, m);
    	
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
    	writer.println("Inside get_successor");
    	writer.flush();
        return successor;
    }

    public ChordNode get_predecessor() {
    	writer.println("Inside get_predecessor");
    	writer.flush();
        return predecessor;
    }

    public void set_predecessor(ChordNode node) {
    	writer.println("Inside set_predecessor");
    	writer.flush();
        this.predecessor = node;
    }
    
    public void insert(String word, String meaning, boolean traceFlag){
    	writer.println("Inside insert");
    	writer.flush();
        map.put(word,meaning);
    }

    public String lookup(String word, boolean traceFlag){
    	writer.println("Inside lookup");
    	writer.flush();
        if(map.containsKey(word)){
            return map.get(word);
        }

        return "Not_Found";
    }

    public void printFingerTable(){
    	
    	System.out.println("--------------------------------------------------------");
    	
    	System.out.println("Printing finger table:");
    	System.out.println("Finger : <finger_number> : 	<start> : <node>");
    	for (int i = 1; i <= m; i++) {
    		System.out.println("Finger : "+ i+ ": "+fingerTable.get(i).start+ ": "+ fingerTable.get(i).node.getKey());
    		System.out.flush();
        }
    	
    	System.out.println("--------------------------------------------------------");
    }
    
    public String find_node(int key, boolean traceFlag){
    	writer.println("Inside find_node");
    	writer.flush();
    	ChordNode n = find_successorDHT(key);
    	
    	return n.getHostname() + ":" + n.getPortnumber();
    }
    
    private boolean checkNodeinInterval (int key, ChordNode n, ChordNode successor) {
    	
    	boolean res = false;
    	
    	if (key >= n.getKey() && key <= successor.getKey()) {
    		res = true;
    	} else if (key <= n.getKey() && key == successor.getKey()) {
    		res = true;
    	} else if (this.predecessor.getKey() == successor.getKey()) {
    		res = true;
    	}
    	
    	return !res;
    }

    public ChordNode find_predecessor (int key) {
    	
    	ChordNode n = new ChordNode(); 
        n.setKey(nodekey);
        n.setHostname(hostname);
        n.setPortnumber(portnumber);
        	 
    	ChordNode successorkeynode = new ChordNode();
    	successorkeynode.setKey(successor.getKey());
        int successorkey = successor.getKey();
        
        ChordNode temp = n;
        boolean isfirst = true;
        String temphostname = hostname;
        int tempportnumber = portnumber;
        
        //TODO: In the first iteration socket will be created with respect to itself. Will this be allowed?
        while (checkNodeinInterval(key, n, successorkeynode)) {
        	
        	temp = closest_preceding_finger(key);
            temphostname = temp.getHostname();
            tempportnumber = temp.getPortnumber();
            successorkey = successorkeynode.getKey();
            n = temp;
            
            if (temp.getKey() == successorkey)
            	break;
        }
        
        return temp;
    }
    
    public ChordNode closest_preceding_finger (int key) {
        for (int i = m; i >= 1; i--) {
            if (fingerTable.get(i).node.getKey() >= nodekey && fingerTable.get(i).node.getKey() <= key) {
                return fingerTable.get(i).node;
            }
        }
        ChordNode n = new ChordNode();
        n.setKey(this.nodekey);
        n.setHostname(hostname);
        n.setPortnumber(portnumber);

       return n;

       
    }
    
    public void joinDHT (boolean isMaster) {

        //Case: If the current node is not Node-0
        if (!isMaster) {

            try {
                NodeInfo nodeinfo = new NodeInfo();

                TTransport transport;
                transport = new TSocket(masterhostname, masterportname);
                transport.open();
                TProtocol protocol = new  TBinaryProtocol(transport);
                ChordNodeService.Client client = new ChordNodeService.Client(protocol);
                nodeinfo = client.join(hostname + ":" + portnumber);
                this.nodekey = nodeinfo.getKey();
                this.nodeMap = new TreeMap<Integer,ChordNode>(nodeinfo.getNodeMap());
                constructInitTable();
                transport.close();
            } 
            catch (TTransportException e) {e.printStackTrace();}
            catch (TException e){e.printStackTrace();}    
        } 
        else {//Case: The current node is Node-0

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

            TProtocol protocol = new  TBinaryProtocol(transport);
            ChordNodeService.Client client = new ChordNodeService.Client(protocol);
            client.insertNewNode(n);
            transport.close();
        }
        catch (TTransportException e) {e.printStackTrace();}
        catch (TException e){e.printStackTrace();}   
    }
    
    public void insertNewNode (ChordNode node) {
    	nodeMap.put(node.getKey(), node);
    	constructInitTable();
    }
    
    public NodeInfo join (String url) {
		NodeInfo info = new NodeInfo();

    	lock.lock();

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
    			if(val.getPortnumber() != this.portnumber && this.hostname.equals(val.getHostname())){
    				
    				call_insert_newnode(val.getHostname(), val.getPortnumber(), n);
    			}
    			
    		}

            nodeMap.put(getHashCode(url), n);
            constructInitTable();
            info.setNodeMap(nodeMap);
            
        lock.unlock();
        return info;
    }
    
    
    public void join_done(){
    	lock.unlock();
    }
    
    public void call_join_done(){
    	try{
    		TTransport transport;
	        transport = new TSocket(masterhostname, masterportname);
	        transport.open();
	
	        TProtocol protocol = new  TBinaryProtocol(transport);
	        ChordNodeService.Client client = new ChordNodeService.Client(protocol);
	        
	        client.join_done();
            transport.close();
	    } catch (TTransportException e) {e.printStackTrace();}
	    catch (TException e){e.printStackTrace();}   
    }
    
    private FingerTableInfo createFingerTableInfo (int start, ChordNode n) {
    	return new FingerTableInfo(start, n);
    }
    
    public ChordNode find_successorDHT (int key) {
    	
    	ChordNode temp = null;
    	boolean successorfound = false;
    	
        writer.println("Inside find_successor for key: " + key);
        writer.flush();
    	ChordNode pred = find_predecessorDHT(key); 
    	
    	return call_get_successor (pred.getHostname(), pred.getPortnumber());
    }
    
    public ChordNode find_successorDHT_construct (int key) {
    	
    	ChordNode temp = null;
    	boolean successorfound = false;
    	
        writer.println("Inside find_successor for key: " + key);
        writer.flush();

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
    	writer.println("Inside find_predecessor for key: " + key);
        writer.flush();

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
			ChordNode n = find_successorDHT_construct(start);
			fingerTable.add(createFingerTableInfo(start, n));
		}
		
    }
    
    private ChordNode call_get_successor(String hostname, int portnumber) {
        ChordNode n = new ChordNode();

        if((hostname.equals(this.hostname)) && (portnumber == this.portnumber)){
        	n = this.get_successor();
        	return n;
        }
        try {
            TTransport transport;
            transport = new TSocket(hostname, portnumber);
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            ChordNodeService.Client client = new ChordNodeService.Client(protocol);
            n = client.get_successor();

            transport.close();
        } catch (TTransportException e) {e.printStackTrace();}
        catch (TException e){e.printStackTrace();}   

        return n;
    }
	
}
