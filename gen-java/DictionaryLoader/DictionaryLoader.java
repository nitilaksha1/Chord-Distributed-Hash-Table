import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.*;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import java.util.*;
import java.io.File;
import java.io.*;

public class DictionaryLoader {

    public DictionaryLoader() {

    }

    private static String connectToChordMaster (String hostname, int portname, int hash) {
        String url = "";

        try {
            TTransport transport;
            transport = new TSocket(hostname, portname);
            transport.open();
            //System.out.println("Open.transport sucess!!");

            TProtocol protocol = new  TBinaryProtocol(transport);
            ChordNodeService.Client client = new ChordNodeService.Client(protocol);
            //url = client.getURLFromHash(hash);
            url = client.find_node(hash, false);

            transport.close();
        } catch (TTransportException e) {e.printStackTrace();}
        catch (TException e){e.printStackTrace();}

        return url;

    }

    private static void connectToChordNode (String hostname, int portname, String word, String meaning) {

        try {
            TTransport transport;
            transport = new TSocket(hostname, portname);
            transport.open();
            //System.out.println("Open.transport sucess!!");

            TProtocol protocol = new  TBinaryProtocol(transport);
            ChordNodeService.Client client = new ChordNodeService.Client(protocol);

            client.insert(word, meaning, true);
            ///System.out.println("Inserted " + word + " at " + portname);

            transport.close();
        } catch (TTransportException e) {e.printStackTrace();}
        catch (TException e) {e.printStackTrace();}

    }

    public static void main (String [] args) {

        String chordnodeurl = args[0];
        String filename = args[1];
        int m = 32;

        try {
            Scanner scan = new Scanner(new File(filename));
            String [] urlinfo = chordnodeurl.split(":");
            String hostname = urlinfo[0];
            int portname = Integer.parseInt(urlinfo[1]);

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] arr = line.split(":");
                String word = arr[0];
                String meaning = arr[1];

                word = word.trim();
                
                //TODO: The power is 2^32 not 32
                int hash = word.hashCode() % (int)Math.pow(2, m);

                if (hash < 0) {
                    hash = hash >>> 1;
                }

                String url = connectToChordMaster(hostname, portname, hash);

                String [] chordnodeurlinfo = url.split(":");
                String chordnodehostname = chordnodeurlinfo[0];
                int chordnodeportname = Integer.parseInt(chordnodeurlinfo[1]);

                connectToChordNode(chordnodehostname, chordnodeportname, word, meaning);
            }

            scan.close();
        } catch (FileNotFoundException e) {}

    }
}