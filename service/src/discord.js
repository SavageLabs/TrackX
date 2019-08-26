const axios = require("axios");
const WebSocket = require("ws");
const { channelId, botToken } = require("./config");
let connected = false;
let instance = axios.create({
  baseURL: "https://discordapp.com",
  headers: { Authorization: `Bot ${botToken}` }
});
let connection;
const getDiscordObject = (opCode, data) => {
  return JSON.stringify({ op: opCode, d: data });
};
const handleConnect = () => {
  return new Promise((resolve, reject) => {
    instance.get("/api/gateway/bot").then(result => {
      let seq = 0;
      connection = new WebSocket(result.data.url);
      const sendHeartBeat = () => {
        connection.send(getDiscordObject(1, seq));
      };
      connection.on("error", () => {
            connection = null;
            handleConnect();
      });
      connection.on("close", () => {
            connection = null;
            handleConnect();
      });
      connection.on("message", message => {
        seq++;
        const parsed = JSON.parse(message);
        switch (parsed.op) {
          case 0: {
              if(parsed.t === "READY") {
                  connected = true
                resolve();
              }
              break;
          }  
          case 10: {
            const interval = parsed.d.heartbeat_interval;
            setInterval(() => {
              sendHeartBeat();
            }, interval);
            connection.send(
              getDiscordObject(2, {
                token: botToken,
                compress: false,
                v: 6,
                properties: {
                  os: "linux",
                  browser: "TrackX",
                  device: "TrackX"
                }
              })
            );
            break;
          }
          case 1: {
            sendHeartBeat();
            break;
          }
        }
      });
    });
  });
};
exports.handleConnect = handleConnect;
exports.pushReport = (message) => {
       return instance.post(`/api/channels/${channelId}/messages`, {content: message, tts: false, nonce: Date.now()});
}