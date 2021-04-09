/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.db.metadata;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.metadata.mysql.*;
import net.hasor.test.db.utils.DsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.hasor.test.db.utils.DsUtils.MYSQL_SCHEMA_NAME;

/***
 *
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlMetadataServiceSupplierTest {
    private Connection            connection;
    private MySqlMetadataSupplier repository;

    @Before
    public void beforeTest() throws SQLException, IOException {
        this.connection = DsUtils.localMySQL();
        this.repository = new MySqlMetadataSupplier(this.connection);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(this.connection);
        //
        List<MySqlTable> allTables = this.repository.getAllTables();
        if (!allTables.isEmpty()) {
            List<String> collect = allTables.stream().map(MySqlTable::getTableName).collect(Collectors.toList());
            //
            if (collect.contains("tb_user")) {
                jdbcTemplate.execute("drop table tb_user");
            }
            if (collect.contains("proc_table_ref")) {
                jdbcTemplate.execute("drop table proc_table_ref");
            }
            if (collect.contains("proc_table")) {
                jdbcTemplate.execute("drop table proc_table");
            }
            if (collect.contains("t3")) {
                jdbcTemplate.execute("drop table t3");
            }
            if (collect.contains("t1")) {
                jdbcTemplate.execute("drop table t1");
            }
        }
        jdbcTemplate.loadSplitSQL(";", StandardCharsets.UTF_8, "/net_hasor_db/metadata/mysql_script.sql");
    }

    @After
    public void afterTest() throws SQLException {
        this.connection.close();
    }

    @Test
    public void getSchemasTest() throws SQLException {
        List<MySqlSchema> schemas = this.repository.getSchemas();
        List<String> collect = schemas.stream().map(MySqlSchema::getName).collect(Collectors.toList());
        assert collect.contains("information_schema");
        assert collect.contains("mysql");
        assert collect.contains(MYSQL_SCHEMA_NAME);
    }

    @Test
    public void getSchemaTest() throws SQLException {
        MySqlSchema schema1 = this.repository.getSchema("abc");
        MySqlSchema schema2 = this.repository.getSchema(MYSQL_SCHEMA_NAME);
        assert schema1 == null;
        assert schema2 != null;
    }

    @Test
    public void getTables() throws SQLException {
        Map<String, List<MySqlTable>> tableList = this.repository.getTables("mysql", "information_schema");
        assert tableList.size() == 2;
        assert tableList.containsKey("mysql");
        assert tableList.containsKey("information_schema");
        List<String> tableForInformationSchema = tableList.get("information_schema").stream().map(MySqlTable::getTableName).collect(Collectors.toList());
        assert tableForInformationSchema.contains("COLUMNS");
        assert tableForInformationSchema.contains("TABLES");
        assert tableForInformationSchema.contains("SCHEMATA");
        assert tableForInformationSchema.size() > 3;
    }

    @Test
    public void findTables() throws SQLException {
        List<MySqlTable> tableList = this.repository.findTable("information_schema", "COLUMNS", "TABLES", "SCHEMATA", "ABC");
        List<String> tableNames = tableList.stream().map(MySqlTable::getTableName).collect(Collectors.toList());
        assert tableNames.size() == 3;
        assert tableNames.contains("COLUMNS");
        assert tableNames.contains("TABLES");
        assert tableNames.contains("SCHEMATA");
    }

    @Test
    public void getTable() throws SQLException {
        MySqlTable tableObj1 = this.repository.getTable("information_schema", "COLUMNS");
        MySqlTable tableObj2 = this.repository.getTable("information_schema", "ABC");
        MySqlTable tableObj3 = this.repository.getTable(MYSQL_SCHEMA_NAME, "t3");
        assert tableObj1 != null;
        assert tableObj1.getTableType() == MySqlTableType.SystemView;
        assert tableObj2 == null;
        assert tableObj3 != null;
        assert tableObj3.getTableType() == MySqlTableType.Table;
    }

    @Test
    public void getColumns_1() throws SQLException {
        List<MySqlColumn> columnList = this.repository.getColumns("information_schema", "COLUMNS");
        Map<String, MySqlColumn> columnMap = columnList.stream().collect(Collectors.toMap(MySqlColumn::getName, c -> c));
        assert columnMap.size() > 11;
        assert columnMap.containsKey("TABLE_NAME");
        assert columnMap.containsKey("TABLE_SCHEMA");
        assert columnMap.containsKey("TABLE_CATALOG");
        assert columnMap.containsKey("DATA_TYPE");
        assert columnMap.containsKey("NUMERIC_PRECISION");
        assert columnMap.containsKey("IS_NULLABLE");
        assert columnMap.containsKey("NUMERIC_SCALE");
        assert columnMap.containsKey("DATETIME_PRECISION");
        assert columnMap.containsKey("CHARACTER_MAXIMUM_LENGTH");
        assert columnMap.containsKey("CHARACTER_OCTET_LENGTH");
        assert columnMap.containsKey("COLUMN_TYPE");
        assert columnMap.get("TABLE_NAME").getSqlType() == MySqlTypes.VARCHAR;
    }

    @Test
    public void getColumns_2() throws SQLException {
        List<MySqlColumn> columnList = this.repository.getColumns(MYSQL_SCHEMA_NAME, "proc_table_ref");
        Map<String, MySqlColumn> columnMap = columnList.stream().collect(Collectors.toMap(MySqlColumn::getName, c -> c));
        assert columnMap.size() == 6;
        assert columnMap.get("r_int").isPrimaryKey();
        assert !columnMap.get("r_int").isUniqueKey();
        assert !columnMap.get("r_k1").isPrimaryKey();
        assert !columnMap.get("r_k1").isUniqueKey();
        assert !columnMap.get("r_k2").isPrimaryKey();
        assert !columnMap.get("r_k2").isUniqueKey();
        assert !columnMap.get("r_name").isPrimaryKey();
        assert columnMap.get("r_name").isUniqueKey();
        assert !columnMap.get("r_index").isPrimaryKey();
        assert !columnMap.get("r_index").isUniqueKey();
        assert !columnMap.get("r_data").isPrimaryKey();
        assert !columnMap.get("r_data").isUniqueKey();
    }

    @Test
    public void getConstraint1() throws SQLException {
        List<MySqlConstraint> columnList = this.repository.getConstraint(MYSQL_SCHEMA_NAME, "proc_table_ref");
        Map<String, MySqlConstraintType> typeMap = columnList.stream().collect(Collectors.toMap(MySqlConstraint::getName, MySqlConstraint::getConstraintType));
        assert typeMap.size() == 3;
        assert typeMap.containsKey("PRIMARY");
        assert typeMap.containsKey("proc_table_ref_uk");
        assert typeMap.containsKey("ptr");
        assert typeMap.get("PRIMARY") == MySqlConstraintType.PrimaryKey;
        assert typeMap.get("proc_table_ref_uk") == MySqlConstraintType.Unique;
        assert typeMap.get("ptr") == MySqlConstraintType.ForeignKey;
    }

    @Test
    public void getConstraint2() throws SQLException {
        List<MySqlConstraint> columnList = this.repository.getConstraint(MYSQL_SCHEMA_NAME, "proc_table_ref", MySqlConstraintType.Unique);
        Map<String, MySqlConstraintType> typeMap = columnList.stream().collect(Collectors.toMap(MySqlConstraint::getName, MySqlConstraint::getConstraintType));
        assert typeMap.size() == 1;
        assert !typeMap.containsKey("PRIMARY");
        assert typeMap.containsKey("proc_table_ref_uk");
        assert !typeMap.containsKey("ptr");
        assert typeMap.get("proc_table_ref_uk") == MySqlConstraintType.Unique;
    }

    @Test
    public void getPrimaryKey1() throws SQLException {
        MySqlPrimaryKey primaryKey = this.repository.getPrimaryKey(MYSQL_SCHEMA_NAME, "proc_table_ref");
        assert primaryKey.getConstraintType() == MySqlConstraintType.PrimaryKey;
        assert primaryKey.getName().equals("PRIMARY");
        assert primaryKey.getColumns().size() == 1;
        assert primaryKey.getColumns().contains("r_int");
    }

    @Test
    public void getPrimaryKey2() throws SQLException {
        MySqlPrimaryKey primaryKey = this.repository.getPrimaryKey(MYSQL_SCHEMA_NAME, "proc_table");
        assert primaryKey.getConstraintType() == MySqlConstraintType.PrimaryKey;
        assert primaryKey.getName().equals("PRIMARY");
        assert primaryKey.getColumns().size() == 2;
        assert primaryKey.getColumns().contains("c_id");
        assert primaryKey.getColumns().contains("c_name");
    }

    @Test
    public void getPrimaryKey3() throws SQLException {
        MySqlTable table = this.repository.getTable(MYSQL_SCHEMA_NAME, "t3");
        MySqlPrimaryKey primaryKey = this.repository.getPrimaryKey(MYSQL_SCHEMA_NAME, "t3");
        assert table != null;
        assert primaryKey == null;
    }

    @Test
    public void getUniqueKey() throws SQLException {
        List<MySqlUniqueKey> uniqueKeyList = this.repository.getUniqueKey(MYSQL_SCHEMA_NAME, "tb_user");
        Map<String, MySqlUniqueKey> uniqueKeyMap = uniqueKeyList.stream().collect(Collectors.toMap(MySqlUniqueKey::getName, u -> u));
        assert uniqueKeyMap.size() == 2;
        assert uniqueKeyMap.containsKey("tb_user_userUUID_uindex");
        assert uniqueKeyMap.containsKey("tb_user_email_userUUID_uindex");
        assert uniqueKeyMap.get("tb_user_userUUID_uindex").getColumns().size() == 1;
        assert uniqueKeyMap.get("tb_user_userUUID_uindex").getColumns().contains("userUUID");
        assert uniqueKeyMap.get("tb_user_email_userUUID_uindex").getColumns().size() == 2;
        assert uniqueKeyMap.get("tb_user_email_userUUID_uindex").getColumns().contains("userUUID");
        assert uniqueKeyMap.get("tb_user_email_userUUID_uindex").getColumns().contains("email");
    }

    @Test
    public void getForeignKey() throws SQLException {
        List<MySqlForeignKey> foreignKeyList1 = this.repository.getForeignKey(MYSQL_SCHEMA_NAME, "tb_user");
        assert foreignKeyList1.size() == 0;
        List<MySqlForeignKey> foreignKeyList2 = this.repository.getForeignKey(MYSQL_SCHEMA_NAME, "proc_table_ref");
        assert foreignKeyList2.size() == 1;
        MySqlForeignKey foreignKey = foreignKeyList2.get(0);
        assert foreignKey.getConstraintType() == MySqlConstraintType.ForeignKey;
        assert foreignKey.getFkColumn().size() == 2;
        assert foreignKey.getFkColumn().get(0).equals("r_k1");
        assert foreignKey.getFkColumn().get(1).equals("r_k2");
        assert foreignKey.getName().equals("ptr");
        assert foreignKey.getReferenceSchema().equals(MYSQL_SCHEMA_NAME);
        assert foreignKey.getReferenceTable().equals("proc_table");
        assert foreignKey.getReferenceMapping().get("r_k1").equals("c_id");
        assert foreignKey.getReferenceMapping().get("r_k2").equals("c_name");
    }

    @Test
    public void getIndexes1() throws SQLException {
        List<MySqlIndex> indexList = this.repository.getIndexes(MYSQL_SCHEMA_NAME, "tb_user");
        Map<String, MySqlIndex> indexMap = indexList.stream().collect(Collectors.toMap(MySqlIndex::getName, i -> i));
        assert indexMap.size() == 4;
        assert indexMap.containsKey("PRIMARY");
        assert indexMap.containsKey("tb_user_userUUID_uindex");
        assert indexMap.containsKey("tb_user_email_userUUID_uindex");
        assert indexMap.containsKey("normal_index_tb_user");
        assert indexMap.get("PRIMARY").getColumns().size() == 1;
        assert indexMap.get("PRIMARY").getColumns().get(0).equals("userUUID");
        assert indexMap.get("PRIMARY").getIndexEnum() == MySqlIndexType.Primary;
        assert indexMap.get("tb_user_userUUID_uindex").getColumns().size() == 1;
        assert indexMap.get("tb_user_userUUID_uindex").getColumns().get(0).equals("userUUID");
        assert indexMap.get("tb_user_userUUID_uindex").getIndexEnum() == MySqlIndexType.Unique;
        assert indexMap.get("tb_user_email_userUUID_uindex").getColumns().size() == 2;
        assert indexMap.get("tb_user_email_userUUID_uindex").getColumns().get(0).equals("email");
        assert indexMap.get("tb_user_email_userUUID_uindex").getColumns().get(1).equals("userUUID");
        assert indexMap.get("tb_user_email_userUUID_uindex").getIndexEnum() == MySqlIndexType.Unique;
        assert indexMap.get("normal_index_tb_user").getColumns().size() == 2;
        assert indexMap.get("normal_index_tb_user").getColumns().get(0).equals("loginPassword");
        assert indexMap.get("normal_index_tb_user").getColumns().get(1).equals("loginName");
        assert indexMap.get("normal_index_tb_user").getIndexEnum() == MySqlIndexType.Normal;
    }

    @Test
    public void getIndexes2() throws SQLException {
        List<MySqlIndex> indexList = this.repository.getIndexes(MYSQL_SCHEMA_NAME, "proc_table_ref");
        Map<String, MySqlIndex> indexMap = indexList.stream().collect(Collectors.toMap(MySqlIndex::getName, i -> i));
        assert indexMap.size() == 4;
        assert indexMap.containsKey("PRIMARY");
        assert indexMap.containsKey("proc_table_ref_uk");
        assert indexMap.containsKey("proc_table_ref_index");
        assert indexMap.containsKey("ptr");
        assert indexMap.get("PRIMARY").getColumns().size() == 1;
        assert indexMap.get("PRIMARY").getColumns().get(0).equals("r_int");
        assert indexMap.get("PRIMARY").getIndexEnum() == MySqlIndexType.Primary;
        assert indexMap.get("proc_table_ref_uk").getColumns().size() == 1;
        assert indexMap.get("proc_table_ref_uk").getColumns().get(0).equals("r_name");
        assert indexMap.get("proc_table_ref_uk").getIndexEnum() == MySqlIndexType.Unique;
        assert indexMap.get("proc_table_ref_index").getColumns().size() == 1;
        assert indexMap.get("proc_table_ref_index").getColumns().get(0).equals("r_index");
        assert indexMap.get("proc_table_ref_index").getIndexEnum() == MySqlIndexType.Normal;
        assert indexMap.get("ptr").getColumns().size() == 2;
        assert indexMap.get("ptr").getColumns().get(0).equals("r_k1");
        assert indexMap.get("ptr").getColumns().get(1).equals("r_k2");
        assert indexMap.get("ptr").getIndexEnum() == MySqlIndexType.Foreign;
    }

    @Test
    public void getIndexes3() throws SQLException {
        List<MySqlIndex> indexList = this.repository.getIndexes(MYSQL_SCHEMA_NAME, "proc_table_ref", MySqlIndexType.Normal, MySqlIndexType.Unique);
        Map<String, MySqlIndex> indexMap = indexList.stream().collect(Collectors.toMap(MySqlIndex::getName, i -> i));
        assert indexMap.size() == 2;
        assert indexMap.containsKey("proc_table_ref_uk");
        assert indexMap.containsKey("proc_table_ref_index");
        assert indexMap.get("proc_table_ref_uk").getColumns().size() == 1;
        assert indexMap.get("proc_table_ref_uk").getColumns().get(0).equals("r_name");
        assert indexMap.get("proc_table_ref_uk").getIndexEnum() == MySqlIndexType.Unique;
        assert indexMap.get("proc_table_ref_index").getColumns().size() == 1;
        assert indexMap.get("proc_table_ref_index").getColumns().get(0).equals("r_index");
        assert indexMap.get("proc_table_ref_index").getIndexEnum() == MySqlIndexType.Normal;
    }

    @Test
    public void getIndexes4() throws SQLException {
        MySqlIndex index = this.repository.getIndexes(MYSQL_SCHEMA_NAME, "proc_table_ref", "ptr");
        assert index.getName().equals("ptr");
        assert index.getColumns().size() == 2;
        assert index.getColumns().get(0).equals("r_k1");
        assert index.getColumns().get(1).equals("r_k2");
        assert index.getIndexEnum() == MySqlIndexType.Foreign;
    }
}