package io.repository;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import io.model.Employee;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StudentRepository {
    static final String TABLE_NAME = "Employee";
    private static final String ID = "Id";
    private static final String NAME = "Name";
    private static final String DEPARTMENT = "Department";
    private static final long CAPACITY_UNITS = 100L;

    private AmazonDynamoDB ddb;

    public StudentRepository(AmazonDynamoDB ddb) {
        this.ddb = ddb;
    }

    public String createTable() {
        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(ID, ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement(ID, KeyType.HASH))
                .withProvisionedThroughput(new ProvisionedThroughput(CAPACITY_UNITS, CAPACITY_UNITS))
                .withTableName(TABLE_NAME);

        CreateTableResult result = ddb.createTable(request);
        return result.getTableDescription().getTableName();
    }

    public void putItem(Employee employee) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(ID, new AttributeValue(employee.getId()));
        item.put(NAME, new AttributeValue(employee.getName()));
        item.put(DEPARTMENT, new AttributeValue(employee.getDepartment()));

        ddb.putItem(TABLE_NAME, item);

        System.out.println("\nData inserted for id:" + employee.getId());
    }

    public Optional<Employee> getItem(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(ID, new AttributeValue(id));
        Map<String, AttributeValue> item = ddb.getItem(TABLE_NAME, key).getItem();
        return item == null ? Optional.empty() :
                Optional.of(new Employee(item.get(ID).getS(), item.get(NAME).getS(), item.get(DEPARTMENT).getS()));
    }
}
