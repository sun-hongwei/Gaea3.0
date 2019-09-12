/**
 * columns格式：{"字段名称":{id:"字段名称",name:"字段别名",type:"字段类型"}}
 * dataset格式：[{"fieldid":value}]
 */

var GlobalDataSources = {
    dataSources: {},

    requestDataset: function (dataSource, callback, force, params) {
        if (!dataSource.useLocal) {
            if (dataSource.useLocal) {
                callback(dataSource, dataSource.columns, dataSource.dataset);
            } else if (!Tools.isNull(dataSource.remote_dataset) && !force) {
                callback(dataSource, dataSource.remote_columns, dataSource.remote_dataset);
            } else {
                if (!Tools.isNull(params) && !Tools.isNull(params[dataSource.id])) {
                    params = params[dataSource.id];
                }
                var url = dataSource.url;
                var queryParams = params;
                if (dataSource.type == "sql") {
                    params["order"] = dataSource.params["order"];
                    url = "/jsonarray/service/query.do";
                    queryParams = {
                        "sql": dataSource.params["sql"],
                        "param": params
                    };
                }


                if (Tools.isNull(queryParams))
                    queryParams = {
                        command: JSON.stringify(dataSource.params)
                    };
                else if (Tools.isNull(queryParams["command"])) {
                    queryParams = {
                        command: JSON.stringify(queryParams)
                    };
                }

                var remoteUrl = Tools.smartGetAjaxTomcatUrl(url);
                Tools.ajaxTomcatDirectSubmit(remoteUrl,
                    undefined, queryParams,
                    false, undefined,
                    function (data) {
                        if (data.ret == 0 && !Tools.isNull(data.data)) {
                            dataSource.remote_columns = data.data.COLUMN;
                            dataSource.remote_dataset = data.data.DATA
                            callback(dataSource, dataSource.remote_columns, dataSource.remote_dataset, url, queryParams.command, params);
                        } else {
                            throw "获取数据源定义失败！";
                        }
                    });
            }
        } else {
            callback(dataSource.id, dataSource.columns, dataSource.dataset);
        }
    },

    /**
     * 将数据源从本地缓冲中删除
     * @param {string} dataSourceId 数据源id 
     */
    reset: function (dataSourceId) {
        if (!Tools.isNull(dataSourceId)) {
            if (!Tools.isNull(GlobalDataSources.dataSources[dataSourceId]))
                delete GlobalDataSources.dataSources[dataSourceId];
        } else
            GlobalDataSources.dataSources = {};
    },

    /**
     * 打开数据源
     * @param {string} datasource 数据源id 
     * @param {callback} callback 回调函数
     * 格式：
     * function("数据源对象", "列集合", "数据集")
     * 列集合格式：[{id:"字段id",name:"字段显示名称",type:"字段值类型",size:"字段定义长度",style:"字段类型：csField, csConst, csExpr"}]
     * 数据集格式：[{"字段id":"字段值"}]
     * @param {bool} force 是否强制刷新数据源，如果数据源已经打开，false直接返回，true忽略并重新打开
     * @param {jsonobject} params 数据集合执行参数，有服务端提供
     */
    saveData: function (dsid, data, callback) {
        var datasource = Tools.getDataSource(dsid);
        if (Tools.isNull(datasource)) {
            throw "datasource[" + dsid + "] not open!";
        }

        var url = "/jsonarray/service/save.do";
        var queryData = {
            table: datasource.params["table"],
            data: data,
            primkey: datasource.params["primkey"]
        };

        queryParams = {
            command: JSON.stringify(queryData)
        };

        Tools.ajaxTomcatDirectSubmit(Tools.smartGetAjaxTomcatUrl(url),
            undefined, queryParams,
            false, undefined,
            function (data) {
                callback(data.ret == 0)
            });

    },

    /**
     * 打开数据源并获取指定的行数据
     * @param {string} datasource 数据源id 
     * @param {int} rowIndex 要获取的行号
     * @return {jsonobject} 参见requestDataset的数据集说明
     */
    getRow: function (dataSourceId, rowIndex,refresh) {
        refresh = refresh?true:false;
        var row;
        GlobalDataSources.get(dataSourceId, refresh, function (ds, columns, dataset) {
            row = dataset[rowIndex];
        });

        return row;
    },

    /**
     * 打开数据源并获取指定的行数据，与getRow不同的是，此函数会将一行数据写入一个列表
     * @param {string} datasource 数据源id 
     * @param {int} rowIndex 要获取的行号
     * @return {array} 格式：[value]
     */
    getRowList: function (dataSourceId, rowIndex,refresh) {
        refresh = refresh?true:false;
        var result = [];
        GlobalDataSources.get(dataSourceId, refresh, function (ds, columns, dataset) {
            var row = dataset[rowIndex];
            for (var field in row) {
                result.push(row[field]);
            }
        });

        return result;
    },

    /**
     * 打开数据源并获取指定的一列数据
     * @param {string} datasource 数据源id 
     * @param {int} colId 要获取的列号
     * @return {array} 格式：[value]
     */
    getCol: function (dataSourceId, colId,refresh) {
        refresh = refresh?true:false;
        result = [];
        GlobalDataSources.get(dataSourceId, refresh, function (ds, columns, dataset) {
            for (var index = 0; index < dataset.length; index++) {
                var row = dataset[index];
                result.push(row[colId]);
            }
        });

        return result;
    },

    /**
     * 打开数据源并获取指定行列的一个数据
     * @param {string} datasource 数据源id 
     * @param {int} rowIndex 要获取的行号
     * @param {int} colId 要获取的列号
     * @return {object} 格式：value
     */
    getValue: function (dataSourceId, rowIndex, colId,refresh) {
        refresh = refresh?true:false;
        result = undefined;
        GlobalDataSources.get(dataSourceId, refresh, function (dsId, columns, dataset) {
            result = dataset[rowIndex][colId];
        });

        return result;
    },

    /**
     * 打开多个数据集，按照列表顺序打开，并在成功打开所有数据集之后调用回调过程
     * @param {array} dataSourceIds 数据源id列表
     * @param {callback} callback 回调函数
     * 格式：
     * function(datasets);
     * 
     * datasets格式：{"数据集id N":{
     *      column:[{id:"id",name:"",type:"",size:10,style:""}],
     *      dataset:[{field:value}]
     * }
     * @param {jsonobject} params 数据集合执行参数，有服务端提供
     */
    gets: function (dataSourceIds, force, callback, params) {
        GlobalDataSources.internalGets(dataSourceIds, force, callback, {}, params);
    },

    internalGets: function (dataSourceIds, force, callback, result, params) {
        var dsId = dataSourceIds[0];
        var param = Tools.isNull(params) ? undefined : params[dsId];
        GlobalDataSources.get(dsId, force,
            function (dataSource, columns, dataset) {
                dataSourceIds.splice(0, 1);
                result[dataSource.id] = {
                    column: columns,
                    dataset: dataset
                };
                if (dataSourceIds.length > 0) {
                    GlobalDataSources.internalGets(dataSourceIds, force, callback, result, params);
                } else {
                    callback(result);
                }
            }, param);
    },

    /**
     * 获取数据源，如果数据源已经存在，并且force设置为true，则不论调用是否成功，都会清除缓冲的数据源内容
     * @param {string} datasource 数据源id 
     * @param {callback} callback 回调函数，参见requestDataset
     * @param {bool} force 是否强制刷新数据源，如果数据源已经打开，false直接返回，true忽略并重新打开
     * @param {jsonobject} params 数据集合执行参数，有服务端提供
     */
    get: function (dataSourceId, force, callback, params) {
        var dataSource;
        force = !Tools.isNull(force) && force;
        if (Tools.isNull(GlobalDataSources.dataSources[dataSourceId]) ||
            (!Tools.isNull(force) && force)) {
            dataSource = Tools.getDataSource(dataSourceId);
            if (Tools.isNull(dataSource))
                return undefined;

            if (!dataSource.useLocal) {
                dataSource.dataset = undefined;
                dataSource.columns = undefined;
            }
            GlobalDataSources.dataSources[dataSourceId] = dataSource;
        } else
            dataSource = GlobalDataSources.dataSources[dataSourceId];

        GlobalDataSources.requestDataset(dataSource, callback, force, params);
    },

    /**
     * 
     * @param {string} datasource 数据源id 
     * @param {ControlInfo[]} controlInfos 要应用的组件信息，通过uiInfo获取
     * @param {callback} callback 回调函数,如果设置field(同时可以设置row属性，未设置默认使用0)
     * 格式：
     * 1有field设置：function("控件id", "控件名字", 字段值, 字段名称)
     * 2无field设置：function("控件id", "控件名字", 数据集, 列集合)
     * 列集合格式：{"列id":{id:"字段id",name:"字段显示名称",type:"字段值类型",size:"字段定义长度",style:"字段类型：csField, csConst, csExpr"}}
     * 数据集格式：[{"字段id":"字段值"}]
     * @param {jsonobject} params 数据集合执行参数，有服务端提供
     */
    loadData: function (datasource, controlInfos, callback, params) {
        var formDS = datasource;
        if (Tools.isNull(formDS))
            return;

        var dIds = [];
        for (id in formDS) {
            if (formDS.hasOwnProperty(id)) {
                dIds.push(id);
            }
        }

        if (dIds.length == 0)
            return;

        GlobalDataSources.gets(dIds, true, function (data) {
            if (Tools.isNull(data))
                return;

            for (var index = 0; index < controlInfos.length; index++) {
                info = controlInfos[index];
                if (!Tools.isNull(info.dataSource) &&
                    !Tools.isNull(data[info.dataSource])) {
                    var columns = data[info.dataSource].columns?data[info.dataSource].columns:data[info.dataSource].column;
                    if (Tools.isNull(columns)) {
                        var columnDefines = data[info.dataSource].column;
                        columns = {};
                        for (var index = 0; index < columnDefines.length; index++) {
                            var column = columnDefines[index];
                            columns[column.id] = column;
                        }
                        data[info.dataSource].columns = columns;
                    }
                    var dataset = data[info.dataSource].dataset;

                    var field = info.field.field;
                    if (Tools.isNull(field)) {
                        callback(info.id, info.name, dataset, columns);
                    } else {
                        var rowIndex = 0;
                        if (!Tools.isNull(info.row)) {
                            rowIndex = info.row;
                        }

                        if (rowIndex >= dataset.length)
                            continue;

                        var row = dataset[rowIndex];
                        callback(info.id, info.name, row[field], field)
                    }
                }
            }
        }, params);
    }

};