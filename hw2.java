import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Yuva Shankar
 */
public class hw2 {
	Connection conn;
	Statement stmt;
	/**
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public hw2() throws ClassNotFoundException, SQLException{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		String url = "jdbc:oracle:thin:@localhost:1521:orcl";
		conn =  DriverManager.getConnection(url,"system","oracle");
		stmt = conn.createStatement();
		System.out.println("INFO: JDBC Connection Established");
	}
	/**
	 * @param args
	 * @throws SQLException
	 */
	public void window(String[] args) throws SQLException{
		System.out.println("INFO: Executing window query");
		//Fetch the table, lower-left and upper-right vertices
		String tableName = args[1];
		String lowerLeft = args[2]+','+args[3];
		String upperRight = args[4]+','+args[5];
		int count = 0;
		String query = new String();
		
		if(tableName.equalsIgnoreCase("students")){
			query = "SELECT Sid FROM Students WHERE SDO_INSIDE(Sloc, MDSYS.SDO_GEOMETRY(2003, NULL, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), MDSYS.SDO_ORDINATE_ARRAY("+lowerLeft+","+upperRight+"))) = 'TRUE'";
		}else if(tableName.equalsIgnoreCase("building")){
			query = "SELECT Bid FROM Building WHERE SDO_INSIDE(BGeo, MDSYS.SDO_GEOMETRY(2003, NULL, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), MDSYS.SDO_ORDINATE_ARRAY("+lowerLeft+","+upperRight+"))) = 'TRUE'";
		}else if(tableName.equalsIgnoreCase("tramstops")){
			query = "SELECT TSpid FROM TramStops WHERE SDO_INSIDE(TSloc, MDSYS.SDO_GEOMETRY(2003, NULL, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), MDSYS.SDO_ORDINATE_ARRAY("+lowerLeft+","+upperRight+"))) = 'TRUE'";			
		}else{
			System.out.println("ERROR: Invalid table name");
		}
		
		System.out.println("INFO: Executing "+query);
	    ResultSet rset = stmt.executeQuery(query);
	    if(rset.next()){
	    	System.out.println ("SID/BID/TSpID");
	    	System.out.println (rset.getString(1));
	    	count = 1;
		    while (rset.next()) {
		    	count++;
		        System.out.println (rset.getString(1));
		    }
		    System.out.println("INFO: Total # of Results " + count);
	    }else{
	    	System.out.println("No result found/zero match");
	    }
	    System.out.println("INFO: Exectuion Completed.");
	}
	/** 
	 * @param args
	 * @throws SQLException
	 * @throws IOException 
	 */
	public void nearestNeighbor(String[] args) throws SQLException, IOException{
		System.out.println("INFO: Executing nearest-neighbor query");
		//Fetch the table, id details
		String tableName = args[1];
		String id = args[2];
		int limit = Integer.parseInt(args[3])+1;
		String coordinates = new String(); 
		String query = new String();
		ResultSet rset;
		ResultSet res;
		
		if(tableName.equalsIgnoreCase("building")){
			// Get the coordinates for the given id
			query = "SELECT SDO_UTIL.TO_WKTGEOMETRY(BGeo) AS BGEo FROM Building WHERE Bid = '"+id+"'";
			System.out.println("INFO: Executing "+query);
			rset = stmt.executeQuery(query);
			while(rset.next()){
		        Reader reader = rset.getCharacterStream("BGeo");
		        BufferedReader br = new BufferedReader(reader);
		        String line;
		        StringBuilder sb = new StringBuilder();
		        while(null != (line = br.readLine())) {
		            sb.append(line);
		        }
		        coordinates = sb.toString().substring(10, sb.toString().length()-2).replace(' ',',').replaceAll(",,", ",");
		        br.close();
			}		
			System.out.println("INFO: Co-ordinates "+coordinates);
			
			query= "Select B.Bid, B.BName, MDSYS.SDO_NN_DISTANCE(1) FROM Building B WHERE B.Bid!='"+id+"' AND SDO_NN(B.Bgeo, MDSYS.SDO_GEOMETRY(2003, NULL, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1), MDSYS.SDO_ORDINATE_ARRAY("+coordinates+")), 'sdo_num_res="+limit+"', 1) = 'TRUE' ORDER BY MDSYS.SDO_NN_DISTANCE(1)";
			System.out.println("INFO: Executing "+query);
			res = stmt.executeQuery(query);
			if(res.next()){
				System.out.println ("BID\tBName\tDistance");
				System.out.println (res.getString(1)+"\t"+res.getString(2)+"\t"+res.getString(3));
				while(res.next()){
					System.out.println (res.getString(1)+"\t"+res.getString(2)+"\t"+res.getString(3));
				}	
			}
			else {
				System.out.println("No result found/zero match");
			}
			System.out.println("INFO: Exectuion Completed.");
		}
		else if(tableName.equalsIgnoreCase("students")){
			// Get the coordinates for the given id
			query = "SELECT SDO_UTIL.TO_WKTGEOMETRY(SLoc) AS SLoc FROM Students WHERE Sid = '"+id+"'";
			System.out.println("INFO: Executing "+query);
			rset = stmt.executeQuery(query);
			String point = new String();
			while(rset.next()){
		        Reader reader = rset.getCharacterStream("SLoc");
		        BufferedReader br = new BufferedReader(reader);
		        String line;
		        StringBuilder sb = new StringBuilder();
		        while(null != (line = br.readLine())) {
		            sb.append(line);
		        }
		        point = sb.toString().substring(7, sb.toString().length()-1).replace(' ',',');
		        br.close();
			}	
			System.out.println("INFO: Co-ordinates "+point);
			
			query= "Select S.Sid, MDSYS.SDO_NN_DISTANCE(1) FROM Students S WHERE S.Sid!='"+id+"' AND SDO_NN(S.SLoc,MDSYS.SDO_GEOMETRY(2001,NULL,SDO_POINT_TYPE("+point+", NULL), NULL, NULL),'sdo_num_res="+limit+"', 1)= 'TRUE' ORDER BY MDSYS.SDO_NN_DISTANCE(1)";
			System.out.println("INFO: Executing "+query);
			res = stmt.executeQuery(query);
			if(res.next()){
				System.out.println("SID\tDistance");
				System.out.println (res.getString(1)+"\t"+res.getString(2));
				while(res.next()){
					System.out.println (res.getString(1)+"\t"+res.getString(2));
				}	
			}else{
				System.out.println("No result found/zero match");
			}
			System.out.println("INFO: Exectuion Completed.");
		}
		else if(tableName.equalsIgnoreCase("tramstops")){
			// Get the coordinates for the given id
			query = "SELECT SDO_UTIL.TO_WKTGEOMETRY(TSLoc) AS TSLoc FROM TramStops WHERE TSpid = '"+id+"'";
			System.out.println("INFO: Executing "+query);
			rset = stmt.executeQuery(query);
			String point = new String();
			while(rset.next()){
		        Reader reader = rset.getCharacterStream("TSLoc");
		        BufferedReader br = new BufferedReader(reader);
		        String line;
		        StringBuilder sb = new StringBuilder();
		        while(null != (line = br.readLine())) {
		            sb.append(line);
		        }
		        point = sb.toString().substring(7, sb.toString().length()-1).replace(' ',',');
		        br.close();
			}	
			System.out.println("INFO: Co-ordinates "+point);
			
			query= "Select TS.TSpid, MDSYS.SDO_NN_DISTANCE(1) FROM TramStops TS WHERE TS.TSpid!='"+id+"' AND SDO_NN(TS.TSLoc, MDSYS.SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE("+point+", NULL), NULL, NULL), 'sdo_num_res="+limit+"', 1) = 'TRUE' ORDER BY MDSYS.SDO_NN_DISTANCE(1)";
			System.out.println("INFO: Executing "+query);
			res = stmt.executeQuery(query);
			if(res.next()) {
				System.out.println("TSpID\tDistance");
				System.out.println (res.getString(1)+"\t"+res.getString(2));
				while(res.next()){
					System.out.println (res.getString(1)+"\t"+res.getString(2));
				}	
			}
			else{
				System.out.println("No result found/zero match");
			}
			System.out.println("INFO: Exectuion Completed.");
		}
		else{
			System.out.println("ERROR: Specify the correct table name");
		}
	}
	/**
	 * @param args
	 * @throws SQLException
	 */
	public void within(String[] args) throws SQLException{
		System.out.println("INFO: Executing within query");
		String id = args[1];
		String distance = args[2];
		String query = new String();
		int count = 0;
			
		String query1 = "SELECT B.Bid, B.BName FROM Building B, Students S WHERE S.Sid ='"+id+"' AND SDO_WITHIN_DISTANCE(B.BGeo, S.Sloc, 'distance = "+distance+"') = 'TRUE'";
		query = query1 +" UNION "+"SELECT TS.Tspid,'NULL' FROM Building B, TramStops TS, Students S WHERE S.Sid ='"+id+"' AND SDO_WITHIN_DISTANCE(TS.TSLoc, S.Sloc, 'distance = "+distance+"') = 'TRUE'";
		System.out.println("INFO: Executing "+query);
		ResultSet rest = stmt.executeQuery(query);
		if(rest.next()){
			System.out.println("BID/SID\tBName/SName");
			System.out.println (rest.getString(1)+"\t"+rest.getString(2));
			count = 1;
			while(rest.next()){
				count++;
				System.out.println (rest.getString(1)+"\t"+rest.getString(2));
			}	
			System.out.println("INFO: Total # of Results " + count);
		}
		else{
			System.out.println("No result found/zero match");
		}		
		System.out.println("INFO: Exectuion Completed.");
	}
	public void fixed(String args[]) throws SQLException{
		String queryNo = args[1];
		System.out.println("INFO: Executing fixed query");

		String query = new String();
		if(queryNo.equals("1")) {
			query = "(SELECT S.Sid,'NULL' FROM Students S, TramStops TS WHERE TS.TSpid = 't2ohe' AND SDO_WITHIN_DISTANCE(S.SLoc, TS.TSLoc, 'distance = 70')  = 'TRUE' UNION SELECT B.Bid, B.BName FROM Building B, TramStops TS WHERE TS.TSpid = 't2ohe' AND SDO_WITHIN_DISTANCE(B.BGeo, TS.TSLoc, 'distance = 70') = 'TRUE') INTERSECT (SELECT S.Sid, 'NULL' FROM Students S, TramStops TS WHERE TS.TSpid = 't6ssl' AND SDO_WITHIN_DISTANCE(S.SLoc, TS.TSLoc, 'distance = 50')  = 'TRUE' UNION SELECT B.Bid, B.BName FROM Building B, TramStops TS WHERE TS.TSpid = 't6ssl' AND SDO_WITHIN_DISTANCE(B.BGeo, TS.TSLoc, 'distance = 50') = 'TRUE')";
			System.out.println("INFO: Executing "+query);
			ResultSet rest = stmt.executeQuery(query);
			int count = 0;
			if(rest.next()) {
				System.out.println("SID");
				System.out.println (rest.getString(1));
				while(rest.next()) {
					count++;
					System.out.println (rest.getString(1));
				}	
				System.out.println("INFO: Total # of Results " + count);
			}
			else {
				System.out.println("No result found/zero match");
			}
		} else if(queryNo.equals("2")) {
			query = "Select S.Sid, TS.TSpid FROM Students S, TramStops TS WHERE SDO_NN(TS.TSLoc, S.SLoc, 'sdo_num_res=2') = 'TRUE'";
			System.out.println("INFO: Executing "+query);
			ResultSet rest = stmt.executeQuery(query);
			int count = 0;
			if(rest.next()) {
				System.out.println("SID\tTSpID");
				System.out.println (rest.getString(1)+"\t"+rest.getString(2));
				count = 1;
				while(rest.next()) {
					count++;
					System.out.println (rest.getString(1)+"\t"+rest.getString(2));
				}	
				System.out.println("INFO: Total # of Results " + count);
			}
			else {
				System.out.println("No result found/zero match");
			}			
		} else if(queryNo.equals("3")) {
			query = "SELECT TSpid, BCount FROM (SELECT TS.TSpid AS TSpid, COUNT(B.Bid) AS BCount FROM Building B, TramStops TS WHERE SDO_WITHIN_DISTANCE(B.BGeo, TS.TSLoc, 'distance = 250') = 'TRUE' GROUP BY(TS.TSpid) ORDER BY(COUNT(B.Bid)) DESC) WHERE rownum =1";
			System.out.println("INFO: Executing "+query);
			ResultSet rest = stmt.executeQuery(query);
			int count = 0;
			if(rest.next()) {
				System.out.println("TSpID\tBCount");
				System.out.println (rest.getString(1)+"\t"+rest.getString(2));
				count = 1;
				while(rest.next()) {
					count++;
					System.out.println (rest.getString(1)+"\t"+rest.getString(2));
				}	
				System.out.println("INFO: Total # of Results " + count);
			}
			else {
				System.out.println("No result found/zero match");
			}
		} else if(queryNo.equals("4")) {
			query = "SELECT SID, Reverse_Nearest_Neighbor_Count FROM (Select S.Sid AS SID, COUNT(B.Bid) AS Reverse_Nearest_Neighbor_Count FROM Students S, Building B WHERE SDO_NN(S.SLoc, B.BGeo, 'sdo_num_res = 1') = 'TRUE' GROUP BY S.Sid ORDER BY COUNT(B.Bid) DESC) WHERE rownum <= 5";
			System.out.println("INFO: Executing "+query);
			ResultSet rest = stmt.executeQuery(query);
			int count = 0;
			if(rest.next()) {
				System.out.println("SID\tReverse_Nearest_Neighbor_Count");
				System.out.println (rest.getString(1)+"\t"+rest.getString(2));
				count = 1;
				while(rest.next()) {
					count++;
					System.out.println (rest.getString(1)+"\t"+rest.getString(2));
				}	
				System.out.println("INFO: Total # of Results " + count);
			}
			else {
				System.out.println("No result found/zero match");
			}			
		} else if(queryNo.equals("5")) {
			query = "SELECT MIN(MbrVertices.x) AS MIN_X, MIN(MbrVertices.y) AS MIN_Y, MAX(MbrVertices.x) AS MAX_X, MAX(MbrVertices.y) AS MAX_Y FROM (SELECT SDO_GEOM.SDO_MBR(BGeo) AS MbrCoords FROM building WHERE UPPER(BName) like 'SS%') Mbr, table(SDO_UTIL.GETVERTICES(Mbr.MbrCoords)) MbrVertices";
			System.out.println("INFO: Executing "+query);
			ResultSet rest = stmt.executeQuery(query);
			System.out.println ("MIN_X\tMIN_Y\tMAX_X\tMAX_Y");
			if(rest.next()) {
				System.out.println (rest.getString(1)+"\t"+rest.getString(2)+"\t"+rest.getString(3)+"\t"+rest.getString(4));
			}
			else {
				System.out.println("No result found/zero match");
			}			
		}
		System.out.println("INFO: Exectuion Completed.");
	}
	
	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		hw2 hw2 = new hw2();	
		if(args.length>0){
			String queryType = args[0];
			if(queryType.equalsIgnoreCase("window")) {
				if(args.length == 6) hw2.window(args); else System.out.println("ERROR: Specify all necessary paramenter for window query. Example: window student 100 100 300 300");
			}
			else if(queryType.equalsIgnoreCase("nearest-neighbor")) {
				if(args.length == 4) hw2.nearestNeighbor(args); else System.out.println("ERROR: Specify all necessary paramenter for nearest-neighbor query. Example: nearest-neighbor building b3 5");
			}
			else if(queryType.equalsIgnoreCase("within")){
				if(args.length == 3) hw2.within(args); else System.out.println("ERROR: Specify all necessary paramenter for within query. Example: within p1 300");
			}
			else if(queryType.equalsIgnoreCase("fixed")) {
				if(args.length == 2) hw2.fixed(args); else System.out.println("ERROR: Specify all necessary paramenter for fixed query. Example: fixed 3");
			}
		}else{
			System.out.println("ERROR: Specify query type and parameters");
		}
		hw2.stmt.close();
	}
}
