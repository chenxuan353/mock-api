id = pathVariable.get('id');

if(id in [1,2,10,15]){
    return {
        "code": 0,
        "msg": "删除成功",
        "des": "这下成功了，[1,2,10,15]都能触发成功。"
    };
}

return {
    "code": 404,
    "msg": "用户不存在",
    "des": "这是模拟JS脚本动态处理，试试 /user/del/1"
};