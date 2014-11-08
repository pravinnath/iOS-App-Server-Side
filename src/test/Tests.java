//This package has functionalities related to test
package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;

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


@Path ("/test")
public class Tests {
	
	//This method returns JSONArray containing all tests
	@Path ("/alltests")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public String viewAllTest (String incomingData) throws Exception{
		String returnString = null;
		Connection conn = null;
		CallableStatement cs = null;
		System.out.println("inside all T");
			
		try{
		
			conn = DBSource.dbSource().getConnection();
			cs = conn.prepareCall("call csc258.retrieve_test()");
			ResultSet rs = cs.executeQuery();
			ToJSON converter = new ToJSON();
			JSONArray json = new JSONArray();

			json = converter.convert(rs);
			returnString = json.toString();
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
		
		finally{
			conn.close();
		}
		
		
		return returnString;
	}

	//This method is for inserting a new test record into DB
	@Path("/newtest")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String createNewTest(String incomingData) throws Exception{
		String returnString = "true";
		Connection conn = null;
		CallableStatement cs = null;
		System.out.println("inside new T");
		//System.out.println("In test");
	
		try{
			conn = DBSource.dbSource().getConnection();
			JSONObject testRecord = new JSONObject (incomingData);
			JSONArray questionsArray = testRecord.getJSONArray("questionnumbers");
			int numOfQuestions = questionsArray.length();
			cs = conn.prepareCall("call csc258.insert_test(?,?,?,?,?,?,?,?)");
			cs.setString(1, testRecord.getString("testname"));
			cs.setInt(2, testRecord.getInt("time"));
			cs.setInt(3, numOfQuestions);
			cs.setInt(4, 90);//testRecord.getInt("apercent"));
			cs.setInt(5, 80);//testRecord.getInt("bpercent"));
			cs.setInt(6, 70);//testRecord.getInt("cpercent"));
			cs.setInt(7, 60);//testRecord.getInt("dpercent"));
			cs.setString(8, testRecord.getString("exp_date"));
			
			ResultSet rs = cs.executeQuery();
			JSONArray maxT = new JSONArray();
			ToJSON converter = new ToJSON();
			maxT = converter.convert(rs);
			
			if (maxT.length() == 0)
				return returnString;
			
			JSONObject jO = new JSONObject();
			jO = maxT.getJSONObject(0);
			int t_id = jO.getInt("max(test_id)");
			for (int i = 0 ; i < numOfQuestions; i++){
				CallableStatement sql = conn.prepareCall("call csc258.insert_q_in_t(?,?)");
				sql.setInt(1, t_id);
				sql.setInt(2, questionsArray.getInt(i));
				int m = sql.executeUpdate();
				if (m == 0)	
					return returnString;
				sql.close();
			}
			returnString = "true";
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

	//This method returns JSONArray containing all questions from particular test
	@Path ("/testquestions")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String viewTestQuestions(String incomingData) throws Exception{
		System.out.println("inside view T Q");
		String returnString = null;
		Connection conn = null;
		CallableStatement cs = null;
		JSONArray questionsArray = new JSONArray();
		try{
			conn = DBSource.dbSource().getConnection();
			ToJSON utility = new ToJSON();
			JSONObject testNum = new JSONObject(incomingData);
			int t_id = testNum.getInt("test_id");
			//int t_id = 1;
			cs = conn.prepareCall("call csc258.retrieve_testquestion(?)");
			cs.setInt(1, t_id);
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
			cs.close();
			conn.close();
		}
		
		//System.out.print(returnString);
		return returnString;
	}
		
	//This method inserts test assignment records into DB
	@Path ("/assigntest")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String assignTest(String incomingData) throws Exception{	
		System.out.println("inside assign T");
		String returnString = "true";
		Connection conn = null;
		CallableStatement cs = null;
	
		try{
			conn = DBSource.dbSource().getConnection();
			JSONObject assignment = new JSONObject (incomingData);
			JSONArray studentList = assignment.getJSONArray("students");
			int numOfStudent = studentList.length();
			if (numOfStudent > 0){
				CallableStatement sql = conn.prepareCall("call csc258.set_test_active(?)");
				sql.setInt(1, assignment.getInt("test_id"));
				int m = sql.executeUpdate();
				if (m == 0){
					returnString = "false";
					return returnString;
				}
			}
			for (int i = 0; i < numOfStudent; i++){
				cs= conn.prepareCall("call csc258.assign_test(?,?)");
				cs.setInt(1, assignment.getInt("test_id"));
				cs.setInt(2, studentList.getInt(i));
				int m = cs.executeUpdate();
				if (m==0)
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
			
	//This method returns JSONArray containing list of all completed tests ready to be graded
	@Path ("/gradetest")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String gradeTest(String incomingData) throws Exception{	
		System.out.println("inside grade Tests");
		String returnString = null;
		Connection conn = null;
		CallableStatement cs = null;
	
		try{
			conn = DBSource.dbSource().getConnection();
			ToJSON utility = new ToJSON();
			JSONArray list = new JSONArray();
			cs = conn.prepareCall("call csc258.retrieve_test_tobe_graded()");
			ResultSet rs = cs.executeQuery();
			list = utility.convert(rs);
			returnString = list.toString();				
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
			
	//This method returns the MCQ score for a particular student for particular test 
	@Path ("/partialscore")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String viewPartialScore(String incomingData) throws Exception{	
		System.out.println("inside partial scores for Test");
		String returnString = null;
		Connection conn = null;
		CallableStatement cs = null;
		JSONArray pScore = new JSONArray();
	
		try{
			conn = DBSource.dbSource().getConnection();
			JSONObject record = new JSONObject(incomingData);
			int sid = record.getInt("student_id");
			int tid = record.getInt("test_id");
			cs = conn.prepareCall("call csc258.retrieve_partial_score(?,?,?,?)");
			cs.setInt(1, sid);
			cs.setInt(2, tid);
			cs.registerOutParameter(3,java.sql.Types.INTEGER);
			cs.registerOutParameter(4, java.sql.Types.INTEGER);
			cs.execute();
			int totalq = cs.getInt(3);
			int score = cs.getInt(4);
			JSONObject partialScore = new JSONObject ();
			partialScore.put("totalquestions", totalq);
			partialScore.put("correctanswers",score);
			pScore.put(partialScore);
			returnString = pScore.toString();		
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
	
	//This method returns JSONArray containing short answer questions from a particular test
	//and student's response for those questions
	@Path ("/studentanswers")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String viewStudentAnswers(String incomingData) throws Exception{	
		System.out.println("inside student answers for Test");
		String returnString = null;
		Connection conn = null;
		CallableStatement cs = null;
		JSONArray SAArray = new JSONArray();
	
		try{
			conn = DBSource.dbSource().getConnection();
			JSONObject record = new JSONObject(incomingData);
			int sid = record.getInt("student_id");
			int tid = record.getInt("test_id");
			//int sid = 1;
			//int tid = 6;
			cs = conn.prepareCall("call csc258.retrieve_test_saq(?)");
			cs.setInt(1, tid);
			ResultSet rs = cs.executeQuery();
			while (rs.next()){
				int q_id = rs.getInt("question_id");
				ToJSON utility = new ToJSON();
				JSONObject question = utility.getQuestion(q_id);
				CallableStatement sql = conn.prepareCall("call csc258.retrieve_answer_to_saq(?,?,?)");
				sql.setInt(1, sid);
				sql.setInt(2, tid);
				sql.setInt(3, q_id);
				ResultSet rs1 = sql.executeQuery();
				if(rs1.first()){
					question.put("answer", rs1.getString("answer"));
				}
				SAArray.put(question);
			}
			returnString = SAArray.toString();

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

	//This method accepts the question wise points for SA questions after graded by Prof. 
	@Path ("/gradesfortest")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String recordGrades(String incomingData) throws Exception{	
		System.out.println("inside record grades for student answers");
		String returnString = "true";
		Connection conn = null;
		CallableStatement cs = null;
		System.out.println(incomingData);
	
		try{
			conn = DBSource.dbSource().getConnection();
			JSONObject record = new JSONObject(incomingData);
			int sid = record.getInt("student_id");
			int tid = record.getInt("test_id");
			JSONArray marksArray = record.getJSONArray("marks");
			int length = marksArray.length();
			for (int i =0; i< length; i++){
				JSONObject singleScore = marksArray.getJSONObject(i);
				int qid = singleScore.getInt("question_id");
				int score = singleScore.getInt("points");
				cs = conn.prepareCall("call csc258.insert_sa_score(?,?,?,?)");
				cs.setInt(1,sid);
				cs.setInt(2, tid);
				cs.setInt(3, qid);
				cs.setInt(4, score);
				int m = cs.executeUpdate();
				if (m==0){
					returnString = "false";
					return returnString;
				}
				cs.close();
			}
			
			CallableStatement sql = conn.prepareCall("call csc258.submit_graded_test(?,?)");
			sql.setInt(1, sid);
			sql.setInt(2, tid);
			int m = sql.executeUpdate();
			if (m == 0){
				returnString = "false";
				return returnString;
			}
			sql.close();
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
