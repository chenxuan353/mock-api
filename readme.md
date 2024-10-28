# Mock - Api模拟服务端

你是否遭遇过接口联调时的无尽等待。

你是否遭遇过本地环境死活访问不到接口的绝望。

你是否遭遇过Api测试工具配置的超级加倍。

又或者你是个前端开发，觉得编写Mock过于麻烦。

这个项目或许适合你...

它启动迅速，配置简单，模拟真实，并且独立于原项目。

拒绝API焦虑，做回自由的自己。

## 特性

- 配置简单
- 多模块
- 动态重载
- 静态资源展示
- 跨域开关
- 支持脚本语言
- MockJs模版响应

### 脚本语言支持

- GraalJs - Javascript(ES14)
- QLExpress - 类java语法（阿里开源的脚本引擎）

## 快速开始

本项目基于Springboot框架编写，Java版本为JDK17。

下载最新的release版本。

下载示例数据文件夹`data`，放置于数据文件夹中。

```shell
java -jar mock.jar
```

好，你已经启动了一个由示例模块驱动的Mock服务。

访问此路径，开始你的Mock之旅。

```text
http://localhost:8990/static/index.html
```

## 快速启动·其二

`data`里随便建一个文件夹，在这个文件夹里放几个`json`启动也能行。

默认会把文件名里的`.`替换为`/`并省略后缀。

例：`a.b.c.json`，访问路径是`a/b/c`，什么方法都行。

例2：`a.b.c..json.json`，访问路径是`a/b/c.json`，俩点会被换成一个点。

> 你就说快不快吧

## 不是启动

如果你下载`static`文件夹放到启动目录的话可以看到管理页面

```text
http://localhost:8990/mock/static/index.html
```

## 引擎摘要

### Javascript - GraalJs

项目地址：https://github.com/oracle/graaljs

GraalJS 是 Oracle 开源的一种基于 GraalVM 构建的快速 JavaScript 语言实现。

对可使用的java类进行了限制

默认载入`Mock.js`，支持通过配置载入内置的`Axios.js`。

### QLExpress

项目地址：https://github.com/alibaba/QLExpress

阿里开源规则引擎

对可使用的java类进行了限制，类Java语法

#### 和java语法相比，要避免的一些ql写法错误

- 不支持try{}catch{}
- 注释目前只支持 \/\*\* \*\*\/，不支持单行注释 \/\/
- 不支持java8的lambda表达式
- 不支持for循环集合操作for (Item item : list)
- 弱类型语言，请不要定义类型声明,更不要用Template（Map\<String, List\>之类的）
- array的声明不一样 (array = \[\])
- min,max,round,print,println,like,in 都是系统默认函数的关键字，请不要作为变量名

#### 示例

```qle
list = new List();
list.add("1");

for(i = 0; i < list.size(); i++){
    item = list.get(i);
}
```

## 使用文档

### 数据文件夹结构

- `global-mock.yml` - 全局设置
- 模块A
    - `module-mock.yml` - 模块配置
    - 响应组A
      - `group-mock.yml` - 响应组配置
      - 响应组A
      - 响应组...
      - 响应组N
      - 请求处理器文件组
      - ...
    - 响应组B
    - ...
    - 响应组N
    - 请求处理器文件组A - 由一组文件构成
    - 请求处理器文件组...
    - 请求处理器文件组N
- 模块B
- ...

### 全局配置

目前只有默认激活模块的配置，模块名就是文件夹名。

```yaml
activeModule: example
```

### 模块、响应组及请求处理器文件组通用配置

适用于`module-mock.yml`、`group-mock.yml`以及`请求处理器文件组.yml`

> 注意：配置文件中的`request`与`response`选项组会自顶向下继承
> 
> `response.headers`与`response.externalTextResources`会合并内部配置
> 
> 其他数据均在子配置后替换父配置对应值。

```yaml
name: 名称
des: 描述
# 是否启用
# enable: true
request:
  # 请求映射路径,支持相对路径或绝对路径
  # 默认取父路径/文件名路径(`.`替换为`/`,`..`替换为`.`)
  path: "example/get/{id}"
  # 请求允许的方法 默认允许所有方法
  methods:
    - POST
  # 限制请求头
  # headers:
  #     - Content-Type=application/json
  # 限制请求参数
  # params:
  #     - version=v1
  # 允许跨域（允许所有）
  # cors: true
  # 静态文件模式（仅用于响应组，将该响应组的文件夹与文件视为静态资源）
  # staticMode: true
response:
  # 响应类型（默认自动识别）
  contentType: application/json
  # 响应头
  headers:
    RTP: 2333
  # 响应体
  body: |
    {
        "code": 100,
        "msg": "${envMsg}"
    }
  # 环境变量设置
  envionment:
    envMsg: "默认响应体"

  # 脚本路径
  # scriptPath: "../example.get.{id}.js"
  # 脚本内容（可以在yml中编写简单脚本）
  # scriptContent: "...."
  # 处理类型
  # processType: JS
  # 是否加载Axios异步请求库(仅适用于 processType=JS)
  # jsLoadAxios: true

  # 脚本外部文本资源，自动读取至环境变量（相对当前路径或模块根路径）
  # 脚本或者外部文本资源的数据大小超过15mb时将跳过读取
  # externalTextResources:
  #     data: "./example.txt"
  # 脚本外部文件资源（只有该列表中的文件允许在脚本响应中以文件形式返回）
  # externalFileResources:
  #     data: "./example.txt"

  # 文件路径(仅适用于 processType=File)
  # filePath: "./example.txt"
  # 下载文件名(仅适用于 processType=File)
  # downloadFileName: "example.txt"
  # 响应类型自适应 (仅适用于 processType=File)
  # fileContentTypeAuto: true

```

#### 响应组及请求处理器文件组通用日志输出配置

```yaml
log:
  # 是否启用
  enable: false
  # 打印执行时间
  printProcessTime: false
  # 请求输出总开关
  printRequest: true
  # 打印请求体
  printRequestBody: true
  # 打印请求头
  printHeaders: true
  # 打印路径参数
  printUriVars: true
  # 打印请求参数
  printParam: true
  # 打印Session（需配置`printSessionKey`）
  printSession: true
  # 打印哪些SessionKey
  # printSessionKey:
  #     - Content-Type
  # 响应输出总开关
  printResponse: true
  # 打印响应头
  printResponseHeaders: true
  # 打印响应体
  printResponseBody: true
```

### 模块

模块的默认基础路径为根路径`\`

### 响应组

以文件夹形式存在，其内文件夹会被递归解析为响应组。

文件则会尝试解析为请求处理器文件组。

默认基础路径是文件夹名称。

例如：`./data/module/group1/group2`

其中`group2`的基础路径为`group2`，完整路径为`/group1/group2`

### 请求处理器文件组

响应组下的文件会被尝试解析为请求处理器

同名不同后缀的文件会被分入同一组。

##### 支持的文件后缀

`yml`、`yaml`、`js`、`qle`、 `text`、`json`、`html`、`data`、`htm`、`txt`、`body`、`css`、`xml`

> `yml`与`yaml`被视为文件组配置
> 
> `qle`视为`QLExpress`脚本文件
> 
> `js`视为`GraalJs`脚本文件

#### 分组示例

##### 有如下文件

- `example.v1.yml`
- `example.v1.js`
- `example.v1.body`
- `example.v2.json`
- `example.v2.mp4`
- `example.v3.txt`
- `example.v4.png`
- `example.v4.jpeg`

##### 会被分为三组

不支持的文件会被忽略

- `example.v1`
  - `example.v1.yml` - 被视为`example.v1`请求处理器文件组的配置
  - `example.v1.js`
  - `example.v1.body`
- `example.v2`
  - `example.v2.json`
- `example.v3`
  - `example.v3.txt`

### 脚本使用

#### 脚本通用变量

| 变量名                   | 描述                                        |
| --------------------- |-------------------------------------------|
| params                | get或post参数字典                              |
| pathVariable          | 路径变量字典                                    |
| headers               | 请求头字典                                     |
| envionment            | 环境变量字典（`envionment`）                      |
| resources             | 资源字典，包含内部与`externalTextResources`定义的外部文本资源 |
| externalFileResources | 支持的外部文件键名字典（`externalFileResources`）      |
| requestBody           | 请求体字符串                                    |
| logger                | 日志对象                                      |
| session               | session对象                                 |
| respHelper            | 响应帮助类，用于设置响应                              |
| JsonHelper            | Json帮助类                                   |
| shareVar              | 执行器共享变量对象                                 |
| parentShareVar        | 父响应组共享变量对象                                |
| globalShareVar        | 全局共享变量对象                                  |

#### 字典使用

- `params`
- `pathVariable`
- `resources`
- `headers`

| 方法签名            | 描述               |
| --------------- | ---------------- |
| get(String key) | params.get("id") |

#### `logger` 日志对象

用于日志输出

| 方法签名                                    | 描述                                           |
| --------------------------------------- | -------------------------------------------- |
| void debug(String var1, Object... var2) | 打印debug日志，`logger.debug("日志 key={}"， "key")` |
| void info(String var1, Object... var2)  | 打印info日志                                     |
| void warn(String var1, Object... var2)  | 打印warn日志                                     |
| void error(String var1, Object... var2) | 打印error日志                                    |

#### `session`对象

| 方法签名                                  | 描述              |
| ------------------------------------- | --------------- |
| long getCreationTime()                | 创建时间戳           |
| String getId()                        | 获取`SessionId`   |
| long getLastAccessedTime()            | 上次请求时间戳         |
| void setMaxInactiveInterval(int i)    | 设置当前会话的失效时间，单位秒 |
| int getMaxInactiveInterval()          | 获取当前会话的失效时间，单位秒 |
| Object getAttribute(String var1)      | 读取session属性     |
| List\<String\> getAttributeNames()    | 获取属性列表          |
| void setAttribute(String s, Object o) | 设置session属性     |
| void removeAttribute(String s)        | 移除session属性     |
| void invalidate()                     | 失效会话            |
| boolean isNew()                       | 是否是新建会话         |

> 注意，只能设置属性值为基本数据类型、HashMap、ArrayList、HashSet等有限类型。

#### `respHelper` 响应帮助类

未注明返回值的方法返回值是其响应帮助类本身

| 方法签名                                | 描述                                               |
| ----------------------------------- | ------------------------------------------------ |
| code(int code)                      | 设置响应状态码                                          |
| contentType(String contentType)     | 设置响应类型                                           |
| body(Object body)                   | 设置响应体                                            |
| file(String fileKey)                | 设置文件响应，`fileKey`需要是配置中`externalFileResources`的键名 |
| fileKey(String fileKey)             | 同`file(String fileKey)`                          |
| download(String fileKey)            | 自动设置文件下载响应，其他同`file(String fileKey)`      |
| headers(Map\<String, ?\> headers)   | 批量设置响应头                                          |
| header(String header, String value) | 设置响应头                                            |
| redirect(String path)               | 302重定向                                           |
| int getCode()                       | 获取当前设置的状态码                                       |
| String getBody()                    | 获取当前设置的响应体                                       |
| String getFileKey()                 | 获取当前设置的文件响应键                                     |
| Map\<String, String\> getHeaders()  | 获取当前设置的响应头                                       |
| String getHeader(String header)     | 获取当前设置的响应头                                       |

#### `JsonHelper`JSON帮助类

用于Json相关转换

| 方法签名                                         | 描述         |
| -------------------------------------------- | ---------- |
| String toJsonString(Object obj)              | 转换为Json字符串 |
| \<T\> T parse(String json, Class\<T\> clazz) | 从Json字符串解析 |

#### `shareVar`共享变量对象

`shareVar`、`parentShareVar`以及`globalShareVar`

| 方法签名                               | 描述                          |
| ---------------------------------- | --------------------------- |
| Object get(String key)             | 读取共享变量                      |
| void set(String key, Object value) | 设置共享变量                      |
| void remove(String key)            | 移除某个键                       |
| void rm(String key)                | 同上`void remove(String key)` |
| int size()                         | 共享变量数量                      |
| int length()                       | 同上                          |
| void clear()                       | 清空共享变量                      |
| List\<String\> keys()              | 共享变量键列表                     |
| List\<Object\> values()            | 共享变量值列表                     |

> 注意，只能设置属性值为基本数据类型、HashMap、ArrayList、HashSet等有限类型。

### 配置文件中的`processType`响应类型

| 值            | 描述             |
| ------------ | -------------- |
| JS           | JS脚本           |
| QLExpress    | QLE脚本          |
| MockJsStr    | Mock模版         |
| TextTemplate | 文本模版           |
| JSONTemplate | JSON模版，与文本模版相同 |
| StaticFile   | 静态资源           |
| File         | 文件，下载或展示       |
| Redirect302  | 请求重定向          |
