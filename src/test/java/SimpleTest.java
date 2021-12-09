import com.pojo.People;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

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
        log.info("==> {}", people.toString());
    }

    /**
     * 自定义函数接口，在方法声明中抛出异常，免掉在函数定义中处理异常的痛苦
     *
     * @param <T> 函数入参
     * @param <R> 函数出参
     */
    @FunctionalInterface
    interface CheckedFunction<T, R> {
        R apply(T t) throws Throwable;
    }

    /**
     * 测试时间对象，发现mac系统小坑：24小时制，中午12点后显示是12 pm，凌晨0点后显示是12 am
     */
    @SneakyThrows
    @Test
    public void test03() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //获取文件的创建时间
        CheckedFunction<File, Date> getCreateDateTime = file -> {
            FileTime creationTime = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
            return Date.from(creationTime.toInstant());
        };
        //将时间转换成对应格式的字符串
        Function<Date, String> dateStringFunction = date -> dateTimeFormatter.format(date.toInstant()
                .atZone(ZoneId.systemDefault()));
        // File file = new File("/Users/lizhao/Downloads/helloWorld.js");
        File file = new File("/Users/lizhao/Downloads/必应/蓝山公园的萤火虫-澳大利亚.jpg");
        Date date = getCreateDateTime.apply(file);
        System.out.println(format.format(date));
        System.out.println(dateStringFunction.apply(date));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime minusHours = localDateTime.minusHours(2L);
        System.out.println(dateTimeFormatter.format(minusHours));
    }
}
