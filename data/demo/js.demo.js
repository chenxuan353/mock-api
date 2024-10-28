// JS环境下`console.log`会输出至运行日志中
console.log("pathVariable", pathVariable)
// 你也可以使用注入的logger打印日志
logger.info("params: {}", params)
logger.info("headers: {}", headers)
logger.info("requestBody: {}", requestBody)


respHelper
    .code(200)
    .body({
        code: 0,
        msg: "随便返回点什么"
    })
