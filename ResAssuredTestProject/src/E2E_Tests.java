
import java.util.List;
import java.util.Map;

//import org.junit.Assert;







import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class E2E_Tests {

	@BeforeClass
	public void createSession(){

	}

	//Step - 1
	//Test will start from generating Token for Authorization
	Response response;
	RequestSpecification request;
	String jsonString;
	String  bookId,token;
	String userID;
	//String token = generateToken(userName, password, request);
	@Test
	public void agetToken(){
		userID = "9b5f49ab-eea9-45f4-9d66-bcf56a531b85";
		String userName = "TOOLSQA-Test";
		String password = "Test@@123";
		String baseUrl = "http://bookstore.toolsqa.com";

		RestAssured.baseURI = baseUrl;
		request = RestAssured.given();

		request.header("Content-Type", "application/json");

		Response response = request.body("{ \"userName\":\"" + userName + "\", \"password\":\"" + password + "\"}")
				.post("/Account/v1/GenerateToken");

		Assert.assertEquals(response.getStatusCode(), 200);

		String jsonString = response.asString();
		Assert.assertTrue(jsonString.contains("token"));

		//This token will be used in later requests
		token = JsonPath.from(jsonString).get("token");
		System.out.println("Token"+token);

	}

	//Step - 2
	// Get Books - No Auth is required for this.
	@Test()
	public void bgetBooks() throws ParseException{
		response = request.get("/BookStore/v1/Books");

		Assert.assertEquals(response.getStatusCode(), 200);

		jsonString = response.asString();
		List<Map<String, String>> books = JsonPath.from(jsonString).get("books");
		Assert.assertTrue(books.size() > 0);

		//This bookId will be used in later requests, to add the book with respective isbn
		bookId = books.get(0).get("isbn");
		
		//Using Json Parsor to retrive the all Author list
		List<String> listOfAuthor=response.jsonPath().getList("author");
		System.out.println(listOfAuthor);
		
		//To retrive all isbn number of books
		for(int i=0;i<books.size();i++){
			
			System.out.println("Title :"+books.get(i).get("title")+"|||| ISBN :"+books.get(i).get("isbn"));
		}
		
		System.out.println("Test"+jsonString+"Test");
		
		//Using another JSON parsor
		JSONParser parse = new JSONParser(); 
		JSONObject jobj = (JSONObject)parse.parse(listOfAuthor.toString()); 
		System.out.println(jobj.get("author"));
		

	}



	//Step - 3
	// Add a book - with Auth
	//The token we had saved in the variable before from response in Step 1, 
	//we will be passing in the headers for each of the succeeding request
	@Test(priority=3)
	public void createBooks(){
		System.out.println("Token"+token);
		request.header("Authorization", "Bearer " + token)
		.header("Content-Type", "application/json");

		response = request.body("{ \"userId\": \"" + userID + "\", " +
				"\"collectionOfIsbns\": [ { \"isbn\": \"" + bookId + "\" } ]}")
				.post("/BookStore/v1/Books");

		Assert.assertEquals( 201, response.getStatusCode());


	}


	//Step - 4
	// Delete a book - with Auth
	@Test
	public void deleteBooks(){
		request.header("Authorization", "Bearer " + token)
		.header("Content-Type", "application/json");

		response = request.body("{ \"isbn\": \"" + bookId + "\", \"userId\": \"" + userID + "\"}")
				.delete("/BookStore/v1/Book");

		Assert.assertEquals(204, response.getStatusCode());
	}

	//Step - 5
	// Get User
	@Test
	public void egetUser(){
		request.header("Authorization", "Bearer " + token)
		.header("Content-Type", "application/json");

		response = request.get("/Account/v1/User/" + userID);
		Assert.assertEquals(200, response.getStatusCode());

		jsonString = response.asString();
		List<Map<String, String>> booksOfUser = JsonPath.from(jsonString).get("books");
		Assert.assertEquals(4, booksOfUser.size());
	}
	//My Try created User
	@Test
	public void createUser(){
		
		String userName = "TOOLSQA-Test-HariOne";
		String password = "Test@@1234";
		request.header("Content-Type", "application/json");
		response= request.body("{ \"userName\":\""+userName+"\", \"password\": \""+password + "\"}").post("/Account/v1/User");
		Assert.assertEquals(201, response.getStatusCode(),"Verified status code");
		
	}
}