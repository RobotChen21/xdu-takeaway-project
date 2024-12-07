package com.sky.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.context.BaseContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时填充
        this.strictInsertFill(metaObject, "createTime",LocalDateTime::now ,LocalDateTime.class);
        this.strictInsertFill(metaObject, "createUser", BaseContext::getCurrentId, Long.class);
        this.strictInsertFill(metaObject, "updateTime",LocalDateTime::now ,LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateUser", BaseContext::getCurrentId, Long.class);
    }
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时填充
        this.strictUpdateFill(metaObject, "updateTime",LocalDateTime::now ,LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updateUser",BaseContext::getCurrentId, Long.class);
    }
}
