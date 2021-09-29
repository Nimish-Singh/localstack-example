package io.repository;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import io.TestHelper;
import io.model.Employee;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static io.repository.StudentRepository.TABLE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(TestHelper.class)
class StudentRepositoryTest {
    private static final String DOCKER_DDB_URL = "http://localhost:4566";
    private static final String LOCALHOST = "localhost";
    private static StudentRepository repository;

    private static AmazonDynamoDB ddb;
    private static String id = "Id1";
    private static String name = "Name1";
    private static String department = "Department1";

    @BeforeAll
    public static void setUp() {
        ddb = getLocalDdbClient();

        if (isTableAlreadyPresent(TABLE_NAME)) {
            deleteTable(TABLE_NAME);
        }

        repository = new StudentRepository(ddb);
        repository.createTable();

        addDummyItem();
    }

    @AfterAll
    public static void tearDown() {
        deleteTable(TABLE_NAME);
    }

    @Test
    public void shouldReturnEmptyOptionalIfEmployeeNotFound() {
        assertFalse(repository.getItem("non-existent-id").isPresent());
    }

    @Test
    public void shouldReturnEmployee() {
        Employee employee = repository.getItem(id).get();
        assertEquals(id, employee.getId());
        assertEquals(name, employee.getName());
        assertEquals(department, employee.getDepartment());
    }

    private static AmazonDynamoDB getLocalDdbClient() {
        AwsClientBuilder.EndpointConfiguration endpoint =
                new AwsClientBuilder.EndpointConfiguration(DOCKER_DDB_URL, LOCALHOST);

        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(endpoint)
                .withRegion(LOCALHOST)
                .build();
    }

    private static void addDummyItem() {
        Employee employee = new Employee(id, name, department);
        repository.putItem(employee);
    }

    private static  boolean isTableAlreadyPresent(String tableName) {
        return !listTables().isEmpty() && listTables().contains(tableName);
    }

    private static List<String> listTables() {
        ListTablesRequest request = new ListTablesRequest().withLimit(10);
        ListTablesResult tablesList = ddb.listTables(request);
        return tablesList.getTableNames();
    }

    private static void deleteTable(String tableName) {
        ddb.deleteTable(tableName);
    }
}