//util package has the reusable utilities

package util;

import org.codehaus.jettison.json.*;

import database.DBSource;

import java.sql.*;


public class ToJSON {

	//This method accepts resultset as an input and converts it into JSONArray
	public JSONArray convert (ResultSet rs) throws Exception{
	     JSONArray respJSON = new JSONArray();

	        try {
	            java.sql.ResultSetMetaData rsmd = rs.getMetaData();
	            int numColumns = rsmd.getColumnCount();
	            while (rs.next()) {
	                JSONObject obj = new JSONObject();
	                for (int i = 1; i < numColumns + 1; i++) {

	                    String columnName = rsmd.getColumnName(i);
	                    if (rsmd.getColumnType(i) == java.sql.Types.ARRAY) {
	                        obj.put(columnName, rs.getArray(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.BIGINT) {
	                        obj.put(columnName, rs.getInt(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.BOOLEAN) {
	                        obj.put(columnName, rs.getBoolean(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.BLOB) {
	                        obj.put(columnName, rs.getBlob(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
	                        obj.put(columnName, rs.getDouble(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
	                        obj.put(columnName, rs.getFloat(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
	                        obj.put(columnName, rs.getInt(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
	                        obj.put(columnName, rs.getNString(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
	                        obj.put(columnName, rs.getString(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
	                        obj.put(columnName, rs.getInt(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.SMALLINT) {
	                        obj.put(columnName, rs.getInt(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
	                        obj.put(columnName, rs.getDate(i));
	                    } else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
	                        obj.put(columnName, rs.getTimestamp(i));
	                    } else {
	                        obj.put(columnName, rs.getObject(i));
	                    }

	                }
	                respJSON.put(obj);
	                //respJSON.add(obj);

	            }
	        }
	            
	            catch (Exception e){
	            	e.printStackTrace();
	            }
	        
	        return respJSON;
	}
	
	//This method accepts question_id and creates a JSONObject with all the details for the question
	public JSONObject getQuestion(int q_id) throws Exception{
		JSONObject question = new JSONObject();
		Connection conn = null;
		CallableStatement cs = null;
		
		try{
			conn = DBSource.dbSource().getConnection();
			cs = conn.prepareCall("call csc258.retrieve_question(?)");
			cs.setInt(1, q_id);
			ResultSet rs = cs.executeQuery();
			if(rs.first()){
				question.put("questionnum", rs.getInt("question_id"));
				question.put("questiontext", rs.getString("question"));
				question.put("type", rs.getString("type"));
				question.put("numparts", rs.getInt("num_parts"));
				question.put("sampleanswer", rs.getString("sample_answ"));
				CallableStatement sql = conn.prepareCall("call csc258.retrieve_questionparts(?,?)");
				sql.setInt(1, q_id);
				sql.setString(2, rs.getString("type"));
				ResultSet parts = sql.executeQuery();				
				JSONArray prt = new JSONArray();
				String qtype = rs.getString("type");
				//System.out.println(qtype);
				if (qtype.equals("MC") ){
					while(parts.next()){
						prt.put(parts.getString("choice"));
						}
					CallableStatement sql1 = conn.prepareCall("call csc258.retrieve_correctChoice(?)");
					sql1.setInt(1, q_id);
					ResultSet cc = sql1.executeQuery();
					if(cc.first()){
						question.put("correctchoice", cc.getString("choice_seq"));
					}
					sql1.close();
				}
				
				else if (qtype.equals("SA")){
					while (parts.next()){
						JSONObject rub = new JSONObject();
						rub.put("rubric", parts.getString("rubric"));
						rub.put("poorpts", parts.getInt("poorpts"));
						rub.put("avgpts", parts.getInt("avgpts"));
						rub.put("goodpts", parts.getInt("goodpts"));
						rub.put("excelpts", parts.getInt("excelpts"));
						prt.put(rub);
					}
				}
				question.put("parts", prt);
				sql.close();
			}
			
			
			
			
			/*
			conn = DBSource.dbSource().getConnection();
			cs = conn.prepareCall("call csc258.retrieve_question(?)");
			cs.setInt(1, q_id);
			ResultSet rs = cs.executeQuery();
			JSONArray maxQ = new JSONArray();
			ToJSON converter = new ToJSON();
			maxQ = converter.convert(rs);
			JSONObject object = maxQ.getJSONObject(0);
			question.put("questionum", object.getInt("question_id"));
			question.put("questiontext", object.getString("question"));
			question.put("type", object.getString("type"));
			question.put("numparts", object.getInt("num_parts"));
			
			//System.out.println(maxQ.toString());
			
			CallableStatement sql = conn.prepareCall("call csc258.retrieve_questionparts(?,?)");
			sql.setInt(1, q_id);
			sql.setString(2, question.getString("type"));
			ResultSet part = sql.executeQuery();
			maxQ = converter.convert(part);
			JSONArray parts = new JSONArray();
			for (int i = 0; i< maxQ.length(); i++){
				JSONObject obj = maxQ.getJSONObject(i);
				parts.put(obj.get("choice"));
			}
			sql.close();
			part.close();
			question.put("parts", parts);
			
			CallableStatement sql1 = conn.prepareCall("call csc258.retrieve_correctChoice (?)");
			sql1.setInt(1, q_id);
			ResultSet ch = sql1.executeQuery();
			JSONArray choice = new JSONArray();
			choice = converter.convert(ch);
			JSONObject ob = choice.getJSONObject(0);
			System.out.println(ob.toString());
			question.put("correctchoice", ob.getString("choice_seq"));
			*/
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			cs.close();
			conn.close();
		}
		
		
		return question;
	}
}
