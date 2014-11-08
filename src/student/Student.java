//This package houses student related functionality
package student;

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

@Path ("/student")
public class Student {
	
	//This method returns JSONArray of all students
	@Path ("/allstudents")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String viewStudentList(String incomingData) throws Exception{
		String returnString = null;
		JSONArray list = new JSONArray();
		Connection conn = null;
		CallableStatement cs = null;
		
		System.out.println("inside all student");
		
		try{
			conn = DBSource.dbSource().getConnection();
			ToJSON utility = new ToJSON();
			cs = conn.prepareCall("call csc258.retrieve_students()");
			ResultSet rs = cs.executeQuery();
			list = utility.convert(rs);
			returnString = list.toString();			
		}
		
		catch(Exception e){
			
		}
		finally{
			cs.close();
			conn.close();
		}
		return returnString;
		
	}

	//This method returns JSONArray of students not assigned to particular test
	@Path ("/unassignedstudents")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String viewUnassignedStudentList(String incomingData) throws Exception{
		String returnString = null;
		Connection conn = null;
		CallableStatement cs = null;
		System.out.println("inside unassigned students");
		
		try{
			conn = DBSource.dbSource().getConnection();
			ToJSON utility = new ToJSON();
			JSONObject test = new JSONObject(incomingData);
			cs = conn.prepareCall("call csc258.retrieve_unassignedstudents(?)");
			cs.setInt(1, test.getInt("test_id"));
			ResultSet rs = cs.executeQuery();
			JSONArray list = utility.convert(rs);
			returnString = list.toString();
		}
		
		catch(Exception e){
			
		}
		finally{
			cs.close();
			conn.close();
		}
		return returnString;
	}

	//This method returns the list of all tests assigned to particular student which are not attempted
	@Path ("/assignedtests")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public String viewAssignedTest (String incomingData) throws Exception{
		String returnString = null;
		Connection conn = null;
		CallableStatement cs = null;
		System.out.println("inside assigned Test");
			
		try{
		
			conn = DBSource.dbSource().getConnection();
			JSONObject student = new JSONObject(incomingData);
			int id = student.getInt("student_id");
			cs = conn.prepareCall("call csc258.retrieve_testassigned(?)");
			cs.setInt(1, id);
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
	
	//This method returns the list of all the graded tests for particular student
	@Path ("/gradedtests")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public String viewGradedTest (String incomingData) throws Exception{
		String returnString = null;
		Connection conn = null;
		CallableStatement cs = null;
		System.out.println("inside graded Test");
			
		try{
		
			conn = DBSource.dbSource().getConnection();
			JSONObject student = new JSONObject(incomingData);
			int id = student.getInt("student_id");
			cs = conn.prepareCall("call csc258.retrieve_testscores(?)");
			cs.setInt(1, id);
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

	//This method accepts the student's responses for a particular test
	@Path ("/answertest")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public String insertAnswersforTest (String incomingData) throws Exception{
		String returnString = "true";
		Connection conn = null;
		CallableStatement cs = null;
		System.out.println("inside answer Test");
		System.out.println(incomingData);
		//incomingData = "";	
		try{
		
			conn = DBSource.dbSource().getConnection();
			JSONObject answer = new JSONObject(incomingData);
			int test_id = answer.getInt("test_id");
			int student_id = answer.getInt("student_id");
			JSONArray ansArray = answer.getJSONArray("answers");
			int length = ansArray.length();
			int flag = 1;
			for (int i = 0; i<length; i++){
				JSONObject singleAns = ansArray.getJSONObject(i);
				int qid = singleAns.getInt("question_id");
				String ans = singleAns.getString("answer");
				cs = conn.prepareCall("call csc258.insert_ans(?,?,?,?)");
				cs.setInt(1, student_id);
				cs.setInt(2, test_id);
				cs.setInt(3, qid);
				cs.setString(4, ans);
				
				int m = cs.executeUpdate();
				if (m ==0){
					returnString = "false";
					flag = 0;
				}  
					
				cs.close();
			}  
			
			if (flag == 1){
				CallableStatement sql  = conn.prepareCall("call csc258.submit_test(?,?)");
				sql.setInt(1, student_id);
				sql.setInt(2, test_id);
				int m = sql.executeUpdate();
				if (m == 0)
					returnString = "false";
			}
			
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
		
		finally{
			conn.close();
		}
		
		
		return returnString;
	}

	//This method returns list of all MCQ appearing on particular test
	@Path ("/testmcq")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String viewTestMCQ(String incomingData) throws Exception{
		System.out.println("inside Test MCQ");
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
			cs = conn.prepareCall("call csc258.retrieve_test_q_mc(?)");
			cs.setInt(1, t_id);
			ResultSet rs = cs.executeQuery();
			while (rs.next()){
				
				JSONObject question = utility.getQuestion(rs.getInt("question_id"));
				questionsArray.put(question);
			}
			
			returnString = questionsArray.toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		finally{
			cs.close();
			conn.close();
		}
		
		//System.out.print(returnString);
		return returnString;
	}

	//This method returns list of all SAQ appearing on particular test
	@Path ("/testsaq")
	@POST
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)

	public String viewTestSAQ(String incomingData) throws Exception{
		System.out.println("inside Test SAQ");
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
			cs = conn.prepareCall("call csc258.retrieve_test_q_sa(?)");
			cs.setInt(1, t_id);
			ResultSet rs = cs.executeQuery();
			while (rs.next()){
				
				JSONObject question = utility.getQuestion(rs.getInt("question_id"));
				questionsArray.put(question);
			}
			
			returnString = questionsArray.toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		finally{
			cs.close();
			conn.close();
		}
		
		//System.out.print(returnString);
		return returnString;
	}
}
