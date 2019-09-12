function testMQResult(requestInfoJson, dataJson) {
    // var json = JSON.parse(requestInfoJson);
    // alert("异步返回*(请求信息):\r\n" + JSON.stringify(json));
    // json = JSON.parse(dataJson);
    // alert("异步返回*(结果信息):\r\n" + JSON.stringify(json));
}

/*
参数列表
*/
function dispatchflowtest_start(uiInfo, initdata) {
    ControlHelp.setEvent(uiInfo, "testmq", "onClick", function (e, uiInfo) {

        var configure = {};
        configure.host = "10.0.2.2";
        configure.port = 5672;
        configure.user = "wy";
        configure.password = "123456";
        configure.virtualHost = "wangyan";
        configure.defaultSenderQueueName = "test-server";
        configure.defaultSenderExchangeName = "testExchange_server";
        configure.defaultSenderRoutingKey = "test";
        configure.defaultSenderExchangeType = "DIRECT";
        configure.defaultReceiverQueueName = "test-client";
        configure.defaultReceiverExchangeName = "testExchange_client";
        configure.defaultReceiverRoutingKey = "test";
        configure.defaultReceiverExchangeType = "DIRECT";
        configure = JSON.stringify(configure);

        jsmq.setup(configure);
        jsmq.addReceiveCallback("test-client", "testMQResult");
        jsmq.open();

        var i = 0;
        while (i++ < 10000) {
            var json = {};
            json["mid"] = "abc-异步";
            json["sen"] = "testExchange_server";
            json["srk"] = "test";
            json["ren"] = "testExchange_client";
            json["rrk"] = "test";
            json["da"] = {
                a: "abc",
                b: false,
                c: true,
                d: 1025
            };

            var json = JSON.stringify(json);

            //异步请求
            jsmq.send(json, "testMQResult");

            //同步请求并返回结果
            var json = {};
            json["mid"] = "abc-同步";
            json["sen"] = "testExchange_server";
            json["srk"] = "test";
            json["ren"] = "testExchange_client";
            json["rrk"] = "test";
            json["da"] = {
                a: "abc",
                b: false,
                c: true,
                d: 1025
            };
            var json = JSON.stringify(json);
            var result = jsmq.sendAndWait(json);
            // if (result == undefined || result == null)
            //     alert("null数据返回！");
            // else
            //     alert("同步返回\r\n" + result);
        }
    });

}
// 
/*

*/
var dispatchflowtest = {

}

var info = getGlobalScriptInfo("dispatchflowtest")
dispatchflowtest_start(info.uiInfo, info.initdata);