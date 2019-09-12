function CallbackStack() {
    this.callbacks = [];
    this.push = function (callback) {
        this.callbacks.push(callback)
    }

    this.pop = function () {
        if (this.callbacks.lenght == 0)
            return undefined;

        var callback = this.callbacks[0];
        this.callbacks.splice(0, 1);
        return callback;
    }

    this.header = function (callback) {
        var newCallbacks = [callback];
        this.callbacks = newCallbacks.concat(this.callbacks);
    }

    this.while = function (arg, callback) {
        while (true) {
            var element = this.pop();
            if (Tools.isNull(element))
                break;
            callback(element, arg, this);
        }
    }
}