import com.pojo.People;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>project: untitled
 * <p>ClassName: SimpleTest
 * <p>Description:
 * <p>Date: 2021/12/6 21:50
 *
 * @author : Zhao Li
 */
@Slf4j
public class SimpleTest {

    /**
     * 演示原生JDBC
     *
     * @throws SQLException exception
     */
    @Test
    public void test01() throws SQLException {
        //注册驱动
        DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
        //建立连接
        @Cleanup Connection connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/test_database", "root", "password");
        //编写sql
        String sql = "SELECT * FROM people LIMIT 100";
        //预编译sql
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        //获取执行结果
        preparedStatement.execute();
        ResultSet rs = preparedStatement.getResultSet();
        while (rs.next()) {
            log.info("people ==> id:【{}】 name:【{}】 age:【{}】", rs.getInt(1), rs.getString(2), rs.getInt(3));
        }
    }

    /**
     * 演示使用MyBatis
     */
    @Test
    public void test02() throws IOException {
        //读取配置文件
        @Cleanup InputStream in = Resources.getResourceAsStream("SqlMapConfig.xml");
        //构建SqlSession工厂
        SqlSessionFactoryBuilder factoryBuilder = new SqlSessionFactoryBuilder();
        SqlSessionFactory build = factoryBuilder.build(in);
        //工厂生产SqlSession对象
        @Cleanup SqlSession sqlSession = build.openSession();
        People people = sqlSession.selectOne("com.mapper.PeopleMapper.selectByPrimaryKey", 1);
        log.info(people.toString());
    }
}
