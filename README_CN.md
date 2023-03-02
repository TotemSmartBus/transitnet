# Transitnet
支持实时公交数据可视化的后端项目，目前支持数据实时预览、数据查询等功能。

> 请使用 jdk11 编译运行

English Document：[README.md](README.md)
## 部署

### 1. 数据库配置

开发前请确认 `src/main/resources/application-dev.properties`的数据库连接是否正确配置。

### 2. 准备数据

1. 数据库建表语句位于 `src/main/resources/sql`目录下，前往数据库创建这些表。 

2. GTFS 数据: https://transitfeeds.com/p/mta
 
3. GTFS 实时数据: http://bt.mta.info/wiki/Developers/Index

4. Kaggle 上的离线版本的数据：https://www.kaggle.com/datasets/haoxingxiao/new-york-city-realtime-bus-data

### 3. 准备依赖的项目程序
需要 hytra 项目包,

### 4. 打包与执行

给项目打包 jar 文件：

``` bash
mvn package
```

运行 Java 程序：

``` bash
 java -jar -Dspring.profiles.active=dev target/transitnet-0.0.1-SNAPSHOT-execute.jar 
```
> `-Dspring.profiles.active` 默认值为 `dev`。仅在生产环境需要指定为 `prod`。

## 关于数据库的注意事项
`lib/oceanbase/oceanbase-client-1.1.5.jar` 是提供给 [OceanBase](https://www.oceanbase.com/) 使用的 JDBC 依赖包，如果使用 MySQL 作为数据库则无需处理。
如果需要使用 OceanBase 作为数据库, 将 `lib/oceanbase/oceanbase-client-1.1.5.jar` 移到 `$PROJECT_ROOT$/../lib/oceanbase/` 确保项目可以正确加载该依赖。