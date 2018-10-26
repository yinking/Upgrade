package dao.Upgrade.dao;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;

import dao.Upgrade.model.UpgradeTransaction;
import daogenerator.services.interfaces.Callback;
import daogenerator.utils.SqlConstructUtil;
import daogenerator.utils.StringsBuildUtil;
import daogenerator.utils.connectionPool.ConnectionPool;
import daogenerator.utils.connectionPool.DBConnection;

public class UpgradeTransactionDAO {
    static ConnectionPool pool = ConnectionPool.getInstance();
    static UpgradeTransactionDAO instance = null;

    public static UpgradeTransactionDAO getInstance() {
        if (null == instance) {
            instance = new UpgradeTransactionDAO("upgrade");
        }
        return instance;
    }

    public static UpgradeTransactionDAO getNewInstance(String dbName) {
        return new UpgradeTransactionDAO(dbName);
    }

    private final String dbName;

    private UpgradeTransactionDAO(String dbName) {
        this.dbName = dbName;
    }

    public void truncate() {
        String sql = String.format("TRUNCATE %s.Transaction", dbName);
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    public int insertReturnId(UpgradeTransaction obj) {
        String sql = String.format("INSERT INTO %s.Transaction(id,fromAccountId,toAccountId,amount,time) VALUES ", dbName);

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            _constructPS(ps, obj, 0);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
            else
                return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
        return -1;
    }

    public void insert(UpgradeTransaction obj) {
        String sql = String.format("INSERT INTO %s.Transaction(id,fromAccountId,toAccountId,amount,time) VALUES (?,?,?,?,?)", dbName);

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            _constructPS(ps, obj, 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    /**
     * @deprecated using insertMultiple instead
     */
    @Deprecated
    public void insertBatch(List<UpgradeTransaction> objList) {
        String sql = String.format("INSERT INTO %s.Transaction(id,fromAccountId,toAccountId,amount,time) VALUES (?,?,?,?,?)", dbName);

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            for (UpgradeTransaction obj : objList) {
                _constructPS(ps, obj, 0);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    public void insertMultiple(List<UpgradeTransaction> objList) {
        if (objList.size() == 0)
            return;
        StringBuilder sqlBuilder = new StringBuilder(String.format("INSERT INTO %s.Transaction(id,fromAccountId,toAccountId,amount,time) VALUES ", dbName));
        for (int i = 0; i < objList.size(); i++) {
            if (i != 0)
                sqlBuilder.append(",");
            sqlBuilder.append("(?,?,?,?,?)");
        }
        String sql = sqlBuilder.toString();
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            int indexCount = 0;
            ps = conn.prepareStatement(sql);
            for (UpgradeTransaction obj : objList) {
                indexCount = _constructPS(ps, obj, indexCount);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    public void insertMultipleBatch(List<UpgradeTransaction> objList, int multipleSize) {
        if (objList.size() == 0)
            return;
        String sqlHead = String.format("INSERT INTO %s.Transaction(id,fromAccountId,toAccountId,amount,time) VALUES ", dbName);
        StringBuilder sqlBuilder = new StringBuilder(sqlHead);
        for (int i = 0; i < multipleSize; i++) {
            if (i != 0)
                sqlBuilder.append(",");
            sqlBuilder.append("(?,?,?,?,?)");
        }
        String sql = sqlBuilder.toString();
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        int startIndex = 0;
        try {
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            while (startIndex + multipleSize <= objList.size()) {
                int indexCount = 0;
                for (UpgradeTransaction obj : objList.subList(startIndex, startIndex + multipleSize)) {
                    indexCount = _constructPS(ps, obj, indexCount);
                }
                ps.addBatch();
                startIndex += multipleSize;
            }
            ps.executeBatch();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
        if (startIndex < objList.size()) {
            insertMultiple(objList.subList(startIndex, objList.size()));
        }
    }

    /**
     * @deprecated using insertMultipleWithLimitedWindow instead
     */
    @Deprecated
    public void insertBatchWithLimitedWindow(List<UpgradeTransaction> objList, int windowSize) {
        int startIndex = 0;
        int displayCounter = 0;
        while (startIndex < objList.size()) {
            insertBatch(objList.subList(startIndex, Math.min(startIndex + windowSize, objList.size())));
            startIndex += windowSize;
            if (++displayCounter % 20 == 0)
                System.out.println(String.format("[inserting UpgradeTransaction] %d / %d", startIndex, objList.size()));
        }
    }

    @Deprecated
    public void insertMultipleWithLimitedWindow(List<UpgradeTransaction> objList, int windowSize) {
        int startIndex = 0;
        int displayCounter = 0;
        while (startIndex < objList.size()) {
            insertMultiple(objList.subList(startIndex, Math.min(startIndex + windowSize, objList.size())));
            startIndex += windowSize;
            if (++displayCounter % 20 == 0)
                System.out.println(String.format("[inserting UpgradeTransaction] %d / %d", startIndex, objList.size()));
        }
    }

    public void insertMultipleBatchWithLimitedWindow(List<UpgradeTransaction> objList, int batchWindowSize, int batchMount) {
        int startIndex = 0;
        long t0 = System.currentTimeMillis();
        while (startIndex < objList.size()) {
            insertMultipleBatch(objList.subList(startIndex, Math.min(startIndex + batchMount * batchWindowSize, objList.size())), batchWindowSize);
            startIndex += batchMount * batchWindowSize;
            System.out.println(String.format("[inserting UpgradeTransaction] %d / %d, cost %d ms, total estimation %d ms", startIndex, objList.size(), //
                    (System.currentTimeMillis() - t0), (System.currentTimeMillis() - t0) * objList.size() / startIndex));
        }
    }

    /**
     * update all field anchor by field <b>id</b>
     */
    public void update(UpgradeTransaction obj) {
        String sql = String.format("UPDATE %s.Transaction SET id = ?,fromAccountId = ?,toAccountId = ?,amount = ?,time = ? WHERE id = ?", dbName);
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql.toString());
            int indexCount = _constructPS(ps, obj, 0);
            ps.setInt(++indexCount, obj.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    /**
     * batch update all field anchor by each elements' field <b>id</b>
     */
    public void updateBatch(List<UpgradeTransaction> objList) {
        String sql = String.format("UPDATE %s.Transaction SET id = ?,fromAccountId = ?,toAccountId = ?,amount = ?,time = ? WHERE id = ?", dbName);

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            int count = 0;
            for (UpgradeTransaction obj : objList) {
                if (++count % 5000 == 0)
                    System.out.println(String.format("[batch updating UpgradeTransaction] %d / %d", count, objList.size()));
                int indexCount = _constructPS(ps, obj, 0);
                ps.setInt(++indexCount, obj.getId());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    /**
     * Batch(<b><i>i</i></b> ):
     * UPDATE Transaction SET <b>setField</b> = <b>setToValueList[<i>i</i> ]</b> WHERE <b>whereField</b> IN
     * (<b>whereInValueList[<i>i</i> ][0 : length]</b>)
     */
    public void updateBatchWithIntegerFieldSelection(String whereField, List<List<Integer>> whereInValueList, //
                                                     String setField, List<Integer> setToValueList) {
        if (null == whereInValueList || whereInValueList.size() == 0) {
            return;
        } else if (whereInValueList.size() != setToValueList.size()) {
            new Exception("Mismatch parameter size").printStackTrace();
            return;
        }

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            conn.setAutoCommit(false);

            for (int i = 0; i < whereInValueList.size(); i++) {
                int whereParamCount = whereInValueList.size();
                StringBuilder sqlBuilder = new StringBuilder( //
                        String.format("UPDATE %s.Transaction SET %s = ? WHERE %s IN (", dbName, setField, whereField));
                for (int j = 0; j < whereParamCount; j++) {
                    sqlBuilder.append('?').append(',');
                }
                sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

                String sql = sqlBuilder.toString();
                ps = conn.prepareStatement(sql);

                SqlConstructUtil.__safeSetInt(ps, 1, setToValueList.get(i));
                for (int j = 0; j < whereParamCount; j++) {
                    SqlConstructUtil.__safeSetInt(ps, j + 2, whereInValueList.get(i).get(j));
                }
                ps.addBatch();
            }
            ps.executeBatch();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    /**
     * UPDATE Transaction SET <b>setField</b> = <b>setToValue</b> WHERE <b>whereField</b> IN (<b>whereInValue[0 : length]</b>)
     */
    public void updateWithIntegerFieldSelection(String whereField, List<Integer> whereInValue, String setField, Integer setToValue) {
        if (null == whereInValue || whereInValue.size() == 0) {
            return;
        }

        StringBuilder sqlBuilder = new StringBuilder( //
                String.format("UPDATE %s.Transaction SET %s = ? WHERE %s IN (", dbName, setField, whereField));
        for (int i = 0; i < whereInValue.size(); i++) {
            sqlBuilder.append('?').append(',');
        }
        sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

        String sql = sqlBuilder.toString();

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            SqlConstructUtil.__safeSetInt(ps, 1, setToValue);
            for (int i = 0; i < whereInValue.size(); i++) {
                SqlConstructUtil.__safeSetInt(ps, i + 2, whereInValue.get(i));
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    /**
     * Batch(<b><i>i</i></b> ):
     * UPDATE Transaction SET <b>setField</b> = <b>setToValueList[<i>i</i> ]</b> WHERE <b>whereField</b> IN
     * (<b>whereInValueList[<i>i</i> ][0 : length]</b>)
     */
    public void updateBatchWithStringFieldSelection(String whereField, List<List<String>> whereInValueList, //
                                                    String setField, List<String> setToValueList) {
        if (null == whereInValueList || whereInValueList.size() == 0) {
            return;
        } else if (whereInValueList.size() != setToValueList.size()) {
            new Exception("Mismatch parameter size").printStackTrace();
            return;
        }

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            conn.setAutoCommit(false);

            for (int i = 0; i < whereInValueList.size(); i++) {
                int whereParamCount = whereInValueList.size();
                StringBuilder sqlBuilder = new StringBuilder( //
                        String.format("UPDATE %s.Transaction SET %s = ? WHERE %s IN (", dbName, setField, whereField));
                for (int j = 0; j < whereParamCount; j++) {
                    sqlBuilder.append('?').append(',');
                }
                sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

                String sql = sqlBuilder.toString();
                ps = conn.prepareStatement(sql);

                SqlConstructUtil.__safeSetString(ps, 1, setToValueList.get(i));
                for (int j = 0; j < whereParamCount; j++) {
                    SqlConstructUtil.__safeSetString(ps, j + 2, whereInValueList.get(i).get(j));
                }
                ps.addBatch();
            }
            ps.executeBatch();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    /**
     * UPDATE Transaction SET <b>setField</b> = <b>setToValue</b> WHERE <b>whereField</b> IN (<b>whereInValue[0 : length]</b>)
     */
    public void updateWithStringFieldSelection(String whereField, List<String> whereInValue, String setField, String setToValue) {
        if (null == whereInValue || whereInValue.size() == 0) {
            return;
        }

        StringBuilder sqlBuilder = new StringBuilder( //
                String.format("UPDATE %s.Transaction SET %s = ? WHERE %s IN (", dbName, setField, whereField));
        for (int i = 0; i < whereInValue.size(); i++) {
            sqlBuilder.append('?').append(',');
        }
        sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

        String sql = sqlBuilder.toString();

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            SqlConstructUtil.__safeSetString(ps, 1, setToValue);
            for (int i = 0; i < whereInValue.size(); i++) {
                SqlConstructUtil.__safeSetString(ps, i + 2, whereInValue.get(i));
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    public void delete(Integer id) {
        String sql = String.format("DELETE FROM %s.Transaction WHERE id = ?", dbName);

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql.toString());
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    @Deprecated
    public void deleteBatch(List<Integer> idList) {
        String sql = String.format("DELETE FROM %s.Transaction WHERE id = ?", dbName);

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            int count = 0;
            for (Integer id : idList) {
                if (++count % 5000 == 0)
                    System.out.println(String.format("[batch deleting UpgradeTransaction] %d / %d", count, idList.size()));
                ps.setInt(1, id);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    public void deleteMultipleById(List<Integer> idList) {
        if (null == idList || idList.size() == 0)
            return;

        StringBuilder sqlBuilder = new StringBuilder("DELETE FROM %s.Transaction WHERE id IN (");
        for (int i = 0; i < idList.size(); i++) {
            sqlBuilder.append('?').append(',');
        }
        sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

        String sql = String.format(sqlBuilder.toString(), dbName);

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < idList.size(); i++) {
                ps.setInt(i + 1, idList.get(i));
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
    }

    public void deleteMultipleBatchById(List<Integer> idList, int multipleSize) {
        if (idList.size() == 0)
            return;
        StringBuilder sqlBuilder = new StringBuilder("DELETE FROM %s.Transaction WHERE id IN (");
        for (int i = 0; i < multipleSize; i++) {
            sqlBuilder.append('?').append(',');
        }
        sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

        String sql = String.format(sqlBuilder.toString(), dbName);
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        int startIndex = 0;
        try {
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            while (startIndex + multipleSize <= idList.size()) {
                List<Integer> subList = idList.subList(startIndex, startIndex + multipleSize);
                for (int i = 0; i < subList.size(); i++) {
                    ps.setInt(i + 1, subList.get(i));
                }
                ps.addBatch();
                startIndex += multipleSize;
            }
            ps.executeBatch();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps);
        }
        if (startIndex < idList.size()) {
            deleteMultipleById(idList.subList(startIndex, idList.size()));
        }
    }

    public void deleteMultipleBatchByIdWithLimitedWindow(List<Integer> idList, int batchWindowSize, int batchMount) {
        int startIndex = 0;
        long t0 = System.currentTimeMillis();
        while (startIndex < idList.size()) {
            deleteMultipleBatchById(idList.subList(startIndex, Math.min(startIndex + batchMount * batchWindowSize, idList.size())), batchWindowSize);
            startIndex += batchMount * batchWindowSize;
            System.out.println(String.format("[deleting UpgradeTransaction] %d / %d, cost %d ms, total estimation %d ms", startIndex, idList.size(), //
                    (System.currentTimeMillis() - t0), (System.currentTimeMillis() - t0) * idList.size() / startIndex));
        }
    }

    public Integer selectMaxId() {
        Integer result = null;
        String sql = String.format("SELECT MAX(id) FROM %s.Transaction", dbName);
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = rs.getInt("MAX(id)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public UpgradeTransaction selectById(Integer id) {
        if (null == id) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s.Transaction WHERE id = ?", dbName);
        UpgradeTransaction result = null;
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = _constructResult(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> selectByIdList(List<Integer> idList) {
        if (null == idList) {
            return null;
        } else if (idList.size() == 0) {
            return new ArrayList<UpgradeTransaction>();
        }

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM %s.Transaction WHERE id IN (");
        for (int i = 0; i < idList.size(); i++) {
            sqlBuilder.append('?').append(',');
        }
        sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

        String sql = String.format(sqlBuilder.toString(), dbName);

        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < idList.size(); i++) {
                ps.setInt(i + 1, idList.get(i));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(_constructResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> selectByIdListWithLimitedWindow(List<Integer> idList, Integer windowSize) {
        if (null == idList)
            return null;

        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        int paramterListStartIndex = 0;
        while (paramterListStartIndex < idList.size()) {
            System.out.println(String.format("[Selecting Id]\t%d / %d", paramterListStartIndex, idList.size()));
            List<Integer> partialIdList = new ArrayList<Integer>();
            int subListEnd = Math.min(paramterListStartIndex + windowSize, idList.size());
            partialIdList.addAll(idList.subList(paramterListStartIndex, subListEnd));
            result.addAll(selectByIdList(partialIdList));

            partialIdList.clear();
            paramterListStartIndex += windowSize;
        }

        return result;
    }

    public UpgradeTransaction selectSingleByStringField(String field, String value) {
        if (field == null || "".equals(field.trim())) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s.Transaction WHERE %s = ? LIMIT 1", dbName, field);
        UpgradeTransaction result = null;
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            SqlConstructUtil.__safeSetString(ps, 1, value);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = _constructResult(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> selectByStringField(String field, String value) {
        if (field == null || "".equals(field.trim())) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s.Transaction WHERE %s = ?", dbName, field);
        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            SqlConstructUtil.__safeSetString(ps, 1, value);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(_constructResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> fetchTenTransaction(String field1, String field2, String value) {
        if (field1 == null || "".equals(field1.trim()) || field2 == null || "".equals(field2.trim())) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s.Transaction WHERE %s = ? or %s = ? order by Id DESC LIMIT 10", dbName, field1, field2);
        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            SqlConstructUtil.__safeSetString(ps, 1, value);
            SqlConstructUtil.__safeSetString(ps, 2, value);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(_constructResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> selectByMultipleStringField(String field, List<String> valueList) {
        if (null == valueList) {
            return null;
        } else if (valueList.size() == 0) {
            return new ArrayList<UpgradeTransaction>();
        }

        StringBuilder sqlBuilder = new StringBuilder(String.format("SELECT * FROM %s.Transaction WHERE %s IN (", dbName, field));
        for (int i = 0; i < valueList.size(); i++) {
            sqlBuilder.append('?').append(',');
        }
        sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

        String sql = sqlBuilder.toString();

        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < valueList.size(); i++) {
                SqlConstructUtil.__safeSetString(ps, i + 1, valueList.get(i));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(_constructResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> selectByMultipleStringField(String field, String[] valueList) {
        if (null == valueList) {
            return null;
        } else if (valueList.length == 0) {
            return new ArrayList<UpgradeTransaction>();
        }

        StringBuilder sqlBuilder = new StringBuilder(String.format("SELECT * FROM %s.abc_photo WHERE %s IN (", dbName, field));
        for (int i = 0; i < valueList.length; i++) {
            sqlBuilder.append('?').append(',');
        }
        sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

        String sql = sqlBuilder.toString();

        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < valueList.length; i++) {
                SqlConstructUtil.__safeSetString(ps, i + 1, valueList[i]);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(_constructResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> selectByMultipleStringFieldWithLimitedWindow(String field, List<String> valueList, Integer windowSize) {
        if (null == valueList)
            return null;

        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        int paramterListStartIndex = 0;
        while (paramterListStartIndex < valueList.size()) {
            System.out.println(String.format("[Selecting String Field \"%s\"]\t%d / %d", field, paramterListStartIndex, valueList.size()));
            List<String> partialIdList = new ArrayList<String>();
            int subListEnd = Math.min(paramterListStartIndex + windowSize, valueList.size());
            partialIdList.addAll(valueList.subList(paramterListStartIndex, subListEnd));
            result.addAll(selectByMultipleStringField(field, partialIdList));

            partialIdList.clear();
            paramterListStartIndex += windowSize;
        }

        return result;
    }

    public UpgradeTransaction selectSingleByIntegerField(String field, Integer value) {
        if (field == null || "".equals(field.trim())) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s.Transaction WHERE %s = ? LIMIT 1", dbName, field);
        UpgradeTransaction result = null;
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            SqlConstructUtil.__safeSetInt(ps, 1, value);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = _constructResult(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> selectByIntegerField(String field, Integer value) {
        if (field == null || "".equals(field.trim())) {
            return null;
        }
        String sql = String.format("SELECT * FROM %s.Transaction WHERE %s = ?", dbName, field);
        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            SqlConstructUtil.__safeSetInt(ps, 1, value);
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(_constructResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> selectByMultipleIntegerField(String field, List<Integer> valueList) {
        if (null == valueList) {
            return null;
        } else if (valueList.size() == 0) {
            return new ArrayList<UpgradeTransaction>();
        }

        StringBuilder sqlBuilder = new StringBuilder(String.format("SELECT * FROM %s.Transaction WHERE %s IN (", dbName, field));
        for (int i = 0; i < valueList.size(); i++) {
            sqlBuilder.append('?').append(',');
        }
        sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

        String sql = sqlBuilder.toString();

        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < valueList.size(); i++) {
                SqlConstructUtil.__safeSetInt(ps, i + 1, valueList.get(i));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(_constructResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> selectByMultipleIntegerField(String field, Integer[] valueList) {
        if (null == valueList) {
            return null;
        } else if (valueList.length == 0) {
            return new ArrayList<UpgradeTransaction>();
        }

        StringBuilder sqlBuilder = new StringBuilder(String.format("SELECT * FROM %s.abc_photo WHERE %s IN (", dbName, field));
        for (int i = 0; i < valueList.length; i++) {
            sqlBuilder.append('?').append(',');
        }
        sqlBuilder.setCharAt(sqlBuilder.length() - 1, ')');

        String sql = sqlBuilder.toString();

        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < valueList.length; i++) {
                SqlConstructUtil.__safeSetInt(ps, i + 1, valueList[i]);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                result.add(_constructResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> selectByMultipleIntegerFieldWithLimitedWindow(String field, List<Integer> valueList, Integer windowSize) {
        if (null == valueList)
            return null;

        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        int paramterListStartIndex = 0;
        while (paramterListStartIndex < valueList.size()) {
            System.out.println(String.format("[Selecting Integer Field \"%s\"]\t%d / %d", field, paramterListStartIndex, valueList.size()));
            List<Integer> partialIdList = new ArrayList<Integer>();
            int subListEnd = Math.min(paramterListStartIndex + windowSize, valueList.size());
            partialIdList.addAll(valueList.subList(paramterListStartIndex, subListEnd));
            result.addAll(selectByMultipleIntegerField(field, partialIdList));

            partialIdList.clear();
            paramterListStartIndex += windowSize;
        }

        return result;
    }


    public List<UpgradeTransaction> walk(List<Field> fieldList, int start, int limit) {
        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();

        boolean defaultField = null == fieldList || fieldList.size() == 0;
        String fieldString = "*";
        if (!defaultField) {
            StringBuilder fieldStringBuilder = new StringBuilder();
            fieldStringBuilder.append("id, ");
            for (Field field : fieldList)
                fieldStringBuilder.append(StringsBuildUtil.escapeSystemKeyword(field.getName(), true)).append(", ");
            fieldStringBuilder.delete(fieldStringBuilder.length() - 2, fieldStringBuilder.length() - 1);
            fieldString = fieldStringBuilder.toString();
        }
        String sql = String.format("SELECT %s FROM %s.Transaction WHERE id >= ? ORDER BY id LIMIT ?", fieldString, dbName);

        DBConnection conn = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, start);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (defaultField)
                    result.add(_constructResult(rs));
                else {
                    UpgradeTransaction obj = SqlConstructUtil._constructResult(fieldList, UpgradeTransaction.class, rs);
                    obj.setId(rs.getInt("id"));
                    result.add(obj);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(conn, ps, rs);
        }
        return result;
    }

    public List<UpgradeTransaction> walk(int start, int limit) {
        return walk(null, start, limit);
    }

    public List<UpgradeTransaction> walkAll() {
        return walkAll(null, -1);
    }

    public List<UpgradeTransaction> walkAll(int walkstep) {
        return walkAll(null, walkstep);
    }

    /**
     * walk given size of data from DB.
     *
     * @param walkstep the number of step, -1 stands for all data, each step contains 500 lines.
     */

    public List<UpgradeTransaction> walkAll(List<Field> fieldList, int walkstep) {
        long t0 = System.currentTimeMillis();
        int stepCount = 0;
        List<UpgradeTransaction> result = new ArrayList<UpgradeTransaction>();
        int startid = 0;
        int limit = 500;
        while (true) {
            List<UpgradeTransaction> walk = walk(startid, limit);
            if (null == walk || walk.size() == 0)
                break;
            result.addAll(walk);
            startid = walk.get(walk.size() - 1).getId() + 1;
            stepCount++;
            if (stepCount % 20 == 0) {
                System.out.println(String.format("[Loading UpgradeTransaction] id:%d, timeUsed:%dms", startid, (System.currentTimeMillis() - t0)));
            }
            if (-1 != walkstep && stepCount >= walkstep) {
                break;
            }
        }
        return result;
    }

    public void fetchAll(Callback<UpgradeTransaction> callback) {
        fetchAll(callback, -1);
    }

    public void fetchAll(Callback<UpgradeTransaction> callback, int fetchstep) {
        fetchAll(callback, null, fetchstep);
    }

    public void fetchAll(Callback<UpgradeTransaction> callback, List<Field> fieldList) {
        fetchAll(callback, fieldList, -1);
    }

    public void fetchAll(Callback<UpgradeTransaction> callback, List<Field> fieldList, int fetchstep) {
        long t0 = System.currentTimeMillis();
        int stepCount = 0;
        int startid = 0;
        int limit = 500;
        while (true) {
            List<UpgradeTransaction> walk = walk(fieldList, startid, limit);
            if (null == walk || walk.size() == 0)
                break;

            callback.process(walk);

            startid = walk.get(walk.size() - 1).getId() + 1;
            stepCount++;
            if (stepCount % 20 == 0) {
                System.out.println(String.format("[Fetching UpgradeTransaction] id:%d, timeUsed:%dms", startid, (System.currentTimeMillis() - t0)));
            }
            if (-1 != fetchstep && stepCount >= fetchstep) {
                break;
            }
        }
    }

    public List<Field> _getFieldList(String[] fieldNameArray) throws NoSuchFieldException {
        List<Field> result = new ArrayList<Field>();
        try {
            for (String fieldName : fieldNameArray) {
                result.add(UpgradeTransaction.class.getDeclaredField(fieldName));
            }
        } catch (NoSuchFieldException e) {
            throw e;
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int _constructPS(PreparedStatement ps, UpgradeTransaction obj, int indexCount) throws SQLException {
        SqlConstructUtil.__safeSetInt(ps, ++indexCount, obj.getId());
        SqlConstructUtil.__safeSetInt(ps, ++indexCount, obj.getFromAccountId());
        SqlConstructUtil.__safeSetInt(ps, ++indexCount, obj.getToAccountId());
        SqlConstructUtil.__safeSetDouble(ps, ++indexCount, obj.getAmount());
        SqlConstructUtil.__safeSetDate(ps, ++indexCount, obj.getTime());

        return indexCount;
    }

    public UpgradeTransaction _constructResult(ResultSet rs) throws SQLException {
        UpgradeTransaction obj = new UpgradeTransaction();
        obj.setId(rs.getInt("id"));
        obj.setFromAccountId(rs.getInt("fromAccountId"));
        obj.setToAccountId(rs.getInt("toAccountId"));
        obj.setAmount(rs.getDouble("amount"));
        obj.setTime(rs.getDate("time"));

        return obj;
    }
}

