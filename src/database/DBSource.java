//This package defines the database connection parameters

package database;

import javax.naming.*;
import javax.sql.*;

public class DBSource {

	private static DataSource myDBSource = null;
	private static Context context = null;
	
	public static DataSource dbSource() throws Exception {
	   
		if (myDBSource != null){
			return myDBSource;
		}
		
	   try{
		   
		   if (context == null){
			   context = new InitialContext();
		   }
		   myDBSource = (DataSource) context.lookup("csc258");
		   
	   }
		
	   catch (Exception e){
		   e.printStackTrace();
	   }
		
		return myDBSource;
	}
	
}
