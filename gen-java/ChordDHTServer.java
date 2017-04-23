import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TFramedTransport;
import java.io.*;
import java.util.*;


@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
public class ChordDHTServer{
	public static ChordNodeServiceHandler handler;
	public static ChordNodeService.Processor processor;

	public ChordDHTServer(){
		
	}
	
  public static void main(String [] args) {
	
    if (args.length < 4) {
        System.out.println("Parameters needed : 1-hostname 2-portnumber 3-nodename 4-isMaster [5-MasterURL]");
      System.exit(1);
     }
    
    final int portnumber = Integer.parseInt(args[1]);
    String hostname = args[0];
    String nodename = args[2];
    boolean isMaster = (args[3].equals("true")) ? true : false;
    String masterHostName;

    if(!isMaster){
        masterHostName = args[4];
    }else{
        masterHostName = hostname;
    }
    
    try {
        handler = new ChordNodeServiceHandler(hostname, portnumber, nodename);
        processor = new ChordNodeService.Processor(handler);
        
        handler.setMasterURL(args[4]);
        handler.setIsMaster(isMaster);
        handler.joinDHT(isMaster);
        

      Runnable simple = new Runnable() {
        public void run() {
          someMethod(processor,portnumber, handler);
        }
      };      
       
        new Thread(simple).start();
    } catch (Exception x) {
        x.printStackTrace();
      }
    }

  public static void someMethod(ChordNodeService.Processor processor, int portnumber, ChordNodeServiceHandler handler) {
	    try {
	      TTransportFactory factory = new TFramedTransport.Factory();
	      TServerTransport serverTransport = new TServerSocket(portnumber);
	      TServer server = new TThreadPoolServer(new Args(serverTransport).processor(processor));
	      System.out.println("Starting the simple server...");
	      server.serve();

	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }
	

}