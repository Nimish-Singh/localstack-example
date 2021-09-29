package io.repository;

import cloud.localstack.Localstack;
import cloud.localstack.LocalstackTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import io.model.Employee;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static io.repository.StudentRepository.TABLE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(LocalstackTestRunner.class)
@LocalstackDockerProperties(services = {"dynamodb"})
public class StudentRepositoryTest {
    private static StudentRepository repository;

    private static AmazonDynamoDB ddb;
    private static String id = "Id1";
    private static String name = "Name1";
    private static String department = "Department1";

    @BeforeClass
    public static void setUp() {
        ddb = getLocalDdbClient();

        if (isTableAlreadyPresent(TABLE_NAME)) {
            deleteTable(TABLE_NAME);
        }

        repository = new StudentRepository(ddb);
        repository.createTable();

        addDummyItem();
    }

    @AfterClass
    public static void tearDown() {
        deleteTable(TABLE_NAME);
    }

    @Test
    public void shouldReturnEmptyOptionalIfEmployeeNotFound() {
        assertFalse(repository.getItem("5").isPresent());
    }

    @Test
    public void shouldReturnEmployee() {
        Employee employee = repository.getItem(id).get();
        assertEquals(id, employee.getId());
        assertEquals(name, employee.getName());
        assertEquals(department, employee.getDepartment());
    }

    private static AmazonDynamoDB getLocalDdbClient() {
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(Localstack.INSTANCE.getEndpointDynamoDB(), "localhost");
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(endpoint)
                .withRegion("localhost")
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