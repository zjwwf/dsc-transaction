package com.zhuo.transaction.repository;

import com.zhuo.transaction.Transaction;
import com.zhuo.transaction.common.exception.TransactionException;
import com.zhuo.transaction.common.utils.DateUtils;
import com.zhuo.transaction.serializer.KryoPoolSerializer;
import com.zhuo.transaction.serializer.ObjectSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * describe: 事务消息 数据库存储
 *
 * @author zhuojing
 * @date 2020/08/27
 */
public class JdbcTransactionRepository extends AbstractCachableTransactionRepositor {

    private static Logger logger = LoggerFactory.getLogger(JdbcTransactionRepository.class);
    private DataSource dataSource;

    private String tableName = "dsc_transaction";
    private ObjectSerializer<Object> serializer = null;

    public JdbcTransactionRepository(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public JdbcTransactionRepository(DataSource dataSource,String tableName){
        this.dataSource = dataSource;
        this.tableName = tableName;
    }

    @Override
    public void init() {
        serializer = new KryoPoolSerializer();
        String createTableSql = "Create Table If Not Exists `dsc_transaction` (\n" +
                "  `id` varchar(100) NOT NULL,\n" +
                "  `body` text,\n" +
                "  `try_time` int(2) DEFAULT '0' COMMENT '重试次数',\n" +
                "  `status` tinyint(2) DEFAULT NULL COMMENT '1：未结束，2：已结束，3：出现异常',\n" +
                "  `cancal_method` varchar(255) DEFAULT NULL,\n" +
                "  `cancal_method_param` blob,\n" +
                "  `confirm_method` varchar(255) DEFAULT NULL,\n" +
                "  `confirm_method_param` blob,\n" +
                "  `transaction_type` int(11) DEFAULT NULL,\n" +
                "  `create_time` datetime DEFAULT NULL,\n" +
                "  `update_time` datetime DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  KEY `index_status_update_time` (`status`,`update_time`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n";
        Connection  connection = null;
        PreparedStatement stmt = null;
        try {
            connection = getConnection();
            stmt = connection.prepareStatement(createTableSql);
            stmt.execute();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("create transaction msg table fail");
        }finally {
            closeStatement(stmt);
            releaseConnection(connection);
        }
    }
    @Override
    protected void doCreate(Transaction transaction) {
        String sql = "INSERT INTO dsc_transaction(id,body,try_time,`status`,cancal_method,cancal_method_param,confirm_method,confirm_method_param,transaction_type,create_time,update_time) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        Connection  connection = null;
        PreparedStatement stmt = null;
        try {
            connection = getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1,transaction.getId());
            stmt.setString(2,transaction.getBody());
            stmt.setInt(3,transaction.getTryTime() == null ? 0 : transaction.getTryTime());
            stmt.setInt(4,transaction.getStatus());
            stmt.setString(5,transaction.getCancalMethod());
            stmt.setBytes(6,transaction.getCancalMethodParam() == null ? null : serializer.serialize(transaction.getCancalMethodParam()));
            stmt.setString(7,transaction.getConfirmMethod());
            stmt.setBytes(8,transaction.getConfirmMethodParam() == null ? null : serializer.serialize(transaction.getConfirmMethodParam()));
            stmt.setInt(9,transaction.getTransactionType());
            stmt.setTimestamp(10,new java.sql.Timestamp(transaction.getCreateTime().getTime()));
            stmt.setTimestamp(11,new java.sql.Timestamp(transaction.getUpdateTime().getTime()));
            stmt.execute();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JdbcTransactionRepository create transaction error,"+e.getMessage());
        }finally {
            closeStatement(stmt);
            releaseConnection(connection);
        }
    }

    @Override
    protected void doUpdateStatus(String transactionId,int statusCode) {
        String sql = "UPDATE dsc_transaction SET `status` = ?,update_time=? WHERE id = ?";
        Connection  connection = null;
        PreparedStatement stmt = null;
        try {
            connection = getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1,statusCode);
            stmt.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setString(3,transactionId);
            stmt.executeUpdate();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JdbcTransactionRepository doUpdateStatus error,"+e.getMessage());
        }finally {
            closeStatement(stmt);
            releaseConnection(connection);
        }
    }

    @Override
    protected void doDelete(String transactionId) {
        String sql = "DELETE FROM dsc_transaction WHERE id = ?";
        Connection  connection = null;
        PreparedStatement stmt = null;
        try {
            connection = getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1,transactionId);
            stmt.executeUpdate();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JdbcTransactionRepository doDelete error,"+e.getMessage());
        }finally {
            closeStatement(stmt);
            releaseConnection(connection);
        }
    }

    @Override
    protected void doAddTryTime(String transactionId) {
        String sql = "UPDATE dsc_transaction SET try_time = try_time+1,update_time= ? WHERE id = ? ";
        Connection  connection = null;
        PreparedStatement stmt = null;
        try {
            connection = getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setTimestamp(1,new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setString(2,transactionId);
            stmt.executeUpdate();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new TransactionException("JdbcTransactionRepository doAddTryTime error,"+e.getMessage());
        }finally {
            closeStatement(stmt);
            releaseConnection(connection);
        }
    }

    @Override
    protected Transaction doGetById(String transactionId) {
        String sql = "SELECT "+getColumns()+" FROM dsc_transaction  WHERE id = ? ";
        Connection  connection = null;
        PreparedStatement stmt = null;
        try {
            connection = getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1,transactionId);
            ResultSet resultSet = stmt.executeQuery();
            if(resultSet.next()){

                return buildTransaction(resultSet);
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }finally {
            closeStatement(stmt);
            releaseConnection(connection);
        }
        return null;
    }

    @Override
    protected List<Transaction> doGetFailTranMsgList() {
        String sql = "SELECT "+getColumns()+" FROM dsc_transaction WHERE `status` = 3 AND update_time < ? " +
                "and cancal_method is not null and  cancal_method != '' ORDER BY update_time  LIMIT "+super.queryListNum;
        Connection  connection = null;
        PreparedStatement stmt = null;
        List<Transaction> result = new ArrayList<>();
        try {
            connection = getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setTimestamp(1, new java.sql.Timestamp(DateUtils.getPreHourDate(1).getTime()));
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()){
                Transaction transaction = buildTransaction(resultSet);
                result.add(transaction);

            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }finally {
            closeStatement(stmt);
            releaseConnection(connection);
        }
        return result;
    }

    @Override
    protected boolean doexist(String transactionId) {
        Transaction transaction = getById(transactionId);
        if(transaction != null){
            return true;
        }
        return false;
    }

    private Transaction buildTransaction(ResultSet resultSet) throws SQLException{
        Transaction tc = new Transaction();
        tc.setId(resultSet.getString("id"));
        tc.setBody(resultSet.getString("body"));
        tc.setStatus(resultSet.getInt("status"));
        tc.setTryTime(resultSet.getInt("try_time"));
        tc.setCancalMethod(resultSet.getString("cancal_method"));
        tc.setConfirmMethod(resultSet.getString("confirm_method"));
        byte[] cancalMethodParams = resultSet.getBytes("cancal_method_param");
        if(cancalMethodParams != null && cancalMethodParams.length > 0) {
            try {
                tc.setCancalMethodParam((Object[]) serializer.deserialize(cancalMethodParams));
            }catch (Exception e){
                logger.error("serializer fail，"+e.getMessage());
            }
        }
        tc.setTransactionType(resultSet.getInt("transaction_type"));
        byte[] confirmMethodParams = resultSet.getBytes("confirm_method_param");
        if(confirmMethodParams != null && confirmMethodParams.length > 0) {

            try {
                tc.setConfirmMethodParam((Object[]) serializer.deserialize(confirmMethodParams));
            }catch (Exception e){
                logger.error("serializer fail，"+e.getMessage());
            }
        }
        tc.setCreateTime(resultSet.getDate("create_time"));
        tc.setUpdateTime(resultSet.getDate("update_time"));
        return tc;
    }

    private Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }

    private void releaseConnection(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }

    private void closeStatement(Statement stmt) {
        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        } catch (Exception ex) {
            throw new TransactionException(ex);
        }
    }


    private String getColumns(){
        return "`id`,`body`,`try_time`,`status`,`cancal_method`,`cancal_method_param`,`confirm_method`,`confirm_method_param`,`transaction_type`,`create_time`,`update_time`";
    }



}
