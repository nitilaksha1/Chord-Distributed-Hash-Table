import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.*;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChordClient {

	private static int getHashCode(String url){
        int hash = url.hashCode() % (int)Math.pow(2, 32);

        if (hash < 0) {
            hash = hash >>> 1;
        }
        
        return hash;

    }

    private static String lookupWord (String hostname, int portname, String word) {

        String meaning = "";

        try {
            TTransport transport;
            transport = new TSocket(hostname, portname);
            transport.open();
            TProtocol protocol = new  TBinaryProtocol(transport);
            ChordNodeService.Client client = new ChordNodeService.Client(protocol);
            
            String url = client.find_node(getHashCode(word), false);
            String[] arr = url.split(":");
            String temphostname = arr[0];
            int tempportname = Integer.parseInt(arr[1]);

            transport.close();

            transport = new TSocket(temphostname, tempportname);
            transport.open();

            protocol = new  TBinaryProtocol(transport);
            client = new ChordNodeService.Client(protocol);

            meaning = client.lookup(word.trim(), true);

        } 
        catch (TTransportException e) {e.printStackTrace();}
        catch (TException e) {e.printStackTrace();}

        return meaning;
    }
	
    public static void main(String[] args) {
        String chordnodeurl = args[0];
        String[] arr = chordnodeurl.split(":");
        String hostname = arr[0];
        int portname = Integer.parseInt(arr[1]);
        Scanner scan =  new Scanner(System.in);

        System.out.println("Enter 1 to Lookup, 2 to Print DHT Information, 3 to exit");

        while (true) {

            System.out.println("Enter your choice: ");
            int choice = scan.nextInt();

            if (choice == 1) {
                System.out.println("Enter a word: ");
                System.out.println("Result: " + lookupWord(hostname, portname, scan.next()));
            } else if (choice == 3) {
                System.out.println("Exiting..");
                break;
            } else if (choice == 2) {
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
				catch (TException e) {e.printStackTrace();}
			}
        }
    }
}
