/*global cordova, module*/

module.exports = {
    playIP: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "playIP", [str]);
    },
    setToken: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "setToken", [str]);
    },
    setLicense: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "setLicense", [str]);
    },
    getAudios: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "getAudios", [str]);
    },
    changeAudio: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "changeAudio", [str]);
    },
    getSubtitles: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "getSubtitles", [str]);
    },
    changeSubtitle: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "changeSubtitle", [str]);
    },
    getSubtitles: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "getSubtitles", [str]);
    },
    supportMulticast: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "supportMulticast", [str]);
    },
    muteVideo: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "muteVideo", [str]);
    },
    playDrm: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "playDrm", [str]);
    },
    playAudio: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "playAudio", [str]);
    },
    pauseAudio: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "pauseAudio", [str]);
    },
    resumeAudio: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "resumeAudio", [str]);
    },
    stopAudio: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "stopAudio", [str]);
    },
    seekAudio: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "seekAudio", [str]);
    },
    playPositionAudio: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "playPositionAudio", [str]);
    },
    playTimeAudio: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "playTimeAudio", [str]);
    },
    playVOD: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "playVOD", [str]);
    },
    pauseVOD: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "pauseVOD", [str]);
    },
    resumeVOD: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "resumeVOD", [str]);
    },
    playPositionVOD: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "playPositionVOD", [str]);
    },
    playTimeVOD: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "playTimeVOD", [str]);
    },
    seekVOD: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "seekVOD", [str]);
    },
    playRtsp: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "playRtsp", [str]);
    },
    ffRtsp: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "ffRtsp", [str]);
    },
    rewindRtsp: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "rewindRtsp", [str]);
    },
    seekRtsp: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "seekRtsp", [str]);
    },
    release: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "release", [str]);
    },
    setVideoSize: function( str, str1, str2, str3, str4, successCallback, errorCallback ) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "setVideoSize", [str, str1, str2, str3, str4]);
    },
    state: function (str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "VideoPlugin", "state", [str]);
    },
};
