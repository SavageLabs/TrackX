const axios = require("axios");


module.exports = (content) => {
    return new Promise((resolve, reject) => {
        axios.post("https://paste.savagellc.net/api/add", { language: "plaintext", content}, {headers: { "Content-type": "application/json"}}).then(res => {
            const { data } = res;
            if(data.success) {
                resolve(data.id);
            } else {
                reject("internal err");
            }
        }).catch(err => reject(err));
    })
}