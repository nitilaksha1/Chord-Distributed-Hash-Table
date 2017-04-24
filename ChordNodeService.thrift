struct FingerTableInfo{
	1: i32 start;
	2: ChordNode node;
}

struct NodeInfo{
	1: i32 key;
	2: ChordNode successor;
	3: ChordNode predecessor;
	4: list<FingerTableInfo> fingertable;
	5: map<i32,ChordNode> nodeMap;
}

struct ChordNode{
    1: i32 key;
    2: string hostname;
    3: i32 portnumber;
}

service ChordNodeService {

   string   find_node                 (1:i32 key, 2:bool traceFlag),
   void     insert                    (1:string word, 2:string meaning, 3:bool traceFlag),
   string   lookup                    (1:string word, 2:bool traceFlag),
   void     printFingerTable          (),
   ChordNode get_successor			  (),
   NodeInfo join                      (1:string url),
   oneway void join_done(),


   void insertNewNode(1:ChordNode n)       
}

