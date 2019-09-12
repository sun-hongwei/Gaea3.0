function RemoteDataset(tag) {
    this.datas = {};
    this.data = [];
    this.tag = tag;
    this.curRow = 0;
    this.removeRows = [];

    this.STATE = "_state";
    this.Update = "modified";
    this.Insert = "added";
    this.Delete = "removed";

    this.newDataset = function() {
        this.data = [];
        this.curRow = 0;
    }

    this.row = function() {
        return this.curRow;
    }

    this.setState = function(state) {
        this.data[this.curRow][this.STATE] = state;
    }

    this.newRow = function() {
        var row = {};
        this.data.push(row);
        this.setCurRow(this.data.length - 1);
    }

    this.join = function(row) {
        this.data.push(row);
        this.setCurRow(this.data.length - 1);
    }

    this.add = function(row) {
        if (Tools.isNull(row))
            row = {};
        row[this.STATE] = this.Insert;
        this.join(row);
    }

    this.edit = function(row) {
        for (field in row) {
            this.data[this.curRow][field] = row[field];
        }
        if (this.data[this.curRow][this.STATE] != this.Insert) {
            this.data[this.curRow][this.STATE] = this.Update;
        }
    }

    this.enumRows = function(callback) {
        for (var index = 0; index < this.data.length; index++) {
            var row = this.data[index];
            callback(row);
        }
    }

    this.remove = function() {
        var row = this.data[this.curRow];
        this.data.splice(this.curRow, 1);
        if (this.data[this.curRow][this.STATE] != this.Insert) {
            this.data[this.curRow][this.STATE] = this.Delete;
            this.removeRows.push(row);
        }
    }

    this.setCurRow = function(index) {
        this.curRow = index;
    }

    this.getData = function() {
        return this.data;
    }

    this.getChange = function() {
        var result = [];
        for (var index = 0; index < this.data.length; index++) {
            var state = this.data[index][this.STATE];
            if (Tools.isNull(state) ||
                !(state == this.Update || state == this.Insert || state == this.Delete))
                continue;
            result.push(this.data[index]);
        }
        return result;
    }

    this.setData = function(data) {
        this.data = data;
    }

    this.pushDataset = function(data, tag) {
        this.datas[tag] = data;
    }

    this.getDatas = function() {
        var result = [];
        for (var tag in this.datas) {
            if (this.tag === tag) {
                continue;
            }
            result.push({ tag: tag, data: this.datas[tag] });
        }
        result.push({ tag: this.tag, data: this.data });

        return result;
    }

    this.postCurrent = function(postUri, postKeys, callback, needCrypt, async) {
        this.post(postUri, postKeys, true, callback, needCrypt, async);
    }

    this.postAll = function(postUri, postKeys, callback, needCrypt, async) {
        this.post(postUri, postKeys, false, callback, needCrypt, async);
    }

    this.post = function(postUri, postKeys, onlyCur, callback, needCrypt, async) {
        var postData = [];
        if (Tools.isNull(onlyCur) || !onlyCur)
            postData = this.getDatas();
        else
            postData.push({ tag: this.tag, data: this.getChange() });

        if (!Tools.isNull(postKeys) && Tools.isObject(postKeys)) {
            postKeys = JSON.stringify(postKeys);
        }

        postData = JSON.stringify(postData);
        if (postData)
            Tools.simpleTomcatSubmit(postUri, {
                key: postKeys,
                data: postData,
            }, function(data) {
                isdo = data.ret == 0;
                if (!Tools.isNull(callback))
                    callback(isdo, data.data)
            }, needCrypt, false, async);

    }
}