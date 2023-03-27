package planets;

import static io.restassured.RestAssured.given;
import java.util.HashSet;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

/**
 * Planets API test
 */
public class Planets {

    RequestSpecification baseReq = new RequestSpecBuilder().setBaseUri("https://swapi.dev/api").setContentType(ContentType.JSON).build();
    ResponseSpecification baseResp = new ResponseSpecBuilder().expectStatusCode(200).expectContentType(ContentType.JSON).build();
    static int expectedTotalCount = 60;
    static int expectedResults = 10;
    static String expectedNext = "https://swapi.dev/api/planets/?page=2";
    static String expectedPrevious = null;
    static String expNext6 = "https://swapi.dev/api/planets/?page=6";
    static String expPrevious4 = "https://swapi.dev/api/planets/?page=4";
    static String idDorin = "49";

    /** Get all planets (only 10 planets from Page 1 are returned) */
    @Test
    public void testGetPlanets() {

        RequestSpecification reqPlanets = given().spec(baseReq);
        String planets = reqPlanets.when().get("/planets").then().spec(baseResp).extract().response().asString();

        JsonPath pl = new JsonPath(planets);
        // Verify that the total count of planets is 60
        Assert.assertEquals(pl.getInt("count"), expectedTotalCount);
        int results = pl.getInt("results.size()");
        // Verify that the number of planets in the response is 10
        Assert.assertEquals(results, expectedResults);
        // Verify that the next page is 2
        Assert.assertEquals(pl.getString("next"), expectedNext);
        // Verify that the previous page is null
        Assert.assertEquals(pl.getString("previous"), expectedPrevious);
        // Verify that the name of the 2nd planet from results is Alderaan
        Assert.assertEquals(pl.get("results.name[1]"), "Alderaan");

        Set<String> page1Names = new HashSet<String>();
        for (int i = 0; i < results; i++) {
            String name = pl.get("results[" + i + "].name");
            page1Names.add(name);
        }
        // Verify that Endor is in the results
        Assert.assertTrue(page1Names.contains("Endor"));
    }

    /** Get planets from Page 5 */
    @Test
    public void testGetPlanetsPage5() {

        RequestSpecification reqPage5 = given().spec(baseReq).param("page", "5");
        String planets = reqPage5.when().get("/planets").then().spec(baseResp).extract().response().asString();

        JsonPath five = new JsonPath(planets);
        // Verify that the total count of planets is 60
        Assert.assertEquals(five.getInt("count"), expectedTotalCount);
        int results = five.getInt("results.size()");
        // Verify that the number of planets in the response is 10
        Assert.assertEquals(results, expectedResults);
        // Verify that the next page is 6
        Assert.assertEquals(five.getString("next"), expNext6);
        // Verify that the previous page is 4
        Assert.assertEquals(five.getString("previous"), expPrevious4);
        // Verify that the name of the last planet on the page is Champala
        Assert.assertEquals(five.get("results.name[9]"), "Champala");

        Set<String> page5Names = new HashSet<String>();
        for (int i = 0; i < results; i++) {
            String name = five.get("results[" + i + "].name");
            page5Names.add(name);
        }
        // Verify that Tund is in the results
        Assert.assertTrue(page5Names.contains("Tund"));

        String urlDorin = five.getString("results.url[8]");
        String idDor = urlDorin.substring(30, urlDorin.lastIndexOf('/'));
        // Verify that Dorin has id 49
        Assert.assertEquals(idDor, idDorin);
    }

    /** Get planets from an invalid page - page 0 */
    @Test
    public void testGetPlanetsInvalidPage() {

        RequestSpecification reqPage0 = given().spec(baseReq).param("page", "0");
        String page0 = reqPage0.when().get("/planets").then().assertThat().statusCode(404).extract().response().asString();
        JsonPath plPage0 = new JsonPath(page0);
        Assert.assertEquals(plPage0.getString("detail"), "Not found");
    }

    /** Get a specific planet passing its id */
    @Test
    public void testGetPlanetDorin() {
        RequestSpecification reqPlanet = given().spec(baseReq);
        String dorin = reqPlanet.when().get("/planets/" + idDorin + "").then().spec(baseResp).extract().response().asString();
        JsonPath dor = new JsonPath(dorin);
        Assert.assertEquals(dor.getString("climate"), "temperate");
    }

    /** Pass an out of range planet id - 71 */
    @Test
    public void testGetNonexistingPlanet() {

        RequestSpecification reqPlanet71 = given().spec(baseReq);
        String planet71 = reqPlanet71.when().get("/planets/71").then().assertThat().statusCode(404).extract().response().asString();
        JsonPath pl71 = new JsonPath(planet71);
        Assert.assertEquals(pl71.getString("detail"), "Not found");
    }

    /** Search planet names with "li" */
    @Test
    public void tesetSearchPlanets() {

        RequestSpecification reqSearchPlanets = given().spec(baseReq).param("search", "li");
        String liPlanets = reqSearchPlanets.when().get("/planets").then().spec(baseResp).extract().response().asString();
        JsonPath li = new JsonPath(liPlanets);
        int liResults = li.getInt("results.size()");
        // Verify that the number of planets returned is equal to the total count (4)
        Assert.assertEquals(li.getInt("count"), liResults);
    }

    /** Get the JSON schema for planets - this expectation fails (the resource is not found), it's a bug */
    @Test
    public void testGetPlanetsSchema() {

        RequestSpecification reqSchema = given().spec(baseReq);
        reqSchema.when().get("/planets/schema").then().assertThat().statusCode(200);
    }

}