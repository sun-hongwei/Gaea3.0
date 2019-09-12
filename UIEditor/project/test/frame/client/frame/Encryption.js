function Encryption() {
    this.rsa = { pri: null, pub: null };
    this.aeskey;
    this.remote_pub_key;
    this.state = "none";
    this.addNewLines = function(str) {
        var finalString = '';
        while (str.length > 0) {
            finalString += str.substring(0, 64) + '\n';
            str = str.substring(64);
        }

        return finalString;
    }

    this.removeLines = function(pem) {
        var lines = pem.split('\n');
        var encodedString = '';
        for (var i = 0; i < lines.length; i++) {
            encodedString += lines[i].trim();
        }
        return encodedString;
    }

    this.stringToArrayBuffer = function(byteString) {
        var byteArray = new Uint8Array(byteString.length);
        for (var i = 0; i < byteString.length; i++) {
            byteArray[i] = byteString.codePointAt(i);
        }
        return byteArray;
    }
    this.bin2text = function(bin) {
        var byteArray = new Uint8Array(bin);
        var byteString = '';
        for (var i = 0; i < byteArray.byteLength; i++) {
            byteString += String.fromCodePoint(byteArray[i]);
        }
        return byteString;
    }
    this.convertKeyToBase64 = function(key) {
        var str = this.bin2text(key);
        str = base64_encode(str);
        str = this.addNewLines(str);
        return str;
    }

    this.convertBase64ToNet = function(data) {
        return data.replace(/\+/g, "-").replace(/\//g, "_");
    }

    this.convertBase64ToLocal = function(data) {
        return data.replace(/-/g, "+").replace(/_/g, "/");
    }

    this.convertBase64ToKey = function(data) {
        var str = this.bin2text(key);
        str = base64_encode(str);
        str = this.addNewLines(str);
        return str;
    }

    this.getPubPem = function() {
        return "-----BEGIN PUBLIC KEY-----" +
            this.rsa.pub +
            "-----END PUBLIC KEY-----"
    }

    this.getRemotePubPem = function() {
        return "-----BEGIN PUBLIC KEY-----" +
            this.remote_pub_key +
            "-----END PUBLIC KEY-----"
    }

    this.getPriPem = function() {
        return "-----BEGIN RSA PRIVATE KEY-----" +
            this.rsa.pri +
            "-----END RSA PRIVATE KEY-----";　　
    }

    this.initCallback = function(key, isPub, callback) {
        if (isPub)
            this.rsa.pub = key;
        else
            this.rsa.pri = key;

        if (!Tools.isNull(this.rsa.pri) && !Tools.isNull(this.rsa.pub))
            callback();
    }

    this.genRSAKey = function(callback) {
        var encrypt = this;
        window.crypto.subtle.generateKey({
                    name: "RSA-OAEP",
                    modulusLength: 2048, //can be 1024, 2048, or 4096
                    publicExponent: new Uint8Array([0x01, 0x00, 0x01]),
                    hash: { name: "SHA-256" }, //can be "SHA-1", "SHA-256", "SHA-384", or "SHA-512"
                },
                true, //whether the key is extractable (i.e. can be used in exportKey)
                ["encrypt", "decrypt"] //must be ["encrypt", "decrypt"] or ["wrapKey", "unwrapKey"]
            )
            .then(function(key) {
                //returns a keypair object
                window.crypto.subtle.exportKey(
                        "pkcs8", //can be "jwk" (public or private), "spki" (public only), or "pkcs8" (private only)
                        key.privateKey //can be a publicKey or privateKey, as long as extractable was true
                    )
                    .then(function(keydata) {
                        encrypt.initCallback(encrypt.convertKeyToBase64(keydata), false, callback);
                    })
                    .catch(function(err) {
                        console.error(err);
                    });
                window.crypto.subtle.exportKey(
                        "spki", //can be "jwk" (public or private), "spki" (public only), or "pkcs8" (private only)
                        key.publicKey //can be a publicKey or privateKey, as long as extractable was true
                    )
                    .then(function(keydata) {
                        encrypt.initCallback(encrypt.convertKeyToBase64(keydata), true, callback);
                    })
                    .catch(function(err) {
                        console.error(err);
                    });

            })
            .catch(function(err) {
                console.error(err);
            });

    }

    this.getPubKey = function() {
        return this.rsa.pub;
    }

    this.setRemotePubKey = function(pubKey) {
        return this.remote_pub_key = pubKey;
    }

    this.setAesKey = function(aesKey) {
        var key = this.decryptRSA(aesKey);
        this.aesKey = CryptoJS.enc.Hex.parse(key);
    }

    this.encryptRSAForRemote = function(data) {
        return this.encryptRSA(this.getRemotePubPem());
    }

    this.encryptRSAForKey = function(data, key) {
        var _encrypt = new JSEncrypt();
        _encrypt.setPublicKey(key);
        var encrypted = _encrypt.encrypt(data);
        return encrypted;
    }

    this.encryptRSA = function(data) {
        return this.encryptRSAForKey(data, this.getPubKey());
    }

    this.decryptRSA = function(data) {
        var _decrypt = new JSEncrypt();
        _decrypt.setPrivateKey(this.getPriPem());
        var uncrypted = _decrypt.decrypt(data);
        return uncrypted;
    }

    this.encryptAES = function(data) {
        var key = this.aesKey;
        // var key = CryptoJS.enc.Utf8.parse(this.aesKey);
        if (Tools.isObject(data)) {
            data = JSON.stringify(data);
        }
        var srcs = CryptoJS.enc.Utf8.parse(data);
        var encrypted = CryptoJS.AES.encrypt(srcs, key, { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 });
        return this.convertBase64ToNet(encrypted.toString());
    }

    this.setRawAesKey = function(keyString) {
        this.aesKey = CryptoJS.enc.Utf8.parse(keyString);
    }

    this.decryptAES = function(data) {
        data = this.convertBase64ToLocal(data);
        var key = this.aesKey;
        // var key = CryptoJS.enc.Utf8.parse(this.aesKey);
        var decrypt = CryptoJS.AES.decrypt(data, key, { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 });
        return CryptoJS.enc.Utf8.stringify(decrypt).toString();
    }

    this.AES = "aes";
    this.RSA = "rsa";
    this.encrypt = function(data, algorithm) {
        if (this.state != "ok") {
            return undefined;
        }

        if (algorithm == this.AES) {
            return this.encryptAES(data);
        } else if (algorithm == this.RSA) {
            return this.encryptRSA(data);
        } else
            return undefined;
    }

    this.decrypt = function(data, algorithm) {
        if (this.state != "ok") {
            return undefined;
        }

        if (algorithm == this.AES) {
            return this.decryptAES(data);
        } else if (algorithm == this.RSA) {
            return this.decryptRSA(data);
        } else
            return undefined;
    }

    this.sign = function(data) { //签名
        var rsa = KEYUTIL.getRSAKeyFromPlainPKCS8PEM(this.getPriPem());　　
        var result = rsa.signString(data, "SHA256withRSA");　　
        return base64_encode(code_conversion(result));
    }

    this.vertify = function(data) { //验证签名
        var rsa = KEYUTIL.getRSAKeyFromPlainPKCS8PEM(this.getRemotePubPem());　　
        var result = rsa.verifyString(data, "SHA256withRSA");　　
        return result;
    }

    this.setPrivateKey = function(pri) {
        this.rsa.pri = pri;
    }

    this.setPublicKey = function(pub) {
        this.rsa.pub = pub;
    }

    this.initService = function(callback) {
        var encryp = this;
        this.state = "running";

        Tools.simpleTomcatSubmit("/encrypt/getrsakey", {},
            function(data) {
                if (data.ret != 0)
                    alert(JSON.stringify(data.data));
                else {
                    var jsonObj = data.data;
                    encryp.rsa.pub = encryp.convertBase64ToLocal(jsonObj.rsa.pub);
                    encryp.rsa.pri = encryp.convertBase64ToLocal(jsonObj.rsa.pri);
                    Tools.simpleTomcatSubmit("/encrypt/setpubkey", { "pub": jsonObj.rsa.pub },
                        function(data) {
                            if (data.ret != 0)
                                alert(JSON.stringify(data.data));
                            else {
                                var jsonObj = data.data;
                                encryp.setRemotePubKey(encryp.convertBase64ToLocal(jsonObj.pub));
                                encryp.setAesKey(encryp.convertBase64ToLocal(jsonObj.aes));
                            }
                            encryp.state = "ok";
                            callback(data.ret);
                        });
                }
            });
    }
}

var GlobalEncrypt = new Encryption();