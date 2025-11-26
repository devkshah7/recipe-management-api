**RECIPE MANAGEMENT API**

A Spring Boot 3.3.x based RESTful API for managing recipes, including full CRUD support, advanced filtering, exception handling, unit tests, integration tests, and OpenAPI documentation.

This project is built as part of a learning assignment and follows clean architecture practices using Java 21, Spring Boot 3, H2 Database, Spring Data JPA, and Springdoc OpenAPI.

**FEATURES:**
**CORE FUNCTIONALITY**
1. Create, read, update, delete recipes
2. Partial update using PATCH
3. Store ingredients as dynamic collections
4. Ingredient normalization (trim + lowercase)

**Advanced Search & Filtering**

Filter recipes by:
1. vegetarian (true/false)
2. servings
3. include ingredients
4. exclude ingredients
5. text search in instructions
6. preparationTime

Uses Spring Data JPA Specifications.

**Global Exception Handling**
1. Centralized @ControllerAdvice
2. JSON error responses for:
   1. 404 (Not Found)
   2. Validation errors
   3. Invalid JSON

**Testing**
1. Mockito-based unit tests for the controller
2. Integration tests using:
   1. H2 in-memory database
   2. MockMvc
   3. Real controller -> service -> repository flow

All major features are covered.

**API Documentation**
1. Interactive Swagger UI via SpringDoc OpenAPI
2. Auto-generated OpenAPI specification (JSON & YAML)

**Technologies Used**

| Area        | Technology                |
|-------------|---------------------------|
| Language    | Java 21                   |
| Framework   | Spring Boot 3.3.x         |
| Database    | H2 (in-memory)            |
| Persistence | Spring Data JPA           |
| API Docs    | Springdoc OpenAPI 2.x     |
| Testing     | JUnit 5, MockMvc, Mockito |
| Build Tool  | Maven

**Project Structure**
1. src/
   1. main/
      1. java/com/..../controller
      2. java/com/..../service
      3. java/com/..../repository
      4. java/com/..../exception
      5. java/com/..../model
      6. java/com/..../specification
      7. resources/application.properties
   2. test/
      1. controller (unit tests - Mockito)
      2. integration (integration tests - H2 + MockMvc)

**Running the application**
1. Clone the repository
   1. git clone https://github.com/devkshah7/recipe-management-api.git
   2. cd recipe-management-api
2. Build the application
   1. mvn clean package
3. Run the application
   1. mvn spring-boot:run
4. Server will start at: http://localhost:8080

**API Endpoints**

Recipe CRUD:

| Method | Endpoint      | Description           |
|--------|---------------|-----------------------|
| POST   | /recipes      | Create a recipe       |
| GET    | /recipes      | Search / list recipes |
| GET    | /recipes/{id} | Get recipe by ID      |
| PUT    | /recipes/{id} | Full update           |
| PATCH  | /recipes/{id} | Partial update        |
| DELETE | /recipes/{id} | Delete recipe     

**Filtering Examples**
1. GET /recipes?vegetarian=true&servings=2
2. GET /recipes?include=potato,carrot
3. GET /recipes?exclude=onion
4. GET /recipes?text=oven
5. GET /recipes?preparationTime=15

**Running Tests:**
1. Run all tests: mvn test
2. Run only unit tests: mvn -Dtest=RecipeControllerUnitTest test
3. Run only integration tests: mvn -Dtest=RecipeIntegrationTest test

**H2 Database Console: How to enable console**
1. Once the application is running,
2. Visit: http://localhost:8082
3. Connect using:
`JDBC URL: jdbc:h2:tcp://localhost:9092/mem:recipesdb`
`User: sa`
`password:`

**API Documentation (Swagger UI)**

Once the application is running, open:

Swagger UI:
`http://localhost:8080/swagger-ui.html`

OpenAPI JSON:
`http://localhost:8080/v3/api-docs`

OpenAPI YAML:
`http://localhost:8080/v3/api-docs.yaml`