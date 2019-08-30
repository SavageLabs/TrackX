console.log("BOOTRAPPING Reporter");
const discord = require("./discord")
const express = require("express");
const paste = require("./paste");
const parser = require("body-parser");
const { allowed } = require("./config");
const recents = [];
const server = express();
server.use(parser.urlencoded());

server.post("/report", (request, response) => {
    const { pluginId, trace, version} = request.body;
    if(!pluginId || !trace || !version) {
        response.end("malformed");
        return;
    }
    if(!allowed.includes(pluginId)) {
        response.end("illegal plugin");
        return;
    }
    if(trace.length < 10 || trace.length > 8000) {
        response.end("bad size");
        return;
    }
    if(version.length < 1 || version.length > 50) {
        response.end("bad size");
        return;
    }
    const ip = request.headers['x-forwarded-for'] || request.connection.remoteAddress;
    if(recents.includes(ip)) {
        response.end("to_rec");
        return;
    }
    const serverVersion = request.body.mcver ? request.body.mcver : "-";
    paste(trace).then(pasteId => {
        const message = "**Report**\n" + `Plugin: ${pluginId}\nVersion: ${version}\nIp Address: ${ip}\nServer Version: ${serverVersion}\nOpenGist: https://paste.savagellc.net/view/${pasteId}\nRaw: https://paste.savagellc.net/raw-display/${pasteId}`;
        discord.pushReport(message).then(() => response.end("reported")).catch(err => response.end("internal error"))
        recents.push(ip);
        setTimeout(() => {
            recents.splice(recents.indexOf(ip), 1);
        }, 1000 * 10) // 10 seconds
    }).catch(err => response.end("internal_err"))
});
server.post("/proxy", (request, response) => {
    const { pluginId, trace, version} = request.body;
    if(!pluginId || !trace || !version) {
        response.end("malformed - proxy");
        return;
    }
    if(!allowed.includes(pluginId)) {
        response.end("illegal plugin - proxy");
        return;
    }
    if(trace.length < 10 || trace.length > 8000) {
        response.end("bad size - proxy");
        return;
    }
    if(version.length < 1 || version.length > 50) {
        response.end("bad size - proxy");
        return;
    }
    const ip = request.headers['x-forwarded-for'] || request.connection.remoteAddress;
    if(recents.includes(ip)) {
        response.end("to_rec - proxy");
        return;
    }
    const serverVersion = request.body.mcver ? request.body.mcver : "-";
    paste(trace).then(pasteId => {
        const message = "**Report**\n" + `Plugin: ${pluginId}\nVersion: ${version}\nIp Address: ${ip}\nServer Version: ${serverVersion}\nIs from Proxy\nOpenGist: https://paste.savagellc.net/view/${pasteId}\nRaw: https://paste.savagellc.net/raw-display/${pasteId}`;
        discord.pushReport(message).then(() => response.end("reported - proxy")).catch(err => response.end("internal error"))
        recents.push(ip);
        setTimeout(() => {
            recents.splice(recents.indexOf(ip), 1);
        }, 1000 * 10) // 10 seconds
    }).catch(err => response.end("internal_err - proxy"))
});
discord.handleConnect().then(() => {
    server.listen(8080, () =>{ 
        discord.pushReport("Backend Running!");
        console.log("http node deployed")
    })
})