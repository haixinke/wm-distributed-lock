# Distributed lock based on Redis
## 功能
基于Redis实现的分布式锁

## 安装

mvn clean install -Dmaven.test.skip=true

## 引用
```
<dependency>
   <groupId>com.wm.spring.boot</groupId>
   <artifactId>wm-distributed-lock-starter</artifactId>
   <version>1.0.0</version>
</dependency>
```   

## 使用
### 注解
在需要增加分布式锁的方法上增加如下注解：
```
@WmLock(key = "#object.id",prefix="business1")
``` 
方法上增加此注解，除了key，其他都有默认值，key是spring的el表达式，prefix是字符串，建议不要为空，定义与业务相关的前缀。
存在redis中锁的key是{spring.application.name}:{prefix}:{key}，如果没有prefix，那就变成{spring.application.name}:{key}，容易重复

### 手动

```
@Autowired
private IDistributedLock redisDistributedLock;

try {
    distributedLock.lock("binlog:" + binLogMessage.getId(),10000,10,500);
    业务代码块。。。
} finally {
    distributedLock.releaseLock("binlog-lock:" + binLogMessage.getId());
}

```
