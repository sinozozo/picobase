package com.picobase.persistence.dbx;


import com.picobase.persistence.repository.PbDatabaseOperate;

public class MysqlPbDbxBuilder extends BaseBuilder implements PbDbxBuilder {


    private QueryBuilder qb;

    public MysqlPbDbxBuilder(PbDatabaseOperate dbOperate) {
        super(dbOperate);
        this.qb = new BaseQueryBuilder();
    }


    @Override
    public SelectQuery select(String... cols) {
        return new SelectQuery(this).select(cols);
    }

    @Override
    public QueryBuilder QueryBuilder() {
        return this.qb;
    }


}
