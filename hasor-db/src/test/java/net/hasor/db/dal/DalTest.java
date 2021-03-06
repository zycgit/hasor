package net.hasor.db.dal;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.lambda.LambdaOperations;
import net.hasor.db.lambda.LambdaTemplate;
import net.hasor.db.mapping.MappingRegistry;
import net.hasor.db.metadata.MetaDataService;
import net.hasor.db.metadata.provider.JdbcMetadataProvider;
import net.hasor.db.types.TypeHandlerRegistry;
import net.hasor.test.db.SingleDsModule;
import net.hasor.test.db.dto.TbUser;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DalTest {
    @Test
    public void test2() throws SQLException {
        System.out.println(UUID.randomUUID().toString());
    }

    @Test
    public void test() throws SQLException {
        try (AppContext appContext = Hasor.create().build(new SingleDsModule(true))) {
            // 数据源
            DataSource dataSource = appContext.getInstance(DataSource.class);
            // 元信息服务（可选）
            MetaDataService metaDataService = new JdbcMetadataProvider(dataSource);
            // 类型处理器（可选）
            TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
            // ORM注册管理（可选）
            MappingRegistry mappingRegistry = new MappingRegistry(typeHandlerRegistry, metaDataService);
            //
            // 两个相关的数据操作接口
            LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource, mappingRegistry);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource, mappingRegistry);
            // Lambda 操作
            LambdaOperations.LambdaQuery<TbUser> lambdaQuery = lambdaTemplate.lambdaQuery(TbUser.class);
            List<TbUser> tbUsers = lambdaQuery.queryForList();
            assert tbUsers.size() == 3;
        }
    }
}
