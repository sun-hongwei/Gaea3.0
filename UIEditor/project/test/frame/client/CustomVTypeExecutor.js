var CustomVTypeExecutor = {
    tmpTypeMap: [],
    specialData:[],
    register: function (errName, msg, callback) {
        mini.VTypes[errName + "ErrorText"] = msg;
        mini.VTypes[errName] = callback;
    },

    unRegister: function (errName) {
        delete mini.VTypes[errName + "ErrorText"];
        delete mini.VTypes[errName];
    },

    get: function (errName) {
        return {
            callback: mini.VTypes[errName],
            text: mini.VTypes[errName + "ErrorText"]
        };
    },
    setSpecialData:function(vtypeJson){
        var array = this.specialData;
        var has = false;
        for (var i = 0; i < array.length; i++) {
            if (array[i].name == vtypeJson.name) {
                has = true;
            }
        }
        if (!has) {
            this.specialData.push(vtypeJson);
        }
    },
    getTmpName: function (errName) {
        return errName + "_tmp";
    },

    tmpRegister: function (errName) {
        var vtype = CustomVTypeExecutor.get(errName);
        var name = CustomVTypeExecutor.getTmpName(errName);
        CustomVTypeExecutor.tmpTypeMap.push(name);
        CustomVTypeExecutor.register(name, vtype.text, vtype.callback);
    },

    clearTmp: function () {
        for (var index = 0; index < CustomVTypeExecutor.tmpTypeMap.length; index++) {
            var element = CustomVTypeExecutor.tmpTypeMap[index];
            CustomVTypeExecutor.unRegister(element.e);
        }
        CustomVTypeExecutor.tmpTypeMap = [];
    },
}