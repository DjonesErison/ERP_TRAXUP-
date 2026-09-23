package com.traxup.tplug.erp.tenant;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.sql.DataSource;
import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

/** Each test migrates an isolated schema, including pre-existing UUID tenants. */
@SpringBootTest
class TenantCodigoEmpresaIntegrationTest {
    @Autowired DataSource dataSource;
    String schema;
    UUID original;
    Connection connection() throws SQLException {
        var c=dataSource.getConnection();
        String originalSchema=c.getSchema();
        c.setSchema(schema);
        return (Connection)java.lang.reflect.Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, (proxy,method,args)->{
            if(method.getName().equals("close")) {
                try {c.setSchema(originalSchema);} finally {c.close();}
                return null;
            }
            try {return method.invoke(c,args);} catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}
        });
    }
    void script(Connection c, String name) throws Exception {
        try(var in=getClass().getResourceAsStream("/db/migration/"+name);var s=c.createStatement()) {
            s.execute(new String(in.readAllBytes(),StandardCharsets.UTF_8));
        }
    }
    String allocate(Connection c) throws SQLException {
        try(var s=c.createStatement();var rs=s.executeQuery("SELECT gerar_codigo_empresa()")) {rs.next();return rs.getString(1);}
    }
    String insert(Connection c) throws SQLException {
        try(var s=c.prepareStatement("INSERT INTO tenants(id,nome) VALUES (?, 'Teste') RETURNING codigo_empresa")) {
            s.setObject(1,UUID.randomUUID());try(var rs=s.executeQuery()){rs.next();return rs.getString(1);}
        }
    }
    @BeforeEach void migrate() throws Exception {
        schema="codigo_test_"+UUID.randomUUID().toString().replace("-", "");original=UUID.randomUUID();
        try(var c=dataSource.getConnection();var s=c.createStatement()) {s.execute("CREATE SCHEMA "+schema);}
        try(var c=connection()) {
            script(c,"V1__estrutura_inicial_tenant_empresa_filial.sql");
            try(var s=c.prepareStatement("INSERT INTO tenants(id,nome) VALUES (?, 'Anterior')")){s.setObject(1,original);s.executeUpdate();}
            script(c,"V109__tenant_codigo_empresa.sql");
        }
    }
    @AfterEach void cleanup() throws Exception {
        try(var c=dataSource.getConnection();var s=c.createStatement()) {s.execute("SET search_path TO public");s.execute("DROP SCHEMA "+schema+" CASCADE");}
    }
    @Test void migrationPreservaUuidPreencheZerosEIndexaCodigo() throws Exception {
        try(var c=connection();var s=c.createStatement();var rs=s.executeQuery("SELECT id,codigo_empresa FROM tenants")) {
            rs.next();assertThat(rs.getObject(1,UUID.class)).isEqualTo(original);assertThat(rs.getString(2)).isEqualTo("0000");
            assertThat(insert(c)).isEqualTo("0001");
            try(var index=s.executeQuery("SELECT count(*) FROM pg_indexes WHERE schemaname='"+schema+"' AND indexname='uk_tenants_codigo_empresa'")) {index.next();assertThat(index.getInt(1)).isEqualTo(1);}
        }
    }
    @Test void bancoRejeitaColisaoEFormatoInvalido() throws Exception {
        try(var c=connection()) {
            for(String code:List.of("0000","123","abcd","12345")) {
                assertThatThrownBy(()->{try(var s=c.prepareStatement("INSERT INTO tenants(id,nome,codigo_empresa) VALUES (?, 'Invalido', ?)")){s.setObject(1,UUID.randomUUID());s.setString(2,code);s.executeUpdate();}}).isInstanceOf(SQLException.class);
            }
        }
    }
    @Test void concorrenciaNaoProduzColisoes() throws Exception {
        try(var pool=Executors.newFixedThreadPool(6)) {
            var start=new CountDownLatch(1);var tasks=new ArrayList<Future<String>>();
            for(int i=0;i<24;i++) tasks.add(pool.submit(()->{start.await();try(var c=connection()){return insert(c);}}));
            start.countDown();var codes=new HashSet<String>();
            for(var task:tasks){String code=task.get(20,TimeUnit.SECONDS);assertThat(code).matches("[0-9]{4}");assertThat(codes.add(code)).isTrue();}
            assertThat(codes).hasSize(24);
        }
    }
    @Test void rollbackNaoConsomeCodigoELimiteNaoRecicla() throws Exception {
        try(var c=connection()) {
            c.setAutoCommit(false);assertThat(allocate(c)).isEqualTo("0001");c.rollback();c.setAutoCommit(true);
            assertThat(allocate(c)).isEqualTo("0001");
            try(var s=c.createStatement()){s.executeUpdate("UPDATE tenant_codigo_empresa_allocator SET proximo=9999");}
            assertThat(allocate(c)).isEqualTo("9999");
            assertThatThrownBy(()->allocate(c)).isInstanceOfSatisfying(SQLException.class,e->assertThat(e.getSQLState()).isEqualTo("54000"));
        }
    }
}
