//This package has functionalities related to question
package questions;

import java.sql.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import database.DBSource;
import util.ToJSON;

@Path ("/question")
public class Questions {

	//This method returns a JSONArray containing all questions
	@Path ("/allquestions")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public String returnAllQuestions(String incomingData) throws Exception {
		System.out.println("inside All Q");
		String returnString = null;
		Connection conn = null;
		CallableStatement cs = null;
		JSONArray questionsArray = new JSONArray();
		try{
			conn = DBSource.dbSource().getConnection();
			ToJSON utility = new ToJSON();
			cs = conn.prepareCall("call csc258.retrieve_allquestions()");			
			ResultSet rs = cs.executeQuery();
			while (rs.next()){
				
				JSONObject question = utility.getQuestion(rs.getInt("question_id"));
				questionsArray.put(question);
			}
			
			returnString = questionsArray.toString();
		}
		catch(Exception e){
			
		}
		
		finally{
			
		}
		
		//System.out.print(returnString);
		return returnString;
	}
	
	//This method accepts new question information and stores it into DB
	@Path ("/newquestions")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public String insertNewQuestion(String incomingData) throws Exception{
		String returnString = "false";
		Connection conn = null;
		CallableStatement cs= null;
		System.out.println("inside new Q");
		System.out.println(incomingData);
		
		try{
			conn = DBSource.dbSource().getConnection();
			JSONObject question = new JSONObject(incomingData);
			String q_type = question.getString("type");
			String q_text = question.getString("questiontext");
			String sampleAnswer = "";
			if (q_type.equals("SA")){
				sampleAnswer = question.getString("sampleanswer");
				System.out.println("sample Answer" + sampleAnswer);
			}
			
			JSONArray parts = question.getJSONArray("parts");
			int num_parts = parts.length();
			
			cs = conn.prepareCall("call csc258.insert_question(?,?,?,?)");
			cs.setString(1, q_text);
			cs.setString(2, q_type);
			cs.setInt(3, num_parts);
			cs.setString(4, sampleAnswer);
			ResultSet rs = cs.executeQuery();
			int q_id = -1, m = 0;
			if (rs.first()){
				q_id = rs.getInt("max(question_id)");
				returnString = "true";
				m = 1;
				rs.close();
			}
			
			System.out.println(q_id);
			if (m==1){
				if (q_type.equals("MC")){
					CallableStatement sql = null;
					String []choice = {"a","b","c","d","e"};
					for (int i = 0; i < num_parts; i++ ){
						sql = conn.prepareCall("call csc258.insert_mc(?,?,?)");
						sql.setInt(1, q_id);
						sql.setString(2, parts.getString(i));
						sql.setString(3, choice[i]);
						int n = sql.executeUpdate();
						if (n==0)
							returnString = "false";
						sql.close();
					}
					
					CallableStatement sql1 = conn.prepareCall("call csc258.set_mc_ch(?,?)");
					sql1.setInt(1, q_id);
					sql1.setString(2, question.getString("correctchoice"));
					int n = sql1.executeUpdate();
					if (n==0)
						returnString = "false";
					sql1.close();
				}
			
			else if (q_type.equals("SA")){
				CallableStatement sql = null;
				for(int i = 0; i < num_parts; i++){
					JSONObject rubric = parts.getJSONObject(i);
					sql = conn.prepareCall("call csc258.insert_sa_rubric(?,?,?,?,?,?)");
					sql.setInt(1, q_id);
					sql.setString(2, rubric.getString("rubric"));
					sql.setInt(3, rubric.getInt("poorpts"));
					sql.setInt(4, rubric.getInt("avgpts"));
					sql.setInt(5, rubric.getInt("goodpts"));
					sql.setInt(6, rubric.getInt("excelpts"));
					int n = sql.executeUpdate();
					if (n==0)
						returnString = "false";
					sql.close();					
				}
				
			}
		}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			cs.close();
			conn.close();
		}
		
		return returnString;
			
		/*
		String returnString = "false";
		JSONObject questionRecord;
		PreparedStatement query = null;
		PreparedStatement query1 = null;
		PreparedStatement query2 = null;
		Connection conn = null;
				
		try{
			conn = DBSource.dbSource().getConnection();
			questionRecord = new JSONObject (incomingData);
				//finding the number of choices				
				JSONArray ansParts = questionRecord.getJSONArray("parts");
				int num_parts = ansParts.length();
				
				//Creating Query to insert record in question table
				query = conn.prepareStatement("insert into csc258.question" + 
												"(type, question, num_parts)" + "values (?,?,?)");
				query.setString(1, questionRecord.getString("type") );
				query.setString(2, questionRecord.getString("questiontext") );
				query.setInt(3, num_parts);
				
				int result = query.executeUpdate();
				query.close();
			//	System.out.println(result);
				//if insertion is successful
				if (result != 0){
					
					//Query to get the question_id of newly inserted question
					query = conn.prepareStatement("select max(question_id) q_id from csc258.question");
				//	System.out.println("Hi");
					ResultSet rs = query.executeQuery();
				//	System.out.println("Hii");
					JSONArray maxQ = new JSONArray();
					ToJSON converter = new ToJSON();
					maxQ = converter.convert(rs);
				//	System.out.println(maxQ.toString());
				//	System.out.println("Hiii");
					query.close();
					JSONObject jO = new JSONObject();
					jO = maxQ.getJSONObject(0);
					int q_id = jO.getInt("q_id");
				//	System.out.println("Question id" + q_id);
					for (int j = 0; j < num_parts ; j++){
						System.out.println(ansParts.getString(j));
					}
					
					returnString = "true";
					String [] choice = {"a", "b","c","d","e"};
					//char [] choice = {'a', 'b','c','d','e'};
					//Query to insert choices into mc_q_choice table 
					for (int j =0 ; j< num_parts ; j++){
						
						query = conn.prepareStatement("insert into csc258.mc_q_choice" + 
								"(question_id, choice_seq, choice)" + "values (?,?,?)");
						query.setInt(1, q_id);
						query.setString(2, choice[j]);
						query.setString(3, ansParts.getString(j));
						
						int m = query.executeUpdate();
						if (m == 0)	
							returnString = "false";
						query.close();
					}
					
					//Query to set the correct answer
					query = conn.prepareStatement("update csc258.mc_q_choice set is_answ = ?"
							+ "where question_id = ?" + " and choice_seq = ?");
					query.setString(1, "1");
					query.setInt(2, q_id);
					query.setString(3, questionRecord.getString("correctchoice").toLowerCase());
		
					//query.setString(1, "1");
					//query.setInt(1, q_id);
					//query.setString(2, questionRecord.getString("correctchoice").toLowerCase());
					int m = query.executeUpdate();
					if (m == 0)	
						returnString = "false";
					query.close();

					
				}
							
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (conn != null) conn.close();
		}

		return returnString;    */
	}

	//This method is for updating an existing question
	@Path ("/editquestions")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String updateQuestion(String incomingData) throws Exception{
		String returnString = "false";
		Connection conn = null;
		CallableStatement cs= null;
		System.out.println();
		System.out.println("inside edit Q");
		System.out.println(incomingData);
		
		try{
			conn = DBSource.dbSource().getConnection();
			JSONObject question = new JSONObject(incomingData);
			int q_id = question.getInt("questionnum");
			String q_type = question.getString("type");
			String q_text = question.getString("questiontext");
			String sampleAnswer = "";
			if (q_type.equals("SA")){
				sampleAnswer = question.getString("sampleanswer");
			}
			
			JSONArray parts = question.getJSONArray("parts");
			int num_parts = parts.length();
			
			cs = conn.prepareCall("call csc258.update_question(?,?,?,?,?,?)");
			cs.setInt(1, q_id);
			cs.setString(2, q_text);
			cs.setString(3, sampleAnswer);
			cs.setInt(4, num_parts);
			cs.setString(5, q_type);
			cs.registerOutParameter(6, java.sql.Types.INTEGER);
	        cs.execute();
	        int p=cs.getInt(6);
			if (p == 0){
				returnString = "false";
				System.out.println(returnString);
				return returnString;
			}
			returnString = "true";
			{
				if (q_type.equals("MC")){
					CallableStatement sql = null;
					String []choice = {"a","b","c","d","e"};
					for (int i = 0; i < num_parts; i++ ){
						sql = conn.prepareCall("call csc258.insert_mc(?,?,?)");
						sql.setInt(1, q_id);
						sql.setString(2, parts.getString(i));
						sql.setString(3, choice[i]);
						int n = sql.executeUpdate();
						if (n==0)
							returnString = "false";
						sql.close();
					}
					
					CallableStatement sql1 = conn.prepareCall("call csc258.set_mc_ch(?,?)");
					sql1.setInt(1, q_id);
					sql1.setString(2, question.getString("correctchoice"));
					int n = sql1.executeUpdate();
					if (n==0)
						returnString = "false";
					sql1.close();
				}
			
				else if (q_type.equals("SA")){
					CallableStatement sql = null;
					for(int i = 0; i < num_parts; i++){
						JSONObject rubric = parts.getJSONObject(i);
						sql = conn.prepareCall("call csc258.insert_sa_rubric(?,?,?,?,?,?)");
						sql.setInt(1, q_id);
						sql.setString(2, rubric.getString("rubric"));
						sql.setInt(3, rubric.getInt("poorpts"));
						sql.setInt(4, rubric.getInt("avgpts"));
						sql.setInt(5, rubric.getInt("goodpts"));
						sql.setInt(6, rubric.getInt("excelpts"));
						int n = sql.executeUpdate();
						if (n==0)
							returnString = "false";
						sql.close();					
					}
				
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			cs.close();
			conn.close();
		}
		
		return returnString;
	}
	
	//This method is to delete a question
	@Path ("/deletequestion")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String deleteQuestion(String incomingData) throws Exception{
		String returnString = "false";
		Connection conn = null;
		CallableStatement cs= null;
		System.out.println("inside delete Q");
		
		try{
			conn = DBSource.dbSource().getConnection();
			JSONObject question = new JSONObject(incomingData);
			int q_id = question.getInt("question_id");
			int n = 0;
			cs = conn.prepareCall("call csc258.delete_question(?,?)");
			cs.setInt(1, q_id);
			cs.registerOutParameter(2, java.sql.Types.INTEGER);
	        cs.execute();
	        n=cs.getInt(2);
			System.out.println("n = " + n);
			if (n==1){
				returnString = "true";
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			cs.close();
			conn.close();
		}
		return returnString;
	}
}
