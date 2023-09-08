## 云课堂

### 1.项目简介

本项目目的是开发一种在线视频课程教育平台，基于 B2B2C 业务模式，支持作者发布课程，支持运营方审核课程，以及支持用户在线学习课程。项目划分的微服务主要包括：课程内容管理服务、课程视频管理服务、搜索服务、订单支付服务、学习中心服务、认证授权服务、网关服务等。本项目采用前后端分离架构，后端采用 SpringBoot、SpringCloud 技术栈开发，数据库使用 MySQL，另外使用 Redis 缓存、MinIO 对象存储、RabbitMQ 消息队列、Elasticsearch 搜索引擎等中间件系统。

### 2.模块简介

#### 课程内容管理模块

课程内容管理模块主要对课程及相关内容进行管理，包括：课程的基本信息、课程图片信息、课程教师信息、课程的授课计划、课程视频信息、课程文档信息等内容的管理，另外该模块提供课程发布操作相关接口。

#### 课程视频管理模块

课程视频管理模块主要提供针对课程视频、课程图片、教学文档等媒资文件上传、存储、转码处理等功能。

#### 搜索模块
搜索模块用于用户通过课程搜索找到课程信息。课程搜索模块包括课程索引及课程搜索两部分。课程索引是将课程信息建立索引。课程搜索是通过前端网页，通过关键字等条件去搜索课程。

#### 学习中心模块
本模块实现了用户选课、下单支付、课程学习的整体流程。平台课程有免费和收费两种，针对收费课程用户需要下单且支付成功课程加入课表。

#### 认证授权模块
认证授权模块实现平台所有用户的身份认证与用户授权功能。用户认证通过后去访问系统的资源，系统会判断用户是否拥有访问资源的权限，只允许访问有权限的系统资源，没有权限的资源将无法访问。

#### 网关模块
网关模块基于 Spring Cloud 网关自动实现。

#### 订单支付模块
订单支付模块用于用户购买课程时提供订单支付功能，并将支付信息通知其他模块。

### 3.技术选型

| 说明      | 框架                       | 说明         | 框架            |
|---------|--------------------------|------------|---------------|
| 基础框架    | Spring Boot+Spring Cloud | 微服务注册及配置管理 | Nacos         |
| 持久框架    | Mybatis-Plus             | 程序构建       | Maven         |
| 关系型数据库  | MySQL                    | 消息中间件      | RabbitMQ      |
| 缓存      | Redis                    | 搜索引擎       | Elasticsearch |
| 安全框架    | Spring Security          | 认证         | JWT           | 
| 定时任务   | xxl-job                  | 文件存储       | MinIO         |
| 日志处理    | Log4j                    | 接口规范       | RESTful       |


