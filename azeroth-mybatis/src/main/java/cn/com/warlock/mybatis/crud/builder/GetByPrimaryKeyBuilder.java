package cn.com.warlock.mybatis.crud.builder;

import static org.apache.ibatis.jdbc.SqlBuilder.BEGIN;
import static org.apache.ibatis.jdbc.SqlBuilder.FROM;
import static org.apache.ibatis.jdbc.SqlBuilder.SELECT;
import static org.apache.ibatis.jdbc.SqlBuilder.SQL;
import static org.apache.ibatis.jdbc.SqlBuilder.WHERE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

import cn.com.warlock.mybatis.crud.GeneralSqlGenerator;
import cn.com.warlock.mybatis.crud.helper.ColumnMapper;
import cn.com.warlock.mybatis.crud.helper.EntityHelper;
import cn.com.warlock.mybatis.crud.helper.EntityMapper;
import cn.com.warlock.mybatis.crud.helper.TableMapper;
import cn.com.warlock.mybatis.parser.EntityInfo;

public class GetByPrimaryKeyBuilder {

    /**
     * @param configuration
     * @param entity
     */
    public static void build(Configuration configuration, LanguageDriver languageDriver, EntityInfo entity) {
        String msId = entity.getMapperClass().getName() + "." + GeneralSqlGenerator.methodDefines.selectName();

        EntityMapper entityMapper = EntityHelper.getEntityMapper(entity.getEntityClass());

        String sql = buildGetByIdSql(entityMapper);

        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, entity.getEntityClass());

        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, msId, sqlSource, SqlCommandType.SELECT);

        // 将返回值修改为实体类型
        MappedStatement statement = statementBuilder.build();
        setResultType(configuration, statement, entity.getEntityClass());

        configuration.addMappedStatement(statement);
    }

    private static String buildGetByIdSql(EntityMapper entityMapper) {

        // 从表注解里获取表名等信息
        TableMapper tableMapper = entityMapper.getTableMapper();
        Set<ColumnMapper> columnsMapper = entityMapper.getColumnsMapper();

        // 根据字段注解和属性值联合生成sql语句
        BEGIN();
        FROM(tableMapper.getName());

        for (ColumnMapper columnMapper : columnsMapper) {
            if (columnMapper.isId()) {
                WHERE(columnMapper.getColumn() + "=#{" + columnMapper.getProperty() + "}");
            }
            SELECT(columnMapper.getColumn());
        }

        return String.format(SqlTemplate.SCRIPT_TEMAPLATE, SQL());
    }

    /**
     * 设置返回值类型
     *
     * @param ms
     * @param entityClass
     */
    private static void setResultType(Configuration configuration, MappedStatement ms, Class<?> entityClass) {
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        resultMaps.add(getResultMap(configuration, entityClass));
        MetaObject metaObject = SystemMetaObject.forObject(ms);
        metaObject.setValue("resultMaps", Collections.unmodifiableList(resultMaps));
    }

    /**
     * 生成当前实体的resultMap对象
     *
     * @param configuration
     * @return
     */
    public static ResultMap getResultMap(Configuration configuration, Class<?> entityClass) {
        List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();

        Set<ColumnMapper> entityClassColumns = EntityHelper.getEntityMapper(entityClass).getColumnsMapper();
        for (ColumnMapper entityColumn : entityClassColumns) {
            ResultMapping.Builder builder = new ResultMapping.Builder(configuration, entityColumn.getProperty(), entityColumn.getColumn(),
                    entityColumn.getJavaType());
            if (entityColumn.getJdbcType() != null) {
                builder.jdbcType(entityColumn.getJdbcType());
            }

            List<ResultFlag> flags = new ArrayList<ResultFlag>();
            if (entityColumn.isId()) {
                flags.add(ResultFlag.ID);
            }
            builder.flags(flags);
            builder.lazy(false);
            resultMappings.add(builder.build());
        }
        ResultMap.Builder builder = new ResultMap.Builder(configuration, "BaseResultMap", entityClass, resultMappings, true);
        return builder.build();
    }
}
